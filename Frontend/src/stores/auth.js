import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authService, configurarApi } from '../services/api'

const CHAVE = 'oversight-sessao'

function lerSessao() {
  try {
    return JSON.parse(localStorage.getItem(CHAVE)) || {}
  } catch {
    return {}
  }
}

export const useAuthStore = defineStore('auth', () => {
  const salva = lerSessao()
  const token = ref(salva.token || '')
  const usuario = ref(salva.usuario || null)

  const logado = computed(() => !!token.value)
  const ehAdmin = computed(() => usuario.value?.perfil === 'ADMIN')

  function guardar() {
    localStorage.setItem(CHAVE, JSON.stringify({ token: token.value, usuario: usuario.value }))
  }

  async function entrar(email, senha) {
    const resp = await authService.entrar(email, senha)
    token.value = resp.token
    usuario.value = resp.usuario
    guardar()
  }

  async function cadastrar(dados) {
    return authService.cadastrar(dados)
  }

  function sair() {
    token.value = ''
    usuario.value = null
    localStorage.removeItem(CHAVE)
  }

  async function validarSessao() {
    if (!token.value) return
    try {
      usuario.value = await authService.eu()
      guardar()
    } catch {
      sair()
    }
  }

  configurarApi({
    getToken: () => token.value,
    onUnauthorized: sair,
  })

  return { token, usuario, logado, ehAdmin, entrar, cadastrar, sair, validarSessao }
})
