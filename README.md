# Sistema de Controle de Orçamentos e Medições

Este projeto é um backend desenvolvido em **Spring Boot** para gerenciar orçamentos, itens e medições, conforme desafio técnico para vaga.

---

## Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 4.0.x**
- **Spring Data JPA**
- **Hibernate 7**
- **PostgreSQL**
- **Docker & Docker Compose**
- **Lombok**
- **Maven**

---

## Modelo de Dados (ER)

A modelagem foi estruturada para garantir a integridade dos dados financeiros, utilizando precisão de 4 casas decimais para quantidades e restrições de integridade (Foreign Keys e Unique Constraints) para protocolos e números de medição.

![ER Diagram](docs/der_v1.png)

---

## Como Executar a Aplicação

### 1. Requisitos
- Docker e Docker Compose instalados.
- JDK 21 instalado (opcional, se rodar via Docker).

### 2. Subir o Banco de Dados
Na raiz do projeto, execute o comando para iniciar o container do PostgreSQL:

```bash
docker-compose up -d
```

### 3. Executar o Backend
Execute a aplicação utilizando o Maven Wrapper:

```bash
./mvnw spring-boot:run
```

> Para parar os containers do banco:
> ```bash
> docker-compose down
> ```

---

## Endpoints / Funcionalidades Principais (exemplos)

- **Orçamentos**
    - `POST /orcamentos` → Criar novo orçamento
    - `PUT /orcamentos/{id}` → Atualizar orçamento existente
    - `GET /orcamentos` → Listar orçamentos
    - `GET /orcamentos/{id}` → Detalhes de um orçamento

- **Itens do Orçamento**
    - `POST /orcamentos/{id}/itens` → Adicionar item ao orçamento
    - `PUT /orcamentos/{id}/itens/{itemId}` → Atualizar item

- **Medições**
    - `POST /medicoes` → Criar nova medição
    - `PUT /medicoes/{id}/validar` → Validar medição
    - `GET /medicoes` → Listar medições

- **Itens da Medição**
    - `POST /medicoes/{id}/itens` → Adicionar item à medição
    - `PUT /medicoes/{id}/itens/{itemId}` → Atualizar item medido (apenas se medição estiver aberta)

---

## Regras de Negócio Implementadas

- **Orçamentos**
    - Cadastro de protocolos únicos com tipos específicos de obra
    - Controle de status: `Aberto` ou `Finalizado`
    - Valor total = soma dos valores dos itens

- **Itens**
    - Gerenciamento de quantidades com precisão decimal
    - Valor total calculado automaticamente (Quantidade × Valor Unitário)
    - Quantidade acumulada atualizada conforme medições
    - Bloqueio de edição para itens de orçamentos finalizados

- **Medições**
    - Apenas uma medição aberta por orçamento
    - Validação atualiza a quantidade acumulada dos itens
    - Quantidade medida não pode ultrapassar a quantidade total do item
---

## Possíveis Melhorias

- Adição de autenticação JWT
- Documentação via OpenAPI/Swagger
- Testes unitários e de integração
- Relatórios financeiros via JasperReports
- Deploy em nuvem ou containerização completa do backend

---

## Contato

Feito por **Rafael Otavio Campos**  
LinkedIn: https://www.linkedin.com/in/rafaelotaviocampos  
GitHub: https://github.com/rafaelotaviocampos  
Email: only_roc@hotmail.com
