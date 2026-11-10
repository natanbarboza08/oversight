package com.senai.oversight.service;                        // pacote desta classe
// ============================================================================
// SIMPLES: O VALIDADOR da planilha (campo a campo).
// Confere cada linha: campos obrigatórios, tamanho, código repetido, segmento, nível A/B/C, faturamento, serviços, data e UF.
// Chamado por: UploadService. Devolve ERRO, AVISO ou VÁLIDA para cada linha.
// ============================================================================

import com.senai.oversight.dto.ClienteDTO;                            // import: o formato de dados ClienteDTO
import com.senai.oversight.service.Padronizacao;                         // import: a ajuda Padronizacao
import static com.senai.oversight.service.Colunas.*;                                // import estático: usa CODIGO, NOME, TODAS... direto

import java.math.BigDecimal;                                                        // import: BigDecimal = número decimal exato (dinheiro)
import java.math.RoundingMode;                                                      // import: modo de arredondamento
import java.time.LocalDate;                                                         // import: LocalDate = data sem hora
import java.time.format.DateTimeFormatter;                                          // import: formato de data
import java.time.format.DateTimeParseException;                                     // import: erro ao ler data
import java.util.*;                                                                 // import: List, Map, Set, Optional, Arrays...
import java.util.regex.Pattern;                                                     // import: expressões regulares (padrões de texto)
import org.springframework.stereotype.Service;                                      // import: marca como serviço do Spring

/**
 * Valida a planilha linha a linha. Aqui só decidimos se a linha PODE entrar (ERRO ou VALIDA).
 * A padronização (IND. -> Indústria, ANA SOUZA -> Ana Souza...) é responsabilidade do ETL em Python.
 */
@Service                                                                            // serviço do Spring
public class ClienteValidacaoService {                                             // VALIDAÇÃO DA PLANILHA: mesmas regras do Pinia no front

    public record ResultadoLinha(int numero, Map<String, String> valores, List<String> erros, List<String> avisos) {  // resultado de uma linha: número, valores limpos, erros e avisos
        public boolean valida() {                                                   // válida = não tem nenhum erro
            return erros.isEmpty();                                                 // devolve true se a lista de erros está vazia
        }                                                                           // fecha o bloco
    }                                                                               // fecha o bloco

