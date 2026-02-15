package br.ce.sop.gestaoorcamento.service;

import br.ce.sop.gestaoorcamento.dto.*;
import br.ce.sop.gestaoorcamento.model.ItemMedicao;
import br.ce.sop.gestaoorcamento.model.Medicao;
import br.ce.sop.gestaoorcamento.model.enums.StatusMedicao;
import br.ce.sop.gestaoorcamento.repository.ItemRepository;
import br.ce.sop.gestaoorcamento.repository.MedicaoRepository;
import br.ce.sop.gestaoorcamento.repository.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicaoService {

    private final MedicaoRepository medicaoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public MedicaoResponseDTO criar(MedicaoRequestDTO dto) {
        if (medicaoRepository.existsByOrcamentoIdAndStatus(dto.orcamentoId(), StatusMedicao.ABERTA)) {
            throw new RuntimeException("Já existe uma medição aberta para este orçamento.");
        }

        medicaoRepository.findByNumeroMedicao(dto.numeroMedicao()).ifPresent(m -> {
            throw new RuntimeException("O número de medição '" + dto.numeroMedicao() + "' já foi utilizado.");
        });

        var orcamento = orcamentoRepository.findById(dto.orcamentoId())
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado"));

        Medicao medicao = new Medicao();
        medicao.setOrcamento(orcamento);
        medicao.setNumeroMedicao(dto.numeroMedicao());
        medicao.setObservacao(dto.observacao());
        medicao.setDataMedicao(LocalDateTime.now());
        medicao.setStatus(StatusMedicao.ABERTA);

        processarItensMedicao(medicao, dto.itens());

        return converterParaDTO(medicaoRepository.save(medicao));
    }

    @Transactional
    public MedicaoResponseDTO atualizar(Long id, MedicaoRequestDTO dto) {
        Medicao medicao = buscarEValidarMedicaoAberta(id);

        medicao.setNumeroMedicao(dto.numeroMedicao());
        medicao.setObservacao(dto.observacao());

        processarItensMedicao(medicao, dto.itens());

        return converterParaDTO(medicaoRepository.save(medicao));
    }

    @Transactional
    public void validar(Long id) {
        Medicao medicao = medicaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medição não encontrada"));

        if (medicao.getStatus() == StatusMedicao.VALIDADA) {
            throw new RuntimeException("Esta medição já foi validada.");
        }

        medicao.getItensMedicao().forEach(itemMedido -> {
            var itemOrcamento = itemMedido.getItem();

            BigDecimal novoAcumulado = itemOrcamento.getQuantidadeAcumulada().add(itemMedido.getQuantidadeMedida());

            if (novoAcumulado.compareTo(itemOrcamento.getQuantidade()) > 0) {
                throw new RuntimeException("Falha na validação: Saldo insuficiente para o item " + itemOrcamento.getDescricao());
            }

            itemOrcamento.setQuantidadeAcumulada(novoAcumulado);
            itemRepository.save(itemOrcamento);
        });

        medicao.setStatus(StatusMedicao.VALIDADA);
        medicaoRepository.save(medicao);
    }

    @Transactional
    public void deletar(Long id) {
        Medicao medicao = buscarEValidarMedicaoAberta(id);

        medicaoRepository.delete(medicao);
    }

    private Medicao buscarEValidarMedicaoAberta(Long id) {
        Medicao medicao = medicaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medição não encontrada"));

        if (medicao.getStatus() != StatusMedicao.ABERTA) {
            throw new RuntimeException("Operação negada: Apenas medições com status ABERTA podem ser alteradas ou removidas.");
        }
        return medicao;
    }

    private void processarItensMedicao(Medicao medicao, List<ItemMedicaoRequestDTO> itensDto) {
        BigDecimal valorTotalMedicao = BigDecimal.ZERO;

        for (ItemMedicaoRequestDTO itemDto : itensDto) {
            if (itemDto.id() != null) {
                // --- CASO 1: ATUALIZAÇÃO OU EXCLUSÃO ---
                ItemMedicao itemMedidoExistente = medicao.getItensMedicao().stream()
                        .filter(im -> im.getId().equals(itemDto.id()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Item de medição ID " + itemDto.id() + " não pertence a esta medição"));

                if (itemDto.excluir()) {
                    medicao.getItensMedicao().remove(itemMedidoExistente);
                    continue; // Pula para o próximo item, este foi removido
                }

                // Validação de saldo para atualização
                validarSaldoItemOrcamento(itemMedidoExistente.getItem(), itemDto.quantidadeMedida());

                // Atualiza os valores do item existente
                itemMedidoExistente.setQuantidadeMedida(itemDto.quantidadeMedida());
                BigDecimal novoTotalItem = itemDto.quantidadeMedida().multiply(itemMedidoExistente.getItem().getValorUnitario());
                itemMedidoExistente.setValorTotalMedido(novoTotalItem);

                valorTotalMedicao = valorTotalMedicao.add(novoTotalItem);

            } else if (!itemDto.excluir()) {
                // --- CASO 2: INCLUSÃO DE NOVO ITEM ---
                var itemOrcamento = itemRepository.findById(itemDto.itemOrcamentoId())
                        .orElseThrow(() -> new RuntimeException("Item do orçamento não encontrado"));

                validarSaldoItemOrcamento(itemOrcamento, itemDto.quantidadeMedida());

                ItemMedicao novoItemMedido = new ItemMedicao();
                novoItemMedido.setItem(itemOrcamento);
                novoItemMedido.setMedicao(medicao);
                novoItemMedido.setQuantidadeMedida(itemDto.quantidadeMedida());

                BigDecimal totalItem = itemDto.quantidadeMedida().multiply(itemOrcamento.getValorUnitario());
                novoItemMedido.setValorTotalMedido(totalItem);

                medicao.adicionarItem(novoItemMedido);
                valorTotalMedicao = valorTotalMedicao.add(totalItem);
            }
        }

        medicao.setValorTotalMedicao(valorTotalMedicao);
    }

    private void validarSaldoItemOrcamento(br.ce.sop.gestaoorcamento.model.Item itemOrcamento, BigDecimal quantidadeDesejada) {
        BigDecimal totalPrevisto = itemOrcamento.getQuantidadeAcumulada().add(quantidadeDesejada);
        if (totalPrevisto.compareTo(itemOrcamento.getQuantidade()) > 0) {
            throw new RuntimeException("Quantidade medida excede o saldo disponível para: " + itemOrcamento.getDescricao());
        }
    }

    private MedicaoResponseDTO converterParaDTO(Medicao m) {
        var itensDTO = m.getItensMedicao().stream()
                .map(i -> new ItemMedicaoResponseDTO(
                        i.getItem().getId(),
                        i.getItem().getDescricao(),
                        i.getQuantidadeMedida(),
                        i.getValorTotalMedido()))
                .toList();

        return new MedicaoResponseDTO(
                m.getId(),
                m.getOrcamento().getId(),
                m.getDataMedicao().toString(),
                m.getValorTotalMedicao(),
                itensDTO
        );
    }

    public List<MedicaoResponseDTO> listarPorOrcamento(Long orcamentoId) {
        return medicaoRepository.findByOrcamentoId(orcamentoId).stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public MedicaoResponseDTO buscarPorId(Long id) {
        Medicao medicao = medicaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medição não encontrada"));
        return converterParaDTO(medicao);
    }
}