package com.senai.oversight.service;                        // pacote desta classe
// ============================================================================
// SIMPLES: As regras do cadastro e do login.
// Cadastro: criptografa a senha (BCrypt) e cria PENDENTE. Login: confere a senha, o status e devolve o token.
// Chamado por: AuthController.
// ============================================================================

import com.senai.oversight.dto.UsuarioDto;                            // import: o formato de dados UsuarioDto
import com.senai.oversight.model.PerfilUsuario;                       // import: a entidade (tabela) PerfilUsuario
import com.senai.oversight.model.StatusUsuario;                       // import: a entidade (tabela) StatusUsuario
import com.senai.oversight.model.Usuario;                             // import: a entidade (tabela) Usuario
import com.senai.oversight.repository.UsuarioRepository;              // import: o repositório (acesso ao banco) UsuarioRepository
import com.senai.oversight.dto.AuthDtos.*;                                        // import: os formatos de entrada e saída
import com.senai.oversight.config.ApiException;                                    // import: nosso erro com código HTTP
import com.senai.oversight.service.Datas;                                           // import: nosso conversor de datas
import com.senai.oversight.config.JwtService;                                    // import: serviço que cria o token
import java.nio.charset.StandardCharsets;                                           // import: UTF-8 para medir o tamanho da senha
import org.springframework.http.HttpStatus;                                         // import: códigos HTTP
import org.springframework.security.crypto.password.PasswordEncoder;                // import: criptografador de senha
import org.springframework.stereotype.Service;                                      // import: marca como serviço do Spring

@Service                                                                            // serviço do Spring
public class AuthService {                                                          // regras de cadastro e login
    private final UsuarioRepository repo;                                           // repositório de usuários
    private final PasswordEncoder encoder;                                          // criptografador BCrypt
    private final JwtService jwt;                                                   // criador de tokens JWT

    public AuthService(UsuarioRepository repo, PasswordEncoder encoder, JwtService jwt) {  // construtor: o Spring entrega as três peças
        this.repo = repo;                                                           // guarda o repositório
        this.encoder = encoder;                                                     // guarda o criptografador
        this.jwt = jwt;                                                             // guarda o serviço de token
    }                                                                               // fecha o bloco

    /** Cadastro de usuário (perfil USER): nasce PENDENTE e só entra depois que um ADMIN aprovar. */
    public MensagemResponse cadastrar(CadastroRequest req) {                        // cadastrar: cria a conta como PENDENTE
        String email = req.email().trim().toLowerCase();                            // e-mail em minúsculo e sem espaços
        if (req.senha().getBytes(StandardCharsets.UTF_8).length > 72) {             // o BCrypt só aceita até 72 bytes
            throw new ApiException(HttpStatus.BAD_REQUEST, "A senha é longa demais (limite do BCrypt: 72 bytes).");  // senha longa demais: erro 400
        }                                                                           // fecha o bloco
        if (repo.existsByEmailIgnoreCase(email)) {                                  // e-mail já cadastrado?
            throw new ApiException(HttpStatus.CONFLICT, "Já existe um cadastro com este e-mail.");  // então erro 409 (conflito)
        }                                                                           // fecha o bloco
        Usuario u = new Usuario();                                                  // cria o objeto usuário
        u.setNome(req.nome().trim());                                               // nome sem espaços nas pontas
        u.setEmail(email);                                                          // e-mail
        u.setSenhaHash(encoder.encode(req.senha()));                                // guarda a senha CRIPTOGRAFADA (BCrypt), nunca pura
        u.setMatricula(vazioParaNulo(req.matricula()));                             // matrícula (ou nulo se vazia)
        u.setTurma(vazioParaNulo(req.turma()));                                     // turma (ou nulo se vazia)
        u.setPerfil(PerfilUsuario.USER);                                            // todo cadastro novo é USER
        u.setStatus(StatusUsuario.PENDENTE);                                        // e nasce PENDENTE até o admin liberar
        u.setCriadoEm(Datas.agoraUtc());                                            // data do cadastro
        repo.save(u);                                                               // salva no banco PostgreSQL
        return new MensagemResponse("Cadastro recebido! Você poderá entrar assim que o administrador liberar o seu acesso.");  // devolve a mensagem para a tela
    }                                                                               // fecha o bloco

    public LoginResponse entrar(LoginRequest req) {                                 // entrar: confere e-mail e senha e devolve o token
        Usuario u = repo.findByEmailIgnoreCase(req.email().trim()).orElse(null);    // busca o usuário (ou nulo se não existe)
        // A senha é conferida antes do status, para não revelar quais e-mails existem.
        if (u == null || !encoder.matches(req.senha(), u.getSenhaHash())) {         // e-mail inexistente ou senha errada
            throw new ApiException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");  // mesma mensagem nos dois casos (não revela quem existe)
        }                                                                           // fecha o bloco
        if (u.getStatus() == StatusUsuario.PENDENTE) {                              // conta ainda PENDENTE?
            throw new ApiException(HttpStatus.FORBIDDEN, "Seu cadastro ainda aguarda a liberação do administrador.");  // erro 403: aguarde a liberação
        }                                                                           // fecha o bloco
        if (u.getStatus() == StatusUsuario.BLOQUEADO) {                             // conta BLOQUEADA?
            throw new ApiException(HttpStatus.FORBIDDEN, "Seu acesso está bloqueado. Fale com o administrador.");  // erro 403: acesso bloqueado
        }                                                                           // fecha o bloco
        return new LoginResponse(jwt.gerar(u), "Bearer", jwt.getExpiracaoSegundos(), UsuarioDto.de(u));  // tudo certo: devolve token + dados do usuário
    }                                                                               // fecha o bloco

    private static String vazioParaNulo(String s) {                                 // devolve nulo se o texto for vazio
        return s == null || s.isBlank() ? null : s.trim();                          // senão devolve o texto sem espaços nas pontas
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
