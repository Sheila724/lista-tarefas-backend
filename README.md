# Lista de Tarefas - Backend
Aplicação backend para gerenciamento de tarefas do dia a dia, desenvolvida com Java Spring Boot e PostgreSQL.

## Sobre o projeto
API REST completa para o gerenciamento de tarefas, permitindo criar, listar, alterar e deletar tarefas. 
Projeto avaliativo desenvolvido para a disciplina Laboratório de Desenvolvimento Multiplataforma - 6° DSM - FATEC Franca "Dr. Thomaz Novelino".

## Tecnologias utilizadas
* **Java 17**
* **Spring Boot 3.3.5**
  * Spring Web
  * Spring Data JPA
  * Spring Validation
* **PostgreSQL 18**
* **Hibernate 6**
* **Lombok**
* **JUnit 5 + Mockito** (testes)
* **H2** (banco em memória para testes de integração)

## Entidade: Tarefa

| Campo | Tipo | Descrição |
| :-----| :----| :---------|
| id    | Long | Identificador único (gerado automaticamente |
| nome  | String | Nome da tarefa (**obrigatório**)
| descrição | String | Descrição detalhada |
| status | Enum | PENDENTE, EM_ANDAMENTO, CONCLUIDA, CANCELADA |
| observações | String | Observações adicionas |
| dataCriacao | LocalDateTime | Preenchida automaticamente pela auditoria JPA |
| dataAtualizacao | LocalDateTime | Atualizada automaticamente pela auditoria JPA |

## Endpoints da API

| **Método** | **Caminho**        | **Ação** |
| :----------|:-------------------|:---------|
| POST       | /api/tarefas       | Criar tarefa |
| PUT | /api/tarefas/{id}  | Alterar tarefa |
| DELETE | /api/tarefas /{id} | Excluir tarefa |
| GET | /api/tarefas/{id} | Buscar tarefa por ID |
| GET | /api/tarefas | Listar todas as tarefas |

## Exemplos de corpo JSON (criar/alterar)

```
{
 "nome": "Estudar Spring Boot",
 "descricao": "Revisar conteúdo de APIs REST",
 "status": "EM_ANDAMENTO",
 "observacoes": "Focar nos endpoints e validações"
}
```

## Regras de negócio

* Não há sistema de login
* Não há conceito de usuário
* O campo `nome` é obrigatório
* A datas `dataCriacao` e `dataAtualizacao`são preenchidas automaticamente

## Como rodar o projeto

**Pré-Requisitos**
* Java 17+
* PostgreSQL instalado e rodando
* IntelliJ IDEA (recomendado)
* Plugin Lombok instalado no IntelliJ

### 1. Clonar o repositório

`git clone https://github.com/Sheila724/lista-tarefas-backend.git`
`cd lista-tarefas-backend`

### 2. Criar o banco de dados

Conecte como superusuário no PostgreSQL e execute os scripts na ordem:

* Criar o banco lista_tarefas
  * `psql -U postgres -f src/main/resources/db/00-create-database.sql`
* Conecta o banco e criar tabela
  * `psql -U postgres -d lista_tarefas -f src/main/resources/db/schema.sql`

### 3. Configurar application.properties

Em `src/main/resources/application.properties`, ajuste as credenciais se necessário:

**spring.datasource.url**=`postgresql://localhost:5432/lista_tarefas`
**spring.datasource.username**=`postgres`
**spring.datasource.password**=`sua_senha`

### 4. Rodar a aplicação
No IntelliJ, abra a classe `ListaTarefasApplication` e clique no botão **▶ Run**.

A aplicação estará disponível em: `http://localhost:8080`

## Testes
O projeto conta com dois tipos de testes:

* **Unitário:** `TarefasServiceImplTest` - testa a camada de serviço com Mockito (repositório mockado)
* **Integração:** `TarefaControllerIntegrationTest` - testa os endpoints com `@SpringBootTest` + `MockMvc`usando H2 em memória (não precisa do PostgreSQL)

Para rodar os testes no IntelliJ:

Maven → test

Ou clique com o botão direito na pasta `src/teste` **→ Run All Tests**

## Estrutura do projeto

```
lista-tarefas-backend
#
├── src/

│   ├── main/

│   │   ├── java/com/listatarefas/

│   │   │   ├── controller/

│   │   │   ├── service/

│   │   │   ├── repository/

│   │   │   ├── model/

│   │   │   └── ListaTarefasApplication.java

│   │   └── resources/

│   │       ├── db/

│   │       │   ├── 00-create-database.sql

│   │       │   └── schema.sql

│   │       ├── application.properties

│   │       └── application-dev.properties

│   └── test/

│       └── java/com/listatarefas/

├── pom.xml

└── README.md
```