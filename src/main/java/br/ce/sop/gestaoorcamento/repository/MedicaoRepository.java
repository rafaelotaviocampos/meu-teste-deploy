package br.ce.sop.gestaoorcamento.repository;

import br.ce.sop.gestaoorcamento.model.Medicao;
import br.ce.sop.gestaoorcamento.model.enums.StatusMedicao;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MedicaoRepository extends JpaRepository<Medicao, Long> {
    Optional<Medicao> findByNumeroMedicao(String numeroMedicao);

    boolean existsByOrcamentoIdAndStatus(@NotNull(message = "O ID do orçamento é obrigatório") Long aLong, StatusMedicao statusMedicao);
}