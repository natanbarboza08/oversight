<script setup>
import { ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useUploadStore } from '../stores/uploadStore'

const uploadStore = useUploadStore()

// Conecta os dados da store à tela
const {
  arquivo,
  dadosTratados,
  erro,
  dataUpload,
  totalLinhas,
  totalColunas,
  totalValidas,
  totalComErro,
  colunas,
  linhasComErro
} = storeToRefs(uploadStore)

const carregando = ref(false)
const arrastando = ref(false)

// Lê e processa o arquivo escolhido (pelo seletor ou arrastado para a área)
async function processarArquivo(file) {
  if (!file) return

  carregando.value = true

  try {
    await uploadStore.lerArquivo(file)
  } finally {
    carregando.value = false
  }
}

function selecionarArquivo(event) {
  const file = event.target.files?.[0]

  // Zera o input para permitir escolher o mesmo arquivo novamente
  event.target.value = ''

  processarArquivo(file)
}

function soltarArquivo(event) {
  arrastando.value = false
  processarArquivo(event.dataTransfer?.files?.[0])
}

function limparDados() {
  uploadStore.limpar()
}

// O faturamento fica numérico no store e é formatado em reais só na exibição
function formatarCelula(coluna, valor) {
  if (coluna === 'faturamento_anual' && typeof valor === 'number') {
    return valor.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    })
  }

  return valor ?? ''
}
</script>

