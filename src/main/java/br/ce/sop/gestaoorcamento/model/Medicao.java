package br.ce.sop.gestaoorcamento.model;

import br.ce.sop.gestaoorcamento.model.enums.StatusMedicao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medicao", schema = "controle_obras")
@SoftDelete(strategy = SoftDeleteType.DELETED)
@Getter
@Setter
@NoArgsConstructor
public class Medicao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // Alterado para EAGER devido à limitação do Hibernate 7 com @SoftDelete
    @JoinColumn(name = "orcamento_id", nullable = false)
    private Orcamento orcamento;

    @Column(name = "numero_medicao", unique = true, nullable = false, length = 50)
    private String numeroMedicao;

    @Column(name = "data_medicao", nullable = false)
    private LocalDateTime dataMedicao;

    @Column(name = "data_validacao")
    private LocalDateTime dataValidacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMedicao status = StatusMedicao.ABERTA;

    @Column(name = "valor_total_medicao", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotalMedicao = BigDecimal.ZERO;

    @Column(length = 500)
    private String observacao;

    @OneToMany(mappedBy = "medicao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemMedicao> itensMedicao = new ArrayList<>();

    public void adicionarItem(ItemMedicao item) {
        this.itensMedicao.add(item);
        item.setMedicao(this);
    }

    public boolean isEditavel() {
        return this.status == StatusMedicao.ABERTA;
    }

    public void validar() {
        if (!isEditavel()) {
            throw new RuntimeException("Operação não permitida: A medição não está mais em estado ABERTO.");
        }
        this.status = StatusMedicao.VALIDADA;
        this.dataValidacao = LocalDateTime.now();
    }


}