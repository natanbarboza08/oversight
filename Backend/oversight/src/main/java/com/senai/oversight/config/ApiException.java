package com.senai.oversight.config;                      // pacote desta classe
// ============================================================================
//  O nosso erro.
// Serve para dizer: 'deu problema, status 400/401/422...' com uma mensagem em português.
// Quem usa: os services dão 'throw new ApiException(...)'; o ErroHandler transforma em resposta.
// ============================================================================

import com.senai.oversight.controller.ErroHandler;                    // import: o controller ErroHandler
import org.springframework.http.HttpStatus;                    // import: HttpStatus guarda códigos como 400, 404, 409

/** Erro de negócio com status HTTP; vira {"erro": "..."} no ErroHandler. */
public class ApiException extends RuntimeException {           // erro nosso, com código HTTP e mensagem para o usuário
    private final HttpStatus status;                           // guarda o código HTTP do erro

    public ApiException(HttpStatus status, String mensagem) {  // construtor: recebe o código e a mensagem
        super(mensagem);                                       // passa a mensagem para a classe mãe (RuntimeException)
        this.status = status;                                  // guarda o código recebido
    }                                                          // fecha o bloco

    public HttpStatus getStatus() {                            // getter: quem captura o erro lê o código por aqui
        return status;                                         // devolve o código HTTP
    }                                                          // fecha o bloco
}                                                              // fecha o bloco
