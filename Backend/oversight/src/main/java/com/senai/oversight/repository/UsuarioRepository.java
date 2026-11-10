package com.senai.oversight.repository;                     // pacote desta classe
// ============================================================================
// O acesso ao banco para o usuário.
// É só uma interface: o Spring cria o código dos SELECT e INSERT sozinho.
// Usado por: AuthService, UsuarioService, AdminInicial.
// ============================================================================

import com.senai.oversight.model.StatusUsuario;                       // import: a entidade (tabela) StatusUsuario
import com.senai.oversight.model.Usuario;                             // import: a entidade (tabela) Usuario
import java.util.List;                                                     // import: tipo lista
import java.util.Optional;                                                 // import: Optional = resultado que pode não existir
import org.springframework.data.jpa.repository.JpaRepository;              // import: JpaRepository traz salvar/buscar prontos

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {  // repositório de usuários (o Spring cria o SQL pelo nome do método)
    Optional<Usuario> findByEmailIgnoreCase(String email);                 // acha por e-mail, sem diferenciar maiúscula de minúscula
    boolean existsByEmailIgnoreCase(String email);                         // diz se o e-mail já está cadastrado
    List<Usuario> findAllByOrderByCriadoEmDesc();                          // todos os usuários, mais novos primeiro
    List<Usuario> findByStatusOrderByCriadoEmDesc(StatusUsuario status);   // só os de um status, mais novos primeiro
}                                                                          // fecha o bloco
