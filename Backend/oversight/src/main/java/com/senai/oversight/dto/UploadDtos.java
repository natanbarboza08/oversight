package com.senai.oversight.dto;                            // pacote desta classe
// ============================================================================
//  Formato do JSON do upload.
// EnvioValidadasRequest = o pacote que o front envia; LinhaEnvio = uma linha dele.
// Usado por: UploadController e UploadService.
// ============================================================================

import java.util.List;                                                              // import: tipo lista

public final class UploadDtos {                                                     // DTOs = formatos de JSON que entram e saem da API
    private UploadDtos() {}                                                         // construtor privado: só agrupa os formatos

    /** Corpo do POST /api/uploads/validadas: só as linhas que o Pinia aprovou no navegador. */
    public record EnvioValidadasRequest(String arquivoNome, Integer descartadas, List<LinhaEnvio> linhas) {}  // JSON que o front envia: nome do arquivo, quantas linhas descartou e as linhas válidas

    /** Uma linha já conferida pelo front. */
    public record LinhaEnvio(Integer numeroLinha, String codigoCliente, String nomeCliente, String consultor,  // uma linha já conferida pelo front (todos os campos como texto)
                             String segmento, String nivelCliente, String faturamentoAnual,  // (continuação) segmento, nível, faturamento
                             String servicosContratados, String dataContratacao, String cidade, String uf) {}  // (continuação) serviços, data, cidade, uf
}                                                                                   // fecha o bloco
