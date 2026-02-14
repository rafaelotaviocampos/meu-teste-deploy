package br.ce.sop.gestaoorcamento.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum TipoOrcamento {
    OBRA_EDIFICACAO("Obra de Edificação"),
    OBRA_RODOVIAS("Obra de Rodovias"),
    OUTROS("Outros");

    private final String descricao;

    TipoOrcamento(String descricao) {
        this.descricao = descricao;
    }
}
