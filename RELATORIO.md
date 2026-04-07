# Desafio de Desenvolvimento — Relatório Técnico

> Sistema de escalação de times para esportes tradicionais e eSports,
> desenvolvido como solução ao desafio técnico proposto.

---

## Sumário

1. [Visão Geral](#1-visão-geral)
2. [Stack Tecnológica](#2-stack-tecnológica)
3. [Arquitetura da Aplicação](#3-arquitetura-da-aplicação)
4. [Estrutura de Dados](#4-estrutura-de-dados)
5. [Funcionalidades Implementadas](#5-funcionalidades-implementadas)
6. [API REST — Referência Completa](#6-api-rest--referência-completa)
7. [Regras de Negócio](#7-regras-de-negócio)
8. [Tratamento de Erros](#8-tratamento-de-erros)
9. [Estratégia de Testes](#9-estratégia-de-testes)
10. [Observabilidade e Logging](#10-observabilidade-e-logging)
11. [Performance](#11-performance)
12. [Perfis de Ambiente](#12-perfis-de-ambiente)
13. [Documentação Interativa — Swagger](#13-documentação-interativa--swagger)
14. [Telas — Frontend](#14-telas--frontend)
15. [Como Executar e Testar](#15-como-executar-e-testar)
16. [Estrutura de Arquivos](#16-estrutura-de-arquivos)
17. [Autor](#17-autor)

---

## 1. Visão Geral

A aplicação resolve o problema de escalação semanal de times, independente do esporte — desde futebol e basquete até Counter-Strike, Valorant e League of Legends. Ela oferece:

- **Cadastro** de integrantes e montagem de times por data
- **Processamento analítico** dos dados históricos inteiramente em Java (sem funções de agregação SQL)
- **Frontend** responsivo com três telas funcionais
- **API REST** documentada e paginada

---

## 2. Stack Tecnológica

| Camada | Tecnologia | Versão |
|--------|-----------|--------|
| Linguagem | Java | 8 |
| Framework principal | Spring Boot | 2.5.3 |
| Persistência | Spring Data JPA / Hibernate | — |
| Banco de dados | H2 (in-memory) | — |
| Validação | Bean Validation (javax) | — |
| Templates / Views | Thymeleaf | — |
| Redução de boilerplate | Lombok | — |
| Documentação da API | springdoc-openapi-ui | 1.6.15 |
| Logging | SLF4J + Logback (via Spring Boot) | — |
| Testes unitários | JUnit 5 (Jupiter) | — |
| Testes de camada web | MockMvc + Mockito | — |
| Testes de integração | TestRestTemplate + Spring Boot Test | — |
| Frontend | Bootstrap 5 + Fetch API | — |
| Build | Maven | — |

---

## 3. Arquitetura da Aplicação

A aplicação segue rigorosamente o padrão em **3 camadas**, garantindo separação de responsabilidades:

```
┌──────────────────────────────────────────────┐
│               PRESENTATION LAYER             │
│   Controllers REST  │  Controllers MVC       │
│  (IntegranteCtrl)   │  (ViewController)      │
│  (TimeController)   │                        │
│  (ApiController)    │                        │
└───────────────────────┬──────────────────────┘
                        │ delega para
┌───────────────────────▼──────────────────────┐
│               SERVICE LAYER                  │
│  IntegranteService  │  TimeService           │
│  ApiService         │                        │
│  (@Transactional, @Slf4j, regras de negócio) │
└───────────────────────┬──────────────────────┘
                        │ acessa
┌───────────────────────▼──────────────────────┐
│             REPOSITORY LAYER                 │
│  IntegranteRepository  │  TimeRepository     │
│  ComposicaoTimeRepository                    │
│  (Spring Data JPA, @EntityGraph)             │
└──────────────────────────────────────────────┘
```

**Princípios aplicados:**
- Controllers **nunca** acessam repositórios diretamente
- Toda lógica de negócio reside na camada de serviço
- DTOs isolam as entidades JPA da representação JSON exposta
- Exceções semânticas substituem retornos condicionais nos controllers

---

## 4. Estrutura de Dados

### Entidade `Integrante`

| Campo | Tipo | Restrição |
|-------|------|-----------|
| `id` | Long | PK, auto-gerado |
| `nome` | String | NOT NULL, not blank |
| `funcao` | String | NOT NULL, not blank |

### Entidade `Time`

| Campo | Tipo | Restrição |
|-------|------|-----------|
| `id` | Long | PK, auto-gerado |
| `nomeClube` | String | NOT NULL, not blank |
| `data` | LocalDate | NOT NULL |

### Entidade `ComposicaoTime`

| Campo | Tipo | Restrição |
|-------|------|-----------|
| `id` | Long | PK, auto-gerado |
| `id_time` | Long | FK → Time |
| `id_integrante` | Long | FK → Integrante |

### Diagrama de Relacionamento

```
Integrante  1 ──────── N  ComposicaoTime  N ──────── 1  Time
   (id, nome, funcao)        (id_time,             (id, nomeClube, data)
                              id_integrante)
```

---

## 5. Funcionalidades Implementadas

### Passo 1 — Processamento em Java (ApiService)

Todos os 7 métodos exigidos foram implementados **exclusivamente com Java Streams**, sem qualquer função de agregação SQL (`COUNT`, `GROUP BY`, stored procedures):

| Método | Descrição |
|--------|-----------|
| `timeDaData(data, times)` | Retorna o time cuja data coincide exatamente |
| `integranteMaisUsado(ini, fim, times)` | Integrante com mais aparições no período |
| `integrantesDoTimeMaisRecorrente(ini, fim, times)` | Nomes dos integrantes do clube mais frequente |
| `funcaoMaisRecorrente(ini, fim, times)` | Função com mais aparições no período |
| `clubeMaisRecorrente(ini, fim, times)` | Nome do clube com mais entradas no período |
| `contagemDeClubesNoPeriodo(ini, fim, times)` | Mapa `{ clube: qtd }` |
| `contagemPorFuncao(ini, fim, times)` | Mapa `{ funcao: qtd }` |

**Destaques de implementação:**
- Método auxiliar `filtrarPorPeriodo` centraliza o filtro de datas com suporte a `null` em ambos os extremos
- Validação `validarPeriodo` rejeita intervalos incoerentes antes de qualquer processamento
- `integrantesDoTimeMaisRecorrente` — em caso de empate por número de aparições, prevalece o clube com registro mais **recente**
- Javadoc completo em todos os métodos públicos e privados

### Passo 2 — API de Cadastro (CRUD)

- **Integrantes:** `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`
- **Times:** `GET`, `GET /{id}`, `POST` (com montagem de composição), `DELETE /{id}`
- Paginação nos endpoints de listagem via `Pageable` (`?page=0&size=20&sort=nome,asc`)
- Todas as respostas `POST` retornam `201 Created` com header `Location` apontando para o recurso criado

### Passo 3 — API de Processamento

7 endpoints analíticos expostos em `/api/*`, consumindo os dados via `TimeService` e processando-os via `ApiService`.

### Passo 4 — Telas

3 telas funcionais em Thymeleaf + Bootstrap 5, comunicando-se com a API via `Fetch API`.

---

## 6. API REST — Referência Completa

### Integrantes — `/api/integrantes`

| Método | Endpoint | Descrição | Status de sucesso |
|--------|----------|-----------|-------------------|
| `GET` | `/api/integrantes?page=0&size=20&sort=nome` | Lista paginada de integrantes | `200 OK` |
| `GET` | `/api/integrantes/{id}` | Busca por id | `200 OK` |
| `POST` | `/api/integrantes` | Cria novo integrante | `201 Created` + `Location` |
| `PUT` | `/api/integrantes/{id}` | Atualiza integrante | `200 OK` |
| `DELETE` | `/api/integrantes/{id}` | Remove integrante | `204 No Content` |

**Corpo da requisição `POST` / `PUT`:**
```json
{ "nome": "Hernanes", "funcao": "Volante" }
```

**Resposta:**
```json
{ "id": 1, "nome": "Hernanes", "funcao": "Volante" }
```

---

### Times — `/api/times`

| Método | Endpoint | Descrição | Status de sucesso |
|--------|----------|-----------|-------------------|
| `GET` | `/api/times?page=0&size=20&sort=data` | Lista paginada de times | `200 OK` |
| `GET` | `/api/times/{id}` | Busca por id | `200 OK` |
| `POST` | `/api/times` | Cria time com composição | `201 Created` + `Location` |
| `DELETE` | `/api/times/{id}` | Remove time | `204 No Content` |

**Corpo da requisição `POST`:**
```json
{
  "nomeClube": "São Paulo",
  "data": "2024-01-07",
  "integranteIds": [1, 2, 3]
}
```

**Resposta:**
```json
{
  "id": 1,
  "nomeClube": "São Paulo",
  "data": "2024-01-07",
  "integrantes": ["Hernanes", "Kaká", "Rogério Ceni"]
}
```

---

### Processamento Analítico — `/api`

| Método | Endpoint | Parâmetros | Exemplo de resposta |
|--------|----------|-----------|---------------------|
| `GET` | `/api/time-da-data` | `data` (obrigatório) | `{ "data": "2024-01-07", "clube": "São Paulo", "integrantes": ["Hernanes"] }` |
| `GET` | `/api/integrante-mais-usado` | `dataInicial`, `dataFinal` (opcionais) | `{ "id": 1, "nome": "Hernanes", "funcao": "Volante" }` |
| `GET` | `/api/integrantes-do-time-mais-recorrente` | `dataInicial`, `dataFinal` (opcionais) | `["Hernanes", "Kaká"]` |
| `GET` | `/api/funcao-mais-recorrente` | `dataInicial`, `dataFinal` (opcionais) | `{ "Função": "Volante" }` |
| `GET` | `/api/clube-mais-recorrente` | `dataInicial`, `dataFinal` (opcionais) | `{ "Clube": "São Paulo" }` |
| `GET` | `/api/contagem-de-clubes` | `dataInicial`, `dataFinal` (opcionais) | `{ "São Paulo": 3, "Palmeiras": 1 }` |
| `GET` | `/api/contagem-por-funcao` | `dataInicial`, `dataFinal` (opcionais) | `{ "Volante": 4, "Meia": 3 }` |

> **Formato de datas:** `yyyy-MM-dd` (ex: `2024-01-07`)

---

## 7. Regras de Negócio

Além dos requisitos explícitos do desafio, foram identificadas e implementadas regras de negócio implícitas:

### RN-01 — Clube único por data
> Um mesmo clube **não pode** ter dois times cadastrados na mesma data.
> Retorna `422 Unprocessable Entity` com código `BUSINESS_RULE_VIOLATION`.

### RN-02 — Sem integrantes duplicados no time
> A lista de `integranteIds` enviada na criação de um time **não pode** conter o mesmo id mais de uma vez.
> Retorna `422 Unprocessable Entity` com código `BUSINESS_RULE_VIOLATION`.

### RN-03 — Período de datas válido
> Quando `dataInicial` e `dataFinal` são ambas fornecidas, `dataInicial` **não pode** ser posterior a `dataFinal`.
> Retorna `400 Bad Request` com código `INVALID_DATE_RANGE`.

### RN-04 — Integrantes existentes na criação de time
> Todos os ids informados em `integranteIds` devem corresponder a integrantes existentes no banco.
> Retorna `404 Not Found` com código `NOT_FOUND`.

### RN-05 — Período opcional nos endpoints analíticos
> Quando `dataInicial` e/ou `dataFinal` são `null`, o processamento considera **todo o histórico** disponível.

---

## 8. Tratamento de Erros

Todos os erros da API retornam um `ErrorResponse` padronizado via `@RestControllerAdvice`:

```json
{
  "codigo": "NOT_FOUND",
  "mensagem": "Integrante não encontrado com id: 99",
  "timestamp": "2024-01-07T10:30:00"
}
```

### Mapeamento de exceções

| Exceção | HTTP Status | Código |
|---------|-------------|--------|
| `EntityNotFoundException` | `404 Not Found` | `NOT_FOUND` |
| `BusinessRuleException` | `422 Unprocessable Entity` | `BUSINESS_RULE_VIOLATION` |
| `InvalidDateRangeException` | `400 Bad Request` | `INVALID_DATE_RANGE` |
| `MethodArgumentNotValidException` | `400 Bad Request` | `VALIDATION_ERROR` |
| `Exception` (genérica) | `500 Internal Server Error` | `INTERNAL_ERROR` |

> O handler genérico loga o stack trace completo para facilitar o diagnóstico sem expor detalhes internos ao cliente.

---

## 9. Estratégia de Testes

A cobertura é organizada em **3 níveis**, totalizando **mais de 40 casos de teste**:

### Nível 1 — Testes Unitários (`ApiServiceTest`)

- **Objetivo:** validar exclusivamente a lógica de processamento em Java
- **Tecnologia:** JUnit 5 puro (sem Spring context, sem banco)
- **Cobertura:** todos os 7 métodos do `ApiService`
- **Cenário:** 5 jogadores históricos do São Paulo FC, 5 escalações em datas distintas
- **Casos incluídos:** com/sem filtro de data, lista vazia, parâmetros nulos, empates

| Método testado | Nº de testes |
|----------------|:-----------:|
| `timeDaData` | 4 |
| `integranteMaisUsado` | 3 |
| `integrantesDoTimeMaisRecorrente` | 3 |
| `funcaoMaisRecorrente` | 3 |
| `clubeMaisRecorrente` | 3 |
| `contagemDeClubesNoPeriodo` | 3 |
| `contagemPorFuncao` | 3 |

### Nível 2 — Testes de Camada Web (`@WebMvcTest`)

- **Objetivo:** validar contratos HTTP dos controllers sem acessar banco
- **Tecnologia:** MockMvc + Mockito (`@MockBean`)
- **Cobertura:** `ApiController`, `IntegranteController`, `TimeController`

Destaques validados:
- Status codes corretos: `200`, `201`, `204`, `400`, `404`, `422`
- Header `Location` presente nos `POST`
- Estrutura JSON de `Page<T>` com `$.content`
- Campo `$.codigo` em todas as respostas de erro
- Regras de negócio propagadas do service → handler → resposta

### Nível 3 — Teste de Integração End-to-End (`IntegracaoApiTest`)

- **Objetivo:** validar o fluxo completo com Spring Boot real + banco H2
- **Tecnologia:** `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate`
- **Profile:** `test` (banco H2 isolado)
- **Fluxo testado (8 passos ordenados):**

```
1. POST /api/integrantes  → 201 + Location
2. GET  /api/integrantes/{id}  → 200
3. POST /api/times  → 201 + Location
4. POST /api/times (duplicado)  → 422  ← regra de negócio validada no banco real
5. GET  /api/time-da-data?data=...  → 200 com integrante correto
6. GET  /api/integrante-mais-usado  → 200 com integrante correto
7. DELETE /api/times/{id}  → 204 → GET confirma 404
8. DELETE /api/integrantes/{id}  → 204 → GET confirma 404
```

---

## 10. Observabilidade e Logging

Logging estruturado com `@Slf4j` (SLF4J + Logback) em todas as camadas críticas:

| Camada | Nível | O que é logado |
|--------|-------|---------------|
| `IntegranteService` | `INFO` | Criação, atualização e remoção de integrantes |
| `TimeService` | `INFO` | Criação e remoção de times (com contagem de integrantes) |
| `ApiController` | `INFO` | Cada endpoint chamado com os parâmetros recebidos |
| `ApiService` | `DEBUG` | Cálculos analíticos com período aplicado |
| `GlobalExceptionHandler` | `WARN` / `ERROR` | Erros de negócio (WARN) e erros inesperados (ERROR com stack trace) |

**Exemplos de saída:**
```
INFO  TimeService - Time criado: id=1, clube=São Paulo, data=2024-01-07, 3 integrante(s)
INFO  ApiController - GET /integrante-mais-usado - periodo=[2024-01-01, 2024-12-31]
WARN  GlobalExceptionHandler - Regra de negócio violada: O clube 'São Paulo' já possui um time cadastrado para 2024-01-07.
ERROR GlobalExceptionHandler - Erro interno inesperado [stack trace completo]
```

---

## 11. Performance

### Problema N+1 resolvido com duas estratégias combinadas

**Estratégia 1 — `@BatchSize(size = 50)` na entidade `Time`:**
```java
@OneToMany(mappedBy = "time", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
@BatchSize(size = 50)
private List<ComposicaoTime> composicaoTimes = new ArrayList<>();
```
Ao invés de 1 query por time, o Hibernate carrega as composições em lotes de 50 — reduzindo de N queries para `ceil(N/50)`.

**Estratégia 2 — `@EntityGraph` no `TimeRepository`:**
```java
@EntityGraph(attributePaths = {"composicaoTimes", "composicaoTimes.integrante"})
@Query("SELECT t FROM Time t")
List<Time> findAllWithComposicao();
```
Para operações de processamento analítico (onde todos os dados são necessários de uma vez), o `@EntityGraph` instrui o Hibernate a realizar um único `JOIN` ao invés de múltiplas queries.

### FetchType.LAZY como padrão
Todas as associações usam `FetchType.LAZY`. Dados só são carregados quando explicitamente necessários, evitando carregamento acidental em operações que não precisam das coleções.

---

## 12. Perfis de Ambiente

A aplicação é configurada via profiles do Spring Boot:

| Profile | Banco | DDL | SQL Log | Swagger | Uso |
|---------|-------|-----|---------|---------|-----|
| `dev` (padrão) | H2 in-memory | `create-drop` | `true` | ✅ | Desenvolvimento local |
| `test` | H2 in-memory isolado | `create-drop` | `false` | ❌ | Testes automatizados |
| `prod` | PostgreSQL (variáveis de ambiente) | `validate` | `false` | ❌ | Produção |

**Ativação do profile de produção:**
```bash
java -jar duxusdesafio.jar --spring.profiles.active=prod \
  --DB_URL=jdbc:postgresql://host:5432/db \
  --DB_USER=usuario \
  --DB_PASS=senha
```

---

## 13. Documentação Interativa — Swagger

Após iniciar a aplicação, a documentação interativa completa de todos os endpoints fica disponível em:

```
http://localhost:8080/swagger-ui.html
```

A especificação OpenAPI no formato JSON pode ser obtida em:

```
http://localhost:8080/v3/api-docs
```

---

## 14. Telas — Frontend

| Rota | Tela | Funcionalidades |
|------|------|-----------------|
| `/integrantes` | Gerenciar Integrantes | Cadastro com validação, listagem e remoção |
| `/times` | Montar Times | Seleção de integrantes via checkboxes, listagem e remoção |
| `/consultas` | Consultas Analíticas | Formulários para todos os 7 endpoints de análise com exibição do JSON de resposta |

- Interface responsiva com **Bootstrap 5**
- Comunicação com a API via **Fetch API** (sem page reload)
- Feedback visual de sucesso/erro em todas as operações
- Alertas padronizados a partir do campo `mensagem` do `ErrorResponse`

---

## 15. Como Executar e Testar

### Pré-requisitos

- Java 8 ou superior
- Maven 3.6+

### Executar localmente

```bash
# Clonar o repositório
git clone https://github.com/matheusaguinelo/aguinelo-desafioDX-AnalistaDev.git
cd duxusdesafio

# Compilar e executar
mvn spring-boot:run

# A aplicação sobe em:
# http://localhost:8080
```

### Acessar as telas

| URL | Descrição |
|-----|-----------|
| `http://localhost:8080/integrantes` | Tela de cadastro de integrantes |
| `http://localhost:8080/times` | Tela de montagem de times |
| `http://localhost:8080/consultas` | Tela de consultas analíticas |
| `http://localhost:8080/swagger-ui.html` | Documentação interativa da API |
| `http://localhost:8080/h2-console` | Console do banco H2 (somente profile dev) |

**Configurações do H2 Console:**
```
JDBC URL:  jdbc:h2:mem:duxusdb
User:      sa
Password:  (vazio)
```

### Executar os testes

```bash
# Todos os testes (unitários + web + integração)
mvn test

# Apenas testes unitários do service
mvn test -Dtest=ApiServiceTest

# Apenas teste de integração end-to-end
mvn test -Dtest=IntegracaoApiTest
```

### Fluxo rápido de validação via curl

```bash
# 1. Criar integrante
curl -X POST http://localhost:8080/api/integrantes \
  -H "Content-Type: application/json" \
  -d '{"nome":"Hernanes","funcao":"Volante"}'

# 2. Criar time (substituir {id} pelo id retornado)
curl -X POST http://localhost:8080/api/times \
  -H "Content-Type: application/json" \
  -d '{"nomeClube":"São Paulo","data":"2024-01-07","integranteIds":[{id}]}'

# 3. Consultar time da data
curl "http://localhost:8080/api/time-da-data?data=2024-01-07"

# 4. Consultar integrante mais usado
curl "http://localhost:8080/api/integrante-mais-usado"

# 5. Contagem de clubes no período
curl "http://localhost:8080/api/contagem-de-clubes?dataInicial=2024-01-01&dataFinal=2024-12-31"
```

---

## 16. Estrutura de Arquivos

```
src/
├── main/
│   ├── java/br/com/duxusdesafio/
│   │   ├── config/
│   │   │   └── OpenApiConfig.java             # Configuração Swagger/OpenAPI
│   │   ├── controller/
│   │   │   ├── ApiController.java             # 7 endpoints analíticos
│   │   │   ├── IntegranteController.java      # CRUD integrantes + paginação
│   │   │   ├── TimeController.java            # CRUD times + paginação
│   │   │   └── ViewController.java            # Roteamento das telas Thymeleaf
│   │   ├── dto/
│   │   │   ├── IntegranteResponse.java        # DTO de saída para integrante
│   │   │   ├── TimeResponse.java              # DTO de saída para time
│   │   │   ├── TimeDaDataResponse.java        # DTO específico do endpoint timeDaData
│   │   │   └── TimeRequest.java               # DTO de entrada para criação de time
│   │   ├── exception/
│   │   │   ├── BusinessRuleException.java
│   │   │   ├── EntityNotFoundException.java
│   │   │   ├── ErrorResponse.java             # DTO padronizado de erro
│   │   │   ├── GlobalExceptionHandler.java    # @RestControllerAdvice centralizado
│   │   │   └── InvalidDateRangeException.java
│   │   ├── model/
│   │   │   ├── ComposicaoTime.java
│   │   │   ├── Integrante.java
│   │   │   └── Time.java                      # FetchType.LAZY + @BatchSize(50)
│   │   ├── repository/
│   │   │   ├── ComposicaoTimeRepository.java
│   │   │   ├── IntegranteRepository.java
│   │   │   └── TimeRepository.java            # @EntityGraph + existsByNomeClubeAndData
│   │   └── service/
│   │       ├── ApiService.java                # 7 métodos + validação de período + @Slf4j
│   │       ├── IntegranteService.java         # @Transactional + @Slf4j
│   │       └── TimeService.java               # @Transactional + regras de negócio + @Slf4j
│   └── resources/
│       ├── templates/
│       │   ├── consultas.html
│       │   ├── integrantes.html
│       │   └── times.html
│       ├── application.properties             # Config comum + profile ativo
│       ├── application-dev.properties         # H2 + console + show-sql
│       └── application-prod.properties        # PostgreSQL + configuração de produção
└── test/
    ├── java/br/com/duxusdesafio/
    │   ├── controller/
    │   │   ├── ApiControllerTest.java         # @WebMvcTest — 11 testes
    │   │   ├── IntegranteControllerTest.java  # @WebMvcTest — 9 testes
    │   │   └── TimeControllerTest.java        # @WebMvcTest — 9 testes
    │   ├── service/
    │   │   └── ApiServiceTest.java            # JUnit 5 puro — 22 testes
    │   ├── DuxusdesafioApplicationTests.java  # Context load
    │   └── IntegracaoApiTest.java             # @SpringBootTest — 8 testes
    └── resources/
        └── application-test.properties        # H2 isolado para testes
```

---

## 17. Autor

---

**🏢 Matheus H Aguinelo da Silva**
Desenvolvimento de Softwares

📄 CNPJ: 58.567.424/0001-12

🌍 [www.matheusaguinelo.com.br](https://www.matheusaguinelo.com.br)

---

*Documento gerado como parte da entrega do Desafio de Desenvolvimento.*
