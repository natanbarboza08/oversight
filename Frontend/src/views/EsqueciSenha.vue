<script setup>
import { ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import PainelBoasVindas from '../components/PainelBoasVindas.vue'
import CampoForm from '../components/CampoForm.vue'

const email = ref('')
const erro = ref('')
const enviado = ref(false)

const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Limpa o erro assim que a pessoa volta a digitar
watch(email, () => (erro.value = ''))

function validar() {
  if (!email.value.trim()) erro.value = 'Informe seu e-mail.'
  else if (!regexEmail.test(email.value)) erro.value = 'Digite um e-mail válido.'
  return !erro.value
}

// Por enquanto só mostra a confirmação.
// Quando o backend existir, chamar aqui o service (POST) que envia o link de recuperação.
// A mensagem de sucesso é a mesma exista ou não a conta, para não revelar quais e-mails estão cadastrados.
function enviar() {
  if (!validar()) return
  enviado.value = true
}

function tentarOutroEmail() {
  enviado.value = false
  email.value = ''
}
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

        <h1 class="mt-6 text-[32px] font-bold leading-9">Esqueci a senha</h1>

        <!-- Formulário -->
        <template v-if="!enviado">
          <p class="mt-3 text-lg leading-6 text-suave">
            Informe o e-mail da sua conta e enviaremos um link para você criar uma nova senha.
          </p>

          <form class="mt-6" novalidate @submit.prevent="enviar">
            <CampoForm
              id="email"
              v-model="email"
              label="E-mail"
              type="email"
              autocomplete="email"
              :error="erro"
            icone="/email.png"
          />

            <button
              type="submit"
              class="mt-6 h-[52px] w-full rounded-lg bg-escuro text-lg text-white transition hover:bg-oliva"
            >
              Enviar link
            </button>
          </form>
        </template>

        <!-- Confirmação -->
        <template v-else>
          <div class="mt-6 rounded-lg border border-campo bg-white p-5 shadow-sm" role="status">
            <p class="text-lg font-semibold">Verifique seu e-mail</p>
            <p class="mt-2 text-base leading-6 text-suave">
              Se existir uma conta com o e-mail
              <span class="font-medium text-ink">{{ email }}</span
              >, você receberá um link para redefinir sua senha em instantes.
            </p>
          </div>

          <RouterLink
            to="/login"
            class="mt-6 flex h-[52px] w-full items-center justify-center rounded-lg bg-escuro text-lg text-white transition hover:bg-oliva"
          >
            Voltar para o login
          </RouterLink>

          <button
            type="button"
            class="mt-4 pl-3 text-lg hover:underline"
            @click="tentarOutroEmail"
          >
            Usar outro e-mail
          </button>
        </template>

        <p v-if="!enviado" class="mt-6 pl-3 text-lg leading-6">
          <RouterLink to="/login" class="hover:underline">Voltar para o login</RouterLink>
        </p>
      </main>
    </div>
  </div>
</template> 