package br.ce.sop.gestaoorcamento.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrcamentoResponseDTO(
        Long id,
        String numeroProtocolo,
        String tipo,
        String status,
        BigDecimal valorTotal,
        List<ItemResponseDTO> itens
) {}
