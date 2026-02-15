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

        medicao.setStatus(StatusMedicao.VALIDADA);
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

        BigDecimal total = BigDecimal.ZERO;
        medicao.getItensMedicao().clear();

        for (var dto : itensDto) {

            var itemOrcamento = itemRepository.findById(dto.itemOrcamentoId())
                    .orElseThrow(() ->
                            new RecursoNaoEncontradoException("Item do orçamento não encontrado."));

            validarQuantidade(dto.quantidadeMedida());
            validarSaldo(itemOrcamento, dto.quantidadeMedida());

            var itemMedicao = new ItemMedicao();
            itemMedicao.setMedicao(medicao);
            itemMedicao.setItem(itemOrcamento);
            itemMedicao.setQuantidadeMedida(dto.quantidadeMedida());

            BigDecimal totalItem =
                    dto.quantidadeMedida().multiply(itemOrcamento.getValorUnitario());

            itemMedicao.setValorTotalMedido(totalItem);

            medicao.adicionarItem(itemMedicao);

            total = total.add(totalItem);
        }

        medicao.setValorTotalMedicao(total);
    }

    private void validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException(
                    "Quantidade medida deve ser maior que zero.");
        }
    }

    private void validarSaldo(Item item, BigDecimal quantidade) {
        BigDecimal totalPrevisto =
                item.getQuantidadeAcumulada().add(quantidade);

        if (totalPrevisto.compareTo(item.getQuantidade()) > 0) {
            throw new RegraDeNegocioException(
                    "Quantidade excede saldo disponível para: " + item.getDescricao());
        }
    }

    private MedicaoResponseDTO toDTO(Medicao medicao) {
        var itens = medicao.getItensMedicao().stream()
                .map(i -> new ItemMedicaoResponseDTO(
                        i.getItem().getId(),
                        i.getItem().getDescricao(),
                        i.getQuantidadeMedida(),
                        i.getValorTotalMedido()))
                .toList();

        return new MedicaoResponseDTO(
                medicao.getId(),
                medicao.getOrcamento().getId(),
                medicao.getDataMedicao().toString(),
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
}