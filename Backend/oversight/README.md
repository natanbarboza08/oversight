# Backend Java

API Spring Boot 3.5 / Java 21. Ela implementa cadastro e login JWT, BCrypt, autorização, DTOs com validação, revalidação dos uploads, chamada autenticada ao FastAPI e leitura de dados persistidos para o dashboard.

## Execução local

Copie `.env.example` para `.env`, configure a conexão PostgreSQL, `JWT_SECRET` (mínimo 32 bytes), `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ETL_URL` e uma `ETL_KEY` que corresponda a `ETL_API_KEY` no serviço Python. A aplicação importa o arquivo `.env` local automaticamente.

```powershell
.\mvnw.cmd spring-boot:run
```

O banco precisa estar acessível; o esquema em `src/main/resources/schema.sql` é inicializado pelo Spring. O primeiro usuário ADMIN é criado na inicialização. Não existe senha padrão de administrador. `/api/health` verifica a API e `/api/ready` também executa `SELECT 1` no PostgreSQL.

## Rotas principais

- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`
- `GET /api/admin/usuarios`, `PATCH /api/admin/usuarios/{id}/status` (ADMIN)
- `POST /api/uploads/validadas` (autenticada)
- `GET /api/dashboard`, `GET /api/dashboard/lotes` (autenticadas)
- `GET /api/health`

## Railway

Use `Backend/oversight` como diretório raiz do serviço. O `Dockerfile` e `railway.json` fazem o build Java 21 e configuram o health check `/api/ready`. Defina no Railway as variáveis do `.env.example`, a URL pública da API Python em `ETL_URL`, a origem Vercel em `CORS_ORIGINS` e os valores Neon em `DB_URL`, `DB_USER` e `DB_PASSWORD`. Nunca envie arquivos `.env` ao repositório.
