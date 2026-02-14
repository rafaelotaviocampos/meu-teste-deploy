package br.ce.sop.gestaoorcamento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ItemRequestDTO(
        @NotBlank(message = "A descrição do item é obrigatória")
        String descricao,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        BigDecimal quantidade,

        @NotNull(message = "O valor unitário é obrigatório")
        @Positive(message = "O valor unitário deve ser maior que zero")
        BigDecimal valorUnitario
) {}