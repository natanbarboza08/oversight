<script setup>
// Campo de formulário com ícone (slot "icon") e mensagem de erro.
// Uso: <CampoForm id="email" label="E-mail" v-model="email" :error="erros.email">
//        <template #icon><svg .../></template>
//      </CampoForm>
defineProps({
  id: { type: String, required: true },
  label: { type: String, required: true },
  type: { type: String, default: 'text' },
  autocomplete: { type: String, default: 'off' },
  error: { type: String, default: '' },
  // Caminho de uma imagem para o ícone (ex: '/email.png'). Sem isso, usa o slot "icon".
  icone: { type: String, default: '' },
})

const model = defineModel()
</script>

<template>
  <div>
    <label :for="id" class="block text-lg">{{ label }}</label>

    <div
      :class="[
        'mt-2 flex h-[52px] items-center gap-2 rounded-lg border bg-white px-3 shadow-sm transition-colors',
        error ? 'border-erro' : 'border-campo focus-within:border-oliva',
      ]"
    >
      <img
        v-if="icone"
        :src="icone"
        alt=""
        aria-hidden="true"
        class="h-6 w-6 shrink-0 object-contain"
      />
      <slot v-else name="icon" />
      <input
        :id="id"
        v-model="model"
        :type="type"
        :autocomplete="autocomplete"
        :aria-invalid="!!error"
        :aria-describedby="error ? id + '-erro' : undefined"
        class="h-full w-full bg-transparent text-base outline-none"
      />
    </div>

    <p v-if="error" :id="id + '-erro'" role="alert" class="mt-1.5 text-sm text-erro">
      {{ error }}
    </p>
  </div>
</template>