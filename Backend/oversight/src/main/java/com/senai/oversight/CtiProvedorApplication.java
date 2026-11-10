package com.senai.oversight;                                // pacote desta classe
// ============================================================================
// O botão LIGAR do backend.
// Tem o método main: o Java começa a rodar por aqui e o Spring Boot liga o servidor.
// Quem usa: você, ao rodar 'mvn spring-boot:run'.
// ============================================================================

import com.senai.oversight.config.AppProperties;                      // import: a configuração AppProperties
import org.springframework.boot.SpringApplication;                               // import: traz a classe que liga o Spring Boot
import org.springframework.boot.autoconfigure.SpringBootApplication;             // import: traz a anotação que ativa a configuração automática
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;  // import: traz a anotação que lê a seção "app" do application.properties

@SpringBootApplication                                                           // diz: esta é a aplicação Spring Boot (liga tudo sozinho)
@ConfigurationPropertiesScan                                                     // procura as classes de configuração (AppProperties)
public class CtiProvedorApplication {                                            // classe principal do backend
    public static void main(String[] args) {                                     // main: o Java começa a executar por aqui
        SpringApplication.run(CtiProvedorApplication.class, args);               // sobe o servidor web na porta 8080
    }                                                                            // fecha o bloco
}                                                                                // fecha o bloco
