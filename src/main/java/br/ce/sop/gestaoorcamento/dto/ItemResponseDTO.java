package br.ce.sop.gestaoorcamento.dto;

import br.ce.sop.gestaoorcamento.model.Item;

import java.math.BigDecimal;

public record ItemResponseDTO(
        Long id,
        String descricao,
        BigDecimal quantidade,
        BigDecimal quantidadeAcumulada,
        BigDecimal valorUnitario,
        BigDecimal valorTotal,
        boolean possuiMedicao
) {
    public ItemResponseDTO(Item item, boolean possuiMedicao) {
        this(
                item.getId(),
                item.getDescricao(),
                item.getQuantidade(),
                item.getQuantidadeAcumulada(),
                item.getValorUnitario(),
                item.getValorTotal(),
                possuiMedicao
        );
    }
}