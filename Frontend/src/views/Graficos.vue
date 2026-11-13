<template>
  <div class="mx-auto max-w-7xl">
    <header class="mb-8">
      <h1 class="font-destaque text-2xl font-bold uppercase leading-tight sm:text-3xl">
        Dashboard analítico
      </h1>

      <p class="mt-3 max-w-2xl text-lg text-suave">
        Acompanhe a qualidade dos dados, o volume de inconsistências e os principais pontos de atenção.
      </p>
    </header>

    <section
      v-if="dadosTratados.length === 0"
      class="rounded-2xl border border-borda bg-white p-10 text-center"
    >
      <h2 class="text-xl font-semibold">Sem dados para visualizar</h2>
      <p class="mx-auto mt-2 max-w-lg text-suave">
        Faça o upload de uma planilha na tela de importação para gerar o dashboard de qualidade de dados.
      </p>

      <RouterLink
        to="/app/upload"
        class="mt-6 inline-flex h-12 items-center rounded-lg bg-escuro px-6 text-white transition hover:bg-oliva"
      >
        Ir para upload
      </RouterLink>
    </section>

    <template v-else>
      <section class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        <article class="rounded-2xl border border-borda bg-white p-5">
          <p class="text-sm font-medium text-suave">Registros processados</p>
          <p class="mt-3 text-3xl font-bold">{{ totalLinhas }}</p>
        </article>

        <article class="rounded-2xl border border-borda bg-white p-5">
          <p class="text-sm font-medium text-suave">Dados válidos</p>
          <p class="mt-3 text-3xl font-bold text-oliva">{{ totalValidas }}</p>
        </article>

        <article class="rounded-2xl border border-borda bg-white p-5">
          <p class="text-sm font-medium text-suave">Com inconsistências</p>
          <p class="mt-3 text-3xl font-bold text-erro">{{ totalComErro }}</p>
        </article>

        <article class="rounded-2xl border border-borda bg-white p-5">
          <p class="text-sm font-medium text-suave">Ocorrências totais</p>
          <p class="mt-3 text-3xl font-bold">{{ ocorrencias.length }}</p>
        </article>
      </section>

      <section class="mt-8 grid gap-6 xl:grid-cols-[1.35fr_0.95fr]">
        <article class="rounded-2xl border border-borda bg-white p-6">
          <div class="flex items-center justify-between">
            <h2 class="text-xl font-semibold">Ocorrências por categoria</h2>
            <span class="text-sm text-suave">Última análise</span>
          </div>

          <div class="mt-6 flex h-64 items-end gap-3">
            <div
              v-for="item in dadosGrafico"
              :key="item.label"
              class="flex h-full flex-1 flex-col justify-end"
            >
              <span class="mb-2 text-center text-xs text-suave">{{ item.valor }}</span>
              <div
                class="w-full rounded-t-xl bg-gradient-to-t from-oliva to-azul transition-all"
                :style="{ height: `${(item.valor / maiorValor) * 100}%` }"
                :title="`${item.label}: ${item.valor}`"
              ></div>
              <span class="mt-2 text-center text-[10px] text-suave">{{ item.label }}</span>
            </div>
          </div>
        </article>

        <article class="rounded-2xl border border-borda bg-white p-6">
          <h2 class="text-xl font-semibold">Qualidade dos dados</h2>

          <div class="mt-5 flex items-center justify-center">
            <div
              class="relative flex h-48 w-48 items-center justify-center rounded-full"
              :style="donutStyle"
            >
              <div class="flex h-28 w-28 flex-col items-center justify-center rounded-full bg-white shadow-inner">
                <span class="text-3xl font-bold">{{ percentualValidos }}%</span>
                <span class="text-xs text-suave">válidos</span>
              </div>
            </div>
          </div>

          <div class="mt-5 space-y-3 text-sm">
            <div class="flex items-center justify-between rounded-lg bg-fundo px-3 py-2">
              <span>Dados válidos</span>
              <strong class="text-oliva">{{ percentualValidos }}%</strong>
            </div>
            <div class="flex items-center justify-between rounded-lg bg-fundo px-3 py-2">
              <span>Dados com erro</span>
              <strong class="text-erro">{{ percentualErro }}%</strong>
            </div>
          </div>
        </article>
      </section>

      <section class="mt-8 grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
        <article class="rounded-2xl border border-borda bg-white p-6">
          <h2 class="text-xl font-semibold">Principais focos de correção</h2>

          <div class="mt-5 space-y-4">
            <div
              v-for="item in topErros"
              :key="item.label"
              class="rounded-xl border border-campo bg-fundo p-4"
            >
              <div class="flex items-center justify-between gap-3">
                <span class="font-medium">{{ item.label }}</span>
                <span class="text-sm text-suave">{{ item.valor }} ocorrências</span>
              </div>

              <div class="mt-3 h-2 overflow-hidden rounded-full bg-white">
                <div
                  class="h-full rounded-full bg-cobre"
                  :style="{ width: `${(item.valor / maiorValor) * 100}%` }"
                ></div>
              </div>
            </div>
          </div>
        </article>

        <article class="rounded-2xl border border-borda bg-white p-6">
          <h2 class="text-xl font-semibold">Resumo executivo</h2>

          <ul class="mt-5 space-y-3 text-sm text-suave">
            <li class="rounded-lg bg-fundo px-3 py-2">
              O dataset está <strong class="text-ink">{{ percentualValidos }}%</strong> consistente.
            </li>
            <li class="rounded-lg bg-fundo px-3 py-2">
              Os maiores riscos estão em categoria de campos vazios e inconsistências de padrão.
            </li>
            <li class="rounded-lg bg-fundo px-3 py-2">
              O processo está preparado para receber mais dados sem alterar a estrutura do dashboard.
            </li>
          </ul>

          <RouterLink
            to="/app/upload"
            class="mt-6 inline-flex h-11 items-center rounded-lg bg-escuro px-4 text-white hover:bg-oliva"
          >
            Reprocessar planilha
          </RouterLink>
        </article>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUploadStore, ORDEM_CATEGORIAS } from '../stores/uploadStore'

