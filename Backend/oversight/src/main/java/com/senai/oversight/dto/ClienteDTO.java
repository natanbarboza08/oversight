package com.senai.oversight.dto;                            // pacote desta classe
// ============================================================================
//  Uma linha da planilha dentro do Java.
// Guarda o número da linha e o texto de cada coluna.
// Usado por: UploadService e ClienteValidacaoService.
// ============================================================================

import java.util.Map;                                                // import: Map = pares chave/valor (coluna -> texto)

/** Uma linha como chegou do front: o número da linha no Excel e os textos de cada coluna. */
public record ClienteDTO(int numero, Map<String, String> campos) {}  // record: número da linha + textos de cada coluna
