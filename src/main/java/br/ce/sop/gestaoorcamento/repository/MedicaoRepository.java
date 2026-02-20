package br.ce.sop.gestaoorcamento.repository;

import br.ce.sop.gestaoorcamento.dto.dashboard.EvolucaoFinanceiraDTO;
import br.ce.sop.gestaoorcamento.model.Medicao;
import br.ce.sop.gestaoorcamento.model.enums.StatusMedicao;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicaoRepository extends JpaRepository<Medicao, Long> {
    Optional<Medicao> findByNumeroMedicao(String numeroMedicao);

    List<Medicao> findByOrcamentoId(Long orcamentoId);

    boolean existsByOrcamentoIdAndStatus(@NotNull(message = "O ID do orçamento é obrigatório") Long aLong, StatusMedicao statusMedicao);

    @Query("""
    SELECT new br.ce.sop.gestaoorcamento.dto.dashboard.EvolucaoFinanceiraDTO(
        CAST(function('to_char', m.dataValidacao, 'YYYY-MM') AS string),
        SUM(m.orcamento.valorTotal), 
        SUM(m.valorTotalMedicao),
        CAST(SUM(m.valorTotalMedicao) * 100.0 / NULLIF(SUM(m.orcamento.valorTotal), 0) AS double)
    )
    FROM Medicao m
    WHERE m.status = br.ce.sop.gestaoorcamento.model.enums.StatusMedicao.VALIDADA
    GROUP BY function('to_char', m.dataValidacao, 'YYYY-MM')
    ORDER BY 1 ASC
""")
    List<EvolucaoFinanceiraDTO> buscarEvolucaoMensal();
}