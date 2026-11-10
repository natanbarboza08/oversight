package com.senai.oversight.service;                        // pacote desta classe
// ============================================================================
// SIMPLES: O CÉREBRO do upload.
// Confere o limite de linhas, chama o validador, separa válidas e rejeitadas e envia só as válidas ao Python.
// Chamado por: UploadController. Devolve ao front as contagens e as rejeitadas.
// ============================================================================

import com.senai.oversight.dto.ClienteDTO;                            // import: o formato de dados ClienteDTO
import static com.senai.oversight.service.Colunas.*;                                // import estático: usa CODIGO, NOME... direto, sem escrever Colunas.

import com.senai.oversight.config.ApiException;                                    // import: nosso erro com código HTTP
import com.senai.oversight.config.AppProperties;                                   // import: configurações (limite de linhas)
import com.senai.oversight.dto.UploadDtos.EnvioValidadasRequest;                // import: formato do JSON que o front envia
import com.senai.oversight.dto.UploadDtos.LinhaEnvio;                           // import: formato de uma linha enviada
import com.senai.oversight.service.ClienteValidacaoService.ResultadoLinha;         // import: resultado da validação de uma linha
import java.util.ArrayList;                                                         // import: lista que cresce
import java.util.LinkedHashMap;                                                     // import: mapa que mantém a ordem
import java.util.List;                                                              // import: tipo lista
import com.fasterxml.jackson.databind.JsonNode;                                     // import: JsonNode = um JSON já lido
import java.util.Map;                                                               // import: tipo mapa
import org.springframework.http.HttpStatus;                                         // import: códigos HTTP
import org.springframework.stereotype.Service;                                      // import: marca como serviço do Spring

@Service                                                                            // serviço do Spring
public class UploadService {                                                        // REGRA DO UPLOAD: valida e ENCAMINHA ao Python (o Java não grava nada)
    private final ClienteValidacaoService validacao;                               // validador (as mesmas regras do front)
    private final AppProperties props;                                              // configurações
    private final PythonEtlService python;                                          // envia os dados ao Python

    public UploadService(ClienteValidacaoService validacao, AppProperties props, PythonEtlService python) {  // construtor: o Spring entrega as peças
        this.validacao = validacao;                                                 // guarda o validador
        this.props = props;                                                         // guarda as configurações
        this.python = python;                                                       // guarda o enviador ao Python
    }                                                                               // fecha o bloco

    /**
     * Recebe SÓ as linhas que o Pinia já validou no navegador (as inválidas nem saem do front).
     * Mesmo assim o Spring confere tudo de novo: nunca se confia apenas no navegador.
     * "descartadas" é quantas linhas o front deixou de fora; entra na contagem do lote para o relatório.
     */
    public Map<String, Object> receberValidadas(EnvioValidadasRequest req) {   // receberValidadas: recebe o JSON do front (ou do Thunder Client)
        if (req == null || req.linhas() == null || req.linhas().isEmpty()) {        // sem linhas?
            throw new ApiException(HttpStatus.BAD_REQUEST, "Não há linhas validadas para enviar.");  // erro 400
        }                                                                           // fecha o bloco
        if (req.linhas().size() > props.upload().maxLinhas()) {                     // mais linhas que o limite?
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,                 // erro 422
                    "A planilha passou do limite de " + props.upload().maxLinhas() + " linhas.");  // (continuação da mensagem)
        }                                                                           // fecha o bloco
        String nome = nomeSeguro(req.arquivoNome());                                // nome do arquivo sem caracteres perigosos
        int descartadas = req.descartadas() == null ? 0 : Math.max(0, req.descartadas());  // quantas linhas o front deixou de fora (nunca negativo)

