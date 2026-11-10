package com.senai.oversight.config;                       // pacote desta classe
// ============================================================================
//  Fábrica de token.
// Cria o token JWT no login e confere se um token é válido.
// Quem usa: AuthService (criar) e JwtAuthenticationFilter (conferir).
// ============================================================================

import com.senai.oversight.config.AppProperties;                                   // import: configurações (segredo e validade do token)
import com.senai.oversight.model.Usuario;                                        // import: a entidade Usuario
import io.jsonwebtoken.Claims;                                                      // import: Claims = os dados guardados dentro do token
import io.jsonwebtoken.JwtException;                                                // import: erro de token inválido
import io.jsonwebtoken.Jwts;                                                        // import: Jwts = fábrica de tokens
import io.jsonwebtoken.security.Keys;                                               // import: Keys = cria a chave de assinatura
import java.nio.charset.StandardCharsets;                                           // import: UTF-8 para converter o segredo em bytes
import java.time.Instant;                                                           // import: Instant = momento no tempo
import java.util.Date;                                                              // import: Date = data (formato que a biblioteca pede)
import java.util.Optional;                                                          // import: Optional = resultado que pode não existir
import javax.crypto.SecretKey;                                                      // import: SecretKey = a chave secreta
import org.springframework.stereotype.Service;                                      // import: marca como serviço do Spring

/** Gera e valida o token JWT (HS256). */
@Service                                                                            // serviço do Spring
public class JwtService {                                                           // cria e confere tokens JWT
    private final SecretKey chave;                                                  // a chave que assina o token
    private final long expiracaoSegundos;                                           // quanto tempo o token vale, em segundos

    public JwtService(AppProperties props) {                                        // construtor: lê o segredo do application.properties
        byte[] segredo = props.jwt().secret().getBytes(StandardCharsets.UTF_8);     // transforma o segredo em bytes
        if (segredo.length < 32) {                                                  // segredo curto demais é inseguro
            throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 caracteres.");  // então o servidor nem liga, e avisa o motivo
        }                                                                           // fecha o bloco
        this.chave = Keys.hmacShaKeyFor(segredo);                                   // cria a chave HMAC a partir do segredo
        this.expiracaoSegundos = props.jwt().expirationMinutes() * 60;              // minutos viram segundos
    }                                                                               // fecha o bloco

    public String gerar(Usuario usuario) {                                          // gerar: cria o token do usuário logado
        Instant agora = Instant.now();                                              // momento atual
        return Jwts.builder()                                                       // começa a montar o token
                .subject(usuario.getEmail())                                        // "subject" = e-mail do dono do token
                .claim("perfil", usuario.getPerfil().name())                        // guarda o perfil (USER ou ADMIN) dentro do token
                .issuedAt(Date.from(agora))                                         // quando foi criado
                .expiration(Date.from(agora.plusSeconds(expiracaoSegundos)))        // quando expira
                .signWith(chave)                                                    // assina com a chave secreta (ninguém consegue falsificar)
                .compact();                                                         // gera o texto final do token
    }                                                                               // fecha o bloco

    /** E-mail (subject) do token, ou vazio se o token for inválido ou estiver expirado. */
    public Optional<String> emailDoToken(String token) {                            // emailDoToken: confere o token e devolve o e-mail
        try {                                                                       // tenta ler o token
            Claims claims = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();  // confere a assinatura e a validade; pega os dados
            return Optional.ofNullable(claims.getSubject());                        // devolve o e-mail (subject)
        } catch (JwtException | IllegalArgumentException e) {                       // token falso, adulterado ou vencido
            return Optional.empty();                                                // devolve vazio
        }                                                                           // fecha o bloco
    }                                                                               // fecha o bloco

    public long getExpiracaoSegundos() {                                            // devolve a validade para o front
        return expiracaoSegundos;                                                   // em segundos
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
