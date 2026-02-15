-- O H2 infelizmente não suporta índices parciais (WHERE)
-- Vamos deixar comentado para não quebrar o teste,
-- mas mantenha no seu arquivo de produção (Flyway/Liquibase)
-- CREATE UNIQUE INDEX uq_item_medicao_ativo ON controle_obras.item_medicao (medicao_id, item_id) WHERE (deleted = false);

-- O H2 não aceita where na criação de index...(Parcial index)
-- Fazendo com que não consiga utilizar o soft delete corretamente;
SELECT 1;