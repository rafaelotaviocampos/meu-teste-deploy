package br.ce.sop.gestaoorcamento.dto;

import java.math.BigDecimal;

public record ItemResponseDTO(
        Long id,
        String descricao,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal
) {}