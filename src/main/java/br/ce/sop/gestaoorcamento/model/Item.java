package br.ce.sop.gestaoorcamento.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "item", schema = "controle_obras")
@SoftDelete(strategy = SoftDeleteType.DELETED)
@Getter
@Setter
@NoArgsConstructor
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // Alterado para EAGER devido à limitação do Hibernate 7 com @SoftDelete
    @JoinColumn(name = "orcamento_id", nullable = false)
    private Orcamento orcamento;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "quantidade_acumulada", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidadeAcumulada = BigDecimal.ZERO;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true )
    private List<ItemMedicao> itensMedicao = new ArrayList<>();

}