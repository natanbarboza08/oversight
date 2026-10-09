<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div>
    <nav>
      <RouterLink to="/app/upload">Upload</RouterLink>
      |
      <RouterLink to="/app/relatorios">Relatórios</RouterLink>
      |
      <RouterLink to="/app/graficos">Gráficos</RouterLink>
    </nav>

    <hr />

    <RouterView />
  </div>
</template>