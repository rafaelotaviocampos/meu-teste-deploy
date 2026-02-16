-- Limpeza (A ordem importa devido às FKs)
DELETE FROM controle_obras.item_medicao;
DELETE FROM controle_obras.medicao;
DELETE FROM controle_obras.item;
DELETE FROM controle_obras.orcamento;

-- 1. Orçamento Aberto (ID: 100)
INSERT INTO controle_obras.orcamento (id, numero_protocolo, status, tipo, valor_total, data_criacao, deleted)
VALUES (100, 'ORC-TESTE-01', 'ABERTO', 'OBRA_EDIFICACAO', 1000.00, NOW(), false);

-- 2. Item com medição parcial (ID: 10)
-- Quantidade: 10, Acumulado: 7 (Saldo disponível: 3)
INSERT INTO controle_obras.item (id, orcamento_id, descricao, quantidade, quantidade_acumulada, valor_unitario, valor_total, data_criacao, deleted)
VALUES (10, 100, 'Piso Cerâmico', 10.00, 7.00, 100.00, 1000.00, NOW(), false);

-- 3. Orçamento Finalizado (ID: 200) - Para testar bloqueio de edição
INSERT INTO controle_obras.orcamento (id, numero_protocolo, status, tipo, valor_total, data_criacao, deleted)
VALUES (200, 'ORC-FINALIZADO', 'FINALIZADO', 'OBRA_EDIFICACAO', 500.00, NOW(), false);