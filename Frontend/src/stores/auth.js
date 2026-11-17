import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authService, configurarApi } from '../services/api'

const CHAVE = 'oversight-sessao'
let sessaoVerificada = false

function lerSessao() {
  for (const storage of [localStorage, sessionStorage]) {
    try {
      const sessao = JSON.parse(storage.getItem(CHAVE))
      if (sessao?.token) return { ...sessao, lembrar: storage === localStorage }
    } catch {
      storage.removeItem(CHAVE)
    }
  }
  return {}
}

export const useAuthStore = defineStore('auth', () => {
  const salva = lerSessao()
  const token = ref(salva.token || '')
  const usuario = ref(salva.usuario || null)
  const manterSessao = ref(salva.lembrar || false)

  const logado = computed(() => !!token.value)
  const ehAdmin = computed(() => usuario.value?.perfil === 'ADMIN')

  function guardar(lembrar = manterSessao.value) {
    const destino = lembrar ? localStorage : sessionStorage
    const outro = lembrar ? sessionStorage : localStorage
    manterSessao.value = lembrar
    outro.removeItem(CHAVE)
    destino.setItem(CHAVE, JSON.stringify({ token: token.value, usuario: usuario.value }))
  }

  async function entrar(email, senha, lembrar = false) {
    const resp = await authService.entrar(email, senha)
    token.value = resp.token
    usuario.value = resp.usuario
    guardar(lembrar)
  }

  async function cadastrar(dados) {
    return authService.cadastrar(dados)
  }

  function sair() {
    token.value = ''
    usuario.value = null
    manterSessao.value = false
    localStorage.removeItem(CHAVE)
    sessionStorage.removeItem(CHAVE)
  }

  async function validarSessao() {
    if (sessaoVerificada) return
    sessaoVerificada = true
    if (!token.value) return
    try {
      usuario.value = await authService.eu()
      guardar()
    } catch (erro) {
      if (erro?.response?.status === 401) {
        sair()
        return
      }
      console.error('Não foi possível validar a sessão com a API.', erro)
    }
  }

  configurarApi({
    getToken: () => token.value,
    onUnauthorized: sair,
  })

  return { token, usuario, logado, ehAdmin, entrar, cadastrar, sair, validarSessao }
})
