import { createApp } from 'vue'
import App from './App.vue'      // Importa o componente da raiz de src/
import router from './router'    // Importa a pasta router (procura o index.js automaticamente)
import './style.css'             // Importa o Tailwind CSS

const app = createApp(App)

app.use(router)
app.mount('#app')