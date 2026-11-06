<template>
  <!-- Painel verde usado nas telas de Login, Cadastro e Esqueci a senha (some no mobile) -->
  <aside
    class="relative hidden h-[807px] w-[526px] shrink-0 overflow-hidden rounded-[64px] bg-oliva bg-[url('/backgroundCard.png')] bg-cover bg-center lg:block"
  >
    <!-- Logo grande desfocada -->
    <img
      src="/logoDesfocada.png"
      alt=""
      aria-hidden="true"
      class="pointer-events-none absolute -left-8 top-8 w-[460px] max-w-none select-none"
    />

    <div class="absolute inset-x-0 top-[300px] px-4">
      <h2 class="font-titulo text-[22px] uppercase leading-[24px] text-white">
        Seja bem-vindo ao Oversight
      </h2>
      <p class="mt-8 text-base leading-[19px] text-white">
        O Oversight é um sistema focado em otimizar os processos de tratamento e análise de dados,
        transformando suas planilhas em referências estratégicas.
      </p>
    </div>

    <!-- Cartão (a forma com recorte e brilho vem da imagem boxCard.png) -->
    <div
      class="absolute bottom-[56px] left-[30px] right-[30px] h-[260px] bg-[url('/boxCard.png')] bg-[length:100%_100%] bg-no-repeat pl-10 pr-9 pt-11 text-right text-white"
    >
      <p class="text-xl font-semibold leading-6">
        Utilize nosso sistema e faça<br />
        VOCÊ suas melhores decisões.
      </p>
      <p class="mt-8 text-base leading-5">
        O Oversight é uma ferramenta fundamental<br />
        para o desenvolvimento do negócio.
      </p>
      <p class="mt-4 text-sm">Venha fazer parte você também.</p>
    </div>
  </aside>
</template>