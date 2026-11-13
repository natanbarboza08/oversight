<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const aberto = ref(false)

function sair() {
  auth.sair()
  aberto.value = false
  router.push('/login')
}
</script>

<template>
  <header class="bg-escuro text-white shadow-sm">
    <div class="mx-auto flex h-14 max-w-6xl items-center justify-between px-4">
      <RouterLink to="/" class="text-xl font-semibold tracking-tight" @click="aberto = false">
        Oversight
      </RouterLink>

      <button class="px-2 text-2xl md:hidden" @click="aberto = !aberto" aria-label="Abrir menu">
        ☰
      </button>

      <nav class="hidden items-center gap-5 text-sm font-semibold md:flex">
        <RouterLink to="/" class="hover:underline">Início</RouterLink>

        <template v-if="auth.logado">
          <RouterLink to="/app/upload" class="hover:underline">Enviar planilha</RouterLink>
          <RouterLink to="/app/graficos" class="hover:underline">Gráficos</RouterLink>
          <RouterLink v-if="auth.ehAdmin" to="/app/admin" class="hover:underline">Liberar acessos</RouterLink>
          <span class="text-white/70">{{ auth.usuario?.nome || 'Usuário' }}</span>
          <button class="rounded bg-white px-3 py-1 text-escuro" @click="sair">Sair</button>
        </template>

        <template v-else>
          <RouterLink to="/login" class="hover:underline">Entrar</RouterLink>
          <RouterLink to="/cadastro" class="rounded bg-white px-3 py-1 text-escuro">Cadastrar</RouterLink>
        </template>
      </nav>
    </div>

    <nav v-if="aberto" class="flex flex-col gap-3 bg-escuro px-4 py-3 text-sm font-semibold md:hidden">
      <RouterLink to="/" @click="aberto = false">Início</RouterLink>

      <template v-if="auth.logado">
        <RouterLink to="/app/upload" @click="aberto = false">Enviar planilha</RouterLink>
        <RouterLink to="/app/graficos" @click="aberto = false">Gráficos</RouterLink>
        <RouterLink v-if="auth.ehAdmin" to="/app/admin" @click="aberto = false">Liberar acessos</RouterLink>
        <button class="text-left" @click="sair">Sair ({{ auth.usuario?.nome || 'Usuário' }})</button>
      </template>

      <template v-else>
        <RouterLink to="/login" @click="aberto = false">Entrar</RouterLink>
        <RouterLink to="/cadastro" @click="aberto = false">Cadastrar</RouterLink>
      </template>
    </nav>
  </header>
</template>
