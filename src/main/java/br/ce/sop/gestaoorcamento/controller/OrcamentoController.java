package br.ce.sop.gestaoorcamento.controller;

import br.ce.sop.gestaoorcamento.dto.OrcamentoRequestDTO;
import br.ce.sop.gestaoorcamento.dto.OrcamentoResponseDTO;
import br.ce.sop.gestaoorcamento.service.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orcamentos")
@RequiredArgsConstructor
@Tag(name = "Orçamentos", description = "Gerenciamento de orçamentos e seus itens")
public class OrcamentoController {

    private final OrcamentoService service;

    @PostMapping
    @Operation(summary = "Criar novo orçamento")
    public ResponseEntity<OrcamentoResponseDTO> criar(@RequestBody @Valid OrcamentoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar orçamento por ID")
    public ResponseEntity<OrcamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar todos os orçamentos")
    public ResponseEntity<List<OrcamentoResponseDTO>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar orçamento existente")
    public ResponseEntity<OrcamentoResponseDTO> atualizar(@PathVariable Long id,
                                                          @RequestBody @Valid OrcamentoRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir orçamento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
