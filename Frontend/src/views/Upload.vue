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
const arquivoInput = ref(null)

// Lê e processa o arquivo selecionado
async function selecionarArquivo(event) {
  const file = event.target.files?.[0]

  if (!file) return

  carregando.value = true

  try {
    await uploadStore.lerArquivo(file)
  } finally {
    carregando.value = false
  }
}

// Limpa os dados e permite selecionar o mesmo arquivo novamente
function limparDados() {
  uploadStore.limpar()

  if (arquivoInput.value) {
    arquivoInput.value.value = ''
  }
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

        <h1 class="text-3xl font-bold tracking-tight sm:text-4xl">
          Importação de dados
        </h1>

        <p class="mt-3 max-w-2xl text-slate-500">
          Envie uma planilha de clientes para processar, higienizar e
          identificar possíveis inconsistências nos dados.
        </p>
      </header>

      <!-- Área de upload -->
      <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
        <div class="mb-5">
          <h2 class="text-xl font-semibold">Enviar planilha</h2>
          <p class="mt-1 text-sm text-slate-500">
            Formatos aceitos: XLSX, XLS e CSV.
          </p>
        </div>

        <div
          class="rounded-xl border-2 border-dashed border-blue-200 bg-blue-50/50 p-6 text-center sm:p-10"
        >
          <div
            class="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-blue-100 text-blue-600"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-7 w-7"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="1.8"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M4 16.5v1A2.5 2.5 0 006.5 20h11a2.5 2.5 0 002.5-2.5v-1M12 15V3m0 0L7.5 7.5M12 3l4.5 4.5"
              />
            </svg>
          </div>

          <p class="font-semibold">
            Selecione um arquivo do seu computador
          </p>

          <p class="mt-2 text-sm text-slate-500">
            A primeira aba da planilha será utilizada no processamento.
          </p>

          <input
            ref="arquivoInput"
            type="file"
            accept=".xlsx,.xls,.csv"
            class="mx-auto mt-5 block w-full max-w-sm cursor-pointer rounded-lg border border-slate-200 bg-white text-sm text-slate-600 file:mr-4 file:cursor-pointer file:border-0 file:bg-blue-600 file:px-4 file:py-3 file:font-semibold file:text-white hover:file:bg-blue-700"
            @change="selecionarArquivo"
          />

          <p v-if="arquivo" class="mt-4 break-all text-sm text-slate-600">
            Arquivo selecionado:
            <span class="font-semibold">{{ arquivo.name }}</span>
          </p>

          <p v-if="carregando" class="mt-4 font-medium text-blue-600">
            Processando planilha...
          </p>
        </div>

        <!-- Mensagem de erro geral -->
        <div
          v-if="erro"
          role="alert"
          class="mt-5 rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700"
        >
          <p class="font-semibold">Não foi possível processar a planilha</p>
          <p class="mt-1">{{ erro }}</p>
        </div>

        <!-- Informações do arquivo -->
        <div
          v-if="dadosTratados.length > 0"
          class="mt-5 flex flex-col gap-3 rounded-lg bg-slate-50 p-4 text-sm sm:flex-row sm:items-center sm:justify-between"
        >
          <div>
            <p class="font-semibold text-slate-700">
              Planilha processada
            </p>
            <p v-if="dataUpload" class="mt-1 text-slate-500">
              Enviada em: {{ dataUpload.toLocaleString('pt-BR') }}
            </p>
          </div>

          <button
            type="button"
            class="rounded-lg border border-slate-300 bg-white px-4 py-2 font-medium text-slate-700 transition hover:bg-slate-100"
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
          <article class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
            <p class="text-sm font-medium text-slate-500">Total de registros</p>
            <p class="mt-2 text-3xl font-bold">{{ totalLinhas }}</p>
          </article>

          <article class="rounded-xl border border-emerald-200 bg-white p-5 shadow-sm">
            <p class="text-sm font-medium text-emerald-700">Registros válidos</p>
            <p class="mt-2 text-3xl font-bold text-emerald-600">
              {{ totalValidas }}
            </p>
          </article>

          <article class="rounded-xl border border-red-200 bg-white p-5 shadow-sm">
            <p class="text-sm font-medium text-red-700">Registros com erros</p>
            <p class="mt-2 text-3xl font-bold text-red-600">
              {{ totalComErro }}
            </p>
          </article>

          <article class="rounded-xl border border-blue-200 bg-white p-5 shadow-sm">
            <p class="text-sm font-medium text-blue-700">Colunas identificadas</p>
            <p class="mt-2 text-3xl font-bold text-blue-600">
              {{ totalColunas }}
            </p>
          </article>
        </div>
      </section>

      <!-- Tabela de registros -->
      <section
        v-if="dadosTratados.length > 0"
        class="mt-8 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm"
      >
        <div class="border-b border-slate-200 p-5 sm:p-6">
          <h2 class="text-xl font-semibold">Dados processados</h2>
          <p class="mt-1 text-sm text-slate-500">
            Confira os dados após a higienização e validação.
          </p>
        </div>

        <div class="max-h-[480px] overflow-auto">
          <table class="w-full min-w-max border-collapse text-left text-sm">
            <thead class="sticky top-0 bg-slate-100 text-xs uppercase tracking-wide text-slate-600">
              <tr>
                <th class="px-4 py-3">Linha</th>

                <th
                  v-for="coluna in colunas"
                  :key="coluna"
                  class="px-4 py-3"
                >
                  {{ coluna.replaceAll('_', ' ') }}
                </th>

                <th class="px-4 py-3">Status</th>
              </tr>
            </thead>

            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="linha in dadosTratados"
                :key="linha.numero_linha"
                class="hover:bg-slate-50"
              >
                <td class="px-4 py-3 text-slate-500">
                  {{ linha.numero_linha }}
                </td>

                <td
                  v-for="coluna in colunas"
                  :key="coluna"
                  class="max-w-xs px-4 py-3"
                >
                  <span class="block truncate" :title="String(linha[coluna] ?? '')">
                    {{ linha[coluna] }}
                  </span>
                </td>

                <td class="px-4 py-3">
                  <span
                    v-if="linha.erros.length === 0"
                    class="rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold text-emerald-700"
                  >
                    Válido
                  </span>

                  <span
                    v-else
                    class="rounded-full bg-red-100 px-3 py-1 text-xs font-semibold text-red-700"
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
        class="mt-8 rounded-2xl border border-red-200 bg-white shadow-sm"
      >
        <div class="border-b border-red-100 p-5 sm:p-6">
          <h2 class="text-xl font-semibold text-red-700">
            Detalhamento dos erros
          </h2>
          <p class="mt-1 text-sm text-slate-500">
            Verifique os problemas encontrados em cada linha da planilha.
          </p>
        </div>

        <div class="space-y-4 p-5 sm:p-6">
          <article
            v-for="linha in linhasComErro"
            :key="linha.numero_linha"
            class="rounded-xl border border-red-100 bg-red-50/50 p-4"
          >
            <h3 class="font-semibold text-slate-800">
              Linha {{ linha.numero_linha }} — {{ linha.codigo_cliente || 'Sem código' }}
            </h3>

            <ul class="mt-3 space-y-2">
              <li
                v-for="(mensagem, indice) in linha.erros"
                :key="indice"
                class="flex gap-2 text-sm text-red-700"
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
        class="mt-8 rounded-2xl border border-slate-200 bg-white px-6 py-10 text-center"
      >
        <h2 class="text-lg font-semibold">Nenhuma planilha processada</h2>
        <p class="mx-auto mt-2 max-w-md text-sm text-slate-500">
          Selecione um arquivo acima para visualizar os dados e conferir
          possíveis inconsistências.
        </p>
      </section>

      <footer class="mt-8 pb-4 text-center text-xs text-slate-400">
        CTI Provedor · Higienização e validação de dados
      </footer>
    </div>
  </main>
</template>
```
