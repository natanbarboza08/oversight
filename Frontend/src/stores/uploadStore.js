Guia 

import { defineStore } from 'pinia' 		// Importa a função defineStore da Pinia para gerenciar o estado global
import * as XLSX from 'xlsx'			 // Importa a biblioteca SheetJS (xlsx) para leitura e manipulação de planilhas

export const useUploadStore = defineStore('upload', {

  // =====================================================
  // STATE
  // =====================================================
  state: () => ({
    arquivo: null,          // Armazena o objeto do arquivo selecionado pelo usuário
    dadosOriginais: [],     // Armazena os dados brutos convertidos diretamente do Excel
    dadosTratados: [],      // Armazena os dados após passarem por limpeza e validação
    erro: '',               // Armazena mensagens globais de erro (ex: formato inválido)
    dataUpload: null        // Guarda a data e hora exatas em que o upload foi feito
  }),


  // =====================================================
  // GETTERS
  // =====================================================
  getters: {

    totalLinhas: (state) => state.dadosTratados.length, 	// Retorna a quantidade total de linhas tratadas

    totalColunas: (state) => {
      if (!state.dadosTratados.length) return 0 // Se não houver dados, retorna 0 colunas
      return Object.keys(state.dadosTratados[0]).filter(coluna => coluna !== 'erros' && coluna !== 'numero_linha').length // Conta as colunas reais ignorando os metadados de controle
    },

    colunas: (state) => {
      if (!state.dadosTratados.length) return [] // Se não houver dados, retorna array vazio
      return Object.keys(state.dadosTratados[0]).filter(coluna => coluna !== 'erros' && coluna !== 'numero_linha') // Retorna os nomes das colunas reais da planilha
    },

    totalValidas: (state) => state.dadosTratados.filter(linha => linha.erros.length === 0).length, // Conta quantas linhas passaram sem nenhum erro
    totalComErro: (state) => state.dadosTratados.filter(linha => linha.erros.length > 0).length, // Conta quantas linhas possuem pelo menos um erro

    linhasComErro: (state) => state.dadosTratados.filter(linha => linha.erros.length > 0), // Retorna apenas as linhas que contêm erros
    linhasValidas: (state) => state.dadosTratados.filter(linha => linha.erros.length === 0)  // Retorna apenas as linhas totalmente válidas
  },


  // =====================================================
  // ACTIONS
  // =====================================================
  actions: {

    async lerArquivo(file) {
      this.erro = '' // Reseta a mensagem de erro global
      this.arquivo = file // Salva a referência do arquivo no estado
      this.dataUpload = new Date() // Registra a data e hora atual do upload
      this.dadosOriginais = [] // Limpa os dados brutos anteriores
      this.dadosTratados = [] // Limpa os dados tratados anteriores

      if (!file) return // Interrompe a execução se nenhum arquivo foi passado

      const extensao = file.name.split('.').pop()?.toLowerCase() // Extrai e converte a extensão do arquivo para minúsculas

      if (!['xlsx', 'xls', 'csv'].includes(extensao)) {
        this.erro = 'Formato inválido. Use XLSX, XLS ou CSV.' // Valida se a extensão é permitida
        return
      }

      try {
        const buffer = await file.arrayBuffer() // Converte o arquivo carregado em um buffer binário ArrayBuffer
        const workbook = XLSX.read(buffer, { type: 'array' }) // Lê o arquivo binário usando a biblioteca SheetJS

        const nomeAba = workbook.SheetNames[0] // Identifica o nome da primeira aba da planilha
        const worksheet = workbook.Sheets[nomeAba] // Obtém os dados da primeira aba

        this.dadosOriginais = XLSX.utils.sheet_to_json(worksheet, { defval: '' }) // Converte a aba do Excel em um array de objetos JSON (células vazias viram string vazia)

        if (!this.dadosOriginais.length) {
          this.erro = 'A planilha está vazia.' // Valida se o arquivo JSON gerado está vazio
          return
        }

        this.tratarDados() // Chama o método responsável por higienizar e validar as linhas
      } catch (error) {
        console.error(error) // Exibe o erro técnico no console para debug
        this.erro = 'Não foi possível ler a planilha.' // Define mensagem amigável caso ocorra falha de leitura
      }
    },


    tratarDados() {
      const codigosEncontrados = new Set() // Cria um Set temporário para rastrear códigos duplicados

      this.dadosTratados = this.dadosOriginais.map((linha, indice) => {
        const novaLinha = {} // Cria um novo objeto higienizado para a linha atual
        const erros = [] // Cria um array para armazenar os erros encontrados nesta linha
        const numeroLinha = indice + 2 // Calcula o número real da linha no Excel (Linha 1 = cabeçalho)

        // 1. REMOVE ESPAÇOS EM BRANCO
        for (const [chave, valor] of Object.entries(linha)) {
          novaLinha[chave] = typeof valor === 'string' ? valor.trim() : valor // Aplica trim se for texto, senão mantém o valor original
        }

        // 2. CÓDIGO DO CLIENTE
        if (typeof novaLinha.codigo_cliente === 'string') {
          novaLinha.codigo_cliente = novaLinha.codigo_cliente.trim().toUpperCase() // Padroniza o código para maiúsculas e remove espaços
        }

        if (!novaLinha.codigo_cliente) {
          erros.push('codigo_cliente está em branco.') // Valida se o código está vazio
        } else {
          const padrao = /^CTI\d{3}$/ // Expressão regular exigindo o formato CTI seguido de 3 dígitos (ex: CTI001)
          if (!padrao.test(novaLinha.codigo_cliente)) {
            erros.push('codigo_cliente inválido. Use CTI001, CTI002...') // Valida se o formato do código está correto
          }

          if (codigosEncontrados.has(novaLinha.codigo_cliente)) {
            erros.push(`codigo_cliente duplicado: ${novaLinha.codigo_cliente}.`) // Valida se o código já apareceu antes na planilha
          } else {
            codigosEncontrados.add(novaLinha.codigo_cliente) // Adiciona o código ao Set para checagem de duplicidade futura
          }
        }

        // 3. NOME DO CLIENTE
        if (!novaLinha.nome_cliente || String(novaLinha.nome_cliente).trim() === '') {
          erros.push('nome_cliente está em branco.') // Valida se o nome do cliente foi preenchido
        }

        // 4. CONSULTOR
        if (!novaLinha.consultor || String(novaLinha.consultor).trim() === '') {
          erros.push('consultor está em branco.') // Valida se o consultor foi preenchido
        }

        // 5. SEGMENTO
        if (!novaLinha.segmento || String(novaLinha.segmento).trim() === '') {
          erros.push('segmento está em branco.') // Valida se o segmento foi preenchido
        } else {
          const segmento = novaLinha.segmento.trim().toUpperCase() // Padroniza para maiúsculas
          const mapaSegmentos = {
            'IND.': 'Indústria', 'INDUSTRIA': 'Indústria', 'INDÚSTRIA': 'Indústria',
            'COMERCIO': 'Comércio', 'COMÉRCIO': 'Comércio',
            'SERVICO': 'Serviços', 'SERVIÇO': 'Serviços', 'SERVICOS': 'Serviços', 'SERVIÇOS': 'Serviços',
            'SAUDE': 'Saúde', 'SAÚDE': 'Saúde',
            'EDUCACAO': 'Educação', 'EDUCAÇÃO': 'Educação',
            'TECNOLOGIA': 'Tecnologia'
          }
          novaLinha.segmento = mapaSegmentos[segmento] || novaLinha.segmento // Mapeia para o nome oficial ou mantém o original
        }

        // 6. NÍVEL DO CLIENTE
        if (typeof novaLinha.nivel_cliente === 'string') {
          novaLinha.nivel_cliente = novaLinha.nivel_cliente.trim().toUpperCase() // Padroniza nível para maiúsculas
        }

        if (!novaLinha.nivel_cliente) {
          erros.push('nivel_cliente está em branco.') // Valida se o nível foi preenchido
        } else if (!['A', 'B', 'C'].includes(novaLinha.nivel_cliente)) {
          erros.push(`nivel_cliente inválido: ${novaLinha.nivel_cliente}. Permitido somente A, B ou C.`) // Valida se o nível é estritamente A, B ou C
        }

        // 7. FATURAMENTO ANUAL
        if (novaLinha.faturamento_anual === '' || novaLinha.faturamento_anual === null || novaLinha.faturamento_anual === undefined) {
          erros.push('faturamento_anual está em branco.') // Valida se o faturamento foi preenchido
        } else {
          let valor = novaLinha.faturamento_anual
          if (typeof valor === 'string') {
            valor = valor.replace('R$', '').trim().replace(/\s/g, '').replace(/\./g, '').replace(',', '.') // Converte formato textual brasileiro para número puro
          }
          const numero = Number(valor)
          if (isNaN(numero)) {
            erros.push('faturamento_anual inválido.') // Valida se a conversão gerou um número válido
          } else {
            novaLinha.faturamento_anual = numero.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) // Formata o número de volta para o padrão monetário BRL (R$)
          }
        }

        // 8. SERVIÇOS CONTRATADOS
        if (!novaLinha.servicos_contratados || String(novaLinha.servicos_contratados).trim() === '') {
          erros.push('servicos_contratados está em branco.') // Valida se os serviços foram preenchidos
        }

        // 9. DATA DE CONTRATAÇÃO
        if (!novaLinha.data_contratacao) {
          erros.push('data_contratacao está em branco.') // Valida se a data foi preenchida
        } else {
          const valorData = novaLinha.data_contratacao
          if (typeof valorData === 'number') {
            const data = XLSX.SSF.parse_date_code(valorData) // Converte o número serial interno do Excel em objeto de data estruturado
            if (data) {
              const dia = String(data.d).padStart(2, '0') // Garante 2 dígitos para o dia
              const mes = String(data.m).padStart(2, '0') // Garante 2 dígitos para o mês
              novaLinha.data_contratacao = `${dia}/${mes}/${data.y}` // Formata para o padrão DD/MM/AAAA
            } else {
              erros.push('data_contratacao inválida.') // Erro caso a conversão serial falhe
            }
          }
        }

        // 10. CIDADE
        if (!novaLinha.cidade || String(novaLinha.cidade).trim() === '') {
          erros.push('cidade está em branco.') // Valida se a cidade foi preenchida
        }

        // 11. UF
        if (typeof novaLinha.uf === 'string') {
          novaLinha.uf = novaLinha.uf.trim().toUpperCase() // Padroniza UF para maiúsculas
        }

        if (!novaLinha.uf) {
          erros.push('uf está em branco.') // Valida se a UF foi preenchida
        } else if (!/^[A-Z]{2}$/.test(novaLinha.uf)) {
          erros.push('uf inválida.') // Valida se a UF possui exatamente 2 letras
        }

        // RESULTADO DA LINHA
        novaLinha.numero_linha = numeroLinha // Atribui o número da linha física do Excel
        novaLinha.erros = erros // Atribui a lista de erros acumulados desta linha

        return novaLinha // Retorna o objeto tratado para o array de dados tratados
      })
    },


    limpar() {
      this.arquivo = null // Reseta o arquivo carregado
      this.dadosOriginais = [] // Limpa os dados brutos
      this.dadosTratados = [] // Limpa os dados tratados
      this.erro = '' // Limpa mensagens de erro
      this.dataUpload = null // Reseta a data e hora do upload
    }
  }
})