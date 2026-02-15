package br.ce.sop.gestaoorcamento.dto;

import java.math.BigDecimal;
import java.util.List;

public record MedicaoResponseDTO(
        Long id,
        Long orcamentoId,
        String dataCriacao,
        BigDecimal valorTotalMedicao,
        List<ItemMedicaoResponseDTO> itens
) {}
