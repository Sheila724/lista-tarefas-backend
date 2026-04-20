# Análise Técnica do Projeto — Lista de Tarefas Backend

> **Revisão:** 2026-04-19
> **Autor da análise:** Claude (Anthropic)
> **Escopo:** Avaliação completa do código-fonte, arquitetura, qualidade de testes e conformidade com os requisitos fornecidos.

---

## 1. Visão Geral

Aplicação REST API desenvolvida em **Java 17 + Spring Boot 3.3.5** para gerenciamento de tarefas do dia a dia. Sem sistema de login ou conceito de usuário. Persistência em **PostgreSQL**, testes com **JUnit 5 + Mockito** e banco em memória **H2** para o perfil de teste.

---

## 2. Mapeamento de Requisitos

| Requisito | Situação | Observação |
|---|---|---|
| Criar tarefa | ✅ Implementado | `POST /api/tarefas` |
| Alterar tarefa | ✅ Implementado | `PUT /api/tarefas/{id}` |
| Deletar tarefa | ✅ Implementado | `DELETE /api/tarefas/{id}` |
| Campo: Nome | ✅ `VARCHAR(200) NOT NULL` | Validação `@NotBlank` + `@Size` |
| Campo: Descrição | ✅ `TEXT` nullable | — |
| Campo: Status | ✅ Enum com 4 estados | `PENDENTE, EM_ANDAMENTO, CONCLUIDA, CANCELADA` |
| Campo: Observações | ✅ `TEXT` nullable | — |
| Data de Criação | ✅ JPA `@CreatedDate` | Imutável após criação (`updatable = false`) |
| Data de Atualização | ✅ JPA `@LastModifiedDate` | Atualizada via `save()` a cada PUT |
| Sem sistema de login | ✅ Correto | Nenhum mecanismo de autenticação presente |
| Sem conceito de usuário | ✅ Correto | Lista global compartilhada |
| Java + Spring Boot | ✅ Java 17, Spring Boot 3.3.5 | — |
| PostgreSQL | ✅ Driver + configuração | Credenciais externalizadas via env vars |
| Script de criação do banco | ✅ `00-create-database.sql` + `schema.sql` | — |
| Classes de modelo | ✅ `Tarefa.java` + `StatusTarefa.java` | — |
| Conexão com banco | ✅ Spring Data JPA + HikariCP | — |
| Serviços de implementação | ✅ Interface `TarefaService` + `TarefaServiceImpl` | — |
| Testes (Unitário) | ✅ `TarefaServiceImplTest` — 8 cenários | — |
| Testes (Integração) | ✅ `TarefaControllerIntegrationTest` — 2 cenários | — |

**Todos os requisitos estão implementados.**

---

## 3. Análise Técnica Crítica

### 3.1 O que está bem implementado

- **Arquitetura em camadas** (Controller → Service → Repository) com responsabilidades bem definidas e baixo acoplamento.
- **Interface de serviço:** `TarefaService` permite trocar a implementação sem afetar o controller — prática sólida.
- **Injeção via construtor** (Lombok `@RequiredArgsConstructor`): favorece testabilidade e deixa dependências explícitas.
- **DTOs de request/response distintos:** impede exposição acidental de campos internos da entidade.
- **Tratamento global de exceções** (`@RestControllerAdvice`): erros 404 e 400 retornam JSON padronizado.
- **Auditoria de timestamps via JPA** (`@CreatedDate`, `@LastModifiedDate`): desacopla a lógica de datas do serviço.
- **Perfis separados** (prod / dev / test): configurações não se misturam entre ambientes.
- **H2 nos testes:** a suíte roda sem dependência de banco externo — facilita CI/CD.
- **Status armazenado como `VARCHAR`** (não `INTEGER`): legível diretamente no banco, sem mapeamento numérico.
- **Índice em `status`:** demonstra preocupação com performance de consulta, mesmo que o projeto seja simples.

---

### 3.2 Problemas identificados e correções aplicadas

#### BUG — `dataAtualizacao` desatualizada na resposta do `PUT`
**Gravidade: Média**

**Antes:**
```java
// TarefaServiceImpl.atualizar()
entity.setNome(request.getNome());
// ...
return TarefaResponse.fromEntity(entity); // timestamp ainda é o antigo
```

O `@LastModifiedDate` é aplicado durante o *flush* do JPA, que ocorre ao término da transação — **depois** do retorno do método. A resposta da API devolvia o valor antigo de `dataAtualizacao`, criando inconsistência entre o JSON retornado e o dado persistido no banco.

**Correção aplicada:**
```java
Tarefa salva = tarefaRepository.save(entity); // força o @PreUpdate imediatamente
return TarefaResponse.fromEntity(salva);
```

---

#### LACUNA — Testes unitários ausentes
**Gravidade: Baixa (cobertura incompleta)**

Os seguintes cenários não tinham cobertura na camada de unidade:

| Método | Cenário ausente |
|---|---|
| `excluir` | Happy path (tarefa existe → `deleteById` é chamado) |
| `buscarPorId` | Happy path (tarefa encontrada → DTO retornado) |
| `buscarPorId` | Not found → `RecursoNaoEncontradoException` |

