import { defineStore } from 'pinia'
import * as XLSX from 'xlsx'

// Colunas que precisam existir na planilha.
// O e-mail é opcional: aceita a coluna "email_cliente" ou "email".
export const COLUNAS_OBRIGATORIAS = [
  'codigo_cliente',
  'nome_cliente',
  'consultor',
  'segmento',
  'nivel_cliente',
  'faturamento_anual',
  'servicos_contratados',
  'data_contratacao',
  'cidade',
  'uf'
]

// Categorias usadas no relatório de validação
export const CATEGORIAS = {
  VAZIO: 'Campos vazios',
  ESPACOS: 'Espaços desnecessários',
  DUPLICADO: 'Registros duplicados',
  EMAIL: 'E-mails inválidos',
  PADRAO: 'Dados fora do padrão',
  OUTROS: 'Outros problemas'
}

export const ORDEM_CATEGORIAS = Object.values(CATEGORIAS)

// Colunas internas, que não fazem parte da planilha original
const COLUNAS_INTERNAS = ['erros', 'ocorrencias', 'numero_linha']

export const useUploadStore = defineStore('upload', {
  // STATE: informações armazenadas temporariamente
  state: () => ({
    arquivo: null,
    dadosOriginais: [],
    dadosTratados: [],
    erro: '',
    dataUpload: null
  }),

  // GETTERS: informações calculadas a partir dos dados
  getters: {
    totalLinhas: (state) => state.dadosTratados.length,

    colunas: (state) => {
      if (!state.dadosTratados.length) return []

      return Object.keys(state.dadosTratados[0]).filter(
        coluna => !COLUNAS_INTERNAS.includes(coluna)
      )
    },

    totalColunas() {
      return this.colunas.length
    },

    totalValidas: (state) =>
      state.dadosTratados.filter(linha => linha.erros.length === 0).length,

    totalComErro: (state) =>
      state.dadosTratados.filter(linha => linha.erros.length > 0).length,

    linhasComErro: (state) =>
      state.dadosTratados.filter(linha => linha.erros.length > 0),

    linhasValidas: (state) =>
      state.dadosTratados.filter(linha => linha.erros.length === 0),

    // Lista única de ocorrências: { linha, campo, categoria, mensagem }
    ocorrencias: (state) =>
      state.dadosTratados.flatMap(linha =>
        linha.ocorrencias.map(ocorrencia => ({
          linha: linha.numero_linha,
          ...ocorrencia
        }))
      ),

    totalOcorrencias() {
      return this.ocorrencias.length
    },

    // Quantidade de ocorrências por categoria (todas as categorias, mesmo com zero)
    contagemPorCategoria() {
      const contagem = Object.fromEntries(
        ORDEM_CATEGORIAS.map(categoria => [categoria, 0])
      )

      for (const ocorrencia of this.ocorrencias) {
        contagem[ocorrencia.categoria]++
      }

      return contagem
    }
  },

  // ACTIONS: leitura, tratamento e validação da planilha
  actions: {
    async lerArquivo(file) {
      this.erro = ''
      this.arquivo = file
      this.dataUpload = new Date()
      this.dadosOriginais = []
      this.dadosTratados = []

      if (!file) return

      const extensao = file.name.split('.').pop()?.toLowerCase()

      if (!['xlsx', 'xls', 'csv'].includes(extensao)) {
        this.erro = 'Formato inválido. Use XLSX, XLS ou CSV.'
        return
      }

      try {
        const buffer = await file.arrayBuffer()
        const workbook = XLSX.read(buffer, { type: 'array' })
        const nomeAba = workbook.SheetNames[0]
        const worksheet = workbook.Sheets[nomeAba]

        this.dadosOriginais = XLSX.utils.sheet_to_json(worksheet, {
          defval: ''
        })

        if (!this.dadosOriginais.length) {
          this.erro = 'A planilha está vazia.'
          return
        }

        // Avisa claramente quando faltam colunas, em vez de marcar todas as linhas como erro
        const colunasDaPlanilha = Object.keys(this.dadosOriginais[0])
        const faltando = COLUNAS_OBRIGATORIAS.filter(
          coluna => !colunasDaPlanilha.includes(coluna)
        )

        if (faltando.length) {
          this.erro =
            `A planilha não tem as colunas obrigatórias: ${faltando.join(', ')}.`
          return
        }

        this.tratarDados()
      } catch (error) {
        console.error(error)
        this.erro = 'Não foi possível ler a planilha.'
      }
    },

    tratarDados() {
      const codigosEncontrados = new Set()

      this.dadosTratados = this.dadosOriginais.map((linha, indice) => {
        const novaLinha = {}
        const erros = []
        const ocorrencias = []
        const numeroLinha = indice + 2

        // Registra o problema com categoria e campo, e guarda a mensagem em "erros"
        const registrar = (categoria, campo, mensagem) => {
          ocorrencias.push({ categoria, campo, mensagem })
          erros.push(mensagem)
        }

        // Verifica espaços desnecessários antes de tratar os textos
        for (const [chave, valor] of Object.entries(linha)) {
          if (typeof valor === 'string') {
            if (/^\s|\s$|\s{2,}/.test(valor)) {
              registrar(
                CATEGORIAS.ESPACOS,
                chave,
                `Espaços desnecessários no campo ${chave}.`
              )
            }

            // Remove espaços das extremidades e repetições
            novaLinha[chave] = valor.trim().replace(/\s+/g, ' ')
          } else {
            novaLinha[chave] = valor
          }
        }

        // Padroniza o código do cliente
        if (typeof novaLinha.codigo_cliente === 'string') {
          novaLinha.codigo_cliente =
            novaLinha.codigo_cliente.toUpperCase()
        }

        // Valida código vazio, formato e duplicidade
        if (
          novaLinha.codigo_cliente === '' ||
          novaLinha.codigo_cliente === null ||
          novaLinha.codigo_cliente === undefined
        ) {
          registrar(
            CATEGORIAS.VAZIO,
            'codigo_cliente',
            'codigo_cliente está em branco.'
          )
        } else {
          const padrao = /^CTI\d{3}$/

          if (!padrao.test(novaLinha.codigo_cliente)) {
            registrar(
              CATEGORIAS.PADRAO,
              'codigo_cliente',
              'codigo_cliente inválido. Use CTI001, CTI002...'
            )
          }

          if (codigosEncontrados.has(novaLinha.codigo_cliente)) {
            registrar(
              CATEGORIAS.DUPLICADO,
              'codigo_cliente',
              `codigo_cliente duplicado: ${novaLinha.codigo_cliente}.`
            )
          } else {
            codigosEncontrados.add(novaLinha.codigo_cliente)
          }
        }

        // Valida o nome do cliente
        if (
          !novaLinha.nome_cliente ||
          String(novaLinha.nome_cliente).trim() === ''
        ) {
          registrar(
            CATEGORIAS.VAZIO,
            'nome_cliente',
            'nome_cliente está em branco.'
          )
        }

        // Valida o consultor
        if (
          !novaLinha.consultor ||
          String(novaLinha.consultor).trim() === ''
        ) {
          registrar(
            CATEGORIAS.VAZIO,
            'consultor',
            'consultor está em branco.'
          )
        }

        // Valida e padroniza o e-mail, caso a coluna exista
        const campoEmail = Object.prototype.hasOwnProperty.call(
          novaLinha,
          'email_cliente'
        )
          ? 'email_cliente'
          : Object.prototype.hasOwnProperty.call(novaLinha, 'email')
            ? 'email'
            : null

        if (campoEmail) {
          const email = String(
            novaLinha[campoEmail] ?? ''
          ).trim().toLowerCase()

          if (!email) {
            registrar(
              CATEGORIAS.VAZIO,
              campoEmail,
              `${campoEmail} está em branco.`
            )
          } else if (
            !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
          ) {
            registrar(
              CATEGORIAS.EMAIL,
              campoEmail,
              `E-mail inválido no campo ${campoEmail}: ${email}.`
            )
          }

          novaLinha[campoEmail] = email
        }

        // Valida e padroniza o segmento
        if (
          !novaLinha.segmento ||
          String(novaLinha.segmento).trim() === ''
        ) {
          registrar(
            CATEGORIAS.VAZIO,
            'segmento',
            'segmento está em branco.'
          )
        } else {
          const segmento = String(novaLinha.segmento).toUpperCase()

          const mapaSegmentos = {
            'IND.': 'Indústria',
            'INDUSTRIA': 'Indústria',
            'INDÚSTRIA': 'Indústria',
            'COMERCIO': 'Comércio',
            'COMÉRCIO': 'Comércio',
            'SERVICO': 'Serviços',
            'SERVIÇO': 'Serviços',
            'SERVICOS': 'Serviços',
            'SERVIÇOS': 'Serviços',
            'SAUDE': 'Saúde',
            'SAÚDE': 'Saúde',
            'EDUCACAO': 'Educação',
            'EDUCAÇÃO': 'Educação',
            'TECNOLOGIA': 'Tecnologia'
          }

          if (mapaSegmentos[segmento]) {
            novaLinha.segmento = mapaSegmentos[segmento]
          } else {
            registrar(
              CATEGORIAS.PADRAO,
              'segmento',
              `segmento fora do padrão: ${novaLinha.segmento}.`
            )
          }
        }

        // Padroniza e valida o nível do cliente
        if (typeof novaLinha.nivel_cliente === 'string') {
          novaLinha.nivel_cliente =
            novaLinha.nivel_cliente.toUpperCase()
        }

        if (!novaLinha.nivel_cliente) {
          registrar(
            CATEGORIAS.VAZIO,
            'nivel_cliente',
            'nivel_cliente está em branco.'
          )
        } else if (
          !['A', 'B', 'C'].includes(novaLinha.nivel_cliente)
        ) {
          registrar(
            CATEGORIAS.PADRAO,
            'nivel_cliente',
            `nivel_cliente inválido: ${novaLinha.nivel_cliente}. Permitido somente A, B ou C.`
          )
        }

        // Valida o faturamento anual (guardado como número, para cálculos e gráficos)
        if (
          novaLinha.faturamento_anual === '' ||
          novaLinha.faturamento_anual === null ||
          novaLinha.faturamento_anual === undefined
        ) {
          registrar(
            CATEGORIAS.VAZIO,
            'faturamento_anual',
            'faturamento_anual está em branco.'
          )
        } else {
          let valor = novaLinha.faturamento_anual

          if (typeof valor === 'string') {
            valor = valor
              .replace('R$', '')
              .trim()
              .replace(/\s/g, '')
              .replace(/\./g, '')
              .replace(',', '.')
          }

          const numero = Number(valor)

          if (!Number.isFinite(numero)) {
            registrar(
              CATEGORIAS.PADRAO,
              'faturamento_anual',
              'faturamento_anual inválido.'
            )
          } else {
            // A formatação em reais é feita só na hora de exibir
            novaLinha.faturamento_anual = numero
          }
        }

        // Valida os serviços contratados
        if (
          !novaLinha.servicos_contratados ||
          String(novaLinha.servicos_contratados).trim() === ''
        ) {
          registrar(
            CATEGORIAS.VAZIO,
            'servicos_contratados',
            'servicos_contratados está em branco.'
          )
        }

        // Valida a data de contratação
        if (!novaLinha.data_contratacao) {
          registrar(
            CATEGORIAS.VAZIO,
            'data_contratacao',
            'data_contratacao está em branco.'
          )
        } else {
          const valorData = novaLinha.data_contratacao

          if (typeof valorData === 'number') {
            const data = XLSX.SSF.parse_date_code(valorData)

            if (data) {
              const dia = String(data.d).padStart(2, '0')
              const mes = String(data.m).padStart(2, '0')

              novaLinha.data_contratacao =
                `${dia}/${mes}/${data.y}`
            } else {
              registrar(
                CATEGORIAS.PADRAO,
                'data_contratacao',
                'data_contratacao inválida.'
              )
            }
          } else if (typeof valorData === 'string') {
            const padraoData = /^\d{2}\/\d{2}\/\d{4}$/

            if (!padraoData.test(valorData)) {
              registrar(
                CATEGORIAS.PADRAO,
                'data_contratacao',
                'data_contratacao fora do padrão. Use DD/MM/AAAA.'
              )
            } else {
              const [dia, mes, ano] =
                valorData.split('/').map(Number)

              const data = new Date(ano, mes - 1, dia)

              if (
                data.getFullYear() !== ano ||
                data.getMonth() !== mes - 1 ||
                data.getDate() !== dia
              ) {
                registrar(
                  CATEGORIAS.PADRAO,
                  'data_contratacao',
                  'data_contratacao inválida.'
                )
              }
            }
          }
        }

        // Valida e padroniza a cidade
        if (
          !novaLinha.cidade ||
          String(novaLinha.cidade).trim() === ''
        ) {
          registrar(
            CATEGORIAS.VAZIO,
            'cidade',
            'cidade está em branco.'
          )
        } else if (typeof novaLinha.cidade === 'string') {
          novaLinha.cidade = novaLinha.cidade
            .toLowerCase()
            .replace(/\b\p{L}/gu, letra => letra.toUpperCase())
        }

        // Padroniza e valida a UF
        if (typeof novaLinha.uf === 'string') {
          novaLinha.uf = novaLinha.uf.toUpperCase()
        }

        if (!novaLinha.uf) {
          registrar(CATEGORIAS.VAZIO, 'uf', 'uf está em branco.')
        } else if (!/^[A-Z]{2}$/.test(novaLinha.uf)) {
          registrar(CATEGORIAS.PADRAO, 'uf', 'uf inválida.')
        }

        // Guarda o número da linha e os problemas encontrados
        novaLinha.numero_linha = numeroLinha
        novaLinha.erros = erros
        novaLinha.ocorrencias = ocorrencias

        return novaLinha
      })
    },

    // Limpa os dados armazenados
    limpar() {
      this.arquivo = null
      this.dadosOriginais = []
      this.dadosTratados = []
      this.erro = ''
      this.dataUpload = null
    }
  }
})