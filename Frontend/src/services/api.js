import axios from 'axios'

const BASE = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api').replace(/\/+$/, '')

export const api = axios.create({
  baseURL: BASE,
  timeout: 60000,
})

let pegarToken = () => null
let aoExpirar = () => {}

export function configurarApi({ getToken, onUnauthorized }) {
  pegarToken = getToken
  aoExpirar = onUnauthorized
}

api.interceptors.request.use((config) => {
  const token = pegarToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (resposta) => resposta,
  (erro) => {
    const url = erro.config?.url || ''
    const publica = url.includes('/auth/login') || url.includes('/auth/register')

    if (erro.response?.status === 401 && !publica) {
      aoExpirar()
    }

    return Promise.reject(erro)
  },
)

export function mensagemDeErro(erro) {
  if (erro?.response?.data?.erro) return erro.response.data.erro
  if (erro?.response) return `Erro ${erro.response.status} no servidor.`
  return 'Não consegui falar com o servidor. Ele está rodando?'
}

const corpo = (resposta) => resposta.data

export const authService = {
  entrar: (email, senha) => api.post('/auth/login', { email, senha }).then(corpo),
  cadastrar: (dados) => api.post('/auth/register', dados).then(corpo),
  eu: () => api.get('/auth/me').then(corpo),
}

export const adminService = {
  listar: (status) => api.get('/admin/usuarios', { params: { status } }).then(corpo),
  alterarStatus: (id, status) => api.patch(`/admin/usuarios/${id}/status`, { status }).then(corpo),
}

export const uploadService = {
  enviarValidadas: (dados) => api.post('/uploads/validadas', dados).then(corpo),
}

export const dashboardService = {
  obter: (loteId) => api.get('/dashboard', { params: { loteId } }).then(corpo),
  lotes: () => api.get('/dashboard/lotes').then(corpo),
}
