<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { dashboardService, mensagemDeErro } from '../services/api'
import BaseChart from '../components/BaseChart.vue'

const route = useRoute()
const lotes = ref([])
const loteSelecionado = ref(route.query.loteId ? String(route.query.loteId) : '')
const lote = ref(null)
const estatisticas = ref(null)
const carregando = ref(true)
const erro = ref('')

const resumo = computed(() => estatisticas.value?.resumo || {})
const moeda = (valor) => Number(valor || 0).toLocaleString('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  maximumFractionDigits: 2,
})

const segmentosData = computed(() => ({
  labels: (estatisticas.value?.porSegmento || []).map((item) => item.segmento),
  datasets: [{
    label: 'Clientes',
    data: (estatisticas.value?.porSegmento || []).map((item) => item.clientes),
    backgroundColor: '#606C3A',
    borderRadius: 6,
  }],
}))

const nivelData = computed(() => ({
  labels: (estatisticas.value?.porNivel || []).map((item) => `Nível ${item.nivel}`),
  datasets: [{
    data: (estatisticas.value?.porNivel || []).map((item) => item.clientes),
    backgroundColor: ['#606C3A', '#96C5F7', '#DDA15E'],
  }],
}))

const servicosData = computed(() => ({
  labels: (estatisticas.value?.porServico || []).map((item) => item.servico),
  datasets: [{
    label: 'Contratos',
    data: (estatisticas.value?.porServico || []).map((item) => item.contratos),
    backgroundColor: '#93ACB5',
    borderRadius: 6,
  }],
}))

const mesesData = computed(() => ({
  labels: (estatisticas.value?.porMes || []).map((item) => item.mes),
  datasets: [{
    label: 'Faturamento',
    data: (estatisticas.value?.porMes || []).map((item) => item.faturamento),
    borderColor: '#606C3A',
    backgroundColor: 'rgba(96, 108, 58, 0.15)',
    fill: true,
    tension: 0.25,
  }],
}))

const opcoesEixo = {
  plugins: { legend: { display: false } },
  scales: { y: { beginAtZero: true } },
}

async function carregarDashboard(id) {
  carregando.value = true
  erro.value = ''
  try {
    const resposta = await dashboardService.obter(id || undefined)
    lote.value = resposta.lote
    estatisticas.value = resposta.estatisticas
    loteSelecionado.value = String(resposta.lote.id)
  } catch (falha) {
    if (falha?.response?.status === 404) {
      lote.value = null
      estatisticas.value = null
    } else {
      erro.value = mensagemDeErro(falha)
    }
  } finally {
    carregando.value = false
  }
}

async function carregar() {
  carregando.value = true
  erro.value = ''
  try {
    lotes.value = await dashboardService.lotes()
    await carregarDashboard(loteSelecionado.value || undefined)
  } catch (falha) {
    erro.value = mensagemDeErro(falha)
    carregando.value = false
  }
}

watch(loteSelecionado, async (id) => {
  if (id && id !== String(lote.value?.id ?? '')) await carregarDashboard(id)
})

onMounted(carregar)
</script>

