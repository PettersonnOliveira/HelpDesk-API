# HelpDesk API

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-purple)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

</div>

API REST para gerenciamento de chamados de suporte, desenvolvida em Java com Spring Boot. Permite cadastro e autenticação de usuários via JWT, abertura de chamados, atualização de status, comentários e controle de acesso por perfil.

## Visão geral

O sistema simula um help desk com três perfis de usuário:

- **CLIENT**: abre chamados, acompanha apenas os próprios e comenta neles.
- **TECHNICIAN**: visualiza todos os chamados, altera status e comenta em qualquer chamado.
- **ADMIN**: mesmas permissões do técnico, além de gerenciar usuários.

A aplicação também serve um frontend estático (HTML/CSS/JS) em `src/main/resources/static`, acessível na raiz da aplicação.

## Stack utilizada

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Spring Security + JWT (jjwt 0.12.6)
- Bean Validation
- PostgreSQL
- Maven

## Funcionalidades

- Registro e login com emissão de token JWT
- CRUD de usuários (restrito a ADMIN)
- Criação, listagem, busca, atualização e exclusão de chamados
- Filtro de chamados por status
- Registro e listagem de comentários por chamado
- Controle de acesso por perfil (cliente só acessa os próprios chamados)
- Tratamento centralizado de erros e validações

## Estrutura do projeto

```text
HelpDesk-API/
├── src/
│   ├── main/
│   │   ├── java/com/portfolio/HelpDesk_API/
│   │   │   ├── Controllers/
│   │   │   ├── Services/
│   │   │   ├── Repositories/
│   │   │   ├── Security/
│   │   │   ├── entities/
│   │   │   ├── dtos/
│   │   │   ├── Exceptions/
│   │   │   └── HelpDeskApiApplication.java
│   │   └── resources/
│   │       ├── static/          # frontend (index.html, app.js, style.css)
│   │       └── application.properties
│   └── test/java/com/portfolio/HelpDesk_API/
├── .env.example
├── Dockerfile
├── pom.xml
├── mvnw
└── README.md
```

## Requisitos

- JDK 21+
- PostgreSQL
- Maven (ou usar o wrapper `./mvnw`)

## Configuração

A aplicação lê as configurações de ambiente (veja `.env.example`):

| Variável | Descrição |
| --- | --- |
| `DB_URL` | URL JDBC do PostgreSQL, ex.: `jdbc:postgresql://host:5432/helpdesk?sslmode=require` |
| `DB_USER` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `PORT` | Porta HTTP (padrão `8080`) |
| `JWT_SECRET` | Segredo usado para assinar os tokens (mínimo 32 caracteres) |

O `application.properties` apenas referencia essas variáveis:

```properties
server.port=${PORT:8080}

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

api.security.token.secret=${JWT_SECRET}
api.security.token.expiration-millis=86400000
```

> Gere um segredo forte para `JWT_SECRET`, por exemplo com `openssl rand -base64 48`.

## Como executar

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

A aplicação (API + frontend) estará disponível em:

```text
http://localhost:8080
```

## Autenticação

Rotas públicas: `POST /auth/register` e `POST /auth/login`. As demais exigem o header:

```http
Authorization: Bearer <token>
```

### Registro

```http
POST /auth/register
```

```json
{
  "name": "Maria Silva",
  "email": "maria@email.com",
  "password": "123456"
}
```

O registro público sempre cria um usuário com perfil `CLIENT`.

### Login

```http
POST /auth/login
```

```json
{
  "email": "maria@email.com",
  "password": "123456"
}
```

Resposta:

```json
{
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "name": "Maria Silva",
    "email": "maria@email.com",
    "role": "CLIENT"
  }
}
```

## Endpoints

### Usuários (somente ADMIN)

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/users` | Cria usuário |
| `GET` | `/users` | Lista usuários |
| `GET` | `/users/{id}` | Busca usuário por ID |
| `PUT` | `/users/{id}` | Atualiza nome/e-mail |
| `DELETE` | `/users/{id}` | Remove usuário |

Body de criação:

```json
{
  "name": "João Souza",
  "email": "joao@email.com",
  "password": "123456",
  "role": "TECHNICIAN"
}
```

Body de atualização:

```json
{
  "name": "João Souza",
  "email": "joao.souza@email.com"
}
```

### Chamados

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/tickets` | Cria chamado (dono = usuário autenticado) |
| `GET` | `/tickets` | Lista chamados (cliente vê só os próprios) |
| `GET` | `/tickets/{id}` | Busca chamado por ID |
| `PUT` | `/tickets/{id}` | Atualiza título/descrição (status só staff) |
| `DELETE` | `/tickets/{id}` | Remove chamado |
| `GET` | `/tickets/status/{status}` | Filtra por status |

Body de criação (o dono vem do token, não do body):

```json
{
  "title": "Erro ao acessar painel",
  "description": "O usuário não consegue entrar no sistema"
}
```

Body de atualização:

```json
{
  "title": "Erro ao acessar painel",
  "description": "Problema corrigido após atualização",
  "status": "IN_PROGRESS"
}
```

Exemplos de filtro:

```http
GET /tickets/status/OPEN
GET /tickets/status/IN_PROGRESS
GET /tickets/status/CLOSED
```

### Comentários

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/comments` | Cria comentário em um chamado |
| `GET` | `/comments/tickets/{id}/comments` | Lista comentários do chamado |

Body de criação:

```json
{
  "message": "Já verifiquei o login e o problema está relacionado à configuração do ambiente.",
  "ticketId": 1
}
```

## Enums

```java
public enum Status {
    OPEN,
    IN_PROGRESS,
    CLOSED
}

public enum Role {
    ADMIN,
    CLIENT,
    TECHNICIAN
}
```

## Regras de negócio

- Um cliente só enxerga e acessa os próprios chamados.
- Apenas ADMIN e TECHNICIAN podem alterar o status de um chamado.
- Um cliente só pode excluir chamados próprios que ainda estejam com status `OPEN`.
- Não é possível comentar em chamado com status `CLOSED`.
- O dono do chamado é sempre o usuário autenticado (o `userId` não é aceito no body).
- E-mail é único por usuário.
- O gerenciamento de usuários (`/users`) é restrito a ADMIN.

## Tratamento de erros

O `GlobalExceptionHandler` padroniza as respostas:

| Situação | Status HTTP |
| --- | --- |
| Recurso não encontrado | `404` |
| Regra de negócio violada | `400` |
| Credenciais inválidas | `401` |
| Erros de validação | `400` (mapa campo → mensagem) |

## Testes

```bash
./mvnw test
```

Cobrem as regras principais de `TicketService` e `CommentService` (controle de acesso, atualização parcial, exclusão e comentários em chamados fechados).

## Docker

```bash
docker build -t helpdesk-api .
docker run --env-file .env -p 8080:8080 helpdesk-api
```

## Licença

Este projeto é de uso livre para fins de estudo e portfólio.

## Desenvolvido por

Petterson Oliveira