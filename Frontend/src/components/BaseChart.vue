<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const props = defineProps({
  type: { type: String, required: true },
  data: { type: Object, required: true },
  options: { type: Object, default: () => ({}) },
  label: { type: String, required: true },
})

const canvas = ref(null)
let chart

function renderChart() {
  if (!canvas.value) return
  chart?.destroy()
  chart = new Chart(canvas.value, {
    type: props.type,
    data: structuredClone(props.data),
    options: {
      responsive: true,
      maintainAspectRatio: false,
      ...structuredClone(props.options),
    },
  })
}

onMounted(renderChart)
watch(() => [props.type, props.data, props.options], renderChart, { deep: true })
onBeforeUnmount(() => chart?.destroy())
</script>

<template>
  <div class="relative h-72 w-full">
    <canvas ref="canvas" role="img" :aria-label="label"></canvas>
  </div>
</template>
