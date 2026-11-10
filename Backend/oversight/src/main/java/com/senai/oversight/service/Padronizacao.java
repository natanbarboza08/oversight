package com.senai.oversight.service;                           // pacote desta classe
// ============================================================================
// SIMPLES: Ajuda a padronizar textos.
// 'IND.' vira 'Indústria' e 'ana souza' vira 'Ana Souza' (para avisar o usuário).
// Usado por: ClienteValidacaoService.
// ============================================================================

import java.text.Normalizer;                                                        // import: tira acentos
import java.util.Locale;                                                            // import: Locale = idioma (usado para minúsculas)
import java.util.Map;                                                               // import: tipo mapa
import java.util.Set;                                                               // import: tipo conjunto

/** Regras de padronização do contrato (docs/CONTRATO.md). O ETL em Python aplica as mesmas regras. */
public final class Padronizacao {                                                   // padroniza textos (segmento e nome)
    private Padronizacao() {}                                                       // construtor privado: só métodos estáticos

    private static final Map<String, String> SEGMENTOS = Map.ofEntries(             // mapa: texto simplificado -> nome oficial do segmento
            Map.entry("ind", "Indústria"), Map.entry("industria", "Indústria"),     // "ind" e "industria" viram Indústria
            Map.entry("comercio", "Comércio"),                                      // comercio vira Comércio
            Map.entry("servicos", "Serviços"), Map.entry("servico", "Serviços"),    // servicos e servico viram Serviços
            Map.entry("saude", "Saúde"),                                            // saude vira Saúde
            Map.entry("educacao", "Educação"),                                      // educacao vira Educação
            Map.entry("tecnologia", "Tecnologia"));                                 // tecnologia vira Tecnologia

    private static final Set<String> PARTICULAS = Set.of("de", "da", "do", "das", "dos", "e");  // palavras que ficam em minúsculo no meio do nome

    /** Sem acento, minúsculo e sem pontuação: "INDÚSTRIA" -> "industria", "IND." -> "ind". */
    static String simples(String texto) {                                           // simples: tira acento, deixa minúsculo e sem pontuação
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");  // separa o acento da letra e apaga o acento
        return semAcento.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();  // minúsculo, só letras/números, sem espaços nas pontas
    }                                                                               // fecha o bloco

    /** Nome padronizado do segmento ou null se o valor não for reconhecido. */
    public static String segmento(String texto) {                                   // segmento: devolve o nome oficial ou nulo se não existe
        return SEGMENTOS.get(simples(texto));                                       // procura no mapa o texto simplificado
    }                                                                               // fecha o bloco

    /** "ANA SOUZA" -> "Ana Souza"; "maria da silva" -> "Maria da Silva". */
    public static String tituloNome(String texto) {                                 // tituloNome: "ANA SOUZA" vira "Ana Souza"
        String[] palavras = texto.trim().toLowerCase(Locale.ROOT).split("\\s+");    // minúsculo e separado por espaços
        StringBuilder sb = new StringBuilder();                                     // monta o resultado aos poucos
        for (int i = 0; i < palavras.length; i++) {                                 // para cada palavra
            String p = palavras[i];                                                 // palavra atual
            if (p.isEmpty()) continue;                                              // ignora palavras vazias
            if (sb.length() > 0) sb.append(' ');                                    // coloca espaço entre as palavras
            if (i > 0 && PARTICULAS.contains(p)) sb.append(p);                      // "da", "de"... ficam minúsculas (menos a 1ª)
            else sb.append(p.substring(0, 1).toUpperCase(Locale.ROOT)).append(p.substring(1));  // as outras: 1ª letra maiúscula e o resto minúsculo
        }                                                                           // fecha o bloco
        return sb.toString();                                                       // devolve o nome pronto
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
