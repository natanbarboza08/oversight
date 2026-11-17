# Oversight

Aplicação de análise de planilhas de clientes: Vue 3 no navegador, API Java/Spring Boot, serviço ETL em Python/FastAPI e PostgreSQL gerenciado pelo Neon.

## Arquitetura e fluxo

1. Vue lê XLSX/XLS/CSV, valida a planilha, mostra as linhas rejeitadas e envia somente as válidas via Axios.
2. Java revalida os DTOs, autentica com JWT, aplica BCrypt às senhas e envia o lote ao serviço Python com uma chave compartilhada.
3. FastAPI limpa os dados, calcula médias e demais indicadores e grava lote, dados e estatísticas no PostgreSQL.
4. Java lê o histórico e as estatísticas do banco. Vue apresenta relatórios e gráficos Chart.js com dados persistidos.

O cadastro cria usuários `PENDENTE`; o administrador inicial é criado na primeira inicialização e pode aprovar/bloquear usuários pela tela **Usuários**.

## Rodar localmente

Requisitos: Node.js 22+, Java 21, Python 3.12+ e acesso a um PostgreSQL compatível (Neon recomendado).

1. Configure o banco uma vez nos arquivos `Backend/oversight/.env` e `analytics-python/.env`, copiando os respectivos `.env.example`. Use o mesmo host, banco, usuário e senha nos dois serviços, `DB_SSLMODE=require` e gere `JWT_SECRET`, `ADMIN_PASSWORD` e uma `ETL_KEY` aleatória. A mesma chave deve ser usada em `ETL_API_KEY`. Gere segredos com `python -c "import secrets; print(secrets.token_urlsafe(48))"` e guarde-os fora do repositório.
2. Terminal 1:
   ```powershell
   Set-Location analytics-python
   py -m venv .venv
   .\.venv\Scripts\Activate.ps1
   pip install -r requirements.txt
   uvicorn api:app --host 0.0.0.0 --port 8000
   ```
3. Terminal 2:
   ```powershell
   Set-Location Backend\oversight
   .\mvnw.cmd spring-boot:run
   ```
   O Java cria as tabelas idempotentemente na inicialização.
4. Terminal 3:
   ```powershell
   Set-Location Frontend
   Copy-Item .env.example .env
   npm ci
   npm run dev
   ```
5. Acesse a URL exibida pelo Vite. A API Java responde em `http://localhost:8080/api/health`; `/api/ready` e `http://localhost:8000/pronto` confirmam também a conexão com o banco.

## Publicar: Neon + Railway + Vercel

As configurações de build e roteamento estão preparadas, mas a aplicação **ainda não está publicada**. Crie suas contas e configure os segredos diretamente nos painéis dos provedores; não os envie por chat nem os salve no Git.

### 1. Neon

Crie um banco PostgreSQL no Neon. Copie host, nome do banco, usuário e senha da conexão. A conexão deve usar TLS (`sslmode=require`). O esquema é inicializado pelo Java usando `Backend/oversight/src/main/resources/schema.sql`; não é necessário executar o arquivo manualmente.

### 2. Railway: ETL Python

Crie um serviço a partir deste repositório, defina o diretório raiz como `analytics-python` e mantenha o builder Dockerfile. Use o nome `oversight-etl` e coloque os dois serviços no mesmo projeto e ambiente Railway. Configure:

| Variável | Valor |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Dados do banco Neon (`DB_PORT=5432`) |
| `DB_SSLMODE` | `require` |
| `ETL_API_KEY` | Segredo aleatório compartilhado somente com o serviço Java |
| `ENABLE_API_DOCS` | `false` |
| `ENABLE_ETL_TEST_ENDPOINT` | `false` |

O health check `/pronto` confirma a conexão ao banco. Não gere domínio público para o serviço Python. Ele será acessado pelo serviço Java na rede privada Railway em `http://oversight-etl.railway.internal:<PORT>/etl`; substitua `<PORT>` pelo valor `PORT` do serviço Python. O endpoint `/etl` também exige a chave `X-ETL-Key`.

### 3. Railway: backend Java

Crie outro serviço para o mesmo repositório, com diretório raiz `Backend/oversight` e builder Dockerfile. Configure:

| Variável | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://HOST-NEON/NOME-DO-BANCO?sslmode=require` |
| `DB_USER`, `DB_PASSWORD` | Usuário e senha Neon |
| `JWT_SECRET` | Segredo aleatório com pelo menos 32 bytes; diferente da chave ETL |
| `ADMIN_NOME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Conta forte usada para semear o primeiro administrador |
| `ETL_URL` | URL privada `http://oversight-etl.railway.internal:<PORT>/etl` |
| `ETL_KEY` | O mesmo valor definido em `ETL_API_KEY` |
| `CORS_ORIGINS` | Origem exata da aplicação Vercel, sem caminho final |
| `DB_INIT_MODE` | `always` na primeira inicialização |

Gere um domínio público para o serviço Java para a Vercel consumir. O serviço atende a porta fornecida pelo Railway. O health check `/api/ready` verifica a API e o banco. `ADMIN_PASSWORD` é usada somente para criar o administrador na primeira inicialização; alterá-la depois não atualiza a senha já salva. A redefinição por e-mail não está implementada, então proteja a credencial inicial e não use os exemplos dos arquivos `.env.example`.

### 4. Vercel: frontend

Importe o mesmo repositório, defina `Frontend` como diretório raiz e configure `VITE_API_URL` como a URL pública do backend Java + `/api` (por exemplo, `https://api-exemplo.up.railway.app/api`). O build é `npm run build`, saída `dist`; `Frontend/vercel.json` mantém o Vue Router funcionando em rotas diretas.

Após o primeiro deploy, atualize `CORS_ORIGINS` no Railway com a origem real, por exemplo `https://oversight-exemplo.vercel.app`, e redeploy o backend. Não use `*` em produção.

### 5. Verificação de ponta a ponta

1. Abra `/api/ready` do backend e `/pronto` do serviço Python; ambos devem responder com status `ok` e banco `ok`.
2. Cadastre uma conta, entre com o administrador inicial e aprove a conta na tela **Usuários**.
3. Entre com a conta aprovada, envie a planilha de exemplo e confirme a mensagem de processamento.
4. Confira o novo lote em **Relatórios** e os indicadores persistidos em **Gráficos**.

Sem contas e variáveis válidas nos provedores, conexão real com Neon e publicação pública não podem ser verificadas daqui. O build local não substitui esse teste.

## Validações locais

```powershell
Set-Location Frontend
npm ci
npm run build
npm audit --audit-level=high

Set-Location ..\Backend\oversight
.\mvnw.cmd test

Set-Location ..\..\analytics-python
python -m compileall -q .
```

O endpoint analítico `/testar` fica desabilitado por padrão; habilite `ENABLE_ETL_TEST_ENDPOINT=true` somente para desenvolvimento. A recuperação de senha por e-mail também não está habilitada, pois requer provedor de envio e fluxo de tokens; a tela informa essa limitação sem simular um envio.