**Correção aplicada:** 3 novos testes adicionados em `TarefaServiceImplTest`.

---

#### SEGURANÇA — Credencial hardcoded no repositório Git
**Gravidade: Alta**

`application.properties` continha a senha do banco em texto plano:
```properties
spring.datasource.password=Gabi0510
```

Qualquer pessoa com acesso ao repositório (ou ao histórico do Git) tem acesso à credencial. Isso viola o princípio básico de **não commitar segredos**.

**Correção aplicada:** externalizadas via variáveis de ambiente:
```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/lista_tarefas}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:}
```

> **Atenção:** a senha ainda pode estar exposta no histórico do Git. Se o repositório for público, recomenda-se trocar a senha do banco imediatamente.

---

### 3.3 Pontos de atenção (sem correção imediata, mas merecem reflexão)

#### `excluir` executa duas consultas desnecessariamente

```java
if (!tarefaRepository.existsById(id)) { // SELECT 1
    throw new RecursoNaoEncontradoException(...);
}
tarefaRepository.deleteById(id); // DELETE (internamente faz outro SELECT + DELETE)
```

Isso resulta em até 3 queries para uma operação simples. Uma alternativa seria buscar com `findById`, lançar exceção se vazio, e deletar o objeto gerenciado:

```java
Tarefa entity = tarefaRepository.findById(id).orElseThrow(...);
tarefaRepository.delete(entity);
```

> Não foi alterado por ser uma decisão de design que envolve trade-off de legibilidade vs. performance — irrelevante no volume atual.

---

#### Ausência de paginação em `GET /api/tarefas`

`listarTodas()` carrega todas as tarefas da tabela em memória. Para uso pessoal isso é aceitável, mas seria prudente adicionar `Pageable` do Spring Data para crescimento futuro.

---

#### Sem filtragem por status

Não há endpoint `GET /api/tarefas?status=PENDENTE`. Para uma aplicação de gerenciamento de tarefas, filtrar por status é uma operação central esperada pelo usuário.

---

#### Transição de status não validada

Uma tarefa `CANCELADA` pode ser reativada como `PENDENTE` ou ir direto para `CONCLUIDA`. Não há máquina de estados. Para o escopo atual isso é aceitável, mas deve ser decisão explícita.

---

#### Sem documentação da API (OpenAPI/Swagger)

Não há Springdoc/Swagger configurado. Quem consumir a API (ex: um frontend) precisará inspecionar o código ou contar com um README manual. Para um projeto de avaliação acadêmica, a adição de `springdoc-openapi-starter-webmvc-ui` geraria `/swagger-ui.html` sem custo significativo.

---

#### Sem configuração de CORS

Se um frontend (ex: React, Angular) for consumir esta API de uma origem diferente, as requisições serão bloqueadas pelo browser. Não há `@CrossOrigin` nem `CorsConfigurationSource` configurado.

---

## 4. Estrutura Final do Projeto

```
lista-tarefas-backend/
├── src/main/java/com/listatarefas/
│   ├── ListaTarefasApplication.java          // @EnableJpaAuditing
│   ├── controller/TarefaController.java      // 5 endpoints REST
│   ├── service/
│   │   ├── TarefaService.java                // interface
│   │   └── impl/TarefaServiceImpl.java       // implementação
│   ├── repository/TarefaRepository.java      // JpaRepository
│   ├── domain/
│   │   ├── Tarefa.java                       // entidade JPA
│   │   └── StatusTarefa.java                 // enum de 4 estados
│   ├── dto/
│   │   ├── TarefaRequest.java                // entrada validada
│   │   └── TarefaResponse.java               // saída mapeada
│   └── exception/
│       ├── RecursoNaoEncontradoException.java
│       └── ApiExceptionHandler.java
├── src/main/resources/
│   ├── application.properties               // credenciais via env vars
│   ├── application-dev.properties
│   └── db/
│       ├── 00-create-database.sql
│       └── schema.sql
└── src/test/java/com/listatarefas/
    ├── service/TarefaServiceImplTest.java    // 8 testes unitários
    └── controller/TarefaControllerIntegrationTest.java // 2 testes integração
```

---

## 5. Endpoints da API

| Método | Endpoint | Status sucesso | Descrição |
|---|---|---|---|
| `POST` | `/api/tarefas` | `201 Created` | Cria uma nova tarefa |
| `GET` | `/api/tarefas` | `200 OK` | Lista todas as tarefas |
| `GET` | `/api/tarefas/{id}` | `200 OK` | Busca tarefa por ID |
| `PUT` | `/api/tarefas/{id}` | `200 OK` | Atualiza todos os campos |
| `DELETE` | `/api/tarefas/{id}` | `204 No Content` | Remove a tarefa |

**Respostas de erro padronizadas:**
- `400 Bad Request` — validação falhou (nome vazio, status nulo)
- `404 Not Found` — tarefa não existe

---

## 6. Como executar

### Pré-requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL em execução na porta `5432`

### 1. Banco de dados

