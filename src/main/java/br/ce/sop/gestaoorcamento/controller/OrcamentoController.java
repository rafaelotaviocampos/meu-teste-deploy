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
        Orcamento orcamento = service.criar(dto);
        OrcamentoResponseDTO response = converterParaDTO(orcamento);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private OrcamentoResponseDTO converterParaDTO(Orcamento o) {
        List<ItemResponseDTO> itensDTO = o.getItens().stream()
                .map(i -> new ItemResponseDTO(i.getId(), i.getDescricao(), i.getQuantidade(), i.getValorUnitario(), i.getValorTotal()))
                .toList();

        return new OrcamentoResponseDTO(o.getId(), o.getNumeroProtocolo(),o.getTipo().getDescricao(), o.getStatus().name(), o.getValorTotal(), itensDTO);
    }
}