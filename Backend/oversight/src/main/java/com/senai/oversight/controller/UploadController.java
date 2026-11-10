package com.senai.oversight.controller;                     // pacote desta classe
// ============================================================================
// Porta de entrada do UPLOAD.
// POST /api/uploads/validadas recebe as linhas do front e entrega ao UploadService.
// Chama: UploadService.
// ============================================================================

import com.senai.oversight.dto.UploadDtos.EnvioValidadasRequest;                // import: formato do JSON de envio
import com.senai.oversight.service.UploadService;                                  // import: serviço de upload
import java.util.Map;                                                               // import: tipo mapa (JSON de resposta)
import org.springframework.http.HttpStatus;                                         // import: códigos HTTP
import org.springframework.web.bind.annotation.*;                                   // import: anotações do Spring MVC

@RestController                                                                     // controller REST de upload
@RequestMapping("/api/uploads")                                                     // rotas começam com /api/uploads
public class UploadController {                                                     // classe do controller
    private final UploadService service;                                            // serviço com a regra de upload

    public UploadController(UploadService service) {                                // construtor: o Spring entrega o serviço
        this.service = service;                                                     // guarda o serviço de upload
    }                                                                               // fecha o bloco

    /** Usado pelo front: recebe SOMENTE as linhas já validadas pelo Pinia (JSON). */
    @PostMapping("/validadas")                                                      // POST /api/uploads/validadas (pode ser testada no Thunder Client)
    @ResponseStatus(HttpStatus.CREATED)                                             // responde 201 (criado)
    public Map<String, Object> enviarValidadas(@RequestBody EnvioValidadasRequest pedido) {  // recebe o JSON com as linhas
        return service.receberValidadas(pedido);                                    // revalida tudo e encaminha ao Python (o Python grava)
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
