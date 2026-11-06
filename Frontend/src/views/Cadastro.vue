<script setup>
import { ref, reactive, watch } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import PainelBoasVindas from '../components/PainelBoasVindas.vue'
import CampoForm from '../components/CampoForm.vue'

const router = useRouter()

const nome = ref('')
const email = ref('')
const senha = ref('')
const confirmar = ref('')
const aceito = ref(false)

const erros = reactive({ nome: '', email: '', senha: '', confirmar: '', aceito: '' })
// Erro vindo do backend (ex: "Este e-mail já está cadastrado.")
const erroGeral = ref('')

const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Limpa o erro de cada campo assim que a pessoa volta a digitar
watch(nome, () => (erros.nome = ''))
watch(email, () => (erros.email = ''))
watch(senha, () => (erros.senha = ''))
watch(confirmar, () => (erros.confirmar = ''))
watch(aceito, () => (erros.aceito = ''))

function validar() {
  if (nome.value.trim().length < 2) erros.nome = 'Informe seu nome.'

  if (!email.value.trim()) erros.email = 'Informe seu e-mail.'
  else if (!regexEmail.test(email.value)) erros.email = 'Digite um e-mail válido.'

  if (!senha.value) erros.senha = 'Crie uma senha.'
  else if (senha.value.length < 8) erros.senha = 'A senha precisa ter pelo menos 8 caracteres.'

  if (!confirmar.value) erros.confirmar = 'Confirme sua senha.'
  else if (confirmar.value !== senha.value) erros.confirmar = 'As senhas não coincidem.'

  if (!aceito.value) erros.aceito = 'Aceite os termos para continuar.'

  return Object.values(erros).every((e) => !e)
}

// Por enquanto só navega para o login.
// Quando o backend existir, chamar aqui o service de cadastro (POST)
// e, em caso de falha, preencher `erroGeral.value`.
function cadastrar() {
  erroGeral.value = ''
  if (!validar()) return
  router.push('/login')
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

        <h1 class="mt-6 text-[32px] font-bold leading-9">Criar conta</h1>

        <form class="mt-6 flex flex-col gap-5" novalidate @submit.prevent="cadastrar">
          <CampoForm
            id="nome"
            v-model="nome"
            label="Nome"
            autocomplete="name"
            :error="erros.nome"
          >
            <template #icon>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="h-6 w-6 shrink-0"
                aria-hidden="true"
              >
                <circle cx="12" cy="8" r="4" />
                <path d="M4 20c0-4 3.6-6 8-6s8 2 8 6" />
              </svg>
            </template>
          </CampoForm>

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
            label="Senha"
            type="password"
            autocomplete="new-password"
            :error="erros.senha"
            icone="/senha.png"
          />

          <CampoForm
            id="confirmar"
            v-model="confirmar"
            label="Confirmar senha"
            type="password"
            autocomplete="new-password"
            :error="erros.confirmar"
            icone="/senha.png"
          />

          <!-- Termos -->
          <div>
            <label class="flex cursor-pointer items-center gap-3 pl-5 text-base">
              <span class="relative flex h-8 w-8 shrink-0">
                <input
                  v-model="aceito"
                  type="checkbox"
                  :aria-invalid="!!erros.aceito"
                  :class="[
                    'peer h-8 w-8 cursor-pointer appearance-none rounded-lg border bg-white shadow-sm checked:border-escuro checked:bg-escuro',
                    erros.aceito ? 'border-erro' : 'border-campo',
                  ]"
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
              <span>Li e aceito os Termos e Condições</span>
            </label>
            <p v-if="erros.aceito" role="alert" class="mt-1.5 pl-5 text-sm text-erro">
              {{ erros.aceito }}
            </p>
          </div>

          <p v-if="erroGeral" role="alert" class="text-sm text-erro">{{ erroGeral }}</p>

          <button
            type="submit"
            class="h-[52px] w-full rounded-lg bg-escuro text-lg text-white transition hover:bg-oliva"
          >
            Criar conta
          </button>
        </form>

        <p class="mt-6 pl-3 text-lg leading-6">
          <span class="text-borda">Já tem uma conta?</span>
          <RouterLink to="/login" class="ml-1 hover:underline">Fazer login</RouterLink>
        </p>
      </main>
    </div>
  </div>
</template>