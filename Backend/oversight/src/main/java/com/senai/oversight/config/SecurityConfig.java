package com.senai.oversight.config;                       // pacote desta classe
// ============================================================================
// As regras de quem pode entrar em cada rota.
// Libera login, cadastro e health para todos; /api/admin só para ADMIN; o resto exige token. Também liga o CORS (front em outra porta) e o BCrypt.
// Roda sozinho na inicialização.
// ============================================================================

import com.senai.oversight.config.AppProperties;                                   // import: configurações (application.properties)
import java.util.List;                                                              // import: tipo lista
import org.springframework.context.annotation.Bean;                                 // import: @Bean = objeto criado e guardado pelo Spring
import org.springframework.context.annotation.Configuration;                        // import: classe de configuração
import org.springframework.http.HttpMethod;                                         // import: tipos de método HTTP (GET, POST...)
import org.springframework.security.config.Customizer;                              // import: configuração padrão
import org.springframework.security.config.annotation.web.builders.HttpSecurity;    // import: HttpSecurity = onde se define quem acessa o quê
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;  // import: liga o Spring Security
import org.springframework.security.config.http.SessionCreationPolicy;              // import: política de sessão
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;            // import: BCrypt = criptografia de senha
import org.springframework.security.crypto.password.PasswordEncoder;                // import: interface de criptografia de senha
import org.springframework.security.web.SecurityFilterChain;                        // import: cadeia de filtros de segurança
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;  // import: filtro padrão de login (nosso filtro vem antes dele)
import org.springframework.web.cors.CorsConfiguration;                              // import: regras de CORS
import org.springframework.web.cors.CorsConfigurationSource;                        // import: fonte das regras de CORS
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;                // import: associa regras de CORS a rotas

@Configuration                                                                      // classe de configuração do Spring
@EnableWebSecurity                                                                  // liga o Spring Security
public class SecurityConfig {                                                       // SEGURANÇA: define quais rotas são públicas e quais exigem login

    @Bean                                 // @Bean: o Spring guarda o resultado deste método
    public SecurityFilterChain filtros(HttpSecurity http, JwtService jwtService,    // define a cadeia de filtros da segurança
                                       UsuarioDetailsService detailsService) throws Exception {  // recebe o HttpSecurity e os serviços do JWT
        http                                                                        // começa a configurar
            .csrf(csrf -> csrf.disable())                       // API stateless com JWT, sem cookies de sessão
            .cors(Customizer.withDefaults())                                        // libera o CORS (front em outra porta)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // sem sessão no servidor: cada pedido traz o token
            .authorizeHttpRequests(auth -> auth                                     // regras por rota:
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()             // o navegador pergunta antes (OPTIONS): sempre liberado
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/health").permitAll()  // cadastro, login e health são públicos
                .requestMatchers("/api/admin/**").hasRole("ADMIN")                  // /api/admin/** só para ADMIN
                .anyRequest().authenticated())                                      // todo o resto exige login
            .exceptionHandling(e -> e                                               // respostas de erro de segurança:
                .authenticationEntryPoint((req, res, ex) -> escreverErro(res, 401, "Faça login para continuar."))  // sem token: 401
                .accessDeniedHandler((req, res, ex) -> escreverErro(res, 403, "Você não tem permissão para isso.")))  // sem permissão: 403
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, detailsService),  // coloca o nosso filtro JWT antes do filtro padrão
                    UsernamePasswordAuthenticationFilter.class);                    // (continuação)
        return http.build();                                                        // devolve a configuração pronta
    }                                                                               // fecha o bloco

    /** BCrypt: a senha nunca é guardada em texto puro. */
    @Bean                                                                           // bean: guarda o criptografador para o resto do projeto usar
    public PasswordEncoder passwordEncoder() {                                      // método que cria o criptografador
        return new BCryptPasswordEncoder(10);                                       // BCrypt: a senha vira um código que não volta ao original
    }                                                                               // fecha o bloco

    @Bean                                                                           // bean: regras de CORS
    public CorsConfigurationSource corsConfigurationSource(AppProperties props) {   // quais origens (sites) podem chamar a API
        CorsConfiguration cfg = new CorsConfiguration();                            // cria as regras
        cfg.setAllowedOrigins(props.cors().origins().stream().map(String::trim).toList());  // lê os endereços permitidos do application.properties
        cfg.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));  // métodos HTTP permitidos
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));  // cabeçalhos permitidos (inclui o Authorization do token)
        cfg.setMaxAge(3600L);                                                       // o navegador pode lembrar a resposta por 1 hora
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();  // associa as regras às rotas
        source.registerCorsConfiguration("/api/**", cfg);                           // vale para todas as rotas /api/**
        return source;                                                              // devolve as regras
    }                                                                               // fecha o bloco

    private static void escreverErro(jakarta.servlet.http.HttpServletResponse res, int status, String mensagem)  // escreve uma resposta de erro em JSON
            throws java.io.IOException {  // pode lançar erro de entrada/saída
        res.setStatus(status);                                                      // define o código HTTP
        res.setContentType("application/json;charset=UTF-8");                       // diz que a resposta é JSON
        res.getWriter().write("{\"erro\":\"" + mensagem + "\"}");                   // escreve {"erro": "mensagem"}
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
