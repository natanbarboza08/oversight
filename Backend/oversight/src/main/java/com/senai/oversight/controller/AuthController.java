package com.senai.oversight.controller;

import com.senai.oversight.dto.AuthDtos.CadastroRequest;
import com.senai.oversight.dto.AuthDtos.LoginRequest;
import com.senai.oversight.dto.AuthDtos.LoginResponse;
import com.senai.oversight.dto.AuthDtos.MensagemResponse;
import com.senai.oversight.dto.UsuarioDto;
import com.senai.oversight.service.AuthService;
import com.senai.oversight.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService auth;
    private final UsuarioService usuarios;
    private final JdbcTemplate jdbc;

    public AuthController(AuthService auth, UsuarioService usuarios, JdbcTemplate jdbc) {
        this.auth = auth;
        this.usuarios = usuarios;
        this.jdbc = jdbc;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public MensagemResponse cadastrar(@Valid @RequestBody CadastroRequest req) {
        return auth.cadastrar(req);
    }

    @PostMapping("/auth/login")
    public LoginResponse entrar(@Valid @RequestBody LoginRequest req) {
        return auth.entrar(req);
    }

    @GetMapping("/auth/me")
    public UsuarioDto eu(Authentication authentication) {
        return UsuarioDto.de(usuarios.atual(authentication));
    }

    @GetMapping("/health")
    public Map<String, String> saude() {
        return Map.of("status", "ok");
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> pronto() {
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok(Map.of("status", "ok", "database", "ok"));
        } catch (DataAccessException erro) {
            log.error("Verificação de prontidão do banco falhou.", erro);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "indisponivel", "database", "indisponivel"));
        }
    }
}
