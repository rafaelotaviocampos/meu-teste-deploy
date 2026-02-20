package br.ce.sop.gestaoorcamento.controller;

import br.ce.sop.gestaoorcamento.model.enums.StatusMedicao;
import br.ce.sop.gestaoorcamento.model.enums.StatusOrcamento;
import br.ce.sop.gestaoorcamento.model.enums.TipoOrcamento;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/enums")
public class EnumController {

    @GetMapping("/tipos-orcamento")
    public List<Map<String, String>> getTiposOrcamento() {
        return Arrays.stream(TipoOrcamento.values())
                .map(e -> Map.of("id", e.name(), "descricao", e.getDescricao(), "cor", e.getCor()))
                .toList();
    }

    @GetMapping("/status-orcamento")
    public List<Map<String, String>> getStatusOrcamento() {
        return Arrays.stream(StatusOrcamento.values())
                .map(e -> Map.of("id", e.name(), "descricao", e.getDescricao(), "cor", e.getCor()))
                .toList();
    }

    @GetMapping("/status-medicao")
    public List<Map<String, String>> getStatusMedicao() {
        return Arrays.stream(StatusMedicao.values())
                .map(e -> Map.of("id", e.name(), "descricao", e.getDescricao(), "cor", e.getCor()))
                .toList();
    }
}