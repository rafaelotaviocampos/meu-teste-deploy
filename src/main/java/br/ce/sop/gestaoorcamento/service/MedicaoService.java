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
        Medicao medicao = buscarEValidarMedicao(id);

        // ESTORNO
        medicao.getItensMedicao().forEach(itemMedido -> {
            var itemOrcamento = itemMedido.getItem();
            itemOrcamento.setQuantidadeAcumulada(
                    itemOrcamento.getQuantidadeAcumulada().subtract(itemMedido.getQuantidadeMedida())
            );
            itemRepository.save(itemOrcamento);
        });

        medicao.getItensMedicao().clear();
        medicao.setNumeroMedicao(dto.numeroMedicao());
        medicao.setObservacao(dto.observacao());

        processarItensMedicao(medicao, dto.itens());

        return converterParaDTO(medicaoRepository.save(medicao));
    }

    @Transactional
    public void validar(Long id) {
        Medicao medicao = buscarEValidarMedicao(id);
        medicao.validar();
        medicaoRepository.save(medicao);
    }

    @Transactional
    public void deletar(Long id) {
        Medicao medicao = buscarEValidarMedicao(id);

        medicao.getItensMedicao().forEach(itemMedido -> {
            var itemOrcamento = itemMedido.getItem();
            itemOrcamento.setQuantidadeAcumulada(
                    itemOrcamento.getQuantidadeAcumulada().subtract(itemMedido.getQuantidadeMedida())
            );
            itemRepository.save(itemOrcamento);
        });

        medicaoRepository.delete(medicao);
    }

    private Medicao buscarEValidarMedicao(Long id) {
        Medicao medicao = medicaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medição não encontrada"));

        if (!medicao.isEditavel()) {
            throw new RuntimeException("Operação não permitida: A medição não está mais em estado ABERTO.");
        }
        return medicao;
    }

    private void processarItensMedicao(Medicao medicao, List<ItemMedicaoRequestDTO> itensDto) {
        BigDecimal valorTotalMedicao = BigDecimal.ZERO;

        for (ItemMedicaoRequestDTO itemDto : itensDto) {
            var itemOrcamento = itemRepository.findById(itemDto.itemOrcamentoId())
                    .orElseThrow(() -> new RuntimeException("Item do orçamento não encontrado"));

            BigDecimal novoAcumulado = itemOrcamento.getQuantidadeAcumulada().add(itemDto.quantidadeMedida());

            if (novoAcumulado.compareTo(itemOrcamento.getQuantidade()) > 0) {
                throw new RuntimeException("Saldo insuficiente para o item: " + itemOrcamento.getDescricao());
            }

            itemOrcamento.setQuantidadeAcumulada(novoAcumulado);
            itemRepository.save(itemOrcamento);

            ItemMedicao im = new ItemMedicao();
            im.setItem(itemOrcamento);
            im.setMedicao(medicao);
            im.setQuantidadeMedida(itemDto.quantidadeMedida());

            BigDecimal totalItem = itemDto.quantidadeMedida().multiply(itemOrcamento.getValorUnitario());
            im.setValorTotalMedido(totalItem);

            medicao.adicionarItem(im);
            valorTotalMedicao = valorTotalMedicao.add(totalItem);
        }

        medicao.setValorTotalMedicao(valorTotalMedicao);
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
}