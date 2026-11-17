# Serviço de análise Python

FastAPI recebe linhas já validadas pelo backend Java, limpa e calcula os indicadores em pandas/SciPy e grava lote, clientes, contratos e resultados no PostgreSQL Neon.

## Desenvolvimento

Copie `.env.example` para `.env`. Configure os dados do Neon (`DB_SSLMODE=require`) e uma `ETL_API_KEY` igual à `ETL_KEY` do Java.

```powershell
py -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn api:app --host 0.0.0.0 --port 8000
```

`GET /saude` é liveness; `GET /pronto` testa também `SELECT 1` no PostgreSQL. O endpoint `POST /etl` exige a chave `X-ETL-Key`. A documentação OpenAPI e a rota local `POST /testar` ficam desabilitadas por padrão.

## Railway

Use `analytics-python` como diretório raiz do serviço. O `Dockerfile` instala as dependências e usa a porta fornecida pelo Railway; `railway.json` configura `/pronto`. Defina as variáveis do `.env.example` no Railway, sem colocar credenciais no repositório.
