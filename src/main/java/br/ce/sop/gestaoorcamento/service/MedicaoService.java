package br.ce.sop.gestaoorcamento.service;

import br.ce.sop.gestaoorcamento.dto.*;
import br.ce.sop.gestaoorcamento.exception.RecursoNaoEncontradoException;
import br.ce.sop.gestaoorcamento.exception.RegraDeNegocioException;
import br.ce.sop.gestaoorcamento.model.Item;
import br.ce.sop.gestaoorcamento.model.ItemMedicao;
import br.ce.sop.gestaoorcamento.model.Medicao;
import br.ce.sop.gestaoorcamento.model.Orcamento;
import br.ce.sop.gestaoorcamento.model.enums.StatusMedicao;
import br.ce.sop.gestaoorcamento.repository.ItemRepository;
import br.ce.sop.gestaoorcamento.repository.MedicaoRepository;
import br.ce.sop.gestaoorcamento.repository.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MedicaoService {

    private final MedicaoRepository medicaoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final ItemRepository itemRepository;

    public MedicaoResponseDTO criar(MedicaoRequestDTO dto) {
        validarUnicidadeMedicaoAberta(dto.orcamentoId());
        validarNumeroMedicaoUnico(dto.numeroMedicao());

        var orcamento = buscarOrcamento(dto.orcamentoId());

        var medicao = new Medicao();
        medicao.setOrcamento(orcamento);
        medicao.setNumeroMedicao(dto.numeroMedicao());
        medicao.setObservacao(dto.observacao());
        medicao.setDataMedicao(LocalDateTime.now());
        medicao.setStatus(StatusMedicao.ABERTA);

        processarItens(medicao, dto.itens());

        return toDTO(medicaoRepository.save(medicao));
    }

    public MedicaoResponseDTO atualizar(Long id, MedicaoRequestDTO dto) {
        var medicao = buscarMedicaoAberta(id);

        medicao.setNumeroMedicao(dto.numeroMedicao());
        medicao.setObservacao(dto.observacao());

        processarItens(medicao, dto.itens());

        return toDTO(medicao);
    }

    public void validar(Long id) {
        var medicao = buscarMedicao(id);

        if (medicao.getStatus() == StatusMedicao.VALIDADA) {
            throw new RegraDeNegocioException("Medição já validada.");
        }

        medicao.getItensMedicao().forEach(itemMedido -> {
            //var item = itemMedido.getItem();
            var item = itemRepository.findByIdWithLock(itemMedido.getItem().getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado."));
            BigDecimal novoAcumulado =
                    item.getQuantidadeAcumulada().add(itemMedido.getQuantidadeMedida());

            if (novoAcumulado.compareTo(item.getQuantidade()) > 0) {
                throw new RegraDeNegocioException(
                        "Saldo insuficiente para o item: " + item.getDescricao());
            }

            item.setQuantidadeAcumulada(novoAcumulado);
        });

        medicao.validar();
    }

    public void deletar(Long id) {
        var medicao = buscarMedicao(id);

        if (medicao.getStatus() == StatusMedicao.VALIDADA) {
            estornarSaldo(medicao);
        }

        medicaoRepository.delete(medicao);
    }

    // ===============================
    // MÉTODOS PRIVADOS
    // ===============================
    private void validarUnicidadeMedicaoAberta(Long orcamentoId) {
        if (medicaoRepository.existsByOrcamentoIdAndStatus(orcamentoId, StatusMedicao.ABERTA)) {
            throw new RegraDeNegocioException(
                    "Já existe medição ABERTA para este orçamento.");
        }
    }

    private void validarNumeroMedicaoUnico(String numero) {
        if (medicaoRepository.findByNumeroMedicao(numero).isPresent()) {
            throw new RegraDeNegocioException(
                    "Número de medição já utilizado: " + numero);
        }
    }

    private Orcamento buscarOrcamento(Long id) {
        return orcamentoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Orçamento não encontrado."));
    }

    private Medicao buscarMedicao(Long id) {
        return medicaoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Medição não encontrada."));
    }

    private Medicao buscarMedicaoAberta(Long id) {
        var medicao = buscarMedicao(id);

        if (medicao.getStatus() != StatusMedicao.ABERTA) {
            throw new RegraDeNegocioException(
                    "Apenas medições ABERTAS podem ser alteradas.");
        }

        return medicao;
    }

    private void estornarSaldo(Medicao medicao) {
        medicao.getItensMedicao().forEach(itemMedido -> {
            var item = itemMedido.getItem();

            BigDecimal novoAcumulado =
                    item.getQuantidadeAcumulada()
                            .subtract(itemMedido.getQuantidadeMedida());

            item.setQuantidadeAcumulada(
                    novoAcumulado.max(BigDecimal.ZERO));
        });
    }

    private void processarItens(Medicao medicao, List<ItemMedicaoRequestDTO> itensDto) {
        BigDecimal totalGeralMedicao = BigDecimal.ZERO;

        // Criamos uma cópia da lista atual para iterar sem erro de concorrência ao remover
        var itensAtuais = new ArrayList<>(medicao.getItensMedicao());

        for (var dto : itensDto) {
            // 1. Verificar se o item já existe nesta medição
            var itemExistente = itensAtuais.stream()
                    .filter(i -> i.getItem().getId().equals(dto.itemOrcamentoId()))
                    .findFirst();

            // 2. Se o front marcou para excluir
            if (dto.excluir() != null && dto.excluir()) {
                itemExistente.ifPresent(medicao.getItensMedicao()::remove);
                continue; // Pula para o próximo item
            }

            // 3. Validar se o item existe no orçamento
            var itemOrcamento = itemRepository.findById(dto.itemOrcamentoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Item do orçamento não encontrado."));

            validarQuantidade(dto.quantidadeMedida());

            // Se for novo ou se a quantidade mudou, validamos o saldo
            // (Considerando que se for atualização, o saldo atual já tem o acumulado antigo)
            validarSaldo(itemOrcamento, dto.quantidadeMedida(), itemExistente.isPresent() ? itemExistente.get().getQuantidadeMedida() : BigDecimal.ZERO);

            BigDecimal valorTotalItemMedicao = dto.quantidadeMedida().multiply(itemOrcamento.getValorUnitario());
            totalGeralMedicao = totalGeralMedicao.add(valorTotalItemMedicao);

            if (itemExistente.isPresent()) {
                // ATUALIZAÇÃO: O Hibernate fará um UPDATE
                var item = itemExistente.get();
                item.setQuantidadeMedida(dto.quantidadeMedida());
                item.setValorTotalMedido(valorTotalItemMedicao);
            } else {
                // INSERÇÃO: Novo item na medição
                var novoItemMedicao = new ItemMedicao();
                novoItemMedicao.setMedicao(medicao);
                novoItemMedicao.setItem(itemOrcamento);
                novoItemMedicao.setQuantidadeMedida(dto.quantidadeMedida());
                novoItemMedicao.setValorTotalMedido(valorTotalItemMedicao);
                medicao.adicionarItem(novoItemMedicao);
            }
        }

        medicao.setValorTotalMedicao(totalGeralMedicao);
    }

    private void validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException(
                    "Quantidade medida deve ser maior que zero.");
        }
    }

    private void validarSaldo(Item item, BigDecimal novaQuantidade, BigDecimal quantidadeAnterior) {
        BigDecimal acumuladoOutrasMedicoes = item.getQuantidadeAcumulada().subtract(quantidadeAnterior);
        BigDecimal totalPrevisto = acumuladoOutrasMedicoes.add(novaQuantidade);

        if (totalPrevisto.compareTo(item.getQuantidade()) > 0) {
            throw new RegraDeNegocioException(
                    "Quantidade excede saldo disponível para: " + item.getDescricao());
        }
    }

    private MedicaoResponseDTO toDTO(Medicao medicao) {
        var itens = medicao.getItensMedicao().stream()
                .map(i -> new ItemMedicaoResponseDTO(
                        i.getId(),
                        i.getItem().getId(),
                        i.getItem().getDescricao(),
                        i.getItem().getValorUnitario(),
                        i.getItem().getQuantidade(),
                        i.getItem().getQuantidadeAcumulada(),
                        i.getItem().getValorTotal(),
                        i.getQuantidadeMedida(),
                        i.getValorTotalMedido()
                        ))
                .toList();

        return new MedicaoResponseDTO(
                medicao.getId(),
                medicao.getOrcamento().getId(),
                medicao.getOrcamento().getNumeroProtocolo(),
                medicao.getNumeroMedicao(),
                medicao.getObservacao(),
                medicao.getDataCriacao().toString(),
                medicao.getDataMedicao().toString(),
                medicao.getDataValidacao() != null ? medicao.getDataValidacao().toString() : null,
                medicao.getStatus().getId(),
                medicao.getValorTotalMedicao(),
                itens
        );
    }

    public List<MedicaoResponseDTO> listarPorOrcamento(Long orcamentoId) {
        return medicaoRepository.findByOrcamentoId(orcamentoId).stream()
                .map(this::toDTO)
                .toList();
    }

    public MedicaoResponseDTO buscarPorId(Long id) {
        return toDTO(buscarMedicao(id));
    }

    public List<MedicaoResponseDTO> listarTodas() {
        return medicaoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }
}