package com.senai.oversight.service;                        // pacote desta classe
// ============================================================================
// SIMPLES: LÊ DO BANCO (Neon ou Azure) o que o Python gravou, para os gráficos.
// Tabelas lidas: upload_lote (o lote e o status) e estatistica_lote (o JSON com as contas).
// O Java só LÊ: quem grava essas tabelas é o Python (banco.py).
// Chamado por: DashboardController.
// ============================================================================

import com.senai.oversight.config.ApiException;                                     // import: nosso erro com código HTTP
import com.fasterxml.jackson.core.JsonProcessingException;                         // import: erro ao ler JSON
import com.fasterxml.jackson.databind.JsonNode;                                    // import: JSON já lido (árvore de campos)
import com.fasterxml.jackson.databind.ObjectMapper;                                // import: transforma texto em JSON
import java.sql.ResultSet;                                                         // import: uma linha do resultado do SQL
import java.sql.SQLException;                                                      // import: erro do banco
import java.sql.Timestamp;                                                         // import: data e hora vinda do banco
import java.util.LinkedHashMap;                                                    // import: mapa que mantém a ordem dos campos
import java.util.List;                                                             // import: tipo lista
import java.util.Map;                                                              // import: tipo mapa (chave -> valor)
import org.springframework.http.HttpStatus;                                        // import: códigos HTTP
import org.springframework.jdbc.core.JdbcTemplate;                                 // import: executa SQL de forma simples
import org.springframework.stereotype.Service;                                     // import: marca como serviço do Spring

@Service                                                                           // serviço do Spring
public class DashboardService {                                                    // LÊ OS DADOS DOS GRÁFICOS

    // Colunas do lote que a tela mostra. Usado em todas as consultas abaixo.
    private static final String SELECT_LOTE = "SELECT id, arquivo_nome, enviado_em, status, total_linhas, "
            + "linhas_validas, linhas_invalidas, linhas_com_aviso, mensagem FROM upload_lote ";  // começo do SQL do lote

    private final JdbcTemplate jdbc;                                               // executa os SELECT no banco
    private final ObjectMapper json;                                               // lê o texto JSON gravado pelo Python

    public DashboardService(JdbcTemplate jdbc, ObjectMapper json) {               // construtor: o Spring entrega as peças
        this.jdbc = jdbc;                                                          // guarda o executor de SQL
        this.json = json;                                                          // guarda o leitor de JSON
    }                                                                              // fecha o bloco

