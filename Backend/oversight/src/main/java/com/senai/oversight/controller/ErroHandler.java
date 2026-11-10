package com.senai.oversight.controller;                     // pacote desta classe
// ============================================================================
// SIMPLES: O organizador de erros.
// Pega qualquer erro e responde sempre no mesmo formato: {"erro": "mensagem"}.
// Roda sozinho quando algo dá errado.
// ============================================================================

import com.senai.oversight.config.ApiException;                    // import: o erro ApiException
import java.util.LinkedHashMap;                                                     // import: mapa que mantém a ordem
import java.util.Map;                                                               // import: tipo mapa
import org.slf4j.Logger;                                                            // import: Logger = escreve no terminal
import org.slf4j.LoggerFactory;                                                     // import: fábrica de Logger
import org.springframework.http.HttpStatus;                                         // import: códigos HTTP
import org.springframework.http.ResponseEntity;                                     // import: resposta HTTP com corpo
import org.springframework.http.converter.HttpMessageNotReadableException;          // import: erro de JSON ilegível
import org.springframework.web.HttpRequestMethodNotSupportedException;              // import: erro de método HTTP errado
import org.springframework.web.bind.MethodArgumentNotValidException;                // import: erro de validação (@Valid)
import org.springframework.web.bind.MissingServletRequestParameterException;        // import: erro de parâmetro faltando
import org.springframework.web.bind.annotation.ExceptionHandler;                    // import: marca um método que trata um tipo de erro
import org.springframework.web.bind.annotation.RestControllerAdvice;                // import: marca a classe que trata erros de todos os controllers
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;  // import: erro de tipo errado na URL
import org.springframework.web.servlet.resource.NoResourceFoundException;           // import: erro de rota inexistente

/** Padroniza todas as respostas de erro: {"erro": "...", "campos": {...}}. */
@RestControllerAdvice                                                               // captura qualquer erro e responde sempre no mesmo formato
public class ErroHandler {                                               // classe tratadora de erros
    private static final Logger log = LoggerFactory.getLogger(ErroHandler.class);  // logger para registrar erros inesperados

    public record ErroResposta(String erro, Map<String, String> campos) {}          // formato do erro: { "erro": "texto", "campos": {...} }

    private ResponseEntity<ErroResposta> resposta(HttpStatus status, String mensagem, Map<String, String> campos) {  // monta a resposta de erro com código HTTP
        return ResponseEntity.status(status).body(new ErroResposta(mensagem, campos));  // devolve o código e o corpo
    }                                                                               // fecha o bloco

    @ExceptionHandler(ApiException.class)                                           // trata os erros que nós mesmos lançamos
    public ResponseEntity<ErroResposta> api(ApiException e) {                       // método que recebe o erro
        return resposta(e.getStatus(), e.getMessage(), null);                       // usa o código e a mensagem que definimos
    }                                                                               // fecha o bloco

    @ExceptionHandler(MethodArgumentNotValidException.class)                        // trata campos inválidos (@Valid)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException e) {  // método que recebe o erro
        Map<String, String> campos = new LinkedHashMap<>();                         // mapa campo -> mensagem
        e.getBindingResult().getFieldErrors().forEach(fe -> campos.putIfAbsent(fe.getField(), fe.getDefaultMessage()));  // preenche o mapa com cada campo errado
        return resposta(HttpStatus.BAD_REQUEST, "Verifique os campos informados.", campos);  // responde 400 com a lista de campos
    }                                                                               // fecha o bloco

    @ExceptionHandler(MissingServletRequestParameterException.class)                // trata parâmetro obrigatório faltando
    public ResponseEntity<ErroResposta> parametroAusente(MissingServletRequestParameterException e) {  // método que recebe o erro
        return resposta(HttpStatus.BAD_REQUEST, "Falta o parâmetro '" + e.getParameterName() + "'.", null);  // responde 400 dizendo qual parâmetro falta
    }                                                                               // fecha o bloco

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})  // trata JSON ilegível ou tipo errado na URL
    public ResponseEntity<ErroResposta> requisicaoInvalida(Exception e) {           // método que recebe o erro
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida.", null);      // responde 400
    }                                                                               // fecha o bloco

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)                 // trata método HTTP errado (ex.: GET em rota POST)
    public ResponseEntity<ErroResposta> metodo(HttpRequestMethodNotSupportedException e) {  // método que recebe o erro
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido.", null);  // responde 405
    }                                                                               // fecha o bloco

    @ExceptionHandler(NoResourceFoundException.class)                               // trata rota que não existe
    public ResponseEntity<ErroResposta> naoEncontrado(NoResourceFoundException e) {  // método que recebe o erro
        return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado.", null);     // responde 404
    }                                                                               // fecha o bloco

    @ExceptionHandler(Exception.class)                                              // trata qualquer outro erro
    public ResponseEntity<ErroResposta> inesperado(Exception e) {                   // método que recebe o erro
        log.error("Erro inesperado", e);                                            // registra o erro no terminal para o professor ver
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente em instantes.", null);  // responde 500 sem expor detalhes
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
