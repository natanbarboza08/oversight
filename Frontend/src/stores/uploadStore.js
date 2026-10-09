
import { defineStore } from 'pinia'
import * as XLSX from 'xlsx'

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

    totalColunas: (state) => {
      if (!state.dadosTratados.length) return 0

      return Object.keys(state.dadosTratados[0]).filter(
        coluna => coluna !== 'erros' && coluna !== 'numero_linha'
      ).length
    },

    colunas: (state) => {
      if (!state.dadosTratados.length) return []

      return Object.keys(state.dadosTratados[0]).filter(
        coluna => coluna !== 'erros' && coluna !== 'numero_linha'
      )
    },

    totalValidas: (state) =>
      state.dadosTratados.filter(linha => linha.erros.length === 0).length,

    totalComErro: (state) =>
      state.dadosTratados.filter(linha => linha.erros.length > 0).length,

    linhasComErro: (state) =>
      state.dadosTratados.filter(linha => linha.erros.length > 0),

    linhasValidas: (state) =>
      state.dadosTratados.filter(linha => linha.erros.length === 0)
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
        const numeroLinha = indice + 2

        // Verifica espaços desnecessários antes de tratar os textos
        for (const [chave, valor] of Object.entries(linha)) {
          if (typeof valor === 'string') {
            if (/^\s|\s$|\s{2,}/.test(valor)) {
              erros.push(`Espaços desnecessários no campo ${chave}.`)
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
          erros.push('codigo_cliente está em branco.')
        } else {
          const padrao = /^CTI\d{3}$/

          if (!padrao.test(novaLinha.codigo_cliente)) {
            erros.push(
              'codigo_cliente inválido. Use CTI001, CTI002...'
            )
          }

          if (codigosEncontrados.has(novaLinha.codigo_cliente)) {
            erros.push(
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
          erros.push('nome_cliente está em branco.')
        }

        // Valida o consultor
        if (
          !novaLinha.consultor ||
          String(novaLinha.consultor).trim() === ''
        ) {
          erros.push('consultor está em branco.')
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
            erros.push(`${campoEmail} está em branco.`)
          } else if (
            !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
          ) {
            erros.push(
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
          erros.push('segmento está em branco.')
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
            erros.push(
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
          erros.push('nivel_cliente está em branco.')
        } else if (
          !['A', 'B', 'C'].includes(novaLinha.nivel_cliente)
        ) {
          erros.push(
            `nivel_cliente inválido: ${novaLinha.nivel_cliente}. Permitido somente A, B ou C.`
          )
        }

        // Valida o faturamento anual
        if (
          novaLinha.faturamento_anual === '' ||
          novaLinha.faturamento_anual === null ||
          novaLinha.faturamento_anual === undefined
        ) {
          erros.push('faturamento_anual está em branco.')
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
            erros.push('faturamento_anual inválido.')
          } else {
            novaLinha.faturamento_anual = numero.toLocaleString(
              'pt-BR',
              {
                style: 'currency',
                currency: 'BRL'
              }
            )
          }
        }

        // Valida os serviços contratados
        if (
          !novaLinha.servicos_contratados ||
          String(novaLinha.servicos_contratados).trim() === ''
        ) {
          erros.push('servicos_contratados está em branco.')
        }

        // Valida a data de contratação
        if (!novaLinha.data_contratacao) {
          erros.push('data_contratacao está em branco.')
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
              erros.push('data_contratacao inválida.')
            }
          } else if (typeof valorData === 'string') {
            const padraoData = /^\d{2}\/\d{2}\/\d{4}$/

            if (!padraoData.test(valorData)) {
              erros.push(
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
                erros.push('data_contratacao inválida.')
              }
            }
          }
        }

        // Valida e padroniza a cidade
        if (
          !novaLinha.cidade ||
          String(novaLinha.cidade).trim() === ''
        ) {
          erros.push('cidade está em branco.')
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
          erros.push('uf está em branco.')
        } else if (!/^[A-Z]{2}$/.test(novaLinha.uf)) {
          erros.push('uf inválida.')
        }

        // Guarda o número da linha e os erros encontrados
        novaLinha.numero_linha = numeroLinha
        novaLinha.erros = erros

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