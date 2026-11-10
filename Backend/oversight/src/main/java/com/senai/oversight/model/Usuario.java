package com.senai.oversight.model;                          // pacote desta classe
// ============================================================================
// A tabela 'usuario' dentro do Java.
// Cada campo daqui é uma coluna da tabela (nome, e-mail, senha criptografada, perfil, status).
// Esta é a ÚNICA tabela que o Java grava.
// ============================================================================

import jakarta.persistence.*;                                                      // import: anotações JPA (@Entity, @Id, @Column...)
import java.time.LocalDateTime;                                                    // import: tipo data e hora

@Entity                                                                            // entidade: cada objeto Usuario é uma linha da tabela
@Table(name = "usuario")                                                           // tabela usuario no PostgreSQL
public class Usuario {                                                             // classe do usuário
    @Id                                                                            // chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY)                            // o banco gera o número sozinho (1, 2, 3...)
    private Long id;                                                               // id do usuário

    @Column(nullable = false, length = 120)                                        // coluna obrigatória, até 120 caracteres
    private String nome;                                                           // nome

    @Column(nullable = false, unique = true, length = 160)                         // coluna obrigatória, única, até 160 caracteres
    private String email;                                                          // e-mail (usado no login)

    @Column(name = "senha_hash", nullable = false, length = 100)                   // coluna senha_hash
    private String senhaHash;                                                      // senha criptografada com BCrypt

    @Column(length = 30)                                                           // coluna opcional
    private String matricula;                                                      // matrícula do usuário

    @Column(length = 60)                                                           // coluna opcional
    private String turma;                                                          // turma do usuário

    @Enumerated(EnumType.STRING)                                                   // guarda o nome do enum como texto
    @Column(nullable = false, length = 20)                                         // coluna obrigatória
    private PerfilUsuario perfil = PerfilUsuario.USER;                             // perfil: começa como USER

    @Enumerated(EnumType.STRING)                                                   // guarda o nome do enum como texto
    @Column(nullable = false, length = 20)                                         // coluna obrigatória
    private StatusUsuario status = StatusUsuario.PENDENTE;                         // status: começa PENDENTE

    @Column(name = "criado_em", nullable = false)                                  // coluna criado_em
    private LocalDateTime criadoEm;                                                // quando a conta foi criada

    public Long getId() { return id; }                                             // getters e setters: ler e trocar cada campo
    public String getNome() { return nome; }                                       // nome: ler
    public void setNome(String nome) { this.nome = nome; }                         // nome: trocar
    public String getEmail() { return email; }                                     // e-mail: ler
    public void setEmail(String email) { this.email = email; }                     // e-mail: trocar
    public String getSenhaHash() { return senhaHash; }                             // hash da senha: ler
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }     // hash da senha: trocar
    public String getMatricula() { return matricula; }                             // matrícula: ler
    public void setMatricula(String matricula) { this.matricula = matricula; }     // matrícula: trocar
    public String getTurma() { return turma; }                                     // turma: ler
    public void setTurma(String turma) { this.turma = turma; }                     // turma: trocar
    public PerfilUsuario getPerfil() { return perfil; }                            // perfil: ler
    public void setPerfil(PerfilUsuario perfil) { this.perfil = perfil; }          // perfil: trocar
    public StatusUsuario getStatus() { return status; }                            // status: ler
    public void setStatus(StatusUsuario status) { this.status = status; }          // status: trocar
    public LocalDateTime getCriadoEm() { return criadoEm; }                        // data de criação: ler
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }  // data de criação: trocar

    public boolean isAdmin() { return perfil == PerfilUsuario.ADMIN; }             // atalho: true se o perfil é ADMIN
}                                                                                  // fecha o bloco
