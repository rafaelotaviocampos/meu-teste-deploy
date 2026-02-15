package br.ce.sop.gestaoorcamento.service;

import br.ce.sop.gestaoorcamento.dto.ItemRequestDTO;
import br.ce.sop.gestaoorcamento.dto.ItemResponseDTO;
import br.ce.sop.gestaoorcamento.dto.OrcamentoRequestDTO;
import br.ce.sop.gestaoorcamento.dto.OrcamentoResponseDTO;
import br.ce.sop.gestaoorcamento.model.Item;
import br.ce.sop.gestaoorcamento.model.Orcamento;
import br.ce.sop.gestaoorcamento.model.enums.StatusOrcamento;
import br.ce.sop.gestaoorcamento.repository.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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

        // Mapeia os DTOs de Itens para a Entidade e calcula os totais de cada item
        if (dto.itens() != null) {
            dto.itens().forEach(itemDto -> {
                Item item = new Item();
                item.setDescricao(itemDto.descricao());
                item.setQuantidade(itemDto.quantidade());
                item.setValorUnitario(itemDto.valorUnitario());

                // Cálculo automático: valor_total_item = qtd * unitario
                BigDecimal valorTotalItem = itemDto.quantidade().multiply(itemDto.valorUnitario());
                item.setValorTotal(valorTotalItem);

                // Helper method para garantir o vínculo bidirecional
                orcamento.adicionarItem(item);
            });
        }

        // Regra de Negócio: O valor total do Orçamento é a soma dos totais dos itens
        BigDecimal valorTotalGeral = orcamento.getItens().stream()
                .map(Item::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        orcamento.setValorTotal(valorTotalGeral);

         repository.save(orcamento);
        return converterParaDTO(orcamento);
    }

    public OrcamentoResponseDTO buscarPorId(Long id) {
        Orcamento orcamento = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado"));
        return converterParaDTO(orcamento);
    }

    public List<OrcamentoResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private OrcamentoResponseDTO converterParaDTO(Orcamento o) {
        var itensDTO = o.getItens().stream()
                .map(i -> new ItemResponseDTO(i.getId(), i.getDescricao(), i.getQuantidade(), i.getValorUnitario(), i.getValorTotal()))
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

    @Transactional
    public void deletar(Long id) {
        Orcamento orcamento = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado"));
        //@SoftDelete
        repository.delete(orcamento);
    }

    @Transactional
    public OrcamentoResponseDTO atualizar(Long id, OrcamentoRequestDTO dto) {
        Orcamento orcamento = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado"));

        if (orcamento.getStatus() != StatusOrcamento.ABERTO) {
            throw new RuntimeException("Não é permitido editar orçamento FINALIZADO.");
        }

        orcamento.setNumeroProtocolo(dto.numeroProtocolo());
        orcamento.setTipo(dto.tipo());

        for (ItemRequestDTO itemDto : dto.itens()) {
            if (itemDto.id() != null) {
                Item itemExistente = orcamento.getItens().stream()
                        .filter(i -> i.getId().equals(itemDto.id()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Item ID " + itemDto.id() + " não pertence a este orçamento"));

                if (itemDto.excluir()) {
                    // Valida restrição de integridade antes da remoção
                    if (itemExistente.getQuantidadeAcumulada().compareTo(BigDecimal.ZERO) > 0) {
                        throw new RuntimeException("Não é possível excluir o item '" + itemExistente.getDescricao() + "' pois ele já possui medições.");
                    }
                    orcamento.getItens().remove(itemExistente);
                } else {
                    itemExistente.setDescricao(itemDto.descricao());
                    itemExistente.setQuantidade(itemDto.quantidade());
                    itemExistente.setValorUnitario(itemDto.valorUnitario());
                    itemExistente.setValorTotal(itemDto.quantidade().multiply(itemDto.valorUnitario()));
                }
            } else if (!itemDto.excluir()) {
                Item novoItem = new Item();
                novoItem.setDescricao(itemDto.descricao());
                novoItem.setQuantidade(itemDto.quantidade());
                novoItem.setValorUnitario(itemDto.valorUnitario());
                novoItem.setValorTotal(itemDto.quantidade().multiply(itemDto.valorUnitario()));
                novoItem.setQuantidadeAcumulada(BigDecimal.ZERO);
                orcamento.adicionarItem(novoItem);
            }
        }

        // Atualiza o valor total do orçamento baseado na composição atual de itens
        BigDecimal novoTotalGeral = orcamento.getItens().stream()
                .map(Item::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orcamento.setValorTotal(novoTotalGeral);

        return converterParaDTO(repository.save(orcamento));
    }
}