    private static final Pattern RE_CODIGO = Pattern.compile("^[A-Za-z0-9_-]{1,60}$");  // padrão do código: letras, números, - e _ (até 60)
    private static final Pattern RE_UF = Pattern.compile("^[A-Za-z]{2}$");          // padrão da UF: 2 letras
    private static final Pattern DATA_ISO = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})([T ].*)?$");  // padrão de data ISO: aaaa-mm-dd
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("d/M/uuuu");  // formato brasileiro: dd/mm/aaaa
    private static final BigDecimal LIMITE_FATURAMENTO = new BigDecimal("1000000000000"); // 1 trilhão
    private static final Set<String> NIVEIS = Set.of("A", "B", "C");                // níveis aceitos

    public List<ResultadoLinha> validar(List<ClienteDTO> linhas) {                  // validar: confere todas as linhas
        List<ResultadoLinha> saida = new ArrayList<>();                             // lista de resultados
        Map<String, Integer> codigos = new HashMap<>();   // código -> 1ª linha em que apareceu
        Map<String, Integer> conteudos = new HashMap<>(); // nome|faturamento|data -> 1ª linha
        Map<String, String> codigoDoConteudo = new HashMap<>();                     // código -> linha onde apareceu (acha duplicados)
        LocalDate hoje = LocalDate.now();                                           // data de hoje (para o aviso de data no futuro)

        for (ClienteDTO linha : linhas) {                                           // para cada linha da planilha
            List<String> erros = new ArrayList<>();                                 // erros desta linha (bloqueiam)
            List<String> avisos = new ArrayList<>();                                // avisos desta linha (não bloqueiam)
            Map<String, String> valores = new LinkedHashMap<>();                    // valores já limpos

            // 1) texto limpo (sem espaços nas pontas / duplicados) + aviso se havia sujeira
            for (String campo : TODAS) {                                            // passo 1: para cada uma das 10 colunas
                String bruto = linha.campos().get(campo);                           // texto como veio
                String limpo = bruto == null ? "" : bruto.strip().replaceAll("\\s{2,}", " ");  // limpa espaços nas pontas e repetidos
                if (bruto != null && !limpo.equals(bruto)) {                        // mudou alguma coisa?
                    avisos.add(campo + ": espaços extras removidos");               // então registra um aviso
                }                                                                   // fecha o bloco
                valores.put(campo, limpo);                                          // guarda o valor limpo
                Integer max = TAMANHO_MAXIMO.get(campo);                            // tamanho máximo dessa coluna
                if (max != null && limpo.length() > max) {                          // passou do tamanho?
                    erros.add(campo + ": excede " + max + " caracteres");           // erro
                    valores.put(campo, limpo.substring(0, max));                    // corta o texto
                }                                                                   // fecha o bloco
            }                                                                       // fecha o bloco

            // 2) obrigatórios
            for (String campo : OBRIGATORIAS) {                                     // passo 2: para cada coluna obrigatória
                if (valores.get(campo).isEmpty()) erros.add(campo + ": campo obrigatório vazio");  // vazia? então erro
            }                                                                       // fecha o bloco

            // 3) código: formato e duplicidade
            String codigo = valores.get(CODIGO);                                    // pega o código
            if (!codigo.isEmpty()) {                                                // se tem código
                if (!RE_CODIGO.matcher(codigo).matches()) {                         // formato inválido?
                    erros.add(CODIGO + ": use apenas letras, números, '-' ou '_'");  // erro de formato
                }                                                                   // fecha o bloco
                String chave = codigo.toUpperCase();                                // compara em maiúsculas (cli-1 = CLI-1)
                Integer primeira = codigos.putIfAbsent(chave, linha.numero());      // guarda a linha; devolve a linha anterior se já existia
                if (primeira != null) {                                             // já existia?
                    erros.add(CODIGO + ": código duplicado (já usado na linha " + primeira + ")");  // erro de código duplicado
                }                                                                   // fecha o bloco
            }                                                                       // fecha o bloco

            // 3b) segmento: reconhecido (com aviso se for corrigido) ou erro
            String segmento = valores.get(SEGMENTO);                                // pega o segmento
            if (!segmento.isEmpty()) {                                              // se preenchido
                String padrao = Padronizacao.segmento(segmento);                    // procura o nome oficial
                if (padrao == null) {                                               // não reconhecido?
                    erros.add(SEGMENTO + ": '" + segmento + "' não reconhecido (use Indústria, Comércio, Serviços, Saúde, Educação ou Tecnologia)");  // erro com a lista dos segmentos aceitos
                } else if (!padrao.equals(segmento)) {                              // reconhecido, mas escrito diferente
                    avisos.add(SEGMENTO + ": '" + segmento + "' será padronizado para " + padrao);  // aviso: será padronizado
                }                                                                   // fecha o bloco
            }                                                                       // fecha o bloco

            // 3c) consultor: aviso se o nome será ajustado
            String consultor = valores.get(CONSULTOR);                              // pega o consultor
            if (!consultor.isEmpty()) {                                             // se preenchido
                String ajustado = Padronizacao.tituloNome(consultor);               // como ficaria com a padronização
                if (!ajustado.equals(consultor)) avisos.add(CONSULTOR + ": '" + consultor + "' será padronizado para " + ajustado);  // se muda, avisa
            }                                                                       // fecha o bloco

            // 4) nível A/B/C
            String nivel = valores.get(NIVEL);                                      // pega o nível
            if (!nivel.isEmpty()) {                                                 // se preenchido
                String maiusculo = nivel.toUpperCase();                             // deixa em maiúscula
                if (!NIVEIS.contains(maiusculo)) {                                  // não é A, B nem C?
                    erros.add(NIVEL + ": '" + nivel + "' inválido (use A, B ou C)");  // erro
                } else if (!maiusculo.equals(nivel)) {                              // era minúscula (a, b, c)
                    avisos.add(NIVEL + ": '" + nivel + "' será convertido para " + maiusculo);  // aviso: será convertido
                }                                                                   // fecha o bloco
            }                                                                       // fecha o bloco

            // 5) faturamento numérico, não negativo
            String fat = valores.get(FATURAMENTO);                                  // pega o faturamento
            if (!fat.isEmpty()) {                                                   // se preenchido
                Optional<BigDecimal> numero = lerDecimal(fat);                      // tenta converter em número
                if (numero.isEmpty()) {                                             // não é número?
                    erros.add(FATURAMENTO + ": '" + fat + "' não é um número");     // erro
                } else if (numero.get().signum() < 0) {                             // negativo?
                    erros.add(FATURAMENTO + ": não pode ser negativo");             // erro
                } else if (numero.get().compareTo(LIMITE_FATURAMENTO) >= 0) {       // gigante demais?
                    erros.add(FATURAMENTO + ": valor acima do permitido");          // erro
                } else {                                                            // válido:
                    if (numero.get().signum() == 0) avisos.add(FATURAMENTO + ": valor zerado");  // zero gera só um aviso
                    valores.put(FATURAMENTO, numero.get().setScale(2, RoundingMode.HALF_UP).toPlainString());  // guarda com 2 casas decimais
                }                                                                   // fecha o bloco
            }                                                                       // fecha o bloco

            // 6) serviços (lista separada por ';')
            String servicos = valores.get(SERVICOS);                                // pega os serviços
            if (!servicos.isEmpty() && Arrays.stream(servicos.split(";")).allMatch(s -> s.isBlank())) {  // se todos os pedaços separados por ; estão vazios
                erros.add(SERVICOS + ": nenhum serviço informado");                 // erro
            }                                                                       // fecha o bloco

            // 7) data de contratação
            String data = valores.get(DATA);                                        // pega a data
            if (!data.isEmpty()) {                                                  // se preenchida
                Optional<LocalDate> d = lerData(data);                              // tenta ler a data
                if (d.isEmpty() || d.get().getYear() < 1990) {                      // inválida ou anterior a 1990?
                    erros.add(DATA + ": '" + data + "' não é uma data válida (use dd/mm/aaaa ou aaaa-mm-dd)");  // erro
                } else {                                                            // válida:
                    if (d.get().isAfter(hoje)) avisos.add(DATA + ": data no futuro");  // no futuro gera aviso
                    valores.put(DATA, d.get().toString());                          // guarda no formato aaaa-mm-dd
                }                                                                   // fecha o bloco
            }                                                                       // fecha o bloco

            // 8) UF opcional, mas se vier precisa ter 2 letras
            String uf = valores.get(UF);                                            // pega a UF
            if (!uf.isEmpty() && !RE_UF.matcher(uf).matches()) {                    // se preenchida e não tem 2 letras
                erros.add(UF + ": '" + uf + "' inválida (use a sigla com 2 letras)");  // erro
            }                                                                       // fecha o bloco

            // 9) mesmo cliente com outro código (aviso, não bloqueia)
            if (!codigo.isEmpty() && !valores.get(NOME).isEmpty()) {                // se tem código e nome
                String chave = valores.get(NOME).toLowerCase() + "|" + valores.get(FATURAMENTO) + "|" + valores.get(DATA);  // chave = nome + faturamento + data
                Integer anterior = conteudos.putIfAbsent(chave, linha.numero());    // guarda a linha; devolve a anterior se já existia
                if (anterior != null && !codigo.equalsIgnoreCase(codigoDoConteudo.get(chave))) {  // já existia com outro código?
                    avisos.add(NOME + ": possível duplicidade (mesmo nome, faturamento e data da linha " + anterior + ")");  // aviso de possível cliente repetido
                }                                                                   // fecha o bloco
                codigoDoConteudo.putIfAbsent(chave, codigo);                        // guarda o código dessa combinação
            }                                                                       // fecha o bloco

            saida.add(new ResultadoLinha(linha.numero(), valores, erros, avisos));  // guarda o resultado desta linha
        }                                                                           // fecha o bloco
        return saida;                                                               // devolve todos os resultados
    }                                                                               // fecha o bloco

    /** Aceita 1850000, 1850000.50, 1.850.000,50, R$ 1.850.000,50. */
    static Optional<BigDecimal> lerDecimal(String texto) {                          // lerDecimal: aceita 1850000, 1.850.000,50 e R$ 1.850.000,50
        String t = texto.replace("R$", "").replace(' ', ' ').replaceAll("\\s", "");  // tira "R$" e espaços
        if (t.isEmpty() || !t.matches("^-?[0-9.,]+$")) return Optional.empty();     // se sobrou algo que não é número, devolve vazio
        boolean ponto = t.contains("."), virgula = t.contains(",");                 // tem ponto? tem vírgula?
        if (ponto && virgula) {                                                     // os dois: o último símbolo é o decimal
            if (t.lastIndexOf(',') > t.lastIndexOf('.')) t = t.replace(".", "").replace(',', '.');  // vírgula é o decimal: tira os pontos
            else t = t.replace(",", "");                                            // ponto é o decimal: tira as vírgulas
        } else if (virgula) {                                                       // só vírgula
            t = t.chars().filter(c -> c == ',').count() > 1 ? t.replace(",", "") : t.replace(',', '.');  // várias vírgulas = milhar; uma só = decimal
        } else if (ponto && t.matches("^-?\\d{1,3}(\\.\\d{3})+$")) {                // só ponto: 1.850.000 é milhar
            t = t.replace(".", "");                                                 // tira os pontos
        }                                                                           // fecha o bloco
        try {                                                                       // tenta converter
            return Optional.of(new BigDecimal(t));                                  // devolve o número
        } catch (NumberFormatException e) {                                         // deu erro de conversão
            return Optional.empty();                                                // devolve vazio
        }                                                                           // fecha o bloco
    }                                                                               // fecha o bloco

    static Optional<LocalDate> lerData(String texto) {                              // lerData: lê dd/mm/aaaa ou aaaa-mm-dd
        try {                                                                       // tenta ler
            var iso = DATA_ISO.matcher(texto);                                      // testa o padrão ISO
            if (iso.matches()) return Optional.of(LocalDate.parse(iso.group(1)));   // é ISO: converte direto
            return Optional.of(LocalDate.parse(texto, BR));                         // senão tenta o formato brasileiro
        } catch (DateTimeParseException e) {                                        // data inexistente (ex.: 31/02)
            return Optional.empty();                                                // devolve vazio
        }                                                                           // fecha o bloco
    }                                                                               // fecha o bloco
}                                                                                   // fecha o bloco
