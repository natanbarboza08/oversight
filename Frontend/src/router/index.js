import { createRouter, createWebHistory } from 'vue-router'
//Páginas Públicas
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'

//Layout do Painel e Páginas Internas
import AppLayout from '../layouts/AppLayout.vue'
import Upload from '../views/Upload.vue'
import Relatorios from '../views/Relatorios.vue'
import Graficos from '../views/Graficos.vue'

const routes =[
{ path: '/', name: 'Home', component: Home },
{ path: '/login', name: 'Login', component: Login },
//Rota Pai do Painel Interno (Contém a Sidebar e o Header)
{
path: '/app',
component: AppLayout,
redirect: '/app/upload',
children: [
{ path: 'upload', name: 'Upload', component: Upload },
{ path: 'relatorios', name: 'Relatorios', component: Relatorios },
{ path: 'graficos', name: 'Graficos', component: Graficos }
]
}
]
const router = createRouter({ history: createWebHistory(), routes })
export default router