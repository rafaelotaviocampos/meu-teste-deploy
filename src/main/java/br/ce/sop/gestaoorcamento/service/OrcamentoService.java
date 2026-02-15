package br.ce.sop.gestaoorcamento.service;

import br.ce.sop.gestaoorcamento.dto.*;
import br.ce.sop.gestaoorcamento.exception.RecursoNaoEncontradoException;
import br.ce.sop.gestaoorcamento.exception.RegraDeNegocioException;
import br.ce.sop.gestaoorcamento.model.Item;
import br.ce.sop.gestaoorcamento.model.Orcamento;
import br.ce.sop.gestaoorcamento.model.enums.StatusOrcamento;
import br.ce.sop.gestaoorcamento.repository.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoRepository repository;

    @Transactional
    public OrcamentoResponseDTO criar(OrcamentoRequestDTO dto) {
        Orcamento orcamento = new Orcamento();
        orcamento.setNumeroProtocolo(dto.numeroProtocolo());
        orcamento.setTipo(dto.tipo());
        orcamento.setStatus(StatusOrcamento.ABERTO);

        // Processa itens
        if (dto.itens() != null) {
            dto.itens().forEach(itemDto -> orcamento.adicionarItem(criarItemDoDTO(itemDto)));
        }

        orcamento.setValorTotal(calcularValorTotal(orcamento));
        repository.save(orcamento);

        return converterParaDTO(orcamento);
    }

    @Transactional
    public OrcamentoResponseDTO atualizar(Long id, OrcamentoRequestDTO dto) {
        Orcamento orcamento = buscarOuFalhar(id);

        if (orcamento.getStatus() != StatusOrcamento.ABERTO) {
            throw new RegraDeNegocioException("Não é permitido editar orçamento FINALIZADO.");
        }

        orcamento.setNumeroProtocolo(dto.numeroProtocolo());
        orcamento.setTipo(dto.tipo());

        atualizarItens(orcamento, dto.itens());
        orcamento.setValorTotal(calcularValorTotal(orcamento));

        return converterParaDTO(repository.save(orcamento));
    }

    @Transactional
    public void deletar(Long id) {
        Orcamento orcamento = buscarOuFalhar(id);
        repository.delete(orcamento);
    }

    public OrcamentoResponseDTO buscarPorId(Long id) {
        return converterParaDTO(buscarOuFalhar(id));
    }

    public List<OrcamentoResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::converterParaDTO)
                .toList();
    }

    /* ---------- MÉTODOS AUXILIARES ---------- */

    private Orcamento buscarOuFalhar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento", id));
    }

    private Item criarItemDoDTO(ItemRequestDTO dto) {
        Item item = new Item();
        item.setDescricao(dto.descricao());
        item.setQuantidade(dto.quantidade());
        item.setValorUnitario(dto.valorUnitario());
        item.setValorTotal(dto.quantidade().multiply(dto.valorUnitario()));
        item.setQuantidadeAcumulada(BigDecimal.ZERO);
        return item;
    }

    private void atualizarItens(Orcamento orcamento, List<ItemRequestDTO> itensDto) {
        if (itensDto == null) return;

        for (ItemRequestDTO dto : itensDto) {
            if (dto.id() != null) {
                Item itemExistente = orcamento.getItens().stream()
                        .filter(i -> i.getId().equals(dto.id()))
                        .findFirst()
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Item do orçamento", dto.id()));

                if (dto.excluir()) {
                    if (itemExistente.getQuantidadeAcumulada().compareTo(BigDecimal.ZERO) > 0) {
                        throw new RegraDeNegocioException(
                                "Não é possível excluir o item '" + itemExistente.getDescricao() + "' pois ele já possui medições."
                        );
                    }
                    orcamento.getItens().remove(itemExistente);
                } else {
                    itemExistente.setDescricao(dto.descricao());
                    itemExistente.setQuantidade(dto.quantidade());
                    itemExistente.setValorUnitario(dto.valorUnitario());
                    itemExistente.setValorTotal(dto.quantidade().multiply(dto.valorUnitario()));
                }
            } else if (!dto.excluir()) {
                orcamento.adicionarItem(criarItemDoDTO(dto));
            }
        }
    }

    private BigDecimal calcularValorTotal(Orcamento orcamento) {
        return orcamento.getItens().stream()
                .map(Item::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrcamentoResponseDTO converterParaDTO(Orcamento o) {
        List<ItemResponseDTO> itensDTO = o.getItens().stream()
                .map(i -> new ItemResponseDTO(
                        i.getId(),
                        i.getDescricao(),
                        i.getQuantidade(),
                        i.getQuantidadeAcumulada(),
                        i.getValorUnitario(),
                        i.getValorTotal()
                ))
                .toList();

        return new OrcamentoResponseDTO(
                o.getId(),
                o.getNumeroProtocolo(),
                o.getTipo().getDescricao(),
                o.getStatus().name(),
                o.getValorTotal(),
                itensDTO
        );
    }
}
