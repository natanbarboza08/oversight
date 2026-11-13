import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// Páginas Públicas
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Cadastro from '../views/Cadastro.vue'
import EsqueciSenha from '../views/EsqueciSenha.vue'

// Layout do Painel e Páginas Internas
import AppLayout from '../layouts/AppLayout.vue'
import Upload from '../views/Upload.vue'
import Relatorios from '../views/Relatorios.vue'
import Graficos from '../views/Graficos.vue'
import NotFound from '../views/NotFound.vue'

// MODO DE TESTE TEMPORÁRIO: desativa a proteção de rota até o backend e o banco ficarem prontos.
// Quando a autenticação real estiver pronta, voltar para false.
const MODO_TESTE = true

const routes = [
  { path: '/', name: 'home', component: Home },
  { path: '/login', name: 'login', component: Login, meta: { soVisitante: true } },
  { path: '/cadastro', name: 'cadastro', component: Cadastro, meta: { soVisitante: true } },
  { path: '/esqueci-senha', name: 'esqueciSenha', component: EsqueciSenha },
  {
    path: '/app',
    component: AppLayout,
    redirect: '/app/upload',
    meta: { requerLogin: true },
    children: [
      { path: 'upload', name: 'upload', component: Upload },
      { path: 'relatorios', name: 'relatorios', component: Relatorios },
      { path: 'graficos', name: 'graficos', component: Graficos },
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'naoEncontrada', component: NotFound },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (MODO_TESTE) return

  if (to.meta.requerLogin && !auth.logado) {
    return { name: 'login' }
  }

  if (to.meta.soVisitante && auth.logado) {
    return { name: 'upload' }
  }
})

export default router