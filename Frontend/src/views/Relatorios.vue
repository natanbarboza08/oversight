
<script setup>
import { storeToRefs } from 'pinia'
import { useUploadStore } from '../stores/uploadStore'

const uploadStore = useUploadStore()

const {
  arquivo,
  dadosTratados,
  totalLinhas,
  totalValidas,
  totalComErro,
  linhasComErro
} = storeToRefs(uploadStore)

// Transforma as mensagens da store em itens detalhados para o relatório
function obterErros() {
  return linhasComErro.value.flatMap((linha) =>
    linha.erros.map((mensagem) => {
      let categoria = 'Outros problemas'
      let campo = 'Não identificado'

      // Identifica problemas com espaços desnecessários
      if (mensagem.includes('Espaços desnecessários')) {
        categoria = 'Espaços desnecessários'
        campo = mensagem.split('campo ')[1]?.replace(/\.$/, '') ||
          'Não identificado'
      }

      // Identifica e-mails inválidos
      else if (mensagem.startsWith('E-mail inválido')) {
        categoria = 'E-mails inválidos'
        const resultado = mensagem.match(/campo ([^:]+):/)
        campo = resultado ? resultado[1] : 'email'
      }

      // Identifica registros duplicados
      else if (mensagem.includes('duplicado')) {
        categoria = 'Registros duplicados'
        campo = 'codigo_cliente'
      }

      // Identifica campos obrigatórios vazios
      else if (mensagem.includes('está em branco')) {
        categoria = 'Campos vazios'
        campo = mensagem.split(' ')[0]
      }

      // Identifica dados fora do padrão
      else if (
        mensagem.includes('inválido') ||
        mensagem.includes('inválida') ||
        mensagem.includes('fora do padrão')
      ) {
        categoria = 'Dados fora do padrão'

        if (mensagem.includes('segmento fora do padrão')) {
          campo = 'segmento'
        } else if (mensagem.includes('data_contratacao fora do padrão')) {
          campo = 'data_contratacao'
        } else {
          campo = mensagem.split(' ')[0]
        }
      }

      return {
        linha: linha.numero_linha,
        campo,
        categoria,
        descricao: mensagem
      }
    })
  )
}

// Categorias apresentadas no relatório
const categorias = [
  'Campos vazios',
  'Espaços desnecessários',
  'Registros duplicados',
  'E-mails inválidos',
  'Dados fora do padrão',
  'Outros problemas'
]

// Calcula a quantidade de erros de cada categoria
function quantidadePorCategoria(categoria) {
  return obterErros().filter(
    erro => erro.categoria === categoria
  ).length
}

const formatarData = (data) => {
  if (!data) return ''
  return new Date(data).toLocaleString('pt-BR')
}
</script>

