<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { dashboardService, mensagemDeErro } from '../services/api'

const lotes = ref([])
const carregando = ref(true)
const erro = ref('')

function dataLocal(valor) {
  return valor ? new Date(valor).toLocaleString('pt-BR') : '—'
}

async function carregar() {
  carregando.value = true
  erro.value = ''
  try {
    lotes.value = await dashboardService.lotes()
  } catch (falha) {
    erro.value = mensagemDeErro(falha)
  } finally {
    carregando.value = false
  }
}

onMounted(carregar)
</script>

<template>
  <div class="mx-auto max-w-7xl">
    <header class="mb-8">
      <h1 class="font-destaque text-2xl font-bold uppercase sm:text-3xl">Histórico de relatórios</h1>
      <p class="mt-3 max-w-2xl text-suave">
        Lotes persistidos no banco de dados e disponibilizados pelo backend Java.
      </p>
    </header>

    <p v-if="erro" class="mb-5 rounded-lg bg-erro/10 p-4 text-sm text-erro" role="alert">
      {{ erro }}
    </p>

    <section v-if="carregando" class="rounded-2xl border border-borda bg-white p-10 text-center" role="status">
      Carregando histórico...
    </section>
    <section v-else-if="lotes.length === 0" class="rounded-2xl border border-borda bg-white p-10 text-center">
      <h2 class="text-xl font-semibold">Nenhum lote encontrado</h2>
      <p class="mt-2 text-suave">Envie uma planilha para começar o histórico de análises.</p>
      <RouterLink to="/app/upload" class="mt-5 inline-flex rounded-lg bg-escuro px-5 py-3 text-white">
        Enviar planilha
      </RouterLink>
    </section>

    <section v-else class="overflow-x-auto rounded-2xl border border-borda bg-white">
      <table class="w-full min-w-[720px] text-left text-sm">
        <thead class="bg-fundo text-suave">
          <tr>
            <th class="px-5 py-3">Lote</th>
            <th class="px-5 py-3">Arquivo</th>
            <th class="px-5 py-3">Data</th>
            <th class="px-5 py-3">Registros</th>
            <th class="px-5 py-3">Status</th>
            <th class="px-5 py-3">Ação</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-campo">
          <tr v-for="lote in lotes" :key="lote.id">
            <td class="px-5 py-4">#{{ lote.id }}</td>
            <td class="px-5 py-4">{{ lote.arquivoNome }}</td>
            <td class="px-5 py-4">{{ dataLocal(lote.enviadoEm) }}</td>
            <td class="px-5 py-4">{{ lote.linhasValidas }} / {{ lote.totalLinhas }}</td>
            <td class="px-5 py-4">
              <span class="rounded-full bg-fundo px-3 py-1">{{ lote.status }}</span>
            </td>
            <td class="px-5 py-4">
              <RouterLink
                v-if="lote.status === 'PROCESSADO'"
                :to="{ name: 'graficos', query: { loteId: lote.id } }"
                class="font-medium text-oliva underline"
              >
                Ver análise
              </RouterLink>
              <span v-else class="text-suave">{{ lote.mensagem || 'Sem análise disponível' }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>
