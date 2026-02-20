package br.ce.sop.gestaoorcamento.dto.dashboard;

import java.math.BigDecimal;

public record ResumoObraDTO(
        Long orcamentoId,           // o.id
        String protocolo,           // o.numeroProtocolo
        BigDecimal valorTotal,      // o.valorTotal
        BigDecimal totalMedido,     // SUM(...)
        BigDecimal saldoAReceber,   // o.valorTotal - SUM
        Double percentualConcluido  // CAST(... AS double)
) {}