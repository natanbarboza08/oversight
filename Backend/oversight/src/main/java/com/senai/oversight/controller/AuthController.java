package com.senai.oversight.controller;                     // pacote desta classe
// ============================================================================
//Porta de entrada do login.
// POST /auth/register (cadastro), POST /auth/login (token), GET /auth/me e GET /health.
// Chama: AuthService.
// ============================================================================

import com.senai.oversight.service.AuthService;                       // import: o serviço AuthService
import com.senai.oversight.dto.AuthDtos.*;                                        // import: os formatos de entrada/saída (CadastroRequest...)
import com.senai.oversight.dto.UsuarioDto;                                     // import: DTO do usuário
import com.senai.oversight.service.UsuarioService;                                 // import: serviço de usuários
import jakarta.validation.Valid;                                                    // import: @Valid ativa as validações do DTO
import java.util.Map;                                                               // import: tipo mapa
import org.springframework.http.HttpStatus;                                         // import: códigos HTTP
import org.springframework.security.core.Authentication;                            // import: usuário logado
import org.springframework.web.bind.annotation.*;                                   // import: anotações do Spring MVC

@RestController                                                                     // controller REST de cadastro e login
@RequestMapping("/api")                                                             // rotas começam com /api
public class AuthController {                                                       // classe do controller
    private final AuthService auth;                                                 // serviço com a regra de cadastro/login
    private final UsuarioService usuarios;                                          // serviço de usuários

    public AuthController(AuthService auth, UsuarioService usuarios) {              // construtor: o Spring entrega os serviços
        this.auth = auth;                                                           // guarda o serviço de autenticação
        this.usuarios = usuarios;                                                   // guarda o serviço de usuários
    }                                                                               // fecha o bloco

    @PostMapping("/auth/register")                                                  // POST /api/auth/register: cria conta
    @ResponseStatus(HttpStatus.CREATED)                                             // responde 201 (criado)
    public MensagemResponse cadastrar(@Valid @RequestBody CadastroRequest req) {    // @Valid confere os campos; @RequestBody lê o JSON
        return auth.cadastrar(req);                                                 // cria o usuário como PENDENTE
    }                                                                               // fecha o bloco

    @PostMapping("/auth/login")                                                     // POST /api/auth/login: entra
    public LoginResponse entrar(@Valid @RequestBody LoginRequest req) {             // lê e valida o JSON { email, senha }
        return auth.entrar(req);                                                    // confere a senha e devolve o token JWT
    }                                                                               // fecha o bloco

    @GetMapping("/auth/me")                                                         // GET /api/auth/me: quem sou eu
    public UsuarioDto eu(Authentication authentication) {                           // authentication vem do token enviado
        return UsuarioDto.de(usuarios.atual(authentication));                       // devolve os dados do usuário logado
    }                                                                               // fecha o bloco

    /** Para testar se o servidor está no ar. */
    @GetMapping("/health")                                                          // GET /api/health
    public Map<String, String> saude() {                                            // método da rota de saúde
        return Map.of("status", "ok");                                              // devolve { "status": "ok" }
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