<template>
  <div class="mx-auto max-w-7xl">
    <header class="mb-8 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 class="font-destaque text-2xl font-bold uppercase sm:text-3xl">Dashboard analítico</h1>
        <p class="mt-3 max-w-2xl text-suave">
          Indicadores calculados pelo serviço Python a partir dos lotes persistidos no PostgreSQL.
        </p>
      </div>
      <div v-if="lotes.length" class="flex items-center gap-2">
        <label for="lote" class="text-sm font-medium">Lote</label>
        <select id="lote" v-model="loteSelecionado" class="rounded-lg border border-borda bg-white px-3 py-2">
          <option v-for="item in lotes" :key="item.id" :value="String(item.id)">
            #{{ item.id }} — {{ item.arquivoNome }}
          </option>
        </select>
      </div>
    </header>

    <p v-if="erro" class="mb-5 rounded-lg bg-erro/10 p-4 text-sm text-erro" role="alert">
      {{ erro }}
    </p>
    <section v-if="carregando" class="rounded-2xl border border-borda bg-white p-10 text-center" role="status">
      Carregando indicadores...
    </section>
    <section
      v-else-if="!lote"
      class="rounded-2xl border border-borda bg-white p-10 text-center"
    >
      <h2 class="text-xl font-semibold">Ainda não há lotes processados</h2>
      <p class="mx-auto mt-2 max-w-lg text-suave">
        Envie uma planilha válida para gerar os indicadores e gráficos.
      </p>
      <RouterLink to="/app/upload" class="mt-6 inline-flex rounded-lg bg-escuro px-6 py-3 text-white">
        Ir para upload
      </RouterLink>
    </section>
    <template v-else-if="estatisticas">
      <p class="mb-5 text-sm text-suave">
        Lote #{{ lote.id }} · {{ lote.arquivoNome }} · {{ lote.enviadoEm ? new Date(lote.enviadoEm).toLocaleString('pt-BR') : '' }}
      </p>

      <section class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <article class="rounded-2xl border border-borda bg-white p-5">
          <p class="text-sm text-suave">Clientes</p><p class="mt-2 text-2xl font-bold">{{ resumo.totalClientes }}</p>
        </article>
        <article class="rounded-2xl border border-borda bg-white p-5">
          <p class="text-sm text-suave">Faturamento total</p><p class="mt-2 text-2xl font-bold">{{ moeda(resumo.faturamentoTotal) }}</p>
        </article>
        <article class="rounded-2xl border border-borda bg-white p-5">
          <p class="text-sm text-suave">Faturamento médio</p><p class="mt-2 text-2xl font-bold">{{ moeda(resumo.media) }}</p>
        </article>
        <article class="rounded-2xl border border-borda bg-white p-5">
          <p class="text-sm text-suave">Mediana</p><p class="mt-2 text-2xl font-bold">{{ moeda(resumo.mediana) }}</p>
        </article>
      </section>

      <section class="mt-6 grid gap-6 xl:grid-cols-2">
        <article class="rounded-2xl border border-borda bg-white p-5">
          <h2 class="mb-4 font-semibold">Clientes por segmento</h2>
          <BaseChart type="bar" :data="segmentosData" :options="opcoesEixo" label="Clientes por segmento" />
        </article>
        <article class="rounded-2xl border border-borda bg-white p-5">
          <h2 class="mb-4 font-semibold">Distribuição por nível</h2>
          <BaseChart
            type="doughnut"
            :data="nivelData"
            :options="{ plugins: { legend: { position: 'bottom' } } }"
            label="Distribuição de clientes por nível"
          />
        </article>
        <article class="rounded-2xl border border-borda bg-white p-5">
          <h2 class="mb-4 font-semibold">Contratos por serviço</h2>
          <BaseChart type="bar" :data="servicosData" :options="opcoesEixo" label="Contratos por serviço" />
        </article>
        <article class="rounded-2xl border border-borda bg-white p-5">
          <h2 class="mb-4 font-semibold">Faturamento por mês</h2>
          <BaseChart
            type="line"
            :data="mesesData"
            :options="{ plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } }"
            label="Faturamento por mês"
          />
        </article>
      </section>

      <section class="mt-6 grid gap-6 lg:grid-cols-2">
        <article class="rounded-2xl border border-borda bg-white p-5">
          <h2 class="mb-4 text-lg font-semibold">Maiores clientes</h2>
          <div class="overflow-x-auto">
            <table class="w-full text-left text-sm">
              <thead class="text-suave"><tr><th class="py-2">Cliente</th><th class="py-2">Segmento</th><th class="py-2 text-right">Faturamento</th></tr></thead>
              <tbody class="divide-y divide-campo">
                <tr v-for="cliente in estatisticas.topClientes || []" :key="cliente.codigo">
                  <td class="py-2">{{ cliente.nome }}</td><td class="py-2">{{ cliente.segmento }}</td>
                  <td class="py-2 text-right">{{ moeda(cliente.faturamento) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
        <article class="rounded-2xl border border-borda bg-white p-5">
          <h2 class="mb-4 text-lg font-semibold">Insights</h2>
          <ul class="space-y-3 text-sm text-suave">
            <li v-for="(insight, indice) in estatisticas.insights || []" :key="indice" class="rounded-lg bg-fundo p-3">
              {{ insight }}
            </li>
          </ul>
        </article>
      </section>
    </template>
    <section v-else-if="lote" class="rounded-2xl border border-borda bg-white p-6">
      <h2 class="font-semibold">Lote #{{ lote.id }} — {{ lote.status }}</h2>
      <p class="mt-2 text-suave">{{ lote.mensagem || 'Os indicadores ainda não estão disponíveis.' }}</p>
    </section>
  </div>
</template>
