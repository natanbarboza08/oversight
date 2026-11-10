package com.senai.oversight.dto;                            // pacote desta classe
// ============================================================================
// O usuário que sai na resposta.
// Mostra nome, e-mail, perfil e status, sem a senha.
// Usado por: AuthService e UsuarioService.
// ============================================================================

import com.senai.oversight.model.Usuario;                             // import: a entidade (tabela) Usuario
import com.senai.oversight.service.Datas;                                           // import: Datas (nosso conversor de data)
import java.time.Instant;                                                           // import: Instant = momento no tempo

public record UsuarioDto(Long id, String nome, String email, String matricula, String turma,  // DTO = o que a API devolve do usuário (sem a senha!)
                         String perfil, String status, Instant criadoEm) {          // campos que o front recebe
    public static UsuarioDto de(Usuario u) {                                        // de: converte a entidade Usuario neste DTO
        return new UsuarioDto(u.getId(), u.getNome(), u.getEmail(), u.getMatricula(), u.getTurma(),  // monta o DTO copiando cada campo
                u.getPerfil().name(), u.getStatus().name(), Datas.paraInstant(u.getCriadoEm()));  // perfil e status viram texto; data vira Instant
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
