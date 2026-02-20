package br.ce.sop.gestaoorcamento.controller;

import br.ce.sop.gestaoorcamento.dto.ItemResponseDTO;
import br.ce.sop.gestaoorcamento.dto.OrcamentoRequestDTO;
import br.ce.sop.gestaoorcamento.dto.OrcamentoResponseDTO;
import br.ce.sop.gestaoorcamento.service.ItemService;
import br.ce.sop.gestaoorcamento.service.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orcamentos")
@RequiredArgsConstructor
@Tag(name = "Orçamentos", description = "Gerenciamento de orçamentos e seus itens")
public class OrcamentoController {

    private final OrcamentoService service;
    private final ItemService itemService;

    @PostMapping
    @Operation(summary = "Criar novo orçamento")
    public ResponseEntity<OrcamentoResponseDTO> criar(@RequestBody @Valid OrcamentoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
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

    @GetMapping("/{id}/itens")
    @Operation(summary = "Listar itens de um orçamento", description = "Retorna todos os itens vinculados a um orçamento específico para seleção na medição")
    public ResponseEntity<List<ItemResponseDTO>> listarItens(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.listarPorOrcamento(id));
    }

    @PatchMapping("/{id}/finalizar")
    @Operation(summary = "Validar orçamento")
    public ResponseEntity<Void> finalizar(@PathVariable Long id) {
        service.finalizar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/disponiveis-para-medicao")
    @Operation(summary = "Lista orçamentos que não possuem medições abertas no momento")
    public ResponseEntity<List<OrcamentoResponseDTO>> listarDisponiveis() {
        return ResponseEntity.ok(service.listarDisponiveisParaMedicao());
    }

    @GetMapping("/{id}/itens-com-saldo")
    @Operation(summary = "Lista apenas os itens de um orçamento que ainda possuem saldo para serem medidos")
    public ResponseEntity<List<ItemResponseDTO>> listarItensComSaldo(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.listarItensComSaldo(id));
    }
}
