package br.ce.sop.gestaoorcamento.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusOrcamento {
    ABERTO("ABERTO", "Aberto", "blue"),
    FINALIZADO("FINALIZADO", "Finalizado", "green");

    private final String id;
    private final String descricao;
    private final String cor;
//    ABERTO("Aberto"),
//    FINALIZADO("Finalizado");
//
//    private final String descricao;
}