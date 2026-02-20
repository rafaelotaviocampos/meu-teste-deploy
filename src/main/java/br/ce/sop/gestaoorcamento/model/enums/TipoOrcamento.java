package br.ce.sop.gestaoorcamento.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoOrcamento {
    OBRA_EDIFICACAO("OBRA_EDIFICACAO", "Obra de Edificação", "blue"),
    OBRA_RODOVIAS("OBRA_RODOVIAS", "Obra de Rodovias", "green"),
    OUTROS("OUTROS", "Outros", "default");

    private final String id;
    private final String descricao;
    private final String cor;
    //private final String descricao;

//    TipoOrcamento(String descricao) {
//        this.descricao = descricao;
//    }

    // Converter o JSON em Enum
//    @JsonCreator
//    public static TipoOrcamento fromValue(String value) {
//        for (TipoOrcamento tipo : TipoOrcamento.values()) {
//            if (tipo.name().equalsIgnoreCase(value) ||
//                    tipo.descricao.equalsIgnoreCase(value)) {
//                return tipo;
//            }
//        }
//        throw new IllegalArgumentException("Tipo de orçamento inválido: " + value);
//    }

//    @JsonValue
//    public String getDescricao() {
//        return descricao;
//    }
}
