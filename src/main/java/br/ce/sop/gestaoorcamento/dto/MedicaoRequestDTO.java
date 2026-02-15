package br.ce.sop.gestaoorcamento.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MedicaoRequestDTO(
        @NotNull(message = "O ID do orçamento é obrigatório")
        Long orcamentoId,

        @NotBlank(message = "O número da medição é obrigatório")
        String numeroMedicao,

        String observacao,

        @Valid
        @NotEmpty(message = "A medição deve conter pelo menos um item")
        List<ItemMedicaoRequestDTO> itens
) {}
