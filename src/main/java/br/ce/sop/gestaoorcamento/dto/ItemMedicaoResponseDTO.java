package br.ce.sop.gestaoorcamento.dto;

import java.math.BigDecimal;

public record ItemMedicaoResponseDTO(
        Long itemOrcamentoId,
        String descricao,
        BigDecimal quantidadeMedida,
        BigDecimal valorTotalItemMedicao
) {}