import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', name: 'home', component: () => import('../views/Home.vue') },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { soVisitante: true } },
  { path: '/cadastro', name: 'cadastro', component: () => import('../views/Cadastro.vue'), meta: { soVisitante: true } },
  { path: '/esqueci-senha', name: 'esqueciSenha', component: () => import('../views/EsqueciSenha.vue') },
  {
    path: '/app',
    component: () => import('../layouts/AppLayout.vue'),
    redirect: '/app/upload',
    meta: { requerLogin: true },
    children: [
      { path: 'upload', name: 'upload', component: () => import('../views/Upload.vue') },
      { path: 'relatorios', name: 'relatorios', component: () => import('../views/Relatorios.vue') },
      { path: 'graficos', name: 'graficos', component: () => import('../views/Graficos.vue') },
      {
        path: 'admin/usuarios',
        name: 'adminUsuarios',
        component: () => import('../views/AdminUsuarios.vue'),
        meta: { requerAdmin: true },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'naoEncontrada', component: () => import('../views/NotFound.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  await auth.validarSessao()

  if (to.meta.requerLogin && !auth.logado) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requerAdmin && !auth.ehAdmin) {
    return { name: 'upload' }
  }

  if (to.meta.soVisitante && auth.logado) {
    return { name: 'upload' }
  }
})

export default router