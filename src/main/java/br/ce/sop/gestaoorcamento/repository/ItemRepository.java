package br.ce.sop.gestaoorcamento.repository;

import br.ce.sop.gestaoorcamento.model.Item;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOrcamentoId(Long orcamentoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id = :id")
    Optional<Item> findByIdWithLock(Long id);

    @Query("SELECT i FROM Item i WHERE i.orcamento.id = :orcamentoId AND i.quantidadeAcumulada < i.quantidade")
    List<Item> findItensComSaldoPorOrcamento(Long orcamentoId);

    @Query("SELECT COUNT(im) > 0 FROM ItemMedicao im WHERE im.item.id = :itemId")
    boolean existsByMedicaoVinculada(Long itemId);

    @Query("SELECT im.item.id FROM ItemMedicao im WHERE im.item.orcamento.id = :orcamentoId")
    Set<Long> findIdsItensComMedicaoPorOrcamento(Long orcamentoId);
}