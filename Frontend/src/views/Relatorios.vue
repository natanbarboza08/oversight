<script setup>
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUploadStore, ORDEM_CATEGORIAS } from '../stores/uploadStore'

const uploadStore = useUploadStore()

const {
  arquivo,
  dadosTratados,
  dataUpload,
  totalLinhas,
  totalValidas,
  totalComErro,
  linhasComErro,
  ocorrencias,
  totalOcorrencias,
  contagemPorCategoria
} = storeToRefs(uploadStore)

// Filtro do detalhamento ('' = todas as categorias)
const categoriaFiltro = ref('')

const percentualValidos = computed(() =>
  totalLinhas.value ? Math.round((totalValidas.value / totalLinhas.value) * 100) : 0
)

// Maior quantidade entre as categorias, para dimensionar as barras
const maiorContagem = computed(() =>
  Math.max(1, ...Object.values(contagemPorCategoria.value))
)

const ocorrenciasFiltradas = computed(() =>
  categoriaFiltro.value
    ? ocorrencias.value.filter((item) => item.categoria === categoriaFiltro.value)
    : ocorrencias.value
)

function larguraBarra(categoria) {
  return `${(contagemPorCategoria.value[categoria] / maiorContagem.value) * 100}%`
}

function formatarData(data) {
  if (!data) return ''
  return new Date(data).toLocaleString('pt-BR')
}
</script>

