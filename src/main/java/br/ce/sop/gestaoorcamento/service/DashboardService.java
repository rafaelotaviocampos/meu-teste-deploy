package br.ce.sop.gestaoorcamento.service;

import br.ce.sop.gestaoorcamento.dto.dashboard.EvolucaoFinanceiraDTO;
import br.ce.sop.gestaoorcamento.dto.dashboard.ResumoObraDTO;
import br.ce.sop.gestaoorcamento.model.enums.StatusMedicao;
import br.ce.sop.gestaoorcamento.repository.MedicaoRepository;
import br.ce.sop.gestaoorcamento.repository.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import br.ce.sop.gestaoorcamento.model.Medicao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MedicaoRepository medicaoRepository;
    private final OrcamentoRepository orcamentoRepository;

    public List<EvolucaoFinanceiraDTO> getEvolucaoFinanceira() {
        return medicaoRepository.buscarEvolucaoMensal();
    }

    public List<ResumoObraDTO> getResumoObras() {
        return orcamentoRepository.buscarResumoObrasProjetado();
    }
}
