<script setup>
import { onMounted, ref } from 'vue'
import { adminService, mensagemDeErro } from '../services/api'

const usuarios = ref([])
const filtro = ref('PENDENTE')
const carregando = ref(false)
const usuarioAtualizando = ref(null)
const erro = ref('')
const mensagem = ref('')

async function carregarUsuarios() {
  carregando.value = true
  erro.value = ''
  try {
    usuarios.value = await adminService.listar(filtro.value || undefined)
  } catch (falha) {
    erro.value = mensagemDeErro(falha)
  } finally {
    carregando.value = false
  }
}

async function alterarStatus(usuario, status) {
  usuarioAtualizando.value = usuario.id
  erro.value = ''
  mensagem.value = ''
  try {
    const atualizado = await adminService.alterarStatus(usuario.id, status)
    usuarios.value = usuarios.value.map((item) => item.id === usuario.id ? atualizado : item)
    mensagem.value = `Acesso de ${usuario.nome} atualizado para ${status.toLowerCase()}.`
  } catch (falha) {
    erro.value = mensagemDeErro(falha)
  } finally {
    usuarioAtualizando.value = null
  }
}

onMounted(carregarUsuarios)
</script>

<template>
  <div class="mx-auto max-w-7xl">
    <header class="mb-8 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 class="font-destaque text-2xl font-bold uppercase sm:text-3xl">Usuários</h1>
        <p class="mt-3 text-suave">Aprove ou bloqueie os cadastros da plataforma.</p>
      </div>
      <label class="text-sm font-medium">
        Status
        <select
          v-model="filtro"
          class="ml-2 rounded-lg border border-borda bg-white px-3 py-2"
          @change="carregarUsuarios"
        >
          <option value="PENDENTE">Pendentes</option>
          <option value="APROVADO">Aprovados</option>
          <option value="BLOQUEADO">Bloqueados</option>
          <option value="">Todos</option>
        </select>
      </label>
    </header>

    <p v-if="erro" class="mb-4 rounded-lg bg-erro/10 p-4 text-sm text-erro" role="alert">
      {{ erro }}
    </p>
    <p v-if="mensagem" class="mb-4 rounded-lg bg-menta p-4 text-sm" role="status">
      {{ mensagem }}
    </p>

    <div class="overflow-x-auto rounded-2xl border border-borda bg-white">
      <table class="w-full min-w-[640px] text-left text-sm">
        <thead class="bg-fundo text-suave">
          <tr>
            <th class="px-5 py-3">Nome</th>
            <th class="px-5 py-3">E-mail</th>
            <th class="px-5 py-3">Status</th>
            <th class="px-5 py-3">Ações</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-campo">
          <tr v-for="usuario in usuarios" :key="usuario.id">
            <td class="px-5 py-4 font-medium">{{ usuario.nome }}</td>
            <td class="px-5 py-4">{{ usuario.email }}</td>
            <td class="px-5 py-4">{{ usuario.status }}</td>
            <td class="px-5 py-4">
              <div class="flex gap-2">
                <button
                  v-if="usuario.status !== 'APROVADO'"
                  type="button"
                  :disabled="usuarioAtualizando === usuario.id"
                  class="rounded-lg bg-escuro px-3 py-2 text-white disabled:opacity-60"
                  @click="alterarStatus(usuario, 'APROVADO')"
                >
                  Aprovar
                </button>
                <button
                  v-if="usuario.status !== 'BLOQUEADO'"
                  type="button"
                  :disabled="usuarioAtualizando === usuario.id"
                  class="rounded-lg border border-borda px-3 py-2 disabled:opacity-60"
                  @click="alterarStatus(usuario, 'BLOQUEADO')"
                >
                  Bloquear
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!carregando && usuarios.length === 0">
            <td colspan="4" class="px-5 py-10 text-center text-suave">Nenhum usuário encontrado.</td>
          </tr>
          <tr v-if="carregando">
            <td colspan="4" class="px-5 py-10 text-center text-suave">Carregando usuários...</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
