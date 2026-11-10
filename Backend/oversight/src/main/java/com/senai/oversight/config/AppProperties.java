package com.senai.oversight.config;                         // pacote desta classe
// ============================================================================
// Lê as configurações do application.properties.
// Guarda numa classe os valores de app.jwt, app.cors, app.admin, app.upload e app.etl.
// Quem usa: JwtService, SecurityConfig, UploadService, PythonEtlService.
// ============================================================================

import java.util.List;                                                              // import: tipo lista (List) do Java
import org.springframework.boot.context.properties.ConfigurationProperties;         // import: anotação que liga a classe ao application.properties

/** Propriedades da seção "app" do application.properties. */
@ConfigurationProperties(prefix = "app")                                            // lê tudo que está em "app:" no application.properties
public record AppProperties(Jwt jwt, Cors cors, Admin admin, Upload upload, Etl etl) {  // record = classe só de dados; cada parte é uma seção do yml
    public record Jwt(String secret, long expirationMinutes) {}                     // jwt: segredo que assina o token e quanto tempo ele vale
    public record Cors(List<String> origins) {}                                     // cors: quais endereços de front podem chamar a API
    public record Admin(String nome, String email, String senha) {}                 // admin: dados do administrador criado na 1ª vez
    public record Upload(int maxLinhas) {}                                          // upload: máximo de linhas aceitas por envio
    /** url do serviço Python (API FastAPI) e a chave combinada com ele (vazia = sem chave, só para estudo). */
    public record Etl(String url, String key) {}                                    // etl: endereço da API do Python e a chave
}                                                                                   // fecha o bloco
