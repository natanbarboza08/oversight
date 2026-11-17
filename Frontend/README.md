# Frontend

Aplicação Vue 3 com Vue Router, Pinia, Tailwind CSS, Axios e Chart.js. As rotas internas exigem JWT; o upload é revalidado pelo backend e o dashboard consulta os lotes persistidos.

## Desenvolvimento

```powershell
Copy-Item .env.example .env
npm ci
npm run dev
```

Configure `VITE_API_URL=http://localhost:8080/api` localmente. Para produção, defina a URL pública do backend Java com `/api` no ambiente de build da Vercel. A pasta `Frontend` é o diretório raiz do projeto Vercel; o `vercel.json` aplica o fallback SPA do Vue Router.

Antes do deploy, valide com `npm run build` e `npm audit --audit-level=high`.
