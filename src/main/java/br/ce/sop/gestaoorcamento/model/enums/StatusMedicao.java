package br.ce.sop.gestaoorcamento.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusMedicao {
    ABERTA("ABERTA", "Aberta", "processing"), // "processing" é um azul animado no Ant Design
    VALIDADA("VALIDADA", "Validada", "success"); // "success" é o verde padrão

    private final String id;
    private final String descricao;
    private final String cor;
}

//@Getter
//@AllArgsConstructor
//public enum StatusMedicao {
//    ABERTA("Aberta"),
//    VALIDADA("Validada");
//
//    private final String descricao;
//}