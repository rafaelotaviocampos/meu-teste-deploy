package br.ce.sop.gestaoorcamento.dto;

import br.ce.sop.gestaoorcamento.model.enums.TipoOrcamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrcamentoRequestDTO(
        @NotBlank(message = "O número do protocolo é obrigatório")
        @Size(max = 50, message = "O protocolo não pode exceder 50 caracteres")
        String numeroProtocolo,

        @NotNull(message = "O tipo do orçamento é obrigatório")
        TipoOrcamento tipo,

        @Valid // Importante para validar os itens dentro da lista
        @NotNull(message = "A lista de itens não pode ser nula")
        List<ItemRequestDTO> itens
) {}