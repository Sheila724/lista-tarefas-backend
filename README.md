# Lista de Tarefas - Backend
Aplicação backend para gerenciamento de tarefas do dia a dia, desenvolvida com Java Spring Boot e PostgreSQL.

---

## Sobre o projeto
API REST completa para gerenciamento de tarefas, permitindo criar, listar, alterar e deletar tarefas. Projeto avaliativo desenvolvido para a disciplina de Laboratório de Desenvolvimento Multiplataforma - 6° DSM - FATEC Franca "Dr. Thomaz Novelino".

---

## Tecnologias utilizadas
* **Java 17**
* **Spring Boot 3.3.5**
  * **Spring Web**
  * **Spring Data JPA**
  * **Spring Validation**
* **PostreSQL 18**
* **Hibernate 6**
* **Lombok**
* **JUnit 5 + Mockito (testes unitários)**
* **H2 (banco em memória para testes de integração)**

---

## Entidade: Tarefa

| **Campo**       | **Tipo**      | **Descrição**                                      |
|:----------------|:--------------|:---------------------------------------------------|
| id              | Long          | Identificador único (gerado automaticamente)       |
| nome            | String        | Nome da tarefa (obrigatório                        |
| descricao       | String        | Descrição detalhada                                |
| status          | Enum          | `PENDENTE`,`EM_ANDAMENTO`, `CONCLUIDA`,`CANCELADA` |
| observacoes     | String        | Observações adicionais                             |
| dataCriacao     | LocalDatime   | Preenchida automaticamente pela auditoria JPA      |
| dataAtualizacao | LocalDateTime | Atualizada automaticamente pela auditoria JPA      |


-----

## Endpoints da API

### Base URL: `http://localhost:8080`
| **Método** | **Caminho**        | **Status**       | **Descrição**           |
|:-----------|:-------------------|:-----------------|:------------------------|
| `POST`     | `/api/tarefas`     | `201 Created`    | Criar tarefa            |
| `PUT`      | `/api/tarefas/{id}`| `200 OK`         | Alterar tarefa          |
| `DELETE`   | `/api/tarefas`     | `204 No Content` | Excluir tarefa          |
| `GET`      | `/api/tarefas/{id}`| `200 OK`         | Buscar tarefa por ID    |
| `GET`      | `/api/tarefas/`    | `20O OK`         | Listar todas as tarefas |

### Respostas de erro padronizadas:

* `400 Bad Request` - validação falhou (nome vazio, status inválido)
* `404 Not Found` - tarefa não encontrada

### Exemplo de corpo Json (criar/alterar)

```json
{
  "nome": Estudar Spring Boot",
  "descricao": "Revisar conteúdo de APIs REST",
  "status": "EM_ANDAMENTO",
  "observacoes": "Focar nos endpoints e validações"
}
```
---

## Regas de negócio

* Não há sistema de login
* Não há conceito de usuário
* O campo `nome` é obrigatório
* As datas `dataCriacao` e `dataAtualizacao` são preenchidas automaticamente pela auditoria JPA

---

## Como rodar o projeto

### Pré-requisitos

* Java 17+
* PostgreSQL instalado e rodando
* IntelliJ IDEA (recomendado)
* Plugin Lombok instalado no IntelliJ (Settings → Plugins → Lombok)

**1. Clonar o repositório**

```bash
git clone https://github.com/Sheila724/lista-tarefas-backend.git
cd lista-tarefas-backend
```
**2. Criar o banco de dados**

Conecte como superusuário no PostgreSQL e execute os scripts na ordem:

```bash
# Cria o banco lista_tarefas
psql -U postgres -f src/main/resources/db/00-create-database.sql

# Conecta no banco e cria a tabela
psql -U postgres -d lista_tarefas -f src/main/resources/db/schema/sql
```
**3. Configurar as variáveis de ambiente**

O projeto usa variáveis de ambiente para proteger as credenciais do banco. Configure-as antes de rodar:

**No terminal (Linux/Mac):**

`export DB_URL=jdbc:postgresql://localhost:5432/lista_tarefas`
`export DB_USERNAME=postgres`
`export DB_PASSWORD=`sua_senha_do_banco`

**No terminal (Windows - PowerShell):**

`$env:DB_URL="jdbc:postgresql:/localhost:5432/lista_tarefas"`
`$env:DB_USERNAME="postgres"`
`$env:DB_PASSWORD="sua_senha`

**No IntelliJ (Run/Debug Configurations → Environment variables):**

`DB_URL=jbc:postgresql://localhost5432/lista_tarefas`
`DB_USERNAME=postgres`
`DB_PASSWORD=sua_senha`

**NUNCA COMMITE SENHAS NO REPOSITÓRIO.O arquivo `application.properties` já está cofigurado para ler essas variávies automaticamente.**

**4. Rodar a aplicação**

No IntelliJ, abra a classe `ListaTarefasApplication` e clique no botão **▶ Run**

A aplicação estará disponível em: `http://localhost:8080`

---

## Testes

O projeto conta com dois tipos de testes:

** - Unitário:** `TarefasServiceImplTest` - testa a camada de serviço com Mockito (repositório mockado), cobrindo 8 cenários incluindo happy paths e exceções
** - Integração:** `TarefaControllerIntegrationTest`- testa os endpoints com `@SpringBootTest`+ `MockMvc` usando H2 em mémoria (não requer PostgreSQL)

Para rodar os testes no IntelliJ:

Maven → test

Ou clique com o botão direito na pasta `src/test`→ Run All Tests

---

## Estrutura do projeto

lista-tarefas-backend/

```
├── src/

│   ├── main/

│   │   ├── java/com/listatarefas/

│   │   │   ├── controller/        # endpoints REST

│   │   │   ├── service/           # interface + implementação

│   │   │   ├── repository/        # JpaRepository

│   │   │   ├── domain/            # entidade Tarefa + enum StatusTarefa

│   │   │   ├── dto/               # TarefaRequest e TarefaResponse

│   │   │   ├── exception/         # tratamento global de erros

│   │   │   └── ListaTarefasApplication.java

│   │   └── resources/

│   │       ├── db/

│   │       │   ├── 00-create-database.sql

│   │       │   └── schema.sql

│   │       ├── application.properties

│   │       └── application-dev.properties

│   └── test/

│       └── java/com/listatarefas/

│           ├── service/TarefaServiceImplTest.java

│           └── controller/TarefaControllerIntegrationTest.java

├── pom.xml

└── README.md
```

# Autores

### Desenvolvido por **Gabriel Araujo** e **Sheila Alves** - 6° DSM - FATEC Franca "Dr. Thomaz Novelino"