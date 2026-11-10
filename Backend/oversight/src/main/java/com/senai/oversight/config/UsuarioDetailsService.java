package com.senai.oversight.config;                       // pacote desta classe
// ============================================================================
// Busca o usuário no banco para o Spring Security.
// Só contas APROVADAS conseguem entrar.
// Roda sozinho durante a autenticação.
// ============================================================================

import com.senai.oversight.model.StatusUsuario;                                  // import: os 3 estados da conta
import com.senai.oversight.model.Usuario;                                        // import: a entidade Usuario
import com.senai.oversight.repository.UsuarioRepository;                              // import: repositório para buscar no banco
import org.springframework.security.core.userdetails.User;                          // import: User = usuário no formato do Spring Security
import org.springframework.security.core.userdetails.UserDetails;                   // import: UserDetails = dados que o Security precisa
import org.springframework.security.core.userdetails.UserDetailsService;            // import: interface que ensina o Security a buscar usuários
import org.springframework.security.core.userdetails.UsernameNotFoundException;     // import: erro de "usuário não existe"
import org.springframework.stereotype.Service;                                      // import: marca como serviço do Spring

/** Carrega o usuário do PostgreSQL. Só contas APROVADAS ficam "habilitadas". */
@Service                                                                            // serviço do Spring
public class UsuarioDetailsService implements UserDetailsService {                  // ensina o Spring Security a achar o usuário no PostgreSQL
    private final UsuarioRepository repo;                                           // repositório de usuários

    public UsuarioDetailsService(UsuarioRepository repo) {                          // construtor: o Spring entrega o repositório
        this.repo = repo;                                                           // guarda o repositório
    }                                                                               // fecha o bloco

    @Override  // sobrescreve o método da interface
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {  // o Security chama isto com o e-mail digitado no login
        Usuario u = repo.findByEmailIgnoreCase(email)                               // busca o usuário pelo e-mail
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));  // se não achar, lança erro de "não encontrado"
        return User.withUsername(u.getEmail())                                      // monta o usuário do Security com o e-mail
                .password(u.getSenhaHash())                                         // senha já criptografada (BCrypt) que está no banco
                .authorities("ROLE_" + u.getPerfil().name())                        // papel do usuário: ROLE_USER ou ROLE_ADMIN
                .disabled(u.getStatus() != StatusUsuario.APROVADO)                  // conta que não está APROVADA fica desabilitada (não entra)
                .build();                                                           // constrói o objeto
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
