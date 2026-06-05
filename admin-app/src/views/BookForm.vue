<template>
  <div class="form-page">
    <h2 class="form-title">{{ isEdit ? '编辑图书' : '新增图书' }}</h2>
    <div class="form-card">
      <div class="form-grid">
        <div class="field">
          <label>书名</label>
          <input v-model="form.title" class="input" placeholder="留空则从 EPUB 自动提取" />
          <span class="error" v-if="errors.title">{{ errors.title }}</span>
        </div>
        <div class="field">
          <label>作者</label>
          <input v-model="form.author" class="input" placeholder="留空则从 EPUB 自动提取" />
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
            <option v-for="cat in flatCategories" :key="cat.id" :value="cat.id">{{ cat.displayName }}</option>
          </select>
        </div>
        <div class="field full">
          <label>封面图片</label>
          <div class="upload-zone" @click="triggerUpload">
            <img v-if="coverPreviewUrl" :src="coverPreviewUrl" class="preview-img" />
            <span v-else>点击上传封面图片</span>
          </div>
          <input ref="fileInput" type="file" accept=".jpg,.jpeg,.png" hidden @change="onFileChange" />
        </div>
        <div class="field full">
          <label>简介</label>
          <textarea v-model="form.description" class="textarea"
            placeholder="请输入图书简介（支持HTML标签）" rows="5"></textarea>
        </div>
        <div class="field full">
          <label>EPUB 图书文件 <span class="required">*</span></label>
          <div
            class="epub-upload-zone"
            :class="{ 'has-file': epubFile }"
            @click="$refs.epubInput.click()"
            @dragover.prevent
            @drop.prevent="onDrop"
          >
            <input
              ref="epubInput"
              type="file"
              accept=".epub"
              style="display:none"
              @change="onEpubSelected"
            />
            <template v-if="!epubFile">
              <span class="upload-icon">+</span>
              <span>点击或拖拽上传 EPUB 文件</span>
            </template>
            <template v-else>
              <span class="epub-file-name">{{ epubFile.name }}</span>
              <span class="epub-file-size">({{ formatSize(epubFile.size) }})</span>
              <span class="epub-file-remove" @click.stop="epubFile = null">&times;</span>
            </template>
          </div>
        </div>
      </div>
      <div v-if="submitting && uploadPercent > 0" class="upload-progress">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: uploadPercent + '%' }"></div>
        </div>
        <span class="progress-text">{{ uploadPercent }}%</span>
      </div>
      <div class="form-actions">
        <button class="btn-cancel" @click="$router.push('/')">取消</button>
        <button class="btn-save" @click="save" :disabled="submitting">
          {{ submitting ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getBookById, createBook, updateBook, getCategories } from '../api/book'
import { flattenCategories } from '../utils/category.js'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const flatCategories = ref([])
const fileInput = ref(null)
const coverFile = ref(null)
const coverPreviewUrl = ref(null)
const epubFile = ref(null)
const submitting = ref(false)
const uploadPercent = ref(0)
const form = reactive({
  title: '', author: '', isbn: '', categoryId: null, description: '', content: ''
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
      form.description = b.description || ''
      form.content = b.content || ''
    } catch (err) {
      console.error('获取图书详情失败:', err)
    }
  }
})

function triggerUpload() { fileInput.value?.click() }

function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    alert('封面仅支持 JPEG 或 PNG 格式')
    return
  }
  coverFile.value = file
  coverPreviewUrl.value = URL.createObjectURL(file)
}

function validate() {
  let valid = true
  if (!form.title.trim()) { errors.title = '书名不能为空'; valid = false } else errors.title = ''
  if (!form.author.trim()) { errors.author = '作者不能为空'; valid = false } else errors.author = ''
  if (!form.isbn.trim()) { errors.isbn = 'ISBN不能为空'; valid = false } else errors.isbn = ''
  return valid
}

function onEpubSelected(e) {
  const file = e.target.files[0]
  if (file) epubFile.value = file
}

function onDrop(e) {
  const file = e.dataTransfer.files[0]
  if (file && file.name.endsWith('.epub')) epubFile.value = file
}

function formatSize(bytes) {
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

async function save() {
  if (!form.isbn.trim()) return alert('请输入ISBN')
  if (!isEdit.value && !epubFile.value) return alert('请上传EPUB文件')
  submitting.value = true
  try {
    const fd = new FormData()
    if (epubFile.value) fd.append('file', epubFile.value)
    fd.append('title', form.title || '')
    fd.append('author', form.author || '')
    fd.append('isbn', form.isbn)
    if (form.categoryId) fd.append('categoryId', form.categoryId)
    if (coverFile.value) fd.append('cover', coverFile.value)
    if (form.description) fd.append('description', form.description)

    const onProgress = (e) => {
      if (e.total) uploadPercent.value = Math.round((e.loaded / e.total) * 100)
    }

    if (isEdit.value) {
      await updateBook(route.params.id, fd, onProgress)
    } else {
      await createBook(fd, onProgress)
    }
    router.push('/')
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || '保存失败')
  } finally {
    submitting.value = false
    uploadPercent.value = 0
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
.content-editor { min-height: 200px; }
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

.epub-upload-zone {
  border: 2px dashed #d0c8b4;
  border-radius: 8px;
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.2s;
  color: #a09880;
  font-size: 13px;
}
.epub-upload-zone:hover,
.epub-upload-zone.has-file {
  border-color: #c9a96e;
  color: #c9a96e;
}
.upload-icon {
  display: block;
  font-size: 28px;
  margin-bottom: 6px;
}
.epub-file-name { font-weight: 500; }
.epub-file-size { color: #a09880; margin-left: 8px; font-size: 12px; }
.epub-file-remove { margin-left: 12px; cursor: pointer; color: #c04040; }
.required { color: #c04040; }
.upload-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.progress-bar {
  flex: 1;
  height: 8px;
  background: #e8e4dc;
  border-radius: 4px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: var(--color-primary, #c9a96e);
  border-radius: 4px;
  transition: width 0.3s ease;
}
.progress-text {
  font-size: 13px;
  color: var(--color-text-secondary, #8b8070);
  min-width: 40px;
  text-align: right;
}

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