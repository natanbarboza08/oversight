# ============================================================================
# api.py — a "porta de entrada" do Python para o DEPLOY: transforma o ETL em uma API (FastAPI).
# O Spring chama POST /etl enviando as linhas válidas; o Python CRIA o lote, limpa, calcula e GRAVA tudo no Azure..
# Como ligar:  uvicorn api:app --port 8000        (documentação automática em http://localhost:8000/docs)
# ============================================================================
import logging                                                                      # logging: escreve mensagens no terminal
import os                                                                           # os: lê variáveis de ambiente (ETL_API_KEY)
from typing import Any                                                              # Any: "qualquer tipo" (valores das linhas)

from fastapi import FastAPI, Header, HTTPException                                  # FastAPI: cria a API; Header: lê cabeçalhos; HTTPException: devolve erro HTTP
import pandas as pd                                                                 # pandas: tabelas de dados
from pydantic import BaseModel                                                      # BaseModel: descreve o formato do JSON que o Spring envia

import config                                                                       # config.py: carrega o .env (senha do banco, ETL_API_KEY)
from analise import calcular                                                        # calcular: indicadores e frases (analise.py)
from limpeza import tratar                                                          # tratar: limpeza (limpeza.py)
from main import processar_lote                                                     # a MESMA função que o modo de estudo usa: trata, calcula e grava no Azure

log = logging.getLogger("analytics")                                                # logger desta API
app = FastAPI(title="CTI Insights — ETL em Python")                                 # app: a aplicação web
_engine = None                                                                      # conexão com o Azure (criada na primeira vez que for usada)


class Pedido(BaseModel):                                                            # Pedido: o formato do JSON enviado pelo Spring
    arquivoNome: str = "planilha"                                                   # nome do arquivo enviado
    recebidas: int                                                                  # total de linhas da planilha (boas + ruins), para o relatório de qualidade
    invalidas: int = 0                                                              # quantas linhas tinham erro (o Spring não as enviou)
    comAviso: int = 0                                                               # quantas linhas válidas têm aviso
    linhas: list[dict[str, Any]]                                                    # só as linhas válidas


def _engine_azure():                                                                # devolve a conexão com o Azure (cria só uma vez)
    global _engine                                                                  # usa a variável de fora da função
    if _engine is None:                                                             # ainda não existe?
        import banco                                                                # banco.py: único arquivo que fala com o Azure
        _engine = banco.criar_engine()                                              # abre a conexão
    return _engine                                                                  # devolve


@app.get("/saude")                                                                  # GET /saude: o Spring (ou você) confere se a API está de pé
def saude() -> dict:                                                                # função chamada nesse endereço
    return {"status": "ok"}                                                         # resposta simples


@app.post("/etl")                                                                   # POST /etl: recebe o lote, processa e grava no Azure
def etl(pedido: Pedido, x_etl_key: str | None = Header(default=None)) -> dict:      # x_etl_key: cabeçalho X-ETL-Key
    chave = os.getenv("ETL_API_KEY", "").strip()                                    # chave combinada com o Spring (vazia = sem chave, só para estudo)
    if chave and x_etl_key != chave:                                                # há chave configurada e a enviada não confere?
        raise HTTPException(status_code=401, detail="Chave do ETL inválida.")       # recusa: 401 não autorizado
    if not pedido.linhas:                                                           # veio vazio?
        raise HTTPException(status_code=422, detail="Nenhuma linha válida foi enviada.")  # 422: dados inválidos
    try:                                                                            # tenta processar
        processar_lote(_engine_azure(), pedido.arquivoNome, pedido.linhas,          # cria o lote, trata, calcula e grava no Azure
                                 pedido.recebidas, pedido.invalidas, pedido.comAviso)  # (continuação) contagens
    except Exception as e:  # noqa: BLE001 - o motivo vai para a resposta
        raise HTTPException(status_code=500, detail=f"Falha no ETL: {e}")           # 500: erro no servidor (o lote fica com status ERRO)
    return {"ok": True}                                                             # 200: deu certo; o Python não devolve dados, só gravou no banco


@app.post("/testar")                                                                # POST /testar: teste sem banco (não grava nada no Azure)
def testar(linhas: list[dict[str, Any]]) -> dict:                                   # recebe só a lista de clientes (como amostras/clientes.json)
    res = tratar(pd.DataFrame(linhas).astype(object))                               # limpa
    return calcular(res, len(linhas))                                               # calcula e devolve os números e as frases