<template>
  <main class="min-h-screen bg-slate-100 px-4 py-8 text-slate-800 sm:px-8">
    <div class="mx-auto max-w-7xl">

      <!-- Cabeçalho -->
      <header class="mb-8">
        <p class="mb-2 text-sm font-semibold uppercase tracking-wider text-blue-600">
          CTI Provedor
        </p>

        <h1 class="text-3xl font-bold sm:text-4xl">
          Relatório de validação
        </h1>

        <p class="mt-3 text-slate-500">
          Consulte os resultados da análise e os problemas encontrados na planilha.
        </p>
      </header>

      <!-- Quando ainda não há dados -->
      <section
        v-if="dadosTratados.length === 0"
        class="rounded-2xl border border-slate-200 bg-white p-10 text-center shadow-sm"
      >
        <h2 class="text-xl font-semibold">Nenhuma análise disponível</h2>

        <p class="mx-auto mt-2 max-w-lg text-slate-500">
          Envie uma planilha na tela de upload para gerar o relatório de validação.
        </p>

        <RouterLink
          to="/app/upload"
          class="mt-6 inline-block rounded-lg bg-blue-600 px-5 py-3 font-semibold text-white hover:bg-blue-700"
        >
          Ir para upload
        </RouterLink>
      </section>

      <template v-else>
        <!-- Informações do arquivo -->
        <section class="mb-8 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="text-lg font-semibold">Arquivo analisado</h2>

          <p class="mt-2 break-all text-slate-600">
            {{ arquivo?.name || 'Nome não disponível' }}
          </p>

          <p class="mt-1 text-sm text-slate-400">
            Análise realizada em: {{ formatarData(uploadStore.dataUpload) }}
          </p>
        </section>

        <!-- Indicadores -->
        <section class="mb-8">
          <h2 class="mb-4 text-xl font-semibold">Resumo da validação</h2>

          <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <article class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
              <p class="text-sm text-slate-500">Total de registros</p>
              <p class="mt-2 text-3xl font-bold">{{ totalLinhas }}</p>
            </article>

            <article class="rounded-xl border border-emerald-200 bg-white p-5 shadow-sm">
              <p class="text-sm text-emerald-700">Registros válidos</p>
              <p class="mt-2 text-3xl font-bold text-emerald-600">
                {{ totalValidas }}
              </p>
            </article>

            <article class="rounded-xl border border-red-200 bg-white p-5 shadow-sm">
              <p class="text-sm text-red-700">Registros com erros</p>
              <p class="mt-2 text-3xl font-bold text-red-600">
                {{ totalComErro }}
              </p>
            </article>
          </div>
        </section>

        <!-- Quantidade por tipo de erro -->
        <section class="mb-8 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="mb-5 text-xl font-semibold">Erros por categoria</h2>

          <div class="overflow-x-auto">
            <table class="w-full text-left text-sm">
              <thead class="border-b border-slate-200 bg-slate-50">
                <tr>
                  <th class="px-4 py-3">Tipo de validação</th>
                  <th class="px-4 py-3 text-right">Quantidade de erros</th>
                </tr>
              </thead>

              <tbody class="divide-y divide-slate-100">
                <tr v-for="categoria in categorias" :key="categoria">
                  <td class="px-4 py-3">{{ categoria }}</td>
                  <td class="px-4 py-3 text-right font-semibold">
                    {{ quantidadePorCategoria(categoria) }}
                  </td>
                </tr>

                <tr class="bg-slate-50">
                  <td class="px-4 py-3 font-semibold">Total de ocorrências</td>
                  <td class="px-4 py-3 text-right font-bold">
                    {{ obterErros().length }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <p class="mt-4 text-xs text-slate-400">
            Uma mesma linha pode conter mais de um erro. Por isso, o total de
            ocorrências pode ser maior que o número de registros com erros.
          </p>
        </section>

        <!-- Detalhamento -->
        <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div class="border-b border-slate-200 p-6">
            <h2 class="text-xl font-semibold">Detalhamento das inconsistências</h2>

            <p class="mt-1 text-sm text-slate-500">
              Cada ocorrência mostra a linha, o campo e a descrição do problema.
            </p>
          </div>

          <div v-if="linhasComErro.length === 0" class="p-8 text-center">
            <p class="font-semibold text-emerald-700">
              Nenhum erro encontrado!
            </p>
            <p class="mt-1 text-sm text-slate-500">
              Todos os registros passaram nas validações implementadas.
            </p>
          </div>

          <div v-else class="overflow-x-auto">
            <table class="w-full min-w-[700px] text-left text-sm">
              <thead class="bg-slate-50 text-slate-600">
                <tr>
                  <th class="px-4 py-3">Linha</th>
                  <th class="px-4 py-3">Campo</th>
                  <th class="px-4 py-3">Categoria</th>
                  <th class="px-4 py-3">Descrição do erro</th>
                </tr>
              </thead>

              <tbody class="divide-y divide-slate-100">
                <tr
                  v-for="(item, indice) in obterErros()"
                  :key="indice"
                  class="align-top hover:bg-slate-50"
                >
                  <td class="px-4 py-3">{{ item.linha }}</td>
                  <td class="px-4 py-3 font-medium">{{ item.campo }}</td>
                  <td class="px-4 py-3">{{ item.categoria }}</td>
                  <td class="px-4 py-3 text-red-700">{{ item.descricao }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <div class="mt-6">
          <RouterLink
            to="/app/upload"
            class="inline-block rounded-lg bg-blue-600 px-5 py-3 font-semibold text-white hover:bg-blue-700"
          >
            Voltar para upload
          </RouterLink>
        </div>
      </template>
    </div>
  </main>
</template>