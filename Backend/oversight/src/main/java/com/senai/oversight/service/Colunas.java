package com.senai.oversight.service;                           // pacote desta classe
// ============================================================================
// SIMPLES: Os nomes das 10 colunas da planilha.
// Uma lista de nomes e tamanhos máximos, para não escrever 'codigo_cliente' em vários lugares.
// Usado por: ClienteValidacaoService e UploadService.
// ============================================================================

import java.text.Normalizer;                                                        // import: tira acentos de textos
import java.util.List;                                                              // import: tipo lista
import java.util.Map;                                                               // import: tipo mapa (chave -> valor)

/** Colunas esperadas na aba "upload_clientes" e seus tamanhos máximos (iguais aos do schema.sql). */
public final class Colunas {                                                        // nomes das colunas da planilha e limites de tamanho
    private Colunas() {}                                                            // construtor privado: só usamos as constantes

    public static final String CODIGO = "codigo_cliente";                           // coluna: código do cliente
    public static final String NOME = "nome_cliente";                               // coluna: nome do cliente
    public static final String CONSULTOR = "consultor";                             // coluna: consultor responsável
    public static final String SEGMENTO = "segmento";                               // coluna: segmento (Indústria, Comércio...)
    public static final String NIVEL = "nivel_cliente";                             // coluna: nível A, B ou C
    public static final String FATURAMENTO = "faturamento_anual";                   // coluna: faturamento anual
    public static final String SERVICOS = "servicos_contratados";                   // coluna: serviços separados por ;
    public static final String DATA = "data_contratacao";                           // coluna: data de contratação
    public static final String CIDADE = "cidade";                                   // coluna opcional: cidade
    public static final String UF = "uf";                                           // coluna opcional: UF

    public static final List<String> TODAS =                                        // lista com todas as 10 colunas
            List.of(CODIGO, NOME, CONSULTOR, SEGMENTO, NIVEL, FATURAMENTO, SERVICOS, DATA, CIDADE, UF);  // (continuação) os nomes na ordem da planilha

    /** Sem estas colunas o arquivo inteiro é recusado. cidade e uf são opcionais. */
    public static final List<String> OBRIGATORIAS =                                 // as 8 obrigatórias (cidade e uf ficam de fora)
            List.of(CODIGO, NOME, CONSULTOR, SEGMENTO, NIVEL, FATURAMENTO, SERVICOS, DATA);  // (continuação) lista das obrigatórias

    public static final Map<String, Integer> TAMANHO_MAXIMO = Map.of(               // tamanho máximo de cada texto (igual ao banco)
            CODIGO, 60, NOME, 255, CONSULTOR, 120, SEGMENTO, 80, NIVEL, 20,         // (continuação) código, nome, consultor, segmento, nível
            SERVICOS, 400, CIDADE, 120, UF, 10);                                    // (continuação) serviços, cidade, uf

    /** "Código do Cliente " -> "codigo_do_cliente"; usado para reconhecer o cabeçalho da planilha. */
    public static String normalizarCabecalho(String texto) {                        // limpa o cabeçalho: "Código do Cliente" -> codigo_do_cliente
        if (texto == null) return "";                                               // se for nulo, devolve texto vazio
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");  // separa e apaga os acentos
        return semAcento.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");  // minúsculo e "_" no lugar de espaços e símbolos
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