    /** Devolve { lote, geradoEm, estatisticas }. Sem loteId: o último lote PROCESSADO (ou o último enviado). */
    public Map<String, Object> obter(Long loteId) {                                // obter: monta a resposta do dashboard
        List<Map<String, Object>> achados;                                         // lotes encontrados (0 ou 1)
        if (loteId != null) {                                                      // a tela pediu um lote específico?
            achados = jdbc.query(SELECT_LOTE + "WHERE id = ?", this::lerLote, loteId);  // busca esse lote
        } else {                                                                   // não pediu: escolhe o mais recente
            achados = jdbc.query(SELECT_LOTE + "WHERE status = 'PROCESSADO' ORDER BY id DESC LIMIT 1", this::lerLote);  // último que deu certo
            if (achados.isEmpty()) {                                               // nenhum deu certo ainda?
                achados = jdbc.query(SELECT_LOTE + "ORDER BY id DESC LIMIT 1", this::lerLote);  // pega o último enviado (pode estar PROCESSANDO ou ERRO)
            }                                                                      // fecha o bloco
        }                                                                          // fecha o bloco
        if (achados.isEmpty()) {                                                   // não existe nenhum lote?
            throw new ApiException(HttpStatus.NOT_FOUND, loteId == null            // erro 404 para a tela
                    ? "Nenhuma planilha foi processada ainda. Envie uma planilha para ver os gráficos."  // banco vazio
                    : "Lote " + loteId + " não encontrado.");                      // id que não existe
        }                                                                          // fecha o bloco
        Map<String, Object> lote = achados.get(0);                                 // o lote escolhido

        Map<String, Object> saida = new LinkedHashMap<>();                         // resposta para o front
        saida.put("lote", lote);                                                   // dados do lote (status, totais)
        saida.put("geradoEm", null);                                               // quando o Python calculou (null = ainda não)
        saida.put("estatisticas", null);                                           // JSON das contas (null = ainda não)

        List<Map<String, Object>> contas = jdbc.query(                             // busca o JSON que o Python gravou
                "SELECT gerado_em, resultado FROM estatistica_lote WHERE lote_id = ?",  // SQL: o resultado desse lote
                (rs, n) -> {                                                       // para cada linha encontrada:
                    Map<String, Object> m = new LinkedHashMap<>();                 // um mapinha
                    m.put("geradoEm", data(rs.getTimestamp("gerado_em")));          // data e hora do cálculo
                    m.put("resultado", rs.getString("resultado"));                  // o JSON como texto
                    return m;                                                      // devolve
                }, lote.get("id"));                                                // valor do "?": o id do lote
        if (!contas.isEmpty()) {                                                   // o Python já gravou as contas?
            saida.put("geradoEm", contas.get(0).get("geradoEm"));                  // guarda quando foi gerado
            saida.put("estatisticas", lerJson((String) contas.get(0).get("resultado")));  // texto vira JSON de verdade
        }                                                                          // fecha o bloco
        return saida;                                                              // devolve ao controller
    }                                                                              // fecha o bloco

    /** Os últimos 20 lotes, do mais novo para o mais antigo (para a caixa de escolha da tela). */
    public List<Map<String, Object>> listarLotes() {                               // listarLotes: histórico de envios
        return jdbc.query(SELECT_LOTE + "ORDER BY id DESC LIMIT 20", this::lerLote);  // SQL: os 20 mais recentes
    }                                                                              // fecha o bloco

    // Transforma UMA linha da tabela upload_lote num mapa com nomes no padrão do JavaScript (camelCase).
    private Map<String, Object> lerLote(ResultSet rs, int numeroLinha) throws SQLException {  // lerLote: linha do banco -> mapa
        Map<String, Object> m = new LinkedHashMap<>();                             // mapa campo -> valor
        m.put("id", rs.getLong("id"));                                             // id do lote
        m.put("arquivoNome", rs.getString("arquivo_nome"));                        // nome do arquivo
        m.put("enviadoEm", data(rs.getTimestamp("enviado_em")));                    // quando foi enviado
        m.put("status", rs.getString("status"));                                   // PROCESSANDO, PROCESSADO ou ERRO
        m.put("totalLinhas", rs.getInt("total_linhas"));                           // linhas da planilha
        m.put("linhasValidas", rs.getInt("linhas_validas"));                       // linhas boas
        m.put("linhasInvalidas", rs.getInt("linhas_invalidas"));                   // linhas com erro
        m.put("linhasComAviso", rs.getInt("linhas_com_aviso"));                    // linhas com aviso
        m.put("mensagem", rs.getString("mensagem"));                               // mensagem do Python (ex.: motivo do ERRO)
        return m;                                                                  // devolve o mapa
    }                                                                              // fecha o bloco

    private static Object data(Timestamp t) {                                      // data: Timestamp do banco -> data/hora do Java
        return t == null ? null : t.toLocalDateTime();                             // vazio continua vazio
    }                                                                              // fecha o bloco

    private JsonNode lerJson(String texto) {                                       // lerJson: texto -> JSON
        try {                                                                      // tenta ler
            return json.readTree(texto);                                           // lê o texto como JSON
        } catch (JsonProcessingException e) {                                      // texto quebrado
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,               // erro 500
                    "O resultado gravado pelo Python não é um JSON válido.");      // (continuação da mensagem)
        }                                                                          // fecha o bloco
    }                                                                              // fecha o bloco
}                                                                                  // fecha o bloco