const uploadStore = useUploadStore()

const {
  dadosTratados,
  totalLinhas,
  totalValidas,
  totalComErro,
  contagemPorCategoria,
  ocorrencias,
} = storeToRefs(uploadStore)

const percentualValidos = computed(() => {
  if (!totalLinhas.value) return 72
  return Math.round((totalValidas.value / totalLinhas.value) * 100)
})

const dadosGrafico = computed(() => {
  const valores = ORDEM_CATEGORIAS.map((categoria) => ({
    label: categoria,
    valor: contagemPorCategoria.value[categoria] || 0,
  }))

  return valores.filter((item) => item.valor > 0).length
    ? valores
    : [
        { label: 'Campos vazios', valor: 10 },
        { label: 'Espaços desnecessários', valor: 8 },
        { label: 'Registros duplicados', valor: 6 },
        { label: 'E-mails inválidos', valor: 4 },
        { label: 'Dados fora do padrão', valor: 3 },
        { label: 'Outros problemas', valor: 2 },
      ]
})

const maiorValor = computed(() => {
  const max = Math.max(...dadosGrafico.value.map((item) => item.valor), 1)
  return max
})

const percentualErro = computed(() => 100 - percentualValidos.value)

const donutStyle = computed(() => {
  const valor = percentualValidos.value
  const restante = 100 - valor

  return {
    background: `conic-gradient(#606C3A 0 ${valor}%, #D0F4DE ${valor}% ${valor + restante}%)`,
  }
})

const topErros = computed(() => {
  return [...dadosGrafico.value].sort((a, b) => b.valor - a.valor).slice(0, 4)
})
</script>

<style>
@import "tailwindcss";
</style>