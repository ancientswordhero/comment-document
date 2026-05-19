<template>
  <div class="form-page">
    <h2 class="form-title">{{ isEdit ? '编辑图书' : '新增图书' }}</h2>
    <div class="form-card">
      <div class="form-grid">
        <div class="field">
          <label>书名</label>
          <input v-model="form.title" class="input" placeholder="请输入书名" />
          <span class="error" v-if="errors.title">{{ errors.title }}</span>
        </div>
        <div class="field">
          <label>作者</label>
          <input v-model="form.author" class="input" placeholder="请输入作者" />
          <span class="error" v-if="errors.author">{{ errors.author }}</span>
        </div>
        <div class="field">
          <label>ISBN</label>
          <input v-model="form.isbn" class="input" placeholder="请输入ISBN" />
          <span class="error" v-if="errors.isbn">{{ errors.isbn }}</span>
        </div>
        <div class="field">
          <label>分类</label>
          <select v-model="form.categoryId" class="input">
            <option :value="null">请选择分类</option>
            <option v-for="cat in flatCategories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
        </div>
        <div class="field full">
          <label>封面图片</label>
          <div class="upload-zone" @click="triggerUpload">
            <img v-if="previewUrl" :src="previewUrl" class="preview-img" />
            <span v-else>点击上传封面图片</span>
          </div>
          <input ref="fileInput" type="file" accept="image/*" hidden @change="onFileChange" />
        </div>
        <div class="field full">
          <label>简介</label>
          <textarea v-model="form.description" class="textarea"
            placeholder="请输入图书简介（支持HTML标签）" rows="5"></textarea>
        </div>
      </div>
      <div class="form-actions">
        <button class="btn-cancel" @click="$router.push('/')">取消</button>
        <button class="btn-save" @click="onSubmit" :disabled="saving">
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getBookById, createBook, updateBook, uploadCover, getCategories } from '../api/book'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const flatCategories = ref([])
const fileInput = ref(null)
const previewUrl = ref(null)
const saving = ref(false)
const form = reactive({
  title: '', author: '', isbn: '', categoryId: null, coverUrl: '', description: ''
})
const errors = reactive({ title: '', author: '', isbn: '' })

onMounted(async () => {
  try {
    const catRes = await getCategories()
    flatCategories.value = flattenCategories(catRes)
  } catch (err) {
    console.error('获取分类失败:', err)
  }
  
  if (isEdit.value) {
    try {
      const bookRes = await getBookById(route.params.id)
      const b = bookRes
      form.title = b.title
      form.author = b.author
      form.isbn = b.isbn
      form.categoryId = b.categoryId
      form.coverUrl = b.coverUrl || ''
      form.description = b.description || ''
      if (b.coverUrl) previewUrl.value = 'http://localhost:8080' + b.coverUrl
    } catch (err) {
      console.error('获取图书详情失败:', err)
    }
  }
})

function flattenCategories(cats, prefix = '') {
  let result = []
  for (const cat of cats) {
    result.push({ id: cat.id, name: prefix + cat.name })
    if (cat.children) result.push(...flattenCategories(cat.children, '  ' + prefix))
  }
  return result
}

function triggerUpload() { fileInput.value?.click() }

async function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  try {
    const res = await uploadCover(file)
    form.coverUrl = res
    previewUrl.value = 'http://localhost:8080' + res
  } catch (err) {
    console.error('上传封面失败:', err)
  }
}

function validate() {
  let valid = true
  if (!form.title.trim()) { errors.title = '书名不能为空'; valid = false } else errors.title = ''
  if (!form.author.trim()) { errors.author = '作者不能为空'; valid = false } else errors.author = ''
  if (!form.isbn.trim()) { errors.isbn = 'ISBN不能为空'; valid = false } else errors.isbn = ''
  return valid
}

async function onSubmit() {
  if (!validate()) return
  saving.value = true
  try {
    const data = {
      title: form.title, author: form.author, isbn: form.isbn,
      categoryId: form.categoryId, coverUrl: form.coverUrl, description: form.description
    }
    if (isEdit.value) { await updateBook(route.params.id, data) }
    else { await createBook(data) }
    router.push('/')
  } catch (err) {
    console.error('保存失败:', err)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.form-page { padding: var(--content-padding); max-width: 640px; }
.form-title {
  font-size: 18px; font-family: var(--font-serif); color: var(--color-text);
  letter-spacing: 2px; margin-bottom: 16px;
}
.form-card {
  background: var(--color-card-bg); border: 1px solid var(--color-card-border);
  border-radius: var(--radius); padding: 24px;
}
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.field { display: flex; flex-direction: column; gap: 4px; }
.field.full { grid-column: 1 / -1; }
.field label { font-size: 12px; color: var(--color-text-secondary); }
.input {
  padding: 8px 12px; border: 1px solid var(--color-border); border-radius: var(--radius);
  font-size: 13px; color: var(--color-text); outline: none; background: var(--color-card-bg);
}
.input:focus { border-color: var(--color-primary); }
.error { font-size: 11px; color: var(--color-danger); }
.upload-zone {
  border: 1px dashed var(--color-border); padding: 24px; text-align: center;
  font-size: 12px; color: var(--color-text-muted); background: var(--color-bg);
  cursor: pointer; min-height: 100px; display: flex; align-items: center;
  justify-content: center; border-radius: var(--radius);
}
.preview-img { max-width: 100%; max-height: 200px; object-fit: contain; }
.textarea {
  width: 100%; padding: 10px 12px; border: 1px solid var(--color-border);
  border-radius: var(--radius); font-size: 12px; color: var(--color-text); outline: none;
  resize: vertical; font-family: var(--font-sans);
}
.textarea:focus { border-color: var(--color-primary); }
.form-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 20px; }
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

@media (max-width: 768px) {
  .form-grid { grid-template-columns: 1fr; }
  .field.full { grid-column: 1; }
  .form-card { padding: 16px; }
}

@media (max-width: 480px) {
  .form-page { padding: var(--content-padding); }
  .form-title { font-size: 16px; }
  .form-actions { flex-direction: column-reverse; }
  .btn-cancel, .btn-save { width: 100%; text-align: center; }
}
</style>