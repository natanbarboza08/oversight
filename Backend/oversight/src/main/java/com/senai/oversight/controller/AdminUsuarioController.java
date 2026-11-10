package com.senai.oversight.controller;                     // pacote desta classe
// ============================================================================
// Tela do administrador (porta de entrada HTTP).
// GET lista os usuários e PATCH aprova ou bloqueia um deles.
// Chama: UsuarioService. Só ADMIN acessa.
// ============================================================================

import com.senai.oversight.dto.UsuarioDto;                            // import: o formato de dados UsuarioDto
import com.senai.oversight.model.StatusUsuario;                       // import: a entidade (tabela) StatusUsuario
import com.senai.oversight.config.SecurityConfig;                   // import: a segurança SecurityConfig
import com.senai.oversight.service.UsuarioService;                    // import: o serviço UsuarioService
import jakarta.validation.Valid;                                                    // import: @Valid manda validar o corpo da requisição
import jakarta.validation.constraints.NotNull;                                      // import: valida que o campo não é nulo
import java.util.List;                                                              // import: tipo lista
import org.springframework.security.core.Authentication;                            // import: usuário logado
import org.springframework.web.bind.annotation.*;                                   // import: anotações do Spring MVC (@GetMapping...)

/** Telas de administração: liberar ou bloquear quem se cadastrou. Apenas ADMIN (veja SecurityConfig). */
@RestController                                                                     // controller REST da liberação de acesso
@RequestMapping("/api/admin/usuarios")                                              // rotas começam com /api/admin/usuarios (só ADMIN entra)
public class AdminUsuarioController {                                               // classe do controller
    public record AlterarStatusRequest(@NotNull(message = "Informe o status") StatusUsuario status) {}  // formato do JSON do PATCH: { "status": "APROVADO" }

    private final UsuarioService service;                                           // serviço de usuários

    public AdminUsuarioController(UsuarioService service) {                         // construtor: o Spring entrega o serviço
        this.service = service;                                                     // guarda o serviço
    }                                                                               // fecha o bloco

    @GetMapping                    // rota GET /api/admin/usuarios
    public List<UsuarioDto> listar(@RequestParam(required = false) StatusUsuario status) {  // status é opcional (sem ele lista todos)
        return service.listar(status);                                                // devolve a lista de usuários
    }                                                                                 // fecha o bloco

    @PatchMapping("/{id}/status")  // rota PATCH /api/admin/usuarios/{id}/status
    public UsuarioDto alterarStatus(@PathVariable Long id, @Valid @RequestBody AlterarStatusRequest req,  // PATCH /api/admin/usuarios/{id}/status: aprova ou bloqueia
                                    Authentication authentication) {                // {id} da URL, o JSON validado e quem está logado
        return service.alterarStatus(id, req.status(), service.atual(authentication));  // muda o status; o admin não altera a si mesmo
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
