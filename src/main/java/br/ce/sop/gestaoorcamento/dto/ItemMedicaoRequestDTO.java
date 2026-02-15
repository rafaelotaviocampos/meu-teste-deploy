package br.ce.sop.gestaoorcamento.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemMedicaoRequestDTO(
        Long id,

        @NotNull(message = "O ID do item do orçamento é obrigatório")
        Long itemOrcamentoId,

        @NotNull(message = "A quantidade medida é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        BigDecimal quantidadeMedida,

        Boolean excluir
) {
        public ItemMedicaoRequestDTO {
                if (excluir == null) {
                        excluir = false;
                }
        }
}
