package br.ce.sop.gestaoorcamento.controller;

import br.ce.sop.gestaoorcamento.dto.MedicaoRequestDTO;
import br.ce.sop.gestaoorcamento.dto.MedicaoResponseDTO;
import br.ce.sop.gestaoorcamento.service.MedicaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicoes")
@RequiredArgsConstructor
public class MedicaoController {

    private final MedicaoService service;

    @PostMapping
    public ResponseEntity<MedicaoResponseDTO> criar(@RequestBody @Valid MedicaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicaoResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid MedicaoRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @PatchMapping("/{id}/validar")
    public ResponseEntity<Void> validar(@PathVariable Long id) {
        service.validar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orcamento/{orcamentoId}")
    public ResponseEntity<List<MedicaoResponseDTO>> listarPorOrcamento(@PathVariable Long orcamentoId) {
        return ResponseEntity.ok(service.listarPorOrcamento(orcamentoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

}