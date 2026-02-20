package br.ce.sop.gestaoorcamento.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResumoOrcamentoDTO(
        Long idOrcamento,
        String numeroProtocolo,
        BigDecimal valorTotalOrcamento,
        BigDecimal totalMedidoAcumulado,
        BigDecimal saldoRestante,
        Double percentualConcluido,
        LocalDateTime dataUltimaMedicao
) {}
