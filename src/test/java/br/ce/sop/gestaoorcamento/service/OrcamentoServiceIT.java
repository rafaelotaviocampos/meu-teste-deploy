package br.ce.sop.gestaoorcamento.service;

import br.ce.sop.gestaoorcamento.dto.ItemRequestDTO;
import br.ce.sop.gestaoorcamento.dto.OrcamentoRequestDTO;
import br.ce.sop.gestaoorcamento.exception.RegraDeNegocioException;
import br.ce.sop.gestaoorcamento.model.enums.TipoOrcamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/setup-orcamento.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class OrcamentoServiceIT {

    @Autowired
    private OrcamentoService orcamentoService;

    @Test
    @DisplayName("Não deve permitir reduzir quantidade de item para menos do que já foi medido")
    void deveBarrarReducaoDeQuantidadeAbaixoDoAcumulado() {
        // Cenário: Item 10 tem 7.00 medidos. Tentaremos mudar para 5.00
        Long orcamentoId = 100L;
        var itemDto = new ItemRequestDTO(10L, "Piso Cerâmico", new BigDecimal("5.00"), new BigDecimal("100.00"), false);
        var request = new OrcamentoRequestDTO("ORC-TESTE-01", TipoOrcamento.OBRA_EDIFICACAO, List.of(itemDto));

        assertThatThrownBy(() -> orcamentoService.atualizar(orcamentoId, request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Já foram medidos 7.00");
    }

    @Test
    @DisplayName("Não deve permitir excluir item que já possui medições")
    void deveBarrarExclusaoDeItemComMedicao() {
        // Cenário: Tentar excluir (excluir=true) o item 10 que tem acumulado 7.00
        Long orcamentoId = 100L;
        var itemDto = new ItemRequestDTO(10L, "Piso Cerâmico", new BigDecimal("10.00"), new BigDecimal("100.00"), true);
        var request = new OrcamentoRequestDTO("ORC-TESTE-01", TipoOrcamento.OBRA_EDIFICACAO, List.of(itemDto));

        assertThatThrownBy(() -> orcamentoService.atualizar(orcamentoId, request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("pois ele já possui medições");
    }

    @Test
    @DisplayName("Não deve permitir editar orçamento com status FINALIZADO")
    void deveBarrarEdicaoDeOrcamentoFinalizado() {
        Long orcamentoId = 200L;
        var request = new OrcamentoRequestDTO("PROTO-QUALQUER", TipoOrcamento.OBRA_RODOVIAS, List.of());

        assertThatThrownBy(() -> orcamentoService.atualizar(orcamentoId, request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Não é permitido editar orçamento FINALIZADO");
    }
}
