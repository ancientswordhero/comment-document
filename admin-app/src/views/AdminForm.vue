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
.admin-form-page { padding: var(--content-padding); max-width: 480px; }
.form-title {
  font-size: 18px; font-family: var(--font-serif); color: var(--color-text);
  letter-spacing: 2px; margin-bottom: 16px;
}
.form-card {
  background: var(--color-card-bg); border: 1px solid var(--color-card-border);
  border-radius: var(--radius); padding: 24px;
}
.field { display: flex; flex-direction: column; gap: 4px; margin-bottom: 14px; }
.field label { font-size: 12px; color: var(--color-text-secondary); }
.input {
  padding: 8px 12px; border: 1px solid var(--color-border); border-radius: var(--radius);
  font-size: 13px; color: var(--color-text); outline: none; background: var(--color-card-bg);
}
.input:focus { border-color: var(--color-primary); }
.error { font-size: 11px; color: var(--color-danger); }
.success { font-size: 12px; color: var(--color-success); margin-top: 10px; }
.form-actions { display: flex; gap: 10px; justify-content: flex-end; }
.btn-cancel {
  padding: 8px 20px; background: var(--color-card-bg);
  border: 1px solid var(--color-border); border-radius: var(--radius);
  color: var(--color-text-secondary); font-size: 13px; cursor: pointer;
}
.btn-save {
  padding: 8px 20px; background: var(--color-primary); color: #fff;
  border: none; border-radius: var(--radius); font-size: 13px; cursor: pointer;
  transition: background 0.2s;
}
.btn-save:hover { background: var(--color-primary-hover); }
.btn-save:disabled { opacity: 0.6; cursor: not-allowed; }

@media (max-width: 480px) {
  .form-title { font-size: 16px; }
  .form-actions { flex-direction: column-reverse; }
  .btn-cancel, .btn-save { width: 100%; text-align: center; }
}
</style>
