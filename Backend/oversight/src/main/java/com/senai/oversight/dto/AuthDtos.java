package com.senai.oversight.dto;                            // pacote desta classe
// ============================================================================
//Formatos do login e do cadastro.
// Dizem quais campos o JSON precisa ter (nome, e-mail, senha...) e como validar cada um.
// Usado por: AuthController e AuthService.
// ============================================================================

import com.senai.oversight.dto.UsuarioDto;                                     // import: DTO do usuário (sem senha)
import jakarta.validation.constraints.Email;                                        // import: valida que o texto é um e-mail
import jakarta.validation.constraints.NotBlank;                                     // import: valida que o texto não está vazio
import jakarta.validation.constraints.Pattern;                                      // import: valida o texto com uma expressão regular
import jakarta.validation.constraints.Size;                                         // import: valida o tamanho máximo do texto

public final class AuthDtos {                                                       // agrupa os formatos de entrada e saída do login/cadastro
    private AuthDtos() {}                                                           // construtor privado: não se cria objeto desta classe

    public record CadastroRequest(                                                  // formato do JSON de cadastro (POST /auth/register)
            @NotBlank(message = "Informe o nome") @Size(max = 120, message = "Máximo de 120 caracteres") String nome,  // nome: obrigatório, até 120 caracteres
            @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail inválido")  // e-mail: obrigatório e com formato válido
            @Size(max = 160, message = "Máximo de 160 caracteres") String email,    // e-mail: até 160 caracteres
            @NotBlank(message = "Informe a senha")                                  // senha: obrigatória
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",                   // senha: 8 a 72 caracteres, com letra e número
                    message = "A senha deve ter de 8 a 72 caracteres, com letras e números") String senha,  // mensagem mostrada se a senha não passar
            @Size(max = 30, message = "Máximo de 30 caracteres") String matricula,  // matrícula: opcional, até 30 caracteres
            @Size(max = 60, message = "Máximo de 60 caracteres") String turma) {}   // turma: opcional, até 60 caracteres

    public record LoginRequest(                                                     // formato do JSON de login (POST /auth/login)
            @NotBlank(message = "Informe o e-mail") String email,                   // e-mail obrigatório
            @NotBlank(message = "Informe a senha") String senha) {}                 // senha obrigatória

    public record LoginResponse(String token, String tipo, long expiraEmSegundos, UsuarioDto usuario) {}  // resposta do login: o token JWT e os dados do usuário

    public record MensagemResponse(String mensagem) {}                              // resposta simples com uma mensagem de texto
}                                                                                   // fecha o bloco