<template>
  <div class="mx-auto max-w-7xl">
    <!-- Cabeçalho -->
    <header class="mb-8">
      <h1 class="font-destaque text-2xl font-bold uppercase leading-tight sm:text-3xl">
        Relatório de validação
      </h1>

      <p class="mt-3 max-w-2xl text-lg text-suave">
        Consulte os resultados da análise e os problemas encontrados na planilha.
      </p>
    </header>

    <!-- Quando ainda não há dados -->
    <section
      v-if="dadosTratados.length === 0"
      class="rounded-2xl border border-borda bg-white p-10 text-center"
    >
      <h2 class="text-xl font-semibold">Nenhuma análise disponível</h2>

      <p class="mx-auto mt-2 max-w-lg text-suave">
        Envie uma planilha na tela de upload para gerar o relatório de validação.
      </p>

      <RouterLink
        to="/app/upload"
        class="mt-6 inline-flex h-12 items-center rounded-lg bg-escuro px-6 text-white transition hover:bg-oliva"
      >
        Ir para upload
      </RouterLink>
    </section>

    <template v-else>
      <!-- Informações do arquivo -->
      <section class="mb-8 rounded-2xl border border-borda bg-white p-6">
        <h2 class="text-lg font-semibold">Arquivo analisado</h2>

        <p class="mt-2 break-all text-suave">
          {{ arquivo?.name || 'Nome não disponível' }}
        </p>

        <p class="mt-1 text-sm text-suave">
          Análise realizada em: {{ formatarData(dataUpload) }}
        </p>
      </section>

      <!-- Indicadores -->
      <section class="mb-8">
        <h2 class="mb-4 text-xl font-semibold">Resumo da validação</h2>

        <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
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
        </div>

        <!-- Percentual de registros válidos -->
        <div class="mt-4 rounded-xl border border-borda bg-white p-5">
          <div class="flex items-baseline justify-between">
            <p class="text-sm font-medium text-suave">Registros sem nenhum erro</p>
            <p class="text-lg font-bold">{{ percentualValidos }}%</p>
          </div>

          <div
            class="mt-3 h-3 overflow-hidden rounded-full bg-campo"
            role="progressbar"
            aria-label="Percentual de registros válidos"
            :aria-valuenow="percentualValidos"
            aria-valuemin="0"
            aria-valuemax="100"
          >
            <div
              class="h-full rounded-full bg-oliva transition-all"
              :style="{ width: percentualValidos + '%' }"
            ></div>
          </div>
        </div>
      </section>

      <!-- Quantidade por tipo de erro -->
      <section class="mb-8 rounded-2xl border border-borda bg-white p-6">
        <h2 class="mb-5 text-xl font-semibold">Erros por categoria</h2>

        <div class="overflow-x-auto">
          <table class="w-full text-left text-sm">
            <thead class="border-b border-borda bg-fundo">
              <tr>
                <th class="px-4 py-3">Tipo de validação</th>
                <th class="hidden px-4 py-3 sm:table-cell"></th>
                <th class="px-4 py-3 text-right">Quantidade de erros</th>
              </tr>
            </thead>

            <tbody class="divide-y divide-campo">
              <tr v-for="categoria in ORDEM_CATEGORIAS" :key="categoria">
                <td class="px-4 py-3">{{ categoria }}</td>

                <td class="hidden w-1/3 px-4 py-3 sm:table-cell">
                  <div class="h-2 overflow-hidden rounded-full bg-campo">
                    <div
                      class="h-full rounded-full bg-cobre"
                      :style="{ width: larguraBarra(categoria) }"
                    ></div>
                  </div>
                </td>

                <td class="px-4 py-3 text-right font-semibold">
                  {{ contagemPorCategoria[categoria] }}
                </td>
              </tr>

              <tr class="bg-fundo">
                <td class="px-4 py-3 font-semibold">Total de ocorrências</td>
                <td class="hidden sm:table-cell"></td>
                <td class="px-4 py-3 text-right font-bold">{{ totalOcorrencias }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <p class="mt-4 text-xs text-suave">
          Uma mesma linha pode conter mais de um erro. Por isso, o total de ocorrências pode ser
          maior que o número de registros com erros.
        </p>
      </section>

      <!-- Detalhamento -->
      <section class="overflow-hidden rounded-2xl border border-borda bg-white">
        <div
          class="flex flex-col gap-4 border-b border-borda p-6 sm:flex-row sm:items-end sm:justify-between"
        >
          <div>
            <h2 class="text-xl font-semibold">Detalhamento das inconsistências</h2>

            <p class="mt-1 text-sm text-suave">
              Cada ocorrência mostra a linha, o campo e a descrição do problema.
            </p>
          </div>

          <div v-if="linhasComErro.length > 0">
            <label for="filtro" class="block text-sm text-suave">Filtrar por categoria</label>
            <select
              id="filtro"
              v-model="categoriaFiltro"
              class="mt-1 h-11 w-full rounded-lg border border-campo bg-white px-3 text-sm outline-none focus:border-oliva sm:w-60"
            >
              <option value="">Todas as categorias</option>
              <option v-for="categoria in ORDEM_CATEGORIAS" :key="categoria" :value="categoria">
                {{ categoria }} ({{ contagemPorCategoria[categoria] }})
              </option>
            </select>
          </div>
        </div>

        <div v-if="linhasComErro.length === 0" class="p-8 text-center">
          <p class="font-semibold text-oliva">Nenhum erro encontrado!</p>
          <p class="mt-1 text-sm text-suave">
            Todos os registros passaram nas validações implementadas.
          </p>
        </div>

        <div v-else-if="ocorrenciasFiltradas.length === 0" class="p-8 text-center">
          <p class="text-sm text-suave">Nenhuma ocorrência nesta categoria.</p>
        </div>

        <div v-else class="max-h-[560px] overflow-auto">
          <table class="w-full min-w-[700px] text-left text-sm">
            <thead class="sticky top-0 bg-menta text-escuro">
              <tr>
                <th class="px-4 py-3">Linha</th>
                <th class="px-4 py-3">Campo</th>
                <th class="px-4 py-3">Categoria</th>
                <th class="px-4 py-3">Descrição do erro</th>
              </tr>
            </thead>

            <tbody class="divide-y divide-campo">
              <tr
                v-for="(item, indice) in ocorrenciasFiltradas"
                :key="indice"
                class="align-top hover:bg-fundo"
              >
                <td class="px-4 py-3">{{ item.linha }}</td>
                <td class="px-4 py-3 font-medium">{{ item.campo }}</td>
                <td class="px-4 py-3">{{ item.categoria }}</td>
                <td class="px-4 py-3 text-erro">{{ item.mensagem }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <div class="mt-6">
        <RouterLink
          to="/app/upload"
          class="inline-flex h-12 items-center rounded-lg bg-escuro px-6 text-white transition hover:bg-oliva"
        >
          Voltar para upload
        </RouterLink>
      </div>
    </template>
  </div>
</template>