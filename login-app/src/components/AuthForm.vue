<template>
  <div class="auth-wrapper">
    <img :src="bgSrc" class="auth-bg" alt="" />
    <RainCanvas />
    <div class="auth-overlay"></div>

    <div class="auth-card">
      <div class="auth-logo">
        <span class="logo-icon">書</span>
        <span class="logo-text">云图书馆</span>
      </div>

      <div class="flip-container">
        <div class="flip-inner" :class="{ flipped }">
          <!-- 正面：登录 -->
          <div class="flip-face flip-front">
            <div class="face-title">登 录</div>
            <div class="field">
              <input v-model="loginForm.username" placeholder="用户名"
                @keyup.enter="onLogin" />
            </div>
            <div class="field">
              <input v-model="loginForm.password" type="password"
                placeholder="密码" @keyup.enter="onLogin" />
            </div>
            <div class="error" v-if="loginError">{{ loginError }}</div>
            <button class="btn-submit" @click="onLogin"
              :disabled="loggingIn">{{ loggingIn ? '登录中...' : '登 录' }}</button>
            <p class="flip-hint">
              没有账号？<span class="flip-link" @click="flipCard">立即注册</span>
            </p>
          </div>

          <!-- 背面：注册 -->
          <div class="flip-face flip-back">
            <div class="face-title">注 册</div>
            <div class="field">
              <input v-model="regForm.username" placeholder="用户名（2-50字符）" />
            </div>
            <div class="field">
              <input v-model="regForm.password" type="password"
                placeholder="密码（至少6位）" />
            </div>
            <div class="field">
              <input v-model="regForm.confirmPassword" type="password"
                placeholder="确认密码" />
            </div>
            <div class="error" v-if="regError">{{ regError }}</div>
            <button class="btn-submit" @click="onRegister"
              :disabled="registering">{{ registering ? '注册中...' : '注 册' }}</button>
            <p class="flip-hint">
              已有账号？<span class="flip-link" @click="flipCard">去登录</span>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { login, register } from '../api/auth'
import RainCanvas from './RainCanvas.vue'
import bgSrc from '../assets/login-bg.jpg'

const flipped = ref(false)
const loggingIn = ref(false)
const registering = ref(false)
const loginError = ref('')
const regError = ref('')

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', confirmPassword: '' })

function flipCard() {
  loginError.value = ''
  regError.value = ''
  flipped.value = !flipped.value
}

function validateLogin() {
  if (!loginForm.username.trim()) { loginError.value = '请输入用户名'; return false }
  if (!loginForm.password) { loginError.value = '请输入密码'; return false }
  loginError.value = ''
  return true
}

function validateReg() {
  if (!regForm.username.trim() || regForm.username.trim().length < 2) {
    regError.value = '用户名至少2个字符'; return false
  }
  if (!regForm.password || regForm.password.length < 6) {
    regError.value = '密码至少6位'; return false
  }
  if (regForm.password !== regForm.confirmPassword) {
    regError.value = '两次密码不一致'; return false
  }
  regError.value = ''
  return true
}

const urlParams = new URLSearchParams(window.location.search)
const redirect = urlParams.get('redirect') || ''

function buildTarget(base, token, username) {
  let url = `${base}/?token=${token}&username=${encodeURIComponent(username)}`
  if (redirect) url += `&redirect=${encodeURIComponent(redirect)}`
  return url
}

async function onLogin() {
  if (!validateLogin()) return
  loggingIn.value = true
  try {
    const res = await login(loginForm.username, loginForm.password)
    const { token, role, username } = res
    const target = role === 'ADMIN'
      ? buildTarget('http://localhost:5178', token, username)
      : buildTarget('http://localhost:5174', token, username)
    window.location.href = target
  } catch (e) {
    loginError.value = e.message || '登录失败'
  } finally {
    loggingIn.value = false
  }
}

async function onRegister() {
  if (!validateReg()) return
  registering.value = true
  try {
    const res = await register(regForm.username, regForm.password)
    const { token, username } = res
    window.location.href = buildTarget('http://localhost:5174', token, username)
  } catch (e) {
    regError.value = e.message || '注册失败'
  } finally {
    registering.value = false
  }
}
</script>

<style scoped>
.auth-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40px 20px;
  overflow: hidden;
}
.auth-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
}
.auth-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg,
    rgba(245,240,232,0.45) 0%,
    rgba(250,250,247,0.55) 50%,
    rgba(245,240,232,0.45) 100%
  );
  z-index: 2;
}
.auth-card {
  position: relative;
  z-index: 3;
  width: var(--auth-width);
  text-align: center;
}
.auth-logo { display: flex; align-items: center; justify-content: center; gap: 8px; margin-bottom: 24px; }
.logo-icon {
  width: 36px; height: 36px;
  background: var(--color-primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; border-radius: var(--radius);
}
.logo-text {
  font-weight: 600; font-size: 18px; color: var(--color-text);
  font-family: var(--font-serif); letter-spacing: 2px;
}

/* ---- 3D 翻转 ---- */
.flip-container {
  perspective: 1200px;
}
.flip-inner {
  display: grid;
  grid-template-areas: "face";
  transform-style: preserve-3d;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}
.flip-inner.flipped {
  transform: rotateY(180deg);
}
.flip-face {
  grid-area: face;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.6);
  border-radius: var(--radius);
  padding: var(--auth-padding);
  text-align: left;
  box-shadow: var(--shadow-lg);
}
.flip-back {
  transform: rotateY(180deg);
}

.face-title {
  text-align: center;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-primary);
  font-family: var(--font-serif);
  letter-spacing: 4px;
  margin-bottom: 16px;
}

.field { margin-bottom: 12px; }
.field input {
  width: 100%; padding: 10px 12px;
  border: 1px solid var(--color-border); border-radius: var(--radius);
  font-size: 13px; color: var(--color-text); outline: none;
  background: rgba(255,255,255,0.8);
  transition: border-color 0.2s;
}
.field input:focus { border-color: var(--color-primary); }
.field input::placeholder { color: var(--color-text-muted); }
.error { color: var(--color-danger); font-size: 12px; margin-bottom: 8px; }
.btn-submit {
  width: 100%; margin-top: 8px; padding: 10px 0;
  background: var(--color-primary); color: #fff; border: none;
  border-radius: var(--radius); font-size: 14px; cursor: pointer;
  font-family: var(--font-serif); letter-spacing: 2px;
  transition: background 0.2s;
}
.btn-submit:hover { background: var(--color-primary-hover); }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }

.flip-hint {
  text-align: center;
  margin-top: 16px;
  font-size: 12px;
  color: var(--color-text-muted);
}
.flip-link {
  color: var(--color-primary);
  cursor: pointer;
  transition: color 0.2s;
}
.flip-link:hover { color: var(--color-primary-hover); }

@media (max-width: 480px) {
  .auth-wrapper { padding: 20px 12px; align-items: flex-start; padding-top: 60px; }
  .logo-text { font-size: 16px; }
}
</style>
