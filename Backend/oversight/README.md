# Backend — Spring Boot 3.5 + Java 21

Faz só quatro coisas:
1. **Login** com JWT (grava só a tabela `usuario`).
2. **Valida** cada campo da planilha (DTO + `ClienteValidacaoService`).
3. **Encaminha** as linhas válidas ao Python (`PythonEtlService`). Quem grava os dados é o Python.
4. **Lê do banco** o resultado que o Python gravou e entrega aos gráficos (`DashboardService`).

Não existe model nem repository de upload. Linhas com erro voltam em `rejeitadas` e não são gravadas.

## Rodar
```
cd backend
copy .env.example .env      (Mac/Linux: cp .env.example .env)
mvn spring-boot:run
```
Preencha no `.env` (no Neon: `DB_URL=jdbc:postgresql://HOST/BANCO?sslmode=require`, veja `../database/README.md`): `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGINS`, `ETL_URL`, `ETL_KEY`.
Teste: http://localhost:8080/api/health → `{"status":"ok"}`.
Na primeira vez ele cria as tabelas e o administrador (`admin@cti.local` / `Admin@12345`).

## Rotas
| Rota | Quem | Para quê |
|---|---|---|
| `POST /api/auth/register` | todos | cria conta (fica PENDENTE) |
| `POST /api/auth/login` | todos | devolve o token |
| `GET /api/admin/usuarios` | ADMIN | lista usuários |
| `PATCH /api/admin/usuarios/{id}/status` | ADMIN | aprova ou bloqueia |
| `POST /api/uploads/validadas` | logado | valida e encaminha ao Python |
| `GET /api/dashboard` | logado | último lote PROCESSADO + o JSON das contas (gráficos) |
| `GET /api/dashboard?loteId=3` | logado | um lote específico |
| `GET /api/dashboard/lotes` | logado | os 20 últimos lotes (caixa de escolha da tela) |
| `GET /api/health` | todos | confere se o backend está ligado |

## Pastas (`src/main/java/com/cti/provedor/`)
```
controller/   recebe o pedido HTTP: AuthController, AdminUsuarioController, UploadController, DashboardController, ErroHandler
service/      regras de negócio: AuthService, UsuarioService, ClienteValidacaoService, UploadService, PythonEtlService, DashboardService
              (+ ajudas da validação: Colunas, Datas, Padronizacao)
repository/   fala com o banco (Spring Data JPA): UsuarioRepository
model/        entidades (tabelas): Usuario, PerfilUsuario, StatusUsuario
dto/          formato dos JSONs: ClienteDTO, UploadDtos, AuthDtos, UsuarioDto
config/       configurações: SecurityConfig, JwtService, JwtAuthenticationFilter, UsuarioDetailsService,
              AppProperties, AdminInicial, ApiException
CtiProvedorApplication.java
src/main/resources/application.properties   conexão com o banco (Neon/Azure) e outras configurações
```

## Mapa do backend: o que é cada arquivo (em uma frase)

**Para ligar e configurar**
- `CtiProvedorApplication` — o botão "ligar" do backend (`main`).
- `application.properties` — as configurações: porta, banco, JWT, CORS, endereço do Python.
- `schema.sql` — cria as tabelas no banco ao ligar.
- `pom.xml` — lista as bibliotecas que o Maven baixa.

**Upload (o coração do projeto)** — o fluxo é: `UploadController` → `UploadService` → `ClienteValidacaoService` → `PythonEtlService`
- `controller/UploadController` — recebe o POST do front.
- `service/UploadService` — separa linhas válidas e rejeitadas e envia as válidas.
- `service/ClienteValidacaoService` — valida cada campo de cada linha.
- `service/PythonEtlService` — envia as linhas válidas ao Python (POST /etl).
- `dto/UploadDtos`, `dto/ClienteDTO` — o formato do JSON do upload e de uma linha.
- `service/Colunas`, `service/Padronizacao` — ajudas da validação (nomes das colunas e padronização de texto).

**Dashboard (gráficos)** — o fluxo é: `DashboardController` → `DashboardService` → banco
- `controller/DashboardController` — as rotas `GET /api/dashboard` e `GET /api/dashboard/lotes`.
- `service/DashboardService` — faz os `SELECT` nas tabelas `upload_lote` e `estatistica_lote` (com `JdbcTemplate`) e devolve `{ lote, geradoEm, estatisticas }`. O Java só **lê**: quem grava é o Python.

**Login (JWT)**
- `controller/AuthController` + `service/AuthService` — cadastro e login.
- `controller/AdminUsuarioController` + `service/UsuarioService` — o administrador aprova ou bloqueia usuários.
- `model/Usuario`, `model/PerfilUsuario`, `model/StatusUsuario`, `repository/UsuarioRepository` — a tabela `usuario`.
- `dto/AuthDtos`, `dto/UsuarioDto` — formatos do login, do cadastro e do usuário na resposta.
- `config/SecurityConfig`, `config/JwtService`, `config/JwtAuthenticationFilter`, `config/UsuarioDetailsService` — segurança e token.
- `config/AdminInicial` — cria o primeiro administrador.

**Apoio**
- `controller/ErroHandler` + `config/ApiException` — todo erro volta como `{"erro": "mensagem"}`.
- `config/AppProperties` — lê as configurações do `application.properties`.
- `service/Datas` — ajuda com datas.

Dica para estudar: comece pelo `UploadController` e siga a chamada. Cada arquivo tem no topo um quadro **SIMPLES** dizendo o que é, o que faz e quem chama.

## Deploy
`mvn clean package -DskipTests` e `java -jar target/cti-provedor-api.jar` com as variáveis do `.env` configuradas no serviço.

Passo a passo completo: `../docs/BACKEND.txt`
