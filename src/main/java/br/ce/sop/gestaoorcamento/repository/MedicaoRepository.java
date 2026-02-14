package br.ce.sop.gestaoorcamento.repository;

import br.ce.sop.gestaoorcamento.model.Medicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MedicaoRepository extends JpaRepository<Medicao, Long> {
    Optional<Medicao> findByNumeroMedicao(String numeroMedicao);
}