package br.ce.sop.gestaoorcamento.service;

import br.ce.sop.gestaoorcamento.dto.ItemResponseDTO;
import br.ce.sop.gestaoorcamento.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository repository;

//    public List<ItemResponseDTO> listarPorOrcamento(Long orcamentoId) {
//        return repository.findByOrcamentoId(orcamentoId)
//                .stream()
//                .map(item -> new ItemResponseDTO(
//                        item,
//                        repository.existsByMedicaoVinculada(item.getId()) // Passa a flag aqui
//                ))
//                .toList();
//    }
public List<ItemResponseDTO> listarPorOrcamento(Long orcamentoId) {
    var itens = repository.findByOrcamentoId(orcamentoId);

    // Busca de uma vez só todos os IDs de itens desse orçamento que já possuem medição
    Set<Long> idsComMedicao = repository.findIdsItensComMedicaoPorOrcamento(orcamentoId);

    return itens.stream()
            .map(item -> new ItemResponseDTO(
                    item,
                    idsComMedicao.contains(item.getId()) // Checagem em memória (muito rápido!)
            ))
            .toList();
}

    public List<ItemResponseDTO> listarItensComSaldo(Long orcamentoId) {
        return repository.findItensComSaldoPorOrcamento(orcamentoId)
                .stream()
                .map(item -> new ItemResponseDTO(
                        item,
                        repository.existsByMedicaoVinculada(item.getId()) // Mantém o padrão
                ))
                .toList();
    }
}