package br.ce.sop.gestaoorcamento.dto;

import java.math.BigDecimal;

public record ItemMedicaoResponseDTO(

        Long itemMedicaoId, //itemMedicaoId
        Long itemId,  //itemId
        String descricao,                    // Descrição do item
        BigDecimal valorUnitario,           // <--- ADICIONAR: Para calcular o subtotal na tela
        BigDecimal quantidadeItem,          // <--- ADICIONAR: Qtd total do orçamento (referência)
        BigDecimal quantidadeAcumuladaItem,
        BigDecimal valorTotalItem,          // <--- ADICIONAR: O que já foi medido antes desta
        BigDecimal quantidadeMedida,     // Qtd desta medição
        BigDecimal valorTotalItemMedicao // Valor desta medição (qtdMedida * valorUnitario)
) {}