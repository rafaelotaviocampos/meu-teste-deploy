package br.ce.sop.gestaoorcamento.repository;

import br.ce.sop.gestaoorcamento.model.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {
    Optional<Orcamento> findByNumeroProtocolo(String numeroProtocolo);
}