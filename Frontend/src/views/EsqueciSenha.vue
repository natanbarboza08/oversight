<script setup>
import { RouterLink } from 'vue-router'
import PainelBoasVindas from '../components/PainelBoasVindas.vue'
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-fundo px-6 py-10 font-sans text-ink">
    <div class="flex w-full max-w-[1050px] items-center justify-center gap-16 lg:justify-between">
      <PainelBoasVindas />

      <main class="w-full max-w-[425px]">
        <RouterLink to="/" class="inline-flex flex-col items-start">
          <img src="/logoOver.svg" alt="" aria-hidden="true" class="h-6 w-6" />
          <span class="text-xl leading-6">Oversight</span>
        </RouterLink>

        <h1 class="mt-6 text-[32px] font-bold leading-9">Recuperar senha</h1>
        <p class="mt-4 rounded-lg border border-borda bg-white p-5 text-suave">
          A recuperação por e-mail ainda não está configurada. Solicite a redefinição ao administrador
          da plataforma; nenhum e-mail será enviado por esta tela.
        </p>
        <RouterLink
          to="/login"
          class="mt-6 flex h-[52px] w-full items-center justify-center rounded-lg bg-escuro text-lg text-white transition hover:bg-oliva"
        >
          Voltar para o login
        </RouterLink>
      </main>
    </div>
  </div>
</template>
