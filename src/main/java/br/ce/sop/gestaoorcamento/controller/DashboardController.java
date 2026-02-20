package br.ce.sop.gestaoorcamento.controller;

import br.ce.sop.gestaoorcamento.dto.dashboard.EvolucaoFinanceiraDTO;
import br.ce.sop.gestaoorcamento.dto.dashboard.ResumoObraDTO;
import br.ce.sop.gestaoorcamento.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints para relatórios e indicadores")
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/evolucao-financeira")
    @Operation(summary = "Dados para gráfico de evolução mensal")
    public ResponseEntity<List<EvolucaoFinanceiraDTO>> getEvolucao() {
        return ResponseEntity.ok(service.getEvolucaoFinanceira());
    }

    @GetMapping("/resumo-obras")
    @Operation(summary = "Dados para tabela geral de acompanhamento de obras")
    public ResponseEntity<List<ResumoObraDTO>> getResumo() {
        return ResponseEntity.ok(service.getResumoObras());
    }
}
