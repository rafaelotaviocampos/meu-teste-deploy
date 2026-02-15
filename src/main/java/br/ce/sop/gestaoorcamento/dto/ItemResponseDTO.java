package br.ce.sop.gestaoorcamento.dto;

import java.math.BigDecimal;

public record ItemResponseDTO(
        Long id,
        String descricao,
        BigDecimal quantidade,
        BigDecimal quantidadeAcumulada,
        BigDecimal valorUnitario,
        BigDecimal valorTotal
) {}