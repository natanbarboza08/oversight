<script setup>
import { RouterLink } from 'vue-router'

// Cards da seção "Por que usar o Oversight?"
// Quando tiver as imagens, coloque o caminho em `imagem` (ex: '/img/organizacao.jpg')
const beneficios = [
  {
    titulo: 'Organização',
    texto:
      'Gerencie os dados de suas planilhas de forma automatizada, gerando insights, análises e devolutivas.',
    imagem: '/organizacao.png',
  },
  {
    titulo: 'Otimização',
    texto:
      'Realize a apuração dos dados, aprimore a busca e organize-os, de forma geral e interativa.',
    imagem: '/otimizacao.png',
  },
  {
    titulo: 'Eficiência',
    texto:
      'Agilize os processos da empresa com os resultados obtidos. Utilize do software para agregar valor aos departamentos.',
    imagem: '/eficiencia.png',
  },
]

const linksRodape = [
  { texto: 'Termos e Condições', href: '#' },
  { texto: 'Política de Privacidade', href: '#' },
  { texto: 'Cookies', href: '#' },
]
</script>

<template>
  <div class="min-h-screen bg-fundo font-sans text-ink">
    <!-- NAVBAR -->
    <header class="sticky top-0 z-10 border-b border-borda bg-fundo">
      <div class="flex h-[78px] items-center justify-between px-7">
        <RouterLink to="/" class="flex items-center gap-2">
          <img src="/logoOver.svg" alt="" class="h-8 w-8" aria-hidden="true" />
          <span class="text-2xl font-normal">Oversight</span>
        </RouterLink>

        <nav class="flex items-center gap-3.5">
          <RouterLink
            to="/login"
            class="rounded-md border border-borda bg-fundo px-5 py-2 text-sm transition-colors duration-200 hover:bg-[#DDE3FF]"
          >
            Fazer login
          </RouterLink>
          <RouterLink
            to="/cadastro"
            class="rounded-md bg-verde px-5 py-2 text-sm text-white transition hover:opacity-90"
          >
            Cadastre-se
          </RouterLink>
        </nav>
      </div>
    </header>

    <main>
      <!-- HERO -->
      <section
        id="inicio"
        class="flex min-h-[calc(100vh-78px)] flex-col items-center justify-center gap-10 px-6 py-16 md:flex-row md:justify-between md:px-24"
      >
        <div class="max-w-[620px]">
          <h1 class="text-4xl font-normal leading-tight md:text-[52px] md:leading-[58px]">
            Organize suas planilhas de forma inteligente.
          </h1>
          <p class="mt-3 text-xl leading-7 md:text-2xl">
            Integre a solução Oversight<br />
            e processe melhor seus dados.
          </p>

          <div class="mt-6 flex items-center gap-4">
            <RouterLink
              to="/cadastro"
              class="flex h-[75px] w-[150px] items-center justify-center rounded-md bg-verde text-lg text-white transition hover:opacity-90"
            >
              Comece agora
            </RouterLink>
            <span class="text-lg text-ink">— É de graça.</span>
          </div>
        </div>

        <img
          src="/ilustracao.png"
          alt="Ilustração de uma pessoa analisando gráficos e planilhas"
          class="w-full max-w-[640px] object-contain"
        />
      </section>

      <!-- POR QUE USAR -->
      <section
        id="beneficios"
        class="flex min-h-screen flex-col justify-center px-6 py-20 md:px-10"
      >
        <h2 class="font-titulo text-3xl uppercase leading-none md:text-[44px]">
          Por que usar o Oversight?
        </h2>

        <div class="mx-auto mt-6 flex w-full max-w-[780px] flex-col gap-8">
          <article
            v-for="item in beneficios"
            :key="item.titulo"
            class="flex overflow-hidden rounded-3xl bg-card"
          >
            <!-- Imagem (placeholder quando não houver) -->
            <div class="h-[223px] w-[223px] shrink-0 bg-borda">
              <img
                v-if="item.imagem"
                :src="item.imagem"
                :alt="item.titulo"
                class="h-full w-full object-cover"
              />
              <div
                v-else
                class="flex h-full w-full items-center justify-center text-sm text-suave"
              >
                Imagem
              </div>
            </div>

            <div class="flex flex-col justify-center p-6">
              <h3 class="text-[28px] font-normal leading-8">{{ item.titulo }}</h3>
              <p class="mt-2 text-[26px] font-light leading-[34px] text-suave">
                {{ item.texto }}
              </p>
            </div>
          </article>
        </div>
      </section>
    </main>

    <!-- RODAPÉ -->
    <footer class="border-t border-borda">
      <div
        class="flex flex-col gap-2 px-10 py-9 text-sm text-suave md:flex-row md:items-center md:justify-between"
      >
        <p>2026, CTI Comunicação de Dados e Tecnologia LTDA. Todos os direitos reservados.</p>
        <ul class="flex items-center">
          <li
            v-for="(link, i) in linksRodape"
            :key="link.texto"
            :class="i > 0 ? 'ml-2 border-l border-suave pl-2' : ''"
          >
            <a :href="link.href" class="hover:underline">{{ link.texto }}</a>
          </li>
        </ul>
      </div>
    </footer>
  </div>
</template>