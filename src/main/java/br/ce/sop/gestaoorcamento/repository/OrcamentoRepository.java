package br.ce.sop.gestaoorcamento.repository;

import br.ce.sop.gestaoorcamento.dto.dashboard.ResumoObraDTO;
import br.ce.sop.gestaoorcamento.dto.dashboard.ResumoOrcamentoDTO;
import br.ce.sop.gestaoorcamento.model.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {
    Optional<Orcamento> findByNumeroProtocolo(String numeroProtocolo);

    @Query("SELECT o FROM Orcamento o WHERE NOT EXISTS " +
            "(SELECT m FROM Medicao m WHERE m.orcamento = o AND m.status = 'ABERTA')")
    List<Orcamento> findOrcamentosSemMedicaoAberta();

     @Query("""
    SELECT new br.ce.sop.gestaoorcamento.dto.dashboard.ResumoObraDTO(
        o.id, 
        o.numeroProtocolo, 
        o.valorTotal, 
        COALESCE(SUM(CASE WHEN m.status = 'VALIDADA' THEN m.valorTotalMedicao ELSE 0 END), 0),
        (o.valorTotal - COALESCE(SUM(CASE WHEN m.status = 'VALIDADA' THEN m.valorTotalMedicao ELSE 0 END), 0)),
        CAST(COALESCE(SUM(CASE WHEN m.status = 'VALIDADA' THEN m.valorTotalMedicao ELSE 0 END), 0) / o.valorTotal * 100 AS double)
    )
    FROM Orcamento o
    LEFT JOIN o.medicoes m
    GROUP BY o.id, o.numeroProtocolo, o.valorTotal
""")
    List<ResumoObraDTO> buscarResumoObrasProjetado();
}