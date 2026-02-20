DO $$
DECLARE
    v_orc_id INTEGER;
    v_item_id INTEGER := 1;
    v_med_id INTEGER := 1;
    v_status TEXT;
    v_tipo TEXT;
    v_data_base TIMESTAMP;
    v_valor_unit DECIMAL;
    v_qtd DECIMAL;
BEGIN
    -- Limpa tudo para evitar conflitos (opcional)
    TRUNCATE TABLE controle_obras.item_medicao RESTART IDENTITY CASCADE;
    TRUNCATE TABLE controle_obras.medicao RESTART IDENTITY CASCADE;
    TRUNCATE TABLE controle_obras.item RESTART IDENTITY CASCADE;
    TRUNCATE TABLE controle_obras.orcamento RESTART IDENTITY CASCADE;

    FOR i IN 3..50 LOOP
        -- Alterna status e tipos
        v_status := CASE WHEN i % 3 = 0 THEN 'ABERTO' ELSE 'FINALIZADO' END;
        v_tipo := CASE WHEN i % 2 = 0 THEN 'OBRA_EDIFICACAO' ELSE 'OBRA_RODOVIAS' END;
        v_data_base := '2025-01-01'::timestamp + (i || ' months')::interval + (i || ' days')::interval;

        -- 1. Inserir Orçamento
        INSERT INTO controle_obras.orcamento (id, numero_protocolo, tipo, status, valor_total, data_criacao, atualizado_em, deleted)
        VALUES (i, 'PROT-2026-' || LPAD(i::text, 3, '0'), v_tipo, v_status, 0, v_data_base, v_data_base, false)
        RETURNING id INTO v_orc_id;

        -- 2. Inserir 6 Itens para cada Orçamento
        -- Item 1: Escavação
        INSERT INTO controle_obras.item (id, orcamento_id, descricao, quantidade, valor_unitario, valor_total, quantidade_acumulada, data_criacao, deleted)
        VALUES (v_item_id, v_orc_id, 'Escavação mecânica de solo', 100, 50.00, 5000.00, 0, v_data_base, false);
        v_item_id := v_item_id + 1;

        -- Item 2: Concreto
        INSERT INTO controle_obras.item (id, orcamento_id, descricao, quantidade, valor_unitario, valor_total, quantidade_acumulada, data_criacao, deleted)
        VALUES (v_item_id, v_orc_id, 'Concreto fck 30mpa', 40, 450.00, 18000.00, 0, v_data_base, false);
        v_item_id := v_item_id + 1;

        -- Item 3: Aço
        INSERT INTO controle_obras.item (id, orcamento_id, descricao, quantidade, valor_unitario, valor_total, quantidade_acumulada, data_criacao, deleted)
        VALUES (v_item_id, v_orc_id, 'Aço CA-50 (médio)', 800, 12.00, 9600.00, 0, v_data_base, false);
        v_item_id := v_item_id + 1;

        -- Item 4: Alvenaria
        INSERT INTO controle_obras.item (id, orcamento_id, descricao, quantidade, valor_unitario, valor_total, quantidade_acumulada, data_criacao, deleted)
        VALUES (v_item_id, v_orc_id, 'Alvenaria de vedação 14cm', 300, 85.00, 25500.00, 0, v_data_base, false);
        v_item_id := v_item_id + 1;

        -- Item 5: Pintura
        INSERT INTO controle_obras.item (id, orcamento_id, descricao, quantidade, valor_unitario, valor_total, quantidade_acumulada, data_criacao, deleted)
        VALUES (v_item_id, v_orc_id, 'Pintura Acrílica interna', 600, 30.00, 18000.00, 0, v_data_base, false);
        v_item_id := v_item_id + 1;

        -- Item 6: Porcelanato
        INSERT INTO controle_obras.item (id, orcamento_id, descricao, quantidade, valor_unitario, valor_total, quantidade_acumulada, data_criacao, deleted)
        VALUES (v_item_id, v_orc_id, 'Piso Porcelanato 80x80', 150, 120.00, 18000.00, 0, v_data_base, false);
        v_item_id := v_item_id + 1;

        -- Atualiza valor total do orçamento
        UPDATE controle_obras.orcamento SET valor_total = 94100.00 WHERE id = v_orc_id;

        -- 3. Inserir Medições apenas para Orçamentos FINALIZADOS (Simular histórico)
       IF v_status = 'FINALIZADO' THEN
            -- Medição 1 (Mês seguinte à criação)
            INSERT INTO controle_obras.medicao (id, orcamento_id, numero_medicao, data_medicao, data_validacao, status, valor_total_medicao, data_criacao, deleted)
            VALUES (v_med_id, v_orc_id, 'MED-01/' || i, v_data_base + '1 month'::interval, v_data_base + '1 month 1 day'::interval, 'VALIDADA', 14600.00, now(), false)
            RETURNING id INTO v_med_id; -- Aqui v_med_id já é o ID correto

            -- Itens da Medição 1 (Usamos v_med_id diretamente, sem subtrair)
            INSERT INTO controle_obras.item_medicao (medicao_id, item_id, quantidade_medida, valor_total_medido, data_criacao, deleted)
            VALUES (v_med_id, v_item_id - 6, 100, 5000.00, now(), false),
                   (v_med_id, v_item_id - 4, 800, 9600.00, now(), false);

            -- Atualiza acumulado nos itens
            UPDATE controle_obras.item SET quantidade_acumulada = 100 WHERE id = v_item_id - 6;
            UPDATE controle_obras.item SET quantidade_acumulada = 800 WHERE id = v_item_id - 4;

            -- Incrementa para a próxima medição do próximo loop
            v_med_id := v_med_id + 1;
        END IF;

    END LOOP;

    -- Ajustar sequences
    PERFORM setval('controle_obras.orcamento_id_seq', (SELECT max(id) FROM controle_obras.orcamento));
    PERFORM setval('controle_obras.item_id_seq', (SELECT max(id) FROM controle_obras.item));
    PERFORM setval('controle_obras.medicao_id_seq', (SELECT max(id) FROM controle_obras.medicao));

END $$;