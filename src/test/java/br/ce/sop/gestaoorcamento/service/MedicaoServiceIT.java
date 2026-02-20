package br.ce.sop.gestaoorcamento.service;

import br.ce.sop.gestaoorcamento.dto.ItemMedicaoRequestDTO;
import br.ce.sop.gestaoorcamento.dto.MedicaoRequestDTO;
import br.ce.sop.gestaoorcamento.exception.RegraDeNegocioException;
import br.ce.sop.gestaoorcamento.model.Item;
import br.ce.sop.gestaoorcamento.model.Orcamento;
import br.ce.sop.gestaoorcamento.model.enums.StatusOrcamento;
import br.ce.sop.gestaoorcamento.model.enums.TipoOrcamento;
import br.ce.sop.gestaoorcamento.repository.ItemRepository;
import br.ce.sop.gestaoorcamento.repository.MedicaoRepository;
import br.ce.sop.gestaoorcamento.repository.OrcamentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Sql(
        scripts = "/sql/setup-medicao.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
@DisplayName("Testes de Integração - Fluxo de Medição e Saldo")
class MedicaoServiceIT {

    @Autowired private MedicaoService medicaoService;
    @Autowired private OrcamentoRepository orcamentoRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private MedicaoRepository medicaoRepository;

    private Orcamento prepararCenarioOrcamento(String protocolo, BigDecimal qtdTotal, BigDecimal qtdAcumulada) {
        var item = new Item();
        item.setDescricao("Item de Teste");
        item.setQuantidade(qtdTotal);
        item.setQuantidadeAcumulada(qtdAcumulada);
        item.setValorUnitario(new BigDecimal("100.00"));
        item.setValorTotal(qtdTotal.multiply(item.getValorUnitario()));

        var orcamento = new Orcamento();
        orcamento.setNumeroProtocolo(protocolo);
        orcamento.setTipo(TipoOrcamento.OBRA_EDIFICACAO);
        orcamento.setStatus(StatusOrcamento.ABERTO);
        orcamento.setValorTotal(item.getValorTotal());

        orcamento.setItens(new ArrayList<>(List.of(item)));
        item.setOrcamento(orcamento);
        return orcamentoRepository.save(orcamento);
    }

    @Nested
    @DisplayName("Cenários de Sucesso")
    class Sucesso {

        @Test
        @DisplayName("Deve validar uma medição e atualizar o acumulado do item")
        void deveValidarMedicaoEAtualizarAcumulado() {
            var orcamento = prepararCenarioOrcamento("PROTO-001", new BigDecimal("10.00"), BigDecimal.ZERO);
            var idItem = orcamento.getItens().getFirst().getId();

            var itemDto = new ItemMedicaoRequestDTO(null, idItem, new BigDecimal("2.50"), false);
            var medicaoDto = new MedicaoRequestDTO(orcamento.getId(), "MED-001", "NF 1", List.of(itemDto), false);

            var medicaoCriada = medicaoService.criar(medicaoDto);
            medicaoService.validar(medicaoCriada.id());

            var itemNoBanco = itemRepository.findById(idItem).orElseThrow();
            assertThat(itemNoBanco.getQuantidadeAcumulada()).isEqualByComparingTo(new BigDecimal("2.50"));
        }

        @Test
        @DisplayName("Deve permitir medição que consome o saldo exato (Fronteira)")
        void devePermitirMedicaoNoLimiteDoSaldo() {
            // Saldo de 1.00 (10 total - 9 acumulado)
            var orcamento = prepararCenarioOrcamento("PROTO-LIMITE", new BigDecimal("10.00"), new BigDecimal("9.00"));
            var idItem = orcamento.getItens().getFirst().getId();

            var itemDto = new ItemMedicaoRequestDTO(null, idItem, new BigDecimal("1.00"), false);
            var medicaoDto = new MedicaoRequestDTO(orcamento.getId(), "MED-LIMITE", "Limite", List.of(itemDto),false);

            var medicaoCriada = medicaoService.criar(medicaoDto);
            assertThat(medicaoCriada.id()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Cenários de Regra de Negócio (Erros)")
    class NegocioErros {

        @Test
        @DisplayName("Não deve permitir quantidade que ultrapassa o saldo disponível")
        void deveBarrarExcessoDeSaldo() {
            var orcamento = prepararCenarioOrcamento("PROTO-ERRO", new BigDecimal("5.00"), new BigDecimal("4.00"));
            var idItem = orcamento.getItens().getFirst().getId();

            var itemDto = new ItemMedicaoRequestDTO(null, idItem, new BigDecimal("1.01"), false);
            var medicaoDto = new MedicaoRequestDTO(orcamento.getId(), "MED-ERRO", "Excesso", List.of(itemDto),false);

            assertThatThrownBy(() -> medicaoService.criar(medicaoDto))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Quantidade excede saldo disponível");
        }

        @Test
        @DisplayName("Não deve permitir medição com quantidade negativa")
        void deveBarrarQuantidadeNegativa() {
            var orcamento = prepararCenarioOrcamento("PROTO-NEG", new BigDecimal("10.00"), BigDecimal.ZERO);
            var idItem = orcamento.getItens().getFirst().getId();

            var itemDto = new ItemMedicaoRequestDTO(null, idItem, new BigDecimal("-1.00"), false);
            var medicaoDto = new MedicaoRequestDTO(orcamento.getId(), "MED-NEG", "Negativo", List.of(itemDto),false);

            assertThatThrownBy(() -> medicaoService.criar(medicaoDto))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("deve ser maior que zero");
        }
    }

    @Nested
    @DisplayName("Cenários de Ciclo de Vida (Soft Delete)")
    class CicloDeVida {

        @Test
        @DisplayName("Deve estornar o acumulado do item ao deletar uma medição validada")
        void deveEstornarAcumuladoAoDeletarMedicao() {
            // 1. Cria e valida medição de 3.00
            var orcamento = prepararCenarioOrcamento("PROTO-DEL", new BigDecimal("10.00"), BigDecimal.ZERO);
            var idItem = orcamento.getItens().getFirst().getId();
            var itemDto = new ItemMedicaoRequestDTO(null, idItem, new BigDecimal("3.00"), false);
            var medicaoDto = new MedicaoRequestDTO(orcamento.getId(), "MED-DEL", "Deletar", List.of(itemDto),false);

            var medicao = medicaoService.criar(medicaoDto);
            medicaoService.validar(medicao.id());

            // 2. Verifica se acumulado subiu para 3.00
            assertThat(itemRepository.findById(idItem).get().getQuantidadeAcumulada())
                    .isEqualByComparingTo(new BigDecimal("3.00"));

            // 3. Deleta a medição (Soft Delete)
            medicaoService.deletar(medicao.id());

            // 4. Assert: O acumulado deve ter voltado para 0.00
            var itemAposDeletar = itemRepository.findById(idItem).orElseThrow();
            assertThat(itemAposDeletar.getQuantidadeAcumulada())
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Test
    @DisplayName("Não deve permitir atualizar uma medição que já foi VALIDADA")
    void deveBarrarAtualizacaoDeMedicaoValidada() {
        // Arrange
        var orcamento = prepararCenarioOrcamento("PROTO-BLOQUEIO", new BigDecimal("10.00"), BigDecimal.ZERO);
        var idItem = orcamento.getItens().getFirst().getId();
        var medicao = medicaoService.criar(new MedicaoRequestDTO(orcamento.getId(), "MED-FIXA", "NF",
                List.of(new ItemMedicaoRequestDTO(null, idItem, BigDecimal.ONE, false)), false));

        medicaoService.validar(medicao.id());

        // Act & Assert
        var dtoAtualizacao = new MedicaoRequestDTO(orcamento.getId(), "MED-ALTERADA", "Tentando burlar",
                List.of(new ItemMedicaoRequestDTO(null, idItem, new BigDecimal("2.00"), false)), false);

        assertThatThrownBy(() -> medicaoService.atualizar(medicao.id(), dtoAtualizacao))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Apenas medições ABERTAS podem ser alteradas");
    }
}