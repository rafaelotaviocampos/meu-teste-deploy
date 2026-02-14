package br.ce.sop.gestaoorcamento.repository;

import br.ce.sop.gestaoorcamento.model.ItemMedicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemMedicaoRepository extends JpaRepository<ItemMedicao, Long> {
}