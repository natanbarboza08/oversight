package com.senai.oversight.config;                       // pacote desta classe
// ============================================================================
// O porteiro do token.
// Em TODA requisição, lê o token do cabeçalho Authorization e diz ao Spring quem é o usuário.
// Roda sozinho antes dos controllers.
// ============================================================================

import jakarta.servlet.FilterChain;                                                 // import: a cadeia de filtros (o próximo filtro)
import jakarta.servlet.ServletException;                                            // import: erro de servlet
import jakarta.servlet.http.HttpServletRequest;                                     // import: o pedido HTTP que chegou
import jakarta.servlet.http.HttpServletResponse;                                    // import: a resposta HTTP
import java.io.IOException;                                                         // import: erro de entrada/saída
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;  // import: objeto "usuário autenticado" do Spring Security
import org.springframework.security.core.context.SecurityContextHolder;             // import: onde o Security guarda quem está logado neste pedido
import org.springframework.security.core.userdetails.UserDetails;                   // import: dados do usuário
import org.springframework.security.core.userdetails.UsernameNotFoundException;     // import: erro de usuário inexistente
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;  // import: anexa IP e sessão ao usuário autenticado
import org.springframework.web.filter.OncePerRequestFilter;                         // import: filtro que roda uma vez por pedido

/** Lê "Authorization: Bearer <token>", valida e autentica a requisição. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {                 // FILTRO: roda antes de cada rota e confere o token JWT
    private final JwtService jwtService;                                            // serviço que lê e valida o token
    private final UsuarioDetailsService detailsService;                             // serviço que busca o usuário no banco

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioDetailsService detailsService) {  // construtor: o Spring entrega os dois serviços
        this.jwtService = jwtService;                                               // guarda o serviço do JWT
        this.detailsService = detailsService;                                       // guarda o serviço de usuários
    }                                                                               // fecha o bloco

    @Override                                       // sobrescreve o método da classe mãe
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)  // método que roda a cada pedido
            throws ServletException, IOException {  // pode lançar erros de servlet ou de entrada/saída
        String cabecalho = request.getHeader("Authorization");                      // lê o cabeçalho Authorization do pedido
        if (cabecalho != null && cabecalho.startsWith("Bearer ")                    // veio no formato "Bearer <token>"?
                && SecurityContextHolder.getContext().getAuthentication() == null) {  // e ainda não há ninguém autenticado neste pedido?
            String token = cabecalho.substring(7).trim();                           // tira o "Bearer " e fica só com o token
            jwtService.emailDoToken(token).ifPresent(email -> {                     // se o token é válido, pega o e-mail que está dentro dele
                try {                               // tenta carregar o usuário
                    UserDetails usuario = detailsService.loadUserByUsername(email);  // busca o usuário no banco pelo e-mail
                    // Se o admin bloquear a conta, o token antigo deixa de valer na hora.
                    if (usuario.isEnabled()) {                                      // conta bloqueada ou pendente não passa
                        var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());  // cria o "usuário autenticado" com seus papéis (ROLE_...)
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));  // anexa detalhes do pedido
                        SecurityContextHolder.getContext().setAuthentication(auth);  // marca o pedido como autenticado
                    }                                                               // fecha o bloco
                } catch (UsernameNotFoundException ignorado) {                      // usuário apagado do banco
                    // token de usuário que não existe mais: segue sem autenticar (vai dar 401)
                }                                                                   // fecha o bloco
            });                                                                     // fecha o bloco
        }                                                                           // fecha o bloco
        chain.doFilter(request, response);                                          // passa o pedido para o próximo filtro / a rota
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
