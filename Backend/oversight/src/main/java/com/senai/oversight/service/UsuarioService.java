package com.senai.oversight.service;                        // pacote desta classe
// ============================================================================
// SIMPLES: As regras do administrador.
// Lista usuários (com filtro por status) e muda o status (aprovar, bloquear).
// Chamado por: AdminUsuarioController.
// ============================================================================

import com.senai.oversight.dto.UsuarioDto;                            // import: o formato de dados UsuarioDto
import com.senai.oversight.model.StatusUsuario;                       // import: a entidade (tabela) StatusUsuario
import com.senai.oversight.model.Usuario;                             // import: a entidade (tabela) Usuario
import com.senai.oversight.repository.UsuarioRepository;              // import: o repositório (acesso ao banco) UsuarioRepository
import com.senai.oversight.config.ApiException;                                    // import: nosso erro com código HTTP
import java.util.List;                                                              // import: tipo lista
import org.springframework.http.HttpStatus;                                         // import: códigos HTTP (400, 401, 404)
import org.springframework.security.core.Authentication;                            // import: usuário logado
import org.springframework.stereotype.Service;                                      // import: marca como serviço do Spring

@Service                                                                            // serviço do Spring
public class UsuarioService {                                                       // regras de usuário: quem sou eu, listar, aprovar/bloquear
    private final UsuarioRepository repo;                                           // repositório de usuários

    public UsuarioService(UsuarioRepository repo) {                                 // construtor: o Spring entrega o repositório
        this.repo = repo;                                                           // guarda o repositório
    }                                                                               // fecha o bloco

    /** Usuário logado a partir do token JWT já validado pelo filtro. */
    public Usuario atual(Authentication authentication) {                           // descobre quem está logado a partir do token
        return repo.findByEmailIgnoreCase(authentication.getName())                 // o e-mail do token vira busca no banco
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sessão inválida. Entre novamente."));  // não achou: erro 401, entre de novo
    }                                                                               // fecha o bloco

    public List<UsuarioDto> listar(StatusUsuario status) {                          // lista usuários para a tela de liberação
        List<Usuario> lista = status == null ? repo.findAllByOrderByCriadoEmDesc()  // sem filtro: todos, mais novos primeiro
                : repo.findByStatusOrderByCriadoEmDesc(status);                     // com filtro: só os do status pedido
        return lista.stream().map(UsuarioDto::de).toList();                         // converte cada Usuario em UsuarioDto (sem senha)
    }                                                                               // fecha o bloco

    public UsuarioDto alterarStatus(Long id, StatusUsuario novoStatus, Usuario quemAltera) {  // aprova, bloqueia ou deixa pendente um usuário
        Usuario alvo = repo.findById(id)                                            // busca o usuário alvo pelo id
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));  // não achou: erro 404
        if (alvo.getId().equals(quemAltera.getId())) {                              // o admin está mexendo em si mesmo?
            throw new ApiException(HttpStatus.BAD_REQUEST, "Você não pode alterar o seu próprio acesso.");  // então recusa com erro 400
        }                                                                           // fecha o bloco
        alvo.setStatus(novoStatus);                                                 // troca o status
        return UsuarioDto.de(repo.save(alvo));                                      // salva no banco e devolve o DTO
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
