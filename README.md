# HelpDesk API

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

</div>

API REST para gerenciamento de tickets de suporte. O projeto foi desenvolvido em Java com Spring Boot e permite cadastrar usuários, abrir chamados, atualizar o andamento do atendimento e registrar comentários.

## Visão geral

Este sistema simula um help desk simples, com foco em relações entre usuários e tickets. A aplicação expõe endpoints REST para operações de CRUD e filtros por status.

## Stack utilizada

- Java 21
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- MySQL
- Maven
- Validation

## Funcionalidades

- Cadastro de usuários
- Listagem de usuários
- Busca por ID
- Criação de tickets
- Listagem de tickets
- Busca de ticket por ID
- Atualização de ticket
- Filtro por status
- Registro e listagem de comentários
- Regras de negócio para impedir comentários em tickets fechados

## Estrutura do projeto

```text
HelpDesk-API/
├── src/
│   ├── main/
│   │   ├── java/com/portfolio/HelpDesk_API/
│   │   │   ├── Controllers/
│   │   │   ├── Services/
│   │   │   ├── Repositories/
│   │   │   ├── entities/
│   │   │   ├── dtos/
│   │   │   ├── Exceptions/
│   │   │   └── HelpDeskApiApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/portfolio/HelpDesk_API/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── HELP.md
├── .gitignore
└── README.md
```

## Requisitos

Antes de rodar o projeto, verifique se você possui:

- JDK 21+
- Maven
- MySQL instalado e rodando
- IDE opcional: IntelliJ IDEA / VS Code / Eclipse

## Configuração local

No arquivo `src/main/resources/application.properties`, configure a conexão com o banco:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/HelpDesk-API_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> Se o banco não existir, o parâmetro `createDatabaseIfNotExist=true` tenta criá-lo automaticamente.

## Como executar

Na raiz do projeto, execute:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

A aplicação estará disponível em:

```text
http://localhost:8080
```

## Endpoints da API

### Usuários

#### 1. Criar usuário

```http
POST /users
```

Body exemplo:

```json
{
  "name": "Maria Silva",
  "email": "maria@email.com",
  "password": "123456"
}
```

#### 2. Listar usuários

```http
GET /users
```

#### 3. Buscar usuário por ID

```http
GET /users/{id}
```

### Tickets

#### 1. Criar ticket

```http
POST /tickets
```

Body exemplo:

```json
{
  "title": "Erro ao acessar painel",
  "description": "O usuário não consegue entrar no sistema",
  "status": "OPEN",
  "userId": 1
}
```

#### 2. Listar tickets

```http
GET /tickets
```

#### 3. Buscar ticket por ID

```http
GET /tickets/{id}
```

#### 4. Atualizar ticket

```http
PUT /tickets/{id}
```

Body exemplo:

```json
{
  "title": "Erro ao acessar painel",
  "description": "Problema corrigido após atualização",
  "status": "IN_PROGRESS"
}
```

#### 5. Filtrar tickets por status

```http
GET /tickets/status/{status}
```

Exemplos:

```http
GET /tickets/status/OPEN
GET /tickets/status/IN_PROGRESS
GET /tickets/status/CLOSED
```

### Comentários

#### 1. Criar comentário

```http
POST /comments
```

Body exemplo:

```json
{
  "description": "Já verifiquei o login e o problema está relacionado à configuração do ambiente.",
  "ticketId": 1,
  "userId": 1
}
```

#### 2. Listar comentários de um ticket

```http
GET /comments/tickets/{id}/comments
```

## Enum de status

Os tickets utilizam o seguinte enum:

```java
public enum Status {
    OPEN,
    IN_PROGRESS,
    CLOSED
}
```

## Regras de negócio

- Um comentário só pode ser adicionado se o ticket não estiver fechado.
- O status do ticket pode ser alterado através da rota de atualização.
- A API trabalha com relacionamento entre `User`, `Ticket` e `Comments`.

## Observações

- Esse projeto foi pensado como um exemplo de aplicação REST com Java e Spring Boot.
- Não há autenticação JWT implementada neste momento.
- A API está pronta para uso local e estudo de arquitetura, endpoints e regras de negócio.

## Próximos passos sugeridos

- Implementar autenticação com JWT
- Adicionar paginação e ordenação
- Criar testes automatizados com JUnit e MockMvc
- Melhorar tratamento de erros e validações
- Documentar endpoints com Swagger/OpenAPI

## Licença

Este projeto é de uso livre para fins de estudo e portfólio.

## Desenvolvido por

Petterson Oliveira
