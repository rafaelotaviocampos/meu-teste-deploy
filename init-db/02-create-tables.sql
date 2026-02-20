SET search_path TO controle_obras;

-- ================================
-- Tabela Orçamento
-- ================================
CREATE TABLE "orcamento" (
  "id" BIGSERIAL PRIMARY KEY,
  "numero_protocolo" varchar(50) UNIQUE NOT NULL,
  "tipo" varchar(50) NOT NULL, -- (OBRA_EDIFICACAO, OBRA_RODOVIAS, OUTROS)
  "valor_total" decimal(15,2) NOT NULL DEFAULT 0 CHECK (valor_total >= 0),
  "status" varchar(20) NOT NULL CHECK (status IN ('ABERTO','FINALIZADO')),
  "data_criacao" timestamp NOT NULL DEFAULT now(),
  "data_finalizacao" timestamp,
  "atualizado_em" timestamp,
  "deleted" boolean not null default false
);

-- ================================
-- Tabela Item
-- ================================
CREATE TABLE "item" (
  "id" BIGSERIAL PRIMARY KEY,
  "orcamento_id" bigint NOT NULL,
  "descricao" varchar(255) NOT NULL,
  "quantidade" decimal(15,4) NOT NULL CHECK (quantidade >= 0),
  "valor_unitario" decimal(15,2) NOT NULL CHECK (valor_unitario >= 0),
  "valor_total" decimal(15,2) NOT NULL CHECK (valor_total >= 0), -- (qtd * unitario)
  "quantidade_acumulada" decimal(15,4) NOT NULL DEFAULT 0 CHECK (quantidade_acumulada >= 0),
  "data_criacao" timestamp NOT NULL DEFAULT now(),
  "atualizado_em" timestamp,
  "deleted" boolean not null default false,
  CONSTRAINT fk_item_orcamento FOREIGN KEY ("orcamento_id") REFERENCES "orcamento"("id") ON DELETE RESTRICT
);

-- ================================
-- Tabela Medição
-- ================================
CREATE TABLE "medicao" (
  "id" BIGSERIAL PRIMARY KEY,
  "orcamento_id" bigint NOT NULL,
  "numero_medicao" varchar(50) UNIQUE NOT NULL,
  "data_medicao" timestamp NOT NULL, -- Competência da medição
  "data_validacao" timestamp,
  "status" varchar(20) NOT NULL CHECK (status IN ('ABERTA','VALIDADA')),
  "valor_total_medicao" decimal(15,2) NOT NULL DEFAULT 0 CHECK (valor_total_medicao >= 0), -- Soma dos itens medidos
  "observacao" varchar(500),
  "data_criacao" timestamp NOT NULL DEFAULT now(),
  "atualizado_em" timestamp,
  "deleted" boolean not null default false,
  CONSTRAINT fk_medicao_orcamento FOREIGN KEY ("orcamento_id") REFERENCES "orcamento"("id") ON DELETE RESTRICT
);

-- ================================
-- Tabela Item_Medição
-- ================================
CREATE TABLE "item_medicao" (
  "id" BIGSERIAL PRIMARY KEY,
  "medicao_id" bigint NOT NULL,
  "item_id" bigint NOT NULL,
  "quantidade_medida" decimal(15,4) NOT NULL CHECK (quantidade_medida >= 0),
  "valor_total_medido" decimal(15,2) NOT NULL CHECK (valor_total_medido >= 0), -- (qtd_medida * valor_unitario_item)
  "data_criacao" timestamp NOT NULL DEFAULT now(),
  "atualizado_em" timestamp,
  "deleted" boolean not null default false,
  CONSTRAINT fk_item_medicao_item FOREIGN KEY ("item_id") REFERENCES "item"("id") ON DELETE RESTRICT,
  CONSTRAINT fk_item_medicao_medicao FOREIGN KEY ("medicao_id") REFERENCES "medicao"("id") ON DELETE RESTRICT,
  CONSTRAINT uq_item_medicao UNIQUE ("medicao_id", "item_id") -- evita duplicar o mesmo item na mesma medição
);

-- Remove a constraint antiga
ALTER TABLE controle_obras.item_medicao DROP CONSTRAINT uq_item_medicao;

-- Cria um índice único que ignora registros deletados logicamente
CREATE UNIQUE INDEX uq_item_medicao_ativo
ON controle_obras.item_medicao (medicao_id, item_id)
WHERE (deleted = false);


-- Criação da tabela de usuários
CREATE TABLE IF NOT EXISTS controle_obras.usuario (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT null,
    role VARCHAR(100) not null default 'ROLE_USER'
);

-- Inserção de um usuário inicial (admin / 123456)
-- O hash abaixo é o BCrypt para a senha '123456'
INSERT INTO controle_obras.usuario (login, senha, role)
VALUES ('admin', '$2a$10$Roxuicr.d7Tdw8I1NgVvZeiJSCV6YUMi5M5cukvWGMuAC/GZP.OhK', 'ROLE_ADMIN')
ON CONFLICT (login) DO NOTHING;