<template>
  <div class="admin-form-page">
    <h2 class="form-title">新增管理员</h2>
    <div class="form-card">
      <div class="field">
        <label>用户名</label>
        <input v-model="form.username" class="input" placeholder="请输入管理员用户名" />
        <span class="error" v-if="error">{{ error }}</span>
      </div>
      <div class="field">
        <label>密码</label>
        <input v-model="form.password" type="password" class="input" placeholder="请输入密码（至少6位）" />
      </div>
      <div class="form-actions">
        <button class="btn-cancel" @click="$router.push('/')">取消</button>
        <button class="btn-save" @click="onSubmit" :disabled="saving">{{ saving ? '创建中...' : '创建管理员' }}</button>
      </div>
      <div class="success" v-if="successMsg">{{ successMsg }}</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { createAdmin } from '../api/auth'

const form = reactive({ username: '', password: '' })
const saving = ref(false)
const error = ref('')
const successMsg = ref('')

async function onSubmit() {
  error.value = ''
  successMsg.value = ''
  if (!form.username.trim() || form.username.trim().length < 2) {
    error.value = '用户名至少2个字符'; return
  }
  if (!form.password || form.password.length < 6) {
    error.value = '密码至少6位'; return
  }
  saving.value = true
  try {
    await createAdmin(form.username, form.password)
    successMsg.value = '管理员创建成功'
    form.username = ''
    form.password = ''
  } catch (e) {
    error.value = e.message || '创建失败'
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.admin-form-page { padding: 24px 28px; max-width: 480px; }
.form-title { font-size: 18px; font-family: var(--font-serif); color: var(--text); letter-spacing: 2px; margin-bottom: 16px; }
.form-card { background: #fff; border: 1px solid var(--card-border); padding: 24px; }
.field { display: flex; flex-direction: column; gap: 4px; margin-bottom: 14px; }
.field label { font-size: 12px; color: var(--text-secondary); }
.input {
  padding: 7px 12px; border: 1px solid #e0dbd0; border-radius: 2px;
  font-size: 13px; color: var(--text); outline: none; background: #fff;
}
.input:focus { border-color: var(--accent); }
.error { font-size: 11px; color: #c04040; }
.success { font-size: 12px; color: #5b8c5a; margin-top: 10px; }
.form-actions { display: flex; gap: 10px; justify-content: flex-end; }
.btn-cancel {
  padding: 8px 20px; background: #fff; border: 1px solid #e0dbd0;
  border-radius: 2px; color: var(--text-secondary); font-size: 13px; cursor: pointer;
}
.btn-save {
  padding: 8px 20px; background: var(--accent); color: #fff;
  border: none; border-radius: 2px; font-size: 13px; cursor: pointer;
}
.btn-save:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