```sql
-- Como superusuário PostgreSQL:
\i src/main/resources/db/00-create-database.sql

-- Conectado ao banco lista_tarefas:
\i src/main/resources/db/schema.sql
```

### 2. Variáveis de ambiente

```bash
export DB_URL=jdbc:postgresql://localhost:5432/lista_tarefas
export DB_USERNAME=postgres
export DB_PASSWORD=sua_senha_aqui
```

### 3. Executar a aplicação

```bash
mvn spring-boot:run
# API disponível em http://localhost:8080
```

### 4. Executar os testes

```bash
mvn test
# Não requer banco externo — usa H2 em memória
```

---

## 7. Perguntas Relevantes para o Sucesso do Projeto

As questões abaixo não têm resposta implícita no código ou nos requisitos. Respondê-las define o escopo real do produto e evita retrabalho.

---

### 7.1 Sobre requisitos funcionais

**P1 — Qual é o status padrão ao criar uma tarefa?**
Atualmente o `status` é obrigatório no `TarefaRequest`. O cliente (frontend/Postman) precisa enviar o status explicitamente. Deve o backend assumir `PENDENTE` como default, tornando o campo opcional na criação?

**P2 — Uma tarefa deletada é removida permanentemente?**
O `DELETE` atual executa `deleteById`, apagando o registro fisicamente do banco. Não há soft-delete (campo `deletado_em`, por exemplo). Se houver necessidade de histórico ou de "lixeira", isso precisa ser redesenhado.

**P3 — As transições de status devem seguir uma ordem?**
Atualmente qualquer transição é permitida (ex: `CANCELADA` → `EM_ANDAMENTO`). Se existir uma lógica de negócio (ex: tarefa concluída não pode voltar a pendente), é necessário implementar validação de fluxo de estados.

**P4 — Deve haver um endpoint de busca/filtro por status?**
`GET /api/tarefas` retorna todas as tarefas. Para uso real, filtrar por `PENDENTE` ou `EM_ANDAMENTO` é uma operação esperada. Este endpoint está planejado?

**P5 — As observações substituem ou complementam a descrição?**
Ambos os campos são texto livre. Não há distinção semântica clara na implementação — apenas nos nomes. Existe uma regra de uso diferenciado entre os dois?

---

### 7.2 Sobre requisitos técnicos e operação

**P6 — A API será consumida por um frontend?**
Se sim, qual origem (domínio/porta)? Sem configuração de CORS, o browser bloqueará as requisições. Se for consumida apenas via Postman/IntelliJ HTTP Client, CORS não é necessário.

**P7 — O ambiente de produção é local (notebook) ou haverá deploy em servidor?**
A configuração atual aponta para `localhost:5432`. Para deploy em nuvem (Heroku, Railway, Render, VPS), as variáveis de ambiente precisam ser configuradas na plataforma e o `spring.jpa.hibernate.ddl-auto=validate` exige que o schema já exista.

**P8 — O histórico do Git deve ser limpo?**
A senha `Gabi0510` foi commitada anteriormente no `application.properties`. Ela pode estar no histórico Git. Se o repositório for ou vier a ser público, a senha precisa ser trocada no banco de dados independentemente da correção feita no código.

**P9 — Há requisito de performance ou volume de dados?**
O endpoint `GET /api/tarefas` retorna todos os registros sem paginação. Para uso pessoal com dezenas de tarefas isso é inofensivo. Se o volume crescer ou a API for exposta a múltiplos usuários, paginação se torna necessária.

---

### 7.3 Sobre qualidade e entrega

**P10 — A cobertura de testes tem um percentual mínimo exigido?**
A suíte atual cobre os fluxos críticos (happy path + not found) para todos os métodos do serviço, e o fluxo CRUD completo na integração. Se o critério de avaliação exigir um percentual (ex: 80% via JaCoCo), isso precisa ser verificado e o plugin configurado.

**P11 — Documentação interativa da API (Swagger) é um critério de avaliação?**
A adição de `springdoc-openapi` geraria `/swagger-ui.html` automaticamente com pouquíssimo esforço. É um diferencial relevante para projetos acadêmicos.

**P12 — O `application-dev.properties` deve ser commitado?**
Atualmente está no repositório. Ele não contém segredos (apenas `ddl-auto=update` e `show-sql=true`), mas é um ponto a alinhar — alguns times preferem não commitar arquivos de configuração de ambiente de desenvolvimento.

---

## 8. Resumo das Alterações Realizadas

| Arquivo | Tipo | Descrição |
|---|---|---|
| `TarefaServiceImpl.java` | Correção de bug | `atualizar()` agora chama `save()` e usa a entidade retornada, garantindo `dataAtualizacao` correto na resposta |
| `TarefaServiceImplTest.java` | Novos testes | Adicionados `excluir_quandoExiste_deveInvocarDelete`, `buscarPorId_quandoExiste_deveRetornarDto`, `buscarPorId_quandoNaoExiste_deveLancar` |
| `application.properties` | Segurança | Credenciais substituídas por variáveis de ambiente (`${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`) |
