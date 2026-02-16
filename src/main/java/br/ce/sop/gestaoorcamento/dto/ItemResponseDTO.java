package br.ce.sop.gestaoorcamento.dto;

import br.ce.sop.gestaoorcamento.model.Item;

import java.math.BigDecimal;

public record ItemResponseDTO(
        Long id,
        String descricao,
        BigDecimal quantidade,
        BigDecimal quantidadeAcumulada,
        BigDecimal valorUnitario,
        BigDecimal valorTotal
) {
    public ItemResponseDTO(Item item) {
        this(
                item.getId(),
                item.getDescricao(),
                item.getQuantidade(),
                BigDecimal.ZERO, // Depois você implementa a lógica de acumulado
                item.getValorUnitario(),
                item.getValorTotal()
        );
    }
}