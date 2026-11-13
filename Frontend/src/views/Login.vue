<script setup>
import { ref, reactive, watch } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import PainelBoasVindas from '../components/PainelBoasVindas.vue'
import CampoForm from '../components/CampoForm.vue'

const router = useRouter()
const auth = useAuthStore()

const email = ref('')
const senha = ref('')
const lembrar = ref(false)

const erros = reactive({ email: '', senha: '' })
const erroGeral = ref('')

const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Limpa o erro de cada campo assim que a pessoa volta a digitar
watch(email, () => (erros.email = ''))
watch(senha, () => (erros.senha = ''))

function validar() {
  if (!email.value.trim()) erros.email = 'Informe seu e-mail.'
  else if (!regexEmail.test(email.value)) erros.email = 'Digite um e-mail válido.'

  if (!senha.value) erros.senha = 'Informe sua senha.'

  return !erros.email && !erros.senha
}

// Por enquanto só navega para o painel.
// Quando o backend existir, chamar aqui a store/service de autenticação
// e, em caso de falha, preencher `erroGeral.value`.
async function entrar() {
  erroGeral.value = ''
  if (!validar()) return

  try {
    await auth.entrar(email.value.trim(), senha.value)
    router.push('/app/upload')
  } catch (erro) {
    erroGeral.value = erro?.response?.data?.erro || 'E-mail ou senha incorretos.'
  }
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

        <h1 class="mt-6 text-[32px] font-bold leading-9">Fazer login</h1>

        <form class="mt-6" novalidate @submit.prevent="entrar">
          <CampoForm
            id="email"
            v-model="email"
            label="E-mail"
            type="email"
            autocomplete="email"
            :error="erros.email"
            icone="/email.png"
          />

          <CampoForm
            id="senha"
            v-model="senha"
            class="mt-6"
            label="Senha"
            type="password"
            autocomplete="current-password"
            :error="erros.senha"
            icone="/senha.png"
          />

          <!-- Lembrar -->
          <label class="mt-6 flex cursor-pointer items-center gap-3 pl-5 text-lg">
            <span class="relative flex h-8 w-8 shrink-0">
              <input
                v-model="lembrar"
                type="checkbox"
                class="peer h-8 w-8 cursor-pointer appearance-none rounded-lg border border-campo bg-white shadow-sm checked:border-escuro checked:bg-escuro"
              />
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="3"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="pointer-events-none absolute inset-0 m-auto h-5 w-5 text-white opacity-0 peer-checked:opacity-100"
                aria-hidden="true"
              >
                <path d="M5 12.5 9.5 17 19 7.5" />
              </svg>
            </span>
            Lembre-se de mim
          </label>

          <p v-if="erroGeral" role="alert" class="mt-4 text-sm text-erro">{{ erroGeral }}</p>

          <button
            type="submit"
            class="mt-6 h-[52px] w-full rounded-lg bg-escuro text-lg text-white transition hover:bg-oliva"
          >
            Entrar
          </button>
        </form>

        <div class="mt-6 pl-3 text-lg leading-6">
          <p>
            <span class="text-borda">Não tem uma conta?</span>
            <RouterLink to="/cadastro" class="ml-1 hover:underline">Cadastrar-se</RouterLink>
          </p>
          <RouterLink to="/esqueci-senha" class="hover:underline">Esqueci a senha</RouterLink>
        </div>
      </main>
    </div>
  </div>
</template>