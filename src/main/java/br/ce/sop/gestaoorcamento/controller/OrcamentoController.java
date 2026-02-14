package br.ce.sop.gestaoorcamento.controller;

import br.ce.sop.gestaoorcamento.dto.ItemResponseDTO;
import br.ce.sop.gestaoorcamento.dto.OrcamentoRequestDTO;
import br.ce.sop.gestaoorcamento.dto.OrcamentoResponseDTO;
import br.ce.sop.gestaoorcamento.model.Orcamento;
import br.ce.sop.gestaoorcamento.service.OrcamentoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orcamentos")
@RequiredArgsConstructor

public class OrcamentoController {

    private final OrcamentoService service;

    @PostMapping
    public ResponseEntity<OrcamentoResponseDTO> criar(@RequestBody @Valid OrcamentoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrcamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<OrcamentoResponseDTO>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Retorna 204 se der certo
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }

}