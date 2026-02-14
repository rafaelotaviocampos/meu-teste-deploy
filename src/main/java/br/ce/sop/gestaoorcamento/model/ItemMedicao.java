package br.ce.sop.gestaoorcamento.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_medicao", schema = "controle_obras",
        uniqueConstraints = @UniqueConstraint(columnNames = {"medicao_id", "item_id"}))
@SoftDelete(strategy = SoftDeleteType.DELETED)
@Getter
@Setter
@NoArgsConstructor
public class ItemMedicao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // Alterado para EAGER devido à limitação do Hibernate 7 com @SoftDelete
    @JoinColumn(name = "medicao_id", nullable = false)
    private Medicao medicao;

    @ManyToOne(fetch = FetchType.EAGER) // Alterado para EAGER devido à limitação do Hibernate 7 com @SoftDelete
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "quantidade_medida", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidadeMedida;

    @Column(name = "valor_total_medido", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotalMedido;

}