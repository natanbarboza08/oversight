<script setup>
import { RouterLink, RouterView } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

const itens = [
  {
    texto: 'Upload',
    to: '/app/upload',
    icone: 'M4 16.5v1A2.5 2.5 0 006.5 20h11a2.5 2.5 0 002.5-2.5v-1M12 15V3m0 0L7.5 7.5M12 3l4.5 4.5',
  },
  {
    texto: 'Relatórios',
    to: '/app/relatorios',
    icone: 'M7 3h7l5 5v11a2 2 0 01-2 2H7a2 2 0 01-2-2V5a2 2 0 012-2zM14 3v5h5M8.5 13h7M8.5 17h5',
  },
  {
    texto: 'Gráficos',
    to: '/app/graficos',
    icone: 'M4 20h16M7 16v-5M12 16V6M17 16v-8',
  },
]

function sair() {
  auth.sair()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-fundo font-sans text-ink lg:flex">
    <aside
      class="flex flex-col gap-4 border-b border-borda bg-white px-5 py-4 lg:sticky lg:top-0 lg:h-screen lg:w-64 lg:shrink-0 lg:gap-8 lg:border-b-0 lg:border-r lg:py-6"
    >
      <div class="flex items-center justify-between">
        <RouterLink to="/" class="flex items-center gap-2">
          <img src="/logoOver.svg" alt="" aria-hidden="true" class="h-8 w-8" />
          <span class="text-2xl">Oversight</span>
        </RouterLink>

        <button class="text-sm text-suave hover:underline lg:hidden" @click="sair">
          Sair
        </button>
      </div>

      <nav class="flex gap-1 overflow-x-auto lg:flex-col" aria-label="Menu do painel">
        <RouterLink
          v-for="item in itens"
          :key="item.to"
          :to="item.to"
          class="flex shrink-0 items-center gap-3 rounded-lg px-3 py-2.5 text-base transition-colors hover:bg-fundo"
          active-class="bg-menta font-medium"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.7"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="h-5 w-5 shrink-0"
            aria-hidden="true"
          >
            <path :d="item.icone" />
          </svg>
          {{ item.texto }}
        </RouterLink>
      </nav>

      <button
        class="mt-auto hidden rounded-lg px-3 py-2.5 text-left text-base text-suave transition-colors hover:bg-fundo lg:block"
        @click="sair"
      >
        Sair
      </button>
    </aside>

    <main class="min-w-0 flex-1 px-4 py-8 sm:px-8">
      <RouterView />
    </main>
  </div>
</template>