<template>
  <div class="mx-auto max-w-7xl">
    <!-- Cabeçalho -->
    <header class="mb-8">
      <h1 class="font-destaque text-2xl font-bold uppercase leading-tight sm:text-3xl">
        Importação de dados
      </h1>

      <p class="mt-3 max-w-2xl text-lg text-suave">
        Envie uma planilha de clientes para processar, higienizar e identificar possíveis
        inconsistências nos dados.
      </p>
    </header>

    <!-- Área de upload -->
    <section class="rounded-2xl border border-borda bg-white p-6 sm:p-8">
      <div class="mb-5">
        <h2 class="text-xl font-semibold">Enviar planilha</h2>
        <p class="mt-1 text-sm text-suave">Formatos aceitos: XLSX, XLS e CSV.</p>
      </div>

      <label
        for="arquivo"
        :class="[
          'flex cursor-pointer flex-col items-center rounded-xl border-2 border-dashed p-8 text-center transition-colors focus-within:border-oliva sm:p-10',
          arrastando ? 'border-oliva bg-menta' : 'border-borda bg-fundo hover:bg-menta/50',
        ]"
        @dragover.prevent="arrastando = true"
        @dragleave.prevent="arrastando = false"
        @drop.prevent="soltarArquivo"
      >
        <span
          class="mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-azul-claro text-escuro"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-7 w-7"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="1.8"
            aria-hidden="true"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M4 16.5v1A2.5 2.5 0 006.5 20h11a2.5 2.5 0 002.5-2.5v-1M12 15V3m0 0L7.5 7.5M12 3l4.5 4.5"
            />
          </svg>
        </span>

        <span class="text-lg font-semibold">
          Arraste a planilha aqui ou clique para selecionar
        </span>

        <span class="mt-2 text-sm text-suave">
          A primeira aba da planilha será utilizada no processamento.
        </span>

        <span
          class="mt-5 inline-flex h-11 items-center rounded-lg bg-escuro px-6 text-base text-white transition hover:bg-oliva"
        >
          Selecionar arquivo
        </span>

        <input
          id="arquivo"
          type="file"
          accept=".xlsx,.xls,.csv"
          class="sr-only"
          @change="selecionarArquivo"
        />
      </label>

      <p v-if="arquivo" class="mt-4 break-all text-sm text-suave">
        Arquivo selecionado:
        <span class="font-semibold text-ink">{{ arquivo.name }}</span>
      </p>

      <p v-if="carregando" class="mt-4 font-medium text-oliva" role="status">
        Processando planilha...
      </p>

      <!-- Mensagem de erro geral -->
      <div
        v-if="erro"
        role="alert"
        class="mt-5 rounded-lg border border-erro/30 bg-erro/5 p-4 text-sm text-erro"
      >
        <p class="font-semibold">Não foi possível processar a planilha</p>
        <p class="mt-1">{{ erro }}</p>
      </div>

      <!-- Informações do arquivo -->
      <div
        v-if="dadosTratados.length > 0"
        class="mt-5 flex flex-col gap-3 rounded-lg bg-fundo p-4 text-sm sm:flex-row sm:items-center sm:justify-between"
      >
        <div>
          <p class="font-semibold">Planilha processada</p>
          <p v-if="dataUpload" class="mt-1 text-suave">
            Enviada em: {{ dataUpload.toLocaleString('pt-BR') }}
          </p>
        </div>

        <button
          type="button"
          class="rounded-lg border border-borda bg-white px-4 py-2 font-medium transition hover:bg-menta"
          @click="limparDados"
        >
          Limpar dados
        </button>
      </div>
    </section>

    <!-- Indicadores -->
    <section v-if="dadosTratados.length > 0" class="mt-8">
      <h2 class="mb-4 text-xl font-semibold">Resumo do processamento</h2>

      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <article class="rounded-xl border border-borda border-t-4 border-t-azul bg-white p-5">
          <p class="text-sm font-medium text-suave">Total de registros</p>
          <p class="mt-2 text-3xl font-bold">{{ totalLinhas }}</p>
        </article>

        <article class="rounded-xl border border-borda border-t-4 border-t-oliva bg-white p-5">
          <p class="text-sm font-medium text-suave">Registros válidos</p>
          <p class="mt-2 text-3xl font-bold text-oliva">{{ totalValidas }}</p>
        </article>

        <article class="rounded-xl border border-borda border-t-4 border-t-cobre bg-white p-5">
          <p class="text-sm font-medium text-suave">Registros com erros</p>
          <p class="mt-2 text-3xl font-bold text-erro">{{ totalComErro }}</p>
        </article>

        <article class="rounded-xl border border-borda border-t-4 border-t-aco bg-white p-5">
          <p class="text-sm font-medium text-suave">Colunas identificadas</p>
          <p class="mt-2 text-3xl font-bold">{{ totalColunas }}</p>
        </article>
      </div>
    </section>

    <!-- Tabela de registros -->
    <section
      v-if="dadosTratados.length > 0"
      class="mt-8 overflow-hidden rounded-2xl border border-borda bg-white"
    >
      <div class="border-b border-borda p-5 sm:p-6">
        <h2 class="text-xl font-semibold">Dados processados</h2>
        <p class="mt-1 text-sm text-suave">
          Confira os dados após a higienização e validação. Linhas com erro aparecem destacadas.
        </p>
      </div>

      <div class="max-h-[480px] overflow-auto">
        <table class="w-full min-w-max border-collapse text-left text-sm">
          <thead class="sticky top-0 bg-menta text-xs uppercase tracking-wide text-escuro">
            <tr>
              <th class="px-4 py-3">Linha</th>

              <th v-for="coluna in colunas" :key="coluna" class="px-4 py-3">
                {{ coluna.replaceAll('_', ' ') }}
              </th>

              <th class="px-4 py-3">Status</th>
            </tr>
          </thead>

          <tbody class="divide-y divide-campo">
            <tr
              v-for="linha in dadosTratados"
              :key="linha.numero_linha"
              :class="linha.erros.length > 0 ? 'bg-argila/15' : 'hover:bg-fundo'"
            >
              <td class="px-4 py-3 text-suave">{{ linha.numero_linha }}</td>

              <td v-for="coluna in colunas" :key="coluna" class="max-w-xs px-4 py-3">
                <span
                  class="block truncate"
                  :title="String(formatarCelula(coluna, linha[coluna]))"
                >
                  {{ formatarCelula(coluna, linha[coluna]) }}
                </span>
              </td>

              <td class="px-4 py-3">
                <span
                  v-if="linha.erros.length === 0"
                  class="rounded-full bg-menta px-3 py-1 text-xs font-semibold text-escuro"
                >
                  Válido
                </span>

                <span
                  v-else
                  class="rounded-full bg-erro/10 px-3 py-1 text-xs font-semibold text-erro"
                >
                  {{ linha.erros.length }}
                  {{ linha.erros.length === 1 ? 'erro' : 'erros' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Lista detalhada de erros -->
    <section
      v-if="linhasComErro.length > 0"
      class="mt-8 rounded-2xl border border-erro/30 bg-white"
    >
      <div class="border-b border-erro/20 p-5 sm:p-6">
        <h2 class="text-xl font-semibold text-erro">Detalhamento dos erros</h2>
        <p class="mt-1 text-sm text-suave">
          Verifique os problemas encontrados em cada linha da planilha.
        </p>
      </div>

      <div class="space-y-4 p-5 sm:p-6">
        <article
          v-for="linha in linhasComErro"
          :key="linha.numero_linha"
          class="rounded-xl border border-erro/20 bg-erro/5 p-4"
        >
          <h3 class="font-semibold">
            Linha {{ linha.numero_linha }} — {{ linha.codigo_cliente || 'Sem código' }}
          </h3>

          <ul class="mt-3 space-y-2">
            <li
              v-for="(mensagem, indice) in linha.erros"
              :key="indice"
              class="flex gap-2 text-sm text-erro"
            >
              <span aria-hidden="true">•</span>
              <span>{{ mensagem }}</span>
            </li>
          </ul>
        </article>
      </div>
    </section>

    <!-- Estado inicial -->
    <section
      v-if="dadosTratados.length === 0 && !erro && !carregando"
      class="mt-8 rounded-2xl border border-borda bg-white px-6 py-10 text-center"
    >
      <h2 class="text-lg font-semibold">Nenhuma planilha processada</h2>
      <p class="mx-auto mt-2 max-w-md text-sm text-suave">
        Selecione um arquivo acima para visualizar os dados e conferir possíveis inconsistências.
      </p>
    </section>
  </div>
</template>