        List<ClienteDTO> brutas = new ArrayList<>(req.linhas().size());             // lista onde vamos colocar as linhas no formato do validador
        int posicao = 2; // a linha 1 da planilha é o cabeçalho
        for (LinhaEnvio e : req.linhas()) {                                         // para cada linha recebida
            Map<String, String> campos = new LinkedHashMap<>();                     // mapa coluna -> texto
            campos.put(CODIGO, e.codigoCliente());                                  // código do cliente
            campos.put(NOME, e.nomeCliente());                                      // nome do cliente
            campos.put(CONSULTOR, e.consultor());                                   // consultor
            campos.put(SEGMENTO, e.segmento());                                     // segmento
            campos.put(NIVEL, e.nivelCliente());                                    // nível
            campos.put(FATURAMENTO, e.faturamentoAnual());                          // faturamento
            campos.put(SERVICOS, e.servicosContratados());                          // serviços
            campos.put(DATA, e.dataContratacao());                                  // data
            campos.put(CIDADE, e.cidade());                                         // cidade
            campos.put(UF, e.uf());                                                 // uf
            int numero = e.numeroLinha() == null ? posicao : e.numeroLinha();       // usa o número da linha enviado (ou calcula)
            brutas.add(new ClienteDTO(numero, campos));                             // guarda a linha na lista
            posicao++;                                                              // próxima posição
        }                                                                           // fecha o bloco
        return registrar(nome, brutas, descartadas);                       // validar de novo e enviar ao Python (o Python grava)
    }                                                                               // fecha o bloco

    /** Valida de novo e ENCAMINHA as linhas válidas ao Python. Linhas com erro só voltam na resposta ("rejeitadas"). */
    private Map<String, Object> registrar(String nome, List<ClienteDTO> brutas, int descartadas) {  // registrar: valida de novo e envia
        List<ResultadoLinha> resultado = validacao.validar(brutas);                 // VALIDAÇÃO NO BACKEND: roda as regras em cada linha

        List<Map<String, Object>> validas = new ArrayList<>();                      // linhas válidas: são as que vão ao Python
        List<String> rejeitadas = new ArrayList<>();                                // linhas com erro: só informadas ao usuário
        int comAviso = 0;                                                           // quantas linhas válidas têm aviso
        for (ResultadoLinha r : resultado) {                                        // para cada resultado da validação
            if (!r.valida()) {                                                      // linha com erro?
                rejeitadas.add("Linha " + r.numero() + ": " + String.join(" | ", r.erros()));  // guarda a mensagem para o usuário
                continue;                                                           // não segue para o Python
            }                                                                       // fecha o bloco
            if (!r.avisos().isEmpty()) comAviso++;                                  // conta o aviso
            validas.add(paraPython(r));                                             // monta a linha no formato do Python
        }                                                                           // fecha o bloco
        if (validas.isEmpty()) {                                                    // nenhuma linha válida?
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,                 // erro 422: nada para enviar
                    "Nenhuma linha válida. Corrija a planilha e envie novamente.");  // (continuação da mensagem)
        }                                                                           // fecha o bloco

        int recebidas = resultado.size() + descartadas;                             // total de linhas da planilha (boas + ruins)
        int invalidas = rejeitadas.size() + descartadas;                            // total de inválidas (as do Spring + as do front)
        python.enviar(nome, recebidas, invalidas, comAviso, validas);               // ENCAMINHA ao Python, que cria o lote, calcula e grava (não devolve dados)
        Map<String, Object> saida = new LinkedHashMap<>();                          // resposta ao front
        saida.put("arquivoNome", nome);                                             // nome do arquivo
        saida.put("status", "PROCESSADO");                                          // o Python respondeu 200: gravou
        saida.put("totalLinhas", recebidas);                                        // total de linhas
        saida.put("linhasValidas", validas.size());                                 // quantas seguiram ao Python
        saida.put("linhasInvalidas", invalidas);                                    // quantas não seguiram
        saida.put("rejeitadas", rejeitadas);                                        // motivos das linhas com erro (só informados, não gravados)
        return saida;                                                               // devolve ao controller
    }                                                                               // fecha o bloco

    private Map<String, Object> paraPython(ResultadoLinha r) {                    // converte uma linha válida no JSON que o Python espera
        Map<String, String> v = r.valores();                                        // valores já limpos
        Map<String, Object> m = new LinkedHashMap<>();                              // mapa campo -> valor (mantém a ordem)
        m.put("linha", r.numero());                                                 // número da linha no Excel
        m.put("codigo_cliente", vazioParaNulo(v.get(CODIGO)));                      // código do cliente
        m.put("nome_cliente", vazioParaNulo(v.get(NOME)));                          // nome do cliente
        m.put("consultor", vazioParaNulo(v.get(CONSULTOR)));                        // consultor
        m.put("segmento", vazioParaNulo(v.get(SEGMENTO)));                          // segmento
        m.put("nivel_cliente", vazioParaNulo(v.get(NIVEL)));                        // nível A, B ou C
        m.put("faturamento_anual", vazioParaNulo(v.get(FATURAMENTO)));              // faturamento (texto "1850000.00")
        m.put("servicos_contratados", vazioParaNulo(v.get(SERVICOS)));              // serviços separados por ;
        m.put("data_contratacao", vazioParaNulo(v.get(DATA)));                      // data aaaa-mm-dd
        m.put("cidade", vazioParaNulo(v.get(CIDADE)));                              // cidade
        m.put("uf", vazioParaNulo(v.get(UF)));                                      // UF
        return m;                                                                   // devolve a linha pronta
    }                                                                               // fecha o bloco

    private static String vazioParaNulo(String s) {                                 // vazioParaNulo: texto vazio vira nulo no banco
        return s == null || s.isEmpty() ? null : s;                                 // se é nulo ou vazio, devolve nulo
    }                                                                               // fecha o bloco

    /** Remove caminho e caracteres estranhos do nome enviado pelo navegador. */
    private static String nomeSeguro(String original) {                             // nomeSeguro: limpa o nome do arquivo
        String nome = original == null ? "planilha" : original.replace('\\', '/');  // troca "\" por "/" para tratar caminhos do Windows
        nome = nome.substring(nome.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").strip();  // fica só com o nome, sem pastas nem caracteres de controle
        if (nome.isEmpty()) nome = "planilha";                                      // se sobrou nada, usa "planilha"
        return nome.length() > 255 ? nome.substring(nome.length() - 255) : nome;    // limita a 255 caracteres
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
