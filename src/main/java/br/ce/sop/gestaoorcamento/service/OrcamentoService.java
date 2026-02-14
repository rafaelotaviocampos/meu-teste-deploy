package br.ce.sop.gestaoorcamento.service;

import br.ce.sop.gestaoorcamento.dto.OrcamentoRequestDTO;
import br.ce.sop.gestaoorcamento.model.Item;
import br.ce.sop.gestaoorcamento.model.Orcamento;
import br.ce.sop.gestaoorcamento.model.enums.StatusOrcamento;
import br.ce.sop.gestaoorcamento.repository.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoRepository repository;

    @Transactional
    public Orcamento criar(OrcamentoRequestDTO dto) {
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

        return repository.save(orcamento);
    }
}