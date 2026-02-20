package br.ce.sop.gestaoorcamento.dto.dashboard;

import java.math.BigDecimal;

public record EvolucaoFinanceiraDTO(
        String periodo,
        BigDecimal totalOrcado,
        BigDecimal totalMedido,
        Object percentualExecucao
) {
    public EvolucaoFinanceiraDTO {
        if (percentualExecucao instanceof BigDecimal bd) {
            percentualExecucao = bd.doubleValue();
        } else if (percentualExecucao instanceof Number n) {
            percentualExecucao = n.doubleValue();
        }
    }
}