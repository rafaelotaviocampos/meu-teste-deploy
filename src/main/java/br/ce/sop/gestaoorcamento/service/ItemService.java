package br.ce.sop.gestaoorcamento.service;

import br.ce.sop.gestaoorcamento.dto.ItemResponseDTO;
import br.ce.sop.gestaoorcamento.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository repository;

    public List<ItemResponseDTO> listarPorOrcamento(Long orcamentoId) {
        return repository.findByOrcamentoId(orcamentoId)
                .stream()
                .map(ItemResponseDTO::new)
                .toList();
    }
}