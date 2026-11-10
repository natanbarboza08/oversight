package com.senai.oversight.config;                         // pacote desta classe
// ============================================================================
// 
// Ao ligar, se não existir nenhum ADMIN, cria um com o e-mail e a senha do .env. Ele aprova os outros usuários.
// Roda sozinho uma vez, na inicialização.
// ============================================================================

import com.senai.oversight.model.PerfilUsuario;                       // import: a entidade (tabela) PerfilUsuario
import com.senai.oversight.model.StatusUsuario;                       // import: a entidade (tabela) StatusUsuario
import com.senai.oversight.model.Usuario;                             // import: a entidade (tabela) Usuario
import com.senai.oversight.repository.UsuarioRepository;              // import: o repositório (acesso ao banco) UsuarioRepository
import com.senai.oversight.service.Datas;                                           // import: nosso conversor de datas
import org.slf4j.Logger;                                                            // import: Logger = escreve mensagens no terminal
import org.slf4j.LoggerFactory;                                                     // import: fábrica de Logger
import org.springframework.boot.ApplicationArguments;                               // import: argumentos de inicialização
import org.springframework.boot.ApplicationRunner;                                  // import: interface que roda código ao iniciar
import org.springframework.security.crypto.password.PasswordEncoder;                // import: PasswordEncoder = criptografa senhas (BCrypt)
import org.springframework.stereotype.Component;                                    // import: marca como componente do Spring

/** Cria o primeiro administrador (quem libera os demais usuários), se ainda não existir. */
@Component                                                                          // componente do Spring
public class AdminInicial implements ApplicationRunner {                            // roda uma vez, quando o servidor liga
    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);  // o logger desta classe

    private final UsuarioRepository repo;                                           // repositório de usuários
    private final PasswordEncoder encoder;                                          // o criptografador de senha (BCrypt)
    private final AppProperties props;                                              // as configurações do application.properties

    public AdminInicial(UsuarioRepository repo, PasswordEncoder encoder, AppProperties props) {  // construtor: o Spring entrega as três peças
        this.repo = repo;                                                           // guarda o repositório
        this.encoder = encoder;                                                     // guarda o criptografador
        this.props = props;                                                         // guarda as configurações
    }                                                                               // fecha o bloco

    @Override                                                                       // sobrescreve o método que o Spring chama ao iniciar
    public void run(ApplicationArguments args) {                                    // método executado na partida
        var cfg = props.admin();                                                    // lê os dados do admin do yml
        if (cfg == null || cfg.email() == null || cfg.email().isBlank()) return;    // sem e-mail configurado: não cria nada
        String email = cfg.email().trim().toLowerCase();                            // e-mail em minúsculo e sem espaços
        if (repo.existsByEmailIgnoreCase(email)) return;                            // admin já existe: não cria de novo

        Usuario admin = new Usuario();                                              // cria o objeto usuário
        admin.setNome(cfg.nome());                                                  // nome do admin
        admin.setEmail(email);                                                      // e-mail do admin
        admin.setSenhaHash(encoder.encode(cfg.senha()));                            // guarda a senha criptografada com BCrypt, nunca a senha pura
        admin.setPerfil(PerfilUsuario.ADMIN);                                       // perfil ADMIN
        admin.setStatus(StatusUsuario.APROVADO);                                    // já nasce APROVADO
        admin.setCriadoEm(Datas.agoraUtc());                                        // data de criação
        repo.save(admin);                                                           // salva no banco
        log.warn("Administrador inicial criado: {} (troque a senha via variável ADMIN_PASSWORD em produção)", email);  // avisa no terminal que o admin foi criado
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
