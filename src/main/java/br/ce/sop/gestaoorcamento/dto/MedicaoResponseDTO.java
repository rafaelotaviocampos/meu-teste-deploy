package br.ce.sop.gestaoorcamento.dto;

import java.math.BigDecimal;
import java.util.List;

public record MedicaoResponseDTO(
        Long id,
        Long orcamentoId,
        String numeroProtocolo,
        String numeroMedicao,
        String observacao,
        String dataCriacao,
        String dataMedicao,
        String dataValidacao,
        String status,
        BigDecimal valorTotalMedicao,
        List<ItemMedicaoResponseDTO> itens
) {}
