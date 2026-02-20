package br.ce.sop.gestaoorcamento.controller;

import br.ce.sop.gestaoorcamento.dto.MedicaoRequestDTO;
import br.ce.sop.gestaoorcamento.dto.MedicaoResponseDTO;
import br.ce.sop.gestaoorcamento.service.MedicaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicoes")
@RequiredArgsConstructor
@Tag(name = "Medições", description = "Gerenciamento de medições e controle de saldo de itens")
public class MedicaoController {

    private final MedicaoService service;

    @PostMapping
    @Operation(summary = "Criar nova medição")
    public ResponseEntity<MedicaoResponseDTO> criar(@RequestBody @Valid MedicaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar medição existente")
    public ResponseEntity<MedicaoResponseDTO> atualizar(@PathVariable Long id,
                                                        @RequestBody @Valid MedicaoRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @PatchMapping("/{id}/validar")
    @Operation(summary = "Validar medição e atualizar saldo dos itens")
    public ResponseEntity<Void> validar(@PathVariable Long id) {
        service.validar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir medição")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orcamento/{orcamentoId}")
    @Operation(summary = "Listar todas as medições de um orçamento")
    public ResponseEntity<List<MedicaoResponseDTO>> listarPorOrcamento(@PathVariable Long orcamentoId) {
        return ResponseEntity.ok(service.listarPorOrcamento(orcamentoId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar medição por ID")
    public ResponseEntity<MedicaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping // Agora o GET em /api/v1/medicoes vai funcionar!
    @Operation(summary = "Listar todas as medições")
    public ResponseEntity<List<MedicaoResponseDTO>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }
}
