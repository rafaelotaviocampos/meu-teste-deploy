package br.ce.sop.gestaoorcamento.model;

import br.ce.sop.gestaoorcamento.exception.RegraDeNegocioException;
import br.ce.sop.gestaoorcamento.model.enums.StatusOrcamento;
import br.ce.sop.gestaoorcamento.model.enums.TipoOrcamento;
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
@Table(name = "orcamento", schema = "controle_obras")
@Getter
@Setter
@NoArgsConstructor
@SoftDelete(strategy = SoftDeleteType.DELETED)
public class Orcamento extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_protocolo", unique = true, nullable = false, length = 50)
    private String numeroProtocolo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOrcamento tipo;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrcamento status = StatusOrcamento.ABERTO;

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> itens = new ArrayList<>();

    // Dentro da classe Orcamento
    @OneToMany(mappedBy = "orcamento", fetch = FetchType.LAZY)
    private List<Medicao> medicoes = new ArrayList<>();

    // Helper method para garantir o vínculo bidirecional
    public void adicionarItem(Item item) {
        this.itens.add(item);
        item.setOrcamento(this);
    }

    public void finalizar() {
        if (this.status == StatusOrcamento.FINALIZADO) {
            throw new RegraDeNegocioException("Este orçamento já se encontra finalizado.");
        }
        this.status = StatusOrcamento.FINALIZADO;
    }
}