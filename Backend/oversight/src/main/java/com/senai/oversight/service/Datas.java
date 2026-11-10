package com.senai.oversight.service;                           // pacote desta classe
// ============================================================================
// SIMPLES: Ajuda com datas.
// Converte a data do banco (UTC) para o formato que o front entende.
// Usado por: AuthService, AdminInicial e UsuarioDto.
// ============================================================================

import java.time.Instant;                                               // import: Instant = um momento no tempo (padrão da API)
import java.time.LocalDateTime;                                         // import: LocalDateTime = data e hora sem fuso (como no banco)
import java.time.ZoneOffset;                                            // import: ZoneOffset = fuso; usamos UTC

/** O banco guarda TIMESTAMP em UTC; na API sempre devolvemos Instant (ISO-8601 com "Z"). */
public final class Datas {                                              // classe utilitária de datas
    private Datas() {}                                                  // construtor privado: ninguém precisa criar objeto desta classe

    public static LocalDateTime agoraUtc() {                            // agoraUtc: devolve a data e hora de agora em UTC
        return LocalDateTime.now(ZoneOffset.UTC);                       // pega o relógio do computador no fuso UTC
    }                                                                   // fecha o bloco

    public static Instant paraInstant(LocalDateTime valor) {            // paraInstant: converte a data do banco para o formato da API
        return valor == null ? null : valor.toInstant(ZoneOffset.UTC);  // se for nula devolve nula; senão converte como UTC
    }                                                                   // fecha o bloco
}                                                                       // fecha o bloco
