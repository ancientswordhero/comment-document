<template>
  <canvas ref="canvasRef" class="rain-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  count: { type: Number, default: 130 },
  color: { type: String, default: '74,61,47' },
  minSpeed: { type: Number, default: 1.5 },
  maxSpeed: { type: Number, default: 3.0 },
  minLength: { type: Number, default: 60 },
  maxLength: { type: Number, default: 200 },
  wind: { type: Number, default: 0.4 }
})

const canvasRef = ref(null)
let ctx = null
let animId = null
let drops = []
let resizeObserver = null

function createDrop(canvasW, canvasH) {
  const length = props.minLength + Math.random() * (props.maxLength - props.minLength)
  return {
    x: Math.random() * canvasW,
    y: Math.random() * canvasH,
    speed: props.minSpeed + Math.random() * (props.maxSpeed - props.minSpeed),
    hSpeed: props.wind * (0.7 + Math.random() * 0.6),
    slant: 0.08 + Math.random() * 0.12,
    length,
    opacity: 0.40 + Math.random() * 0.35,
    thickness: 1.2 + Math.random() * 1.8
  }
}

function initDrops(canvasW, canvasH) {
  drops = []
  for (let i = 0; i < props.count; i++) {
    drops.push(createDrop(canvasW, canvasH))
  }
}

function draw() {
  if (!ctx) return
  const canvas = canvasRef.value
  if (!canvas) return
  const w = canvas.width
  const h = canvas.height

  ctx.clearRect(0, 0, w, h)

  const [r, g, b] = props.color.split(',').map(v => parseInt(v.trim()))

  for (const d of drops) {
    const { x, y, length, opacity, thickness, slant } = d
    const bx = x + length * slant

    // 渐变：顶部淡 → 底部深（沿斜线方向）
    const grad = ctx.createLinearGradient(x, y, bx, y + length)
    grad.addColorStop(0, `rgba(${r},${g},${b},0)`)
    grad.addColorStop(0.1, `rgba(${r},${g},${b},${opacity * 0.5})`)
    grad.addColorStop(0.5, `rgba(${r},${g},${b},${opacity * 0.85})`)
    grad.addColorStop(1, `rgba(${r},${g},${b},${opacity})`)

    ctx.beginPath()
    ctx.moveTo(x, y)
    ctx.lineTo(bx, y + length)
    ctx.strokeStyle = grad
    ctx.lineWidth = thickness
    ctx.lineCap = 'round'
    ctx.stroke()
  }
}

function update() {
  const canvas = canvasRef.value
  if (!canvas) return
  const h = canvas.height
  const cssW = canvas.offsetWidth

  for (const d of drops) {
    d.y += d.speed
    d.x += d.hSpeed
    // 水平方向循环
    if (d.x > cssW + 20) d.x = -20
    if (d.x < -20) d.x = cssW + 20
    if (d.y > h) {
      const newLen = props.minLength + Math.random() * (props.maxLength - props.minLength)
      d.y = -newLen
      d.x = Math.random() * cssW
      d.speed = props.minSpeed + Math.random() * (props.maxSpeed - props.minSpeed)
      d.hSpeed = props.wind * (0.7 + Math.random() * 0.6)
      d.slant = 0.08 + Math.random() * 0.12
      d.length = newLen
      d.opacity = 0.40 + Math.random() * 0.35
      d.thickness = 1.2 + Math.random() * 1.8
    }
  }
}

function loop() {
  update()
  draw()
  animId = requestAnimationFrame(loop)
}

function handleResize(entries) {
  for (const entry of entries) {
    const { width, height } = entry.contentRect
    if (ctx && width > 0 && height > 0) {
      const canvas = canvasRef.value
      const dpr = window.devicePixelRatio || 1
      canvas.width = width * dpr
      canvas.height = height * dpr
      canvas.style.width = width + 'px'
      canvas.style.height = height + 'px'
      ctx.scale(dpr, dpr)
      initDrops(width, height)
    }
  }
}

function handleVisibility() {
  if (document.hidden) {
    if (animId) {
      cancelAnimationFrame(animId)
      animId = null
    }
  } else {
    if (!animId) {
      animId = requestAnimationFrame(loop)
    }
  }
}

onMounted(() => {
  const canvas = canvasRef.value
  ctx = canvas.getContext('2d')
  if (!ctx) return

  const dpr = window.devicePixelRatio || 1
  canvas.width = canvas.offsetWidth * dpr
  canvas.height = canvas.offsetHeight * dpr
  canvas.style.width = canvas.offsetWidth + 'px'
  canvas.style.height = canvas.offsetHeight + 'px'
  ctx.scale(dpr, dpr)
  initDrops(canvas.offsetWidth, canvas.offsetHeight)

  animId = requestAnimationFrame(loop)

  resizeObserver = new ResizeObserver(handleResize)
  resizeObserver.observe(canvas)

  document.addEventListener('visibilitychange', handleVisibility)
})

onUnmounted(() => {
  if (animId) cancelAnimationFrame(animId)
  if (resizeObserver) resizeObserver.disconnect()
  document.removeEventListener('visibilitychange', handleVisibility)
})
</script>

<style scoped>
.rain-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}
</style>
