# UI 重设计 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 统一三个前端应用的视觉风格，创建共享设计令牌，实现 Bilibili 风格 Header Banner，打磨所有页面组件。

**Architecture:** 每个 app 独立持有 `design-tokens.css` 副本（避免跨项目依赖）。读者端新建 `BannerHeader.vue` 替换现有 AppHeader + BookCarousel。其余组件在现有文件基础上做 CSS 精炼。素材从 dt 目录复制到各 app 的 assets 目录。

**Tech Stack:** Vue 3 (Composition API + `<script setup>`), Vite, CSS Custom Properties

---

## File Structure

```
reader-app/src/assets/
  ├── design-tokens.css    (新建 - 设计令牌)
  ├── global.css            (修改 - 引用令牌)
  ├── banner-bg.jpg         (新建 - 从 dt/File0001.jpg 复制)
  └── banner-chibi.png      (新建 - 从 dt/File0002.png 复制)
reader-app/src/components/
  ├── BannerHeader.vue      (新建 - Bilibili 风格 Header)
  ├── AppHeader.vue          (保留不动，不再被引用)
  ├── BookCarousel.vue       (保留不动，不再被引用)
  ├── BookCard.vue           (修改)
  ├── Pagination.vue         (修改)
  ├── CategoryNav.vue        (不变)
  └── SearchBar.vue          (不变)
reader-app/src/views/
  ├── BookList.vue           (修改 - 使用 BannerHeader)
  └── BookDetail.vue         (修改)

admin-app/src/assets/
  ├── design-tokens.css    (新建)
  └── global.css            (修改)
admin-app/src/components/
  └── AdminHeader.vue       (修改)
admin-app/src/views/
  ├── BookTable.vue         (修改)
  ├── BookForm.vue          (修改)
  └── AdminForm.vue         (修改)

login-app/src/assets/
  ├── design-tokens.css    (新建)
  ├── global.css            (修改)
  └── login-bg.jpg          (新建 - 从 dt/File0007.jpg 复制)
login-app/src/components/
  └── AuthForm.vue          (修改)
```

---

### Task 1: 复制素材文件并创建设计令牌

**Files:**
- Create: `reader-app/src/assets/design-tokens.css`
- Create: `admin-app/src/assets/design-tokens.css`
- Create: `login-app/src/assets/design-tokens.css`
- Copy: `C:\Users\36295\Desktop\dt\File0001.jpg` → `reader-app/src/assets/banner-bg.jpg`
- Copy: `C:\Users\36295\Desktop\dt\File0002.png` → `reader-app/src/assets/banner-chibi.png`
- Copy: `C:\Users\36295\Desktop\dt\File0011.png` → `reader-app/src/assets/banner-character.png`
- Copy: `C:\Users\36295\Desktop\dt\File0007.jpg` → `login-app/src/assets/login-bg.jpg`

- [ ] **Step 1: 创建 reader-app 设计令牌文件**

```css
/* reader-app/src/assets/design-tokens.css */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Noto+Sans+SC:wght@400;500&display=swap');

:root {
  /* 颜色 */
  --color-bg:            #fafaf7;
  --color-card-bg:       #fff;
  --color-accent-light:  #f0ebe0;
  --color-primary:       #c9a96e;
  --color-primary-hover: #b8944d;
  --color-text:          #4a3d2f;
  --color-text-secondary:#8b8070;
  --color-text-muted:    #a09880;
  --color-border:        #e8e4dc;
  --color-card-border:   #ece8df;
  --color-danger:        #c04040;
  --color-success:       #5b8c5a;
  --color-warning:       #c08840;

  /* 圆角 */
  --radius-sm: 2px;
  --radius:     4px;
  --radius-lg:  8px;

  /* 间距 */
  --space-xs:  4px;
  --space-sm:  8px;
  --space-md:  12px;
  --space-lg:  16px;
  --space-xl:  24px;
  --space-2xl: 32px;

  /* 阴影 */
  --shadow-sm:  0 1px 3px rgba(74,61,47,0.06);
  --shadow-md:  0 4px 16px rgba(74,61,47,0.1);
  --shadow-lg:  0 8px 40px rgba(74,61,47,0.15);

  /* 字体 */
  --font-serif: 'Noto Serif SC', 'SimSun', serif;
  --font-sans:  'Noto Sans SC', 'Microsoft YaHei', sans-serif;

  /* 布局 */
  --header-padding: 10px 24px;
  --content-padding: 0 24px;
  --grid-cols: 5;
  --grid-gap: 16px;
  --category-width: 180px;
  --category-font-size: 14px;
  --category-line-height: 2;
  --card-padding: 12px;
}
```

- [ ] **Step 2: 复制到 admin-app 并微调布局变量**

```css
/* admin-app/src/assets/design-tokens.css */
/* 与 reader-app 相同，但布局变量适配管理后台 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Noto+Sans+SC:wght@400;500&display=swap');

:root {
  /* 颜色 — 与 reader-app 完全一致 */
  --color-bg:            #fafaf7;
  --color-card-bg:       #fff;
  --color-accent-light:  #f0ebe0;
  --color-primary:       #c9a96e;
  --color-primary-hover: #b8944d;
  --color-text:          #4a3d2f;
  --color-text-secondary:#8b8070;
  --color-text-muted:    #a09880;
  --color-border:        #e8e4dc;
  --color-card-border:   #ece8df;
  --color-danger:        #c04040;
  --color-success:       #5b8c5a;
  --color-warning:       #c08840;

  /* 圆角 */
  --radius-sm: 2px;
  --radius:     4px;
  --radius-lg:  8px;

  /* 间距 */
  --space-xs:  4px;
  --space-sm:  8px;
  --space-md:  12px;
  --space-lg:  16px;
  --space-xl:  24px;
  --space-2xl: 32px;

  /* 阴影 */
  --shadow-sm:  0 1px 3px rgba(74,61,47,0.06);
  --shadow-md:  0 4px 16px rgba(74,61,47,0.1);
  --shadow-lg:  0 8px 40px rgba(74,61,47,0.15);

  /* 字体 */
  --font-serif: 'Noto Serif SC', 'SimSun', serif;
  --font-sans:  'Noto Sans SC', 'Microsoft YaHei', sans-serif;

  /* 布局 — 管理后台专用 */
  --header-padding: 10px 24px;
  --content-padding: 16px 24px;
}
```

- [ ] **Step 3: 复制到 login-app 并微调布局变量**

```css
/* login-app/src/assets/design-tokens.css */
/* 与 reader-app 颜色/圆角/阴影完全一致 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Noto+Sans+SC:wght@400;500&display=swap');

:root {
  /* 颜色 — 完全一致 */
  --color-bg:            #fafaf7;
  --color-card-bg:       #fff;
  --color-accent-light:  #f0ebe0;
  --color-primary:       #c9a96e;
  --color-primary-hover: #b8944d;
  --color-text:          #4a3d2f;
  --color-text-secondary:#8b8070;
  --color-text-muted:    #a09880;
  --color-border:        #e8e4dc;
  --color-card-border:   #ece8df;
  --color-danger:        #c04040;
  --color-success:       #5b8c5a;
  --color-warning:       #c08840;

  /* 圆角 */
  --radius-sm: 2px;
  --radius:     4px;
  --radius-lg:  8px;

  /* 间距 */
  --space-xs:  4px;
  --space-sm:  8px;
  --space-md:  12px;
  --space-lg:  16px;
  --space-xl:  24px;
  --space-2xl: 32px;

  /* 阴影 */
  --shadow-sm:  0 1px 3px rgba(74,61,47,0.06);
  --shadow-md:  0 4px 16px rgba(74,61,47,0.1);
  --shadow-lg:  0 8px 40px rgba(74,61,47,0.15);

  /* 字体 */
  --font-serif: 'Noto Serif SC', 'SimSun', serif;
  --font-sans:  'Noto Sans SC', 'Microsoft YaHei', sans-serif;

  /* 布局 — 登录页专用 */
  --auth-width: 380px;
  --auth-padding: 36px 32px;
}
```

- [ ] **Step 4: 复制素材文件**

Run:
```bash
cp /c/Users/36295/Desktop/dt/File0001.jpg /c/Users/36295/Desktop/shuhai/reader-app/src/assets/banner-bg.jpg
cp /c/Users/36295/Desktop/dt/File0002.png /c/Users/36295/Desktop/shuhai/reader-app/src/assets/banner-chibi.png
cp /c/Users/36295/Desktop/dt/File0011.png /c/Users/36295/Desktop/shuhai/reader-app/src/assets/banner-character.png
cp /c/Users/36295/Desktop/dt/File0007.jpg /c/Users/36295/Desktop/shuhai/login-app/src/assets/login-bg.jpg
```

- [ ] **Step 5: 提交**

```bash
git add reader-app/src/assets/design-tokens.css reader-app/src/assets/banner-bg.jpg reader-app/src/assets/banner-chibi.png reader-app/src/assets/banner-character.png
git add admin-app/src/assets/design-tokens.css
git add login-app/src/assets/design-tokens.css login-app/src/assets/login-bg.jpg
git commit -m "feat: add shared design tokens and copy dt assets for UI redesign"
```

---

### Task 2: 创建 BannerHeader 组件（读者端）

**Files:**
- Create: `reader-app/src/components/BannerHeader.vue`
- Modify: `reader-app/src/App.vue`

- [ ] **Step 1: 查看 App.vue 当前布局**

Run: `cat reader-app/src/App.vue`

- [ ] **Step 2: 创建 BannerHeader.vue**

```vue
<!-- reader-app/src/components/BannerHeader.vue -->
<template>
  <div class="banner-wrapper">
    <!-- Banner area -->
    <div class="banner">
      <img :src="currentBg" class="banner-bg" alt="" />
      <div class="banner-overlay"></div>

      <!-- Left chibi decoration -->
      <img :src="chibiSrc" class="banner-chibi" alt="" />

      <!-- Right character illustration -->
      <img :src="characterSrc" class="banner-character" alt="" />

      <!-- Center content -->
      <div class="banner-center">
        <div class="banner-logo">
          <span class="logo-icon">書</span>
          <span class="logo-text">云图书馆</span>
        </div>
        <div class="banner-search">
          <input
            v-model="searchKeywords"
            class="search-input"
            placeholder="搜索书名 · 作者 · ISBN"
            @keyup.enter="onSearch"
          />
          <button class="search-btn" @click="onSearch">搜索</button>
        </div>
        <div class="banner-tagline">万卷古今消永日 · 一窗昏晓送流年</div>
      </div>
    </div>

    <!-- Transparent nav overlay -->
    <div class="banner-nav">
      <span class="nav-item active">首页</span>
      <span class="nav-sep">|</span>
      <span class="nav-item">分类浏览</span>
      <span class="nav-sep">|</span>
      <span class="nav-item">最新上架</span>
      <a href="http://localhost:5174" class="nav-admin">管理后台</a>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { setSearchKeyword } from '../composables/useSearch'
import bannerBg from '../assets/banner-bg.jpg'
import chibiSrc from '../assets/banner-chibi.png'
import characterSrc from '../assets/banner-character.png'

const searchKeywords = ref('')
const currentBg = ref(bannerBg)
let bannerTimer = null

const banners = [
  { title: '翰 墨 书 香', subtitle: '万卷古今消永日，一窗昏晓送流年' },
  { title: '书 香 满 堂', subtitle: '读书破万卷，下笔如有神' },
  { title: '文 以 载 道', subtitle: '腹有诗书气自华，读书万卷始通神' },
  { title: '开 卷 有 益', subtitle: '立身以立学为先，立学以读书为本' }
]

function onSearch() {
  setSearchKeyword(searchKeywords.value)
}

onUnmounted(() => {
  if (bannerTimer) clearInterval(bannerTimer)
})
</script>

<style scoped>
.banner-wrapper {
  position: relative;
}
.banner {
  position: relative;
  height: 300px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.banner-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg,
    rgba(255,255,255,0.1) 0%,
    rgba(255,255,255,0.05) 50%,
    var(--color-bg) 100%
  );
}
.banner-chibi {
  position: absolute;
  bottom: 0;
  left: 8%;
  height: 200px;
  object-fit: contain;
  z-index: 2;
}
.banner-character {
  position: absolute;
  bottom: 0;
  right: 5%;
  height: 270px;
  object-fit: contain;
  z-index: 2;
}
.banner-center {
  position: relative;
  z-index: 3;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.banner-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}
.logo-icon {
  width: 40px;
  height: 40px;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}
.logo-text {
  font-weight: 700;
  font-size: 26px;
  color: var(--color-text);
  font-family: var(--font-serif);
  letter-spacing: 4px;
  text-shadow: 0 1px 2px rgba(255,255,255,0.8);
}
.banner-search {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 24px;
  box-shadow: var(--shadow-md);
  overflow: hidden;
  width: 460px;
  max-width: 85vw;
}
.search-input {
  flex: 1;
  border: none;
  padding: 13px 18px;
  font-size: 13px;
  color: var(--color-text);
  outline: none;
  background: transparent;
}
.search-input::placeholder {
  color: var(--color-text-muted);
}
.search-btn {
  padding: 13px 24px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  font-size: 13px;
  cursor: pointer;
  font-family: var(--font-serif);
  letter-spacing: 2px;
  transition: background 0.2s;
}
.search-btn:hover {
  background: var(--color-primary-hover);
}
.banner-tagline {
  margin-top: 12px;
  font-size: 12px;
  color: var(--color-text-secondary);
  letter-spacing: 2px;
}
.banner-nav {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  padding: 10px 32px;
  background: rgba(255,255,255,0.12);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}
.nav-item {
  font-size: 12px;
  color: var(--color-text);
  cursor: pointer;
  font-weight: 400;
  transition: color 0.2s;
}
.nav-item:hover { color: var(--color-primary); }
.nav-item.active { font-weight: 500; color: var(--color-primary); }
.nav-sep {
  margin: 0 14px;
  color: rgba(74,61,47,0.15);
  font-size: 12px;
}
.nav-admin {
  margin-left: auto;
  padding: 5px 14px;
  background: rgba(201,169,110,0.2);
  color: var(--color-primary);
  border-radius: 16px;
  font-size: 11px;
  text-decoration: none;
  transition: all 0.2s;
}
.nav-admin:hover {
  background: var(--color-primary);
  color: #fff;
}

@media (max-width: 768px) {
  .banner { height: 220px; }
  .banner-chibi { display: none; }
  .banner-character { height: 180px; right: 2%; opacity: 0.6; }
  .logo-text { font-size: 20px; }
  .logo-icon { width: 32px; height: 32px; font-size: 16px; }
  .banner-search { width: 90vw; }
  .search-input { padding: 10px 14px; font-size: 12px; }
  .search-btn { padding: 10px 18px; font-size: 12px; }
  .banner-nav { padding: 8px 16px; }
  .nav-sep { margin: 0 8px; }
}

@media (max-width: 480px) {
  .banner { height: 180px; }
  .banner-character { display: none; }
  .logo-text { font-size: 17px; letter-spacing: 2px; }
  .banner-tagline { display: none; }
  .banner-nav { padding: 6px 12px; }
  .nav-item { font-size: 11px; }
}
</style>
```

- [ ] **Step 3: 提交**

```bash
git add reader-app/src/components/BannerHeader.vue
git commit -m "feat: add Bilibili-style BannerHeader component for reader-app"
```

---

### Task 3: 更新 reader-app 全局样式和页面引用

**Files:**
- Modify: `reader-app/src/assets/global.css`
- Modify: `reader-app/src/views/BookList.vue`
- Modify: `reader-app/src/App.vue`

- [ ] **Step 1: 重写 reader-app global.css，移除重复变量**

```css
/* reader-app/src/assets/global.css */
@import './design-tokens.css';

* { margin: 0; padding: 0; box-sizing: border-box; }

body {
  background: var(--color-bg);
  color: var(--color-text);
  font-family: var(--font-sans);
  font-size: 13px;
  line-height: 1.6;
}

a { color: inherit; text-decoration: none; }

@media (max-width: 1200px) {
  :root {
    --grid-cols: 4;
    --grid-gap: 14px;
  }
}

@media (max-width: 1024px) {
  :root {
    --header-padding: 8px 20px;
    --content-padding: 0 20px;
    --grid-cols: 4;
    --category-width: 160px;
  }
}

@media (max-width: 768px) {
  :root {
    --header-padding: 8px 16px;
    --content-padding: 0 16px;
    --grid-cols: 3;
    --grid-gap: 10px;
    --category-width: 140px;
    --card-padding: 8px;
  }
}

@media (max-width: 480px) {
  :root {
    --header-padding: 6px 12px;
    --content-padding: 0 12px;
    --grid-cols: 2;
    --grid-gap: 8px;
    --card-padding: 6px;
  }
}
```

- [ ] **Step 2: 更新 BookList.vue 使用 BannerHeader 替换 AppHeader + BookCarousel**

将 `<AppHeader />` 和 `<BookCarousel />` 替换为 `<BannerHeader />`：

```vue
<!-- reader-app/src/views/BookList.vue -->
<template>
  <div class="home-layout">
    <BannerHeader />
    <div class="home-body">
      <CategoryNav
        :categories="categories"
        :selected-id="selectedCategoryId"
        :selected-child-id="selectedChildId"
        @select="onCategorySelect"
        @select-child="onChildSelect"
      />
      <div class="home-main">
        <div v-if="loading" class="loading-text">加载中...</div>
        <div v-else-if="books.length === 0" class="empty-text">暂无图书</div>
        <div v-else class="book-grid">
          <BookCard v-for="book in books" :key="book.id" :book="book" />
        </div>
        <Pagination
          v-if="total > 0"
          :page="page" :total="total" :size="20"
          @change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import BannerHeader from '../components/BannerHeader.vue'
import CategoryNav from '../components/CategoryNav.vue'
import BookCard from '../components/BookCard.vue'
import Pagination from '../components/Pagination.vue'
import { getBooks, getCategories } from '../api/book'
import { useSearchState } from '../composables/useSearch'

const books = ref([])
const categories = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const selectedCategoryId = ref(null)
const selectedChildId = ref(null)

const { searchKeyword } = useSearchState()

onMounted(async () => {
  await fetchCategories()
  fetchBooks()
})

async function fetchCategories() {
  try {
    const catRes = await getCategories()
    categories.value = catRes
  } catch (err) {
    console.error('获取分类失败:', err)
  }
}

async function fetchBooks() {
  loading.value = true
  try {
    const effectiveCategory = selectedChildId.value || selectedCategoryId.value
    const res = await getBooks({
      keyword: searchKeyword.value || undefined,
      categoryId: effectiveCategory || undefined,
      page: page.value,
      size: 20
    })
    books.value = res.records
    total.value = res.total
  } catch (err) {
    console.error('获取图书失败:', err)
  } finally {
    loading.value = false
  }
}

watch(searchKeyword, () => {
  page.value = 1
  fetchBooks()
})

function onCategorySelect(catId) {
  selectedCategoryId.value = selectedCategoryId.value === catId ? null : catId
  selectedChildId.value = null
  page.value = 1
  fetchBooks()
}

function onChildSelect(childId) {
  selectedChildId.value = childId
  page.value = 1
  fetchBooks()
}

function onPageChange(p) {
  page.value = p
  fetchBooks()
}
</script>

<style scoped>
.home-layout { padding-top: 0; }
.home-body { display: flex; gap: 24px; padding: 20px var(--content-padding); }
.home-main { flex: 1; }
.book-grid {
  display: grid;
  grid-template-columns: repeat(var(--grid-cols), 1fr);
  gap: var(--grid-gap);
}
.loading-text, .empty-text { text-align: center; padding: 40px; color: var(--color-text-muted); }

@media (max-width: 768px) {
  .home-body { flex-direction: column; gap: 12px; padding: 12px var(--content-padding); }
  .book-grid { gap: 10px; }
}

@media (max-width: 480px) {
  .home-body { padding: 8px var(--content-padding); }
}
</style>
```

- [ ] **Step 3: 提交**

```bash
git add reader-app/src/assets/global.css reader-app/src/views/BookList.vue
git commit -m "refactor: update reader-app global styles and use BannerHeader in BookList"
```

---

### Task 4: 升级读者端 BookCard 组件

**Files:**
- Modify: `reader-app/src/components/BookCard.vue`

- [ ] **Step 1: 重写 BookCard.vue 样式**

```vue
<!-- reader-app/src/components/BookCard.vue -->
<template>
  <div class="book-card" @click="$router.push(`/book/${book.id}`)">
    <div class="book-cover">
      <img v-if="book.coverUrl" :src="coverSrc" :alt="book.title" />
      <span v-else class="cover-placeholder">📖</span>
    </div>
    <div class="book-title">{{ book.title }}</div>
    <div class="book-author">{{ book.author }}</div>
    <div v-if="book.categoryName" class="book-category">{{ book.categoryName }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ book: { type: Object, required: true } })
const coverSrc = computed(() =>
  props.book.coverUrl ? `http://localhost:8080${props.book.coverUrl}` : null
)
</script>

<style scoped>
.book-card {
  background: var(--color-card-bg);
  border: 1px solid var(--color-card-border);
  border-radius: var(--radius);
  padding: 0 0 var(--card-padding) 0;
  text-align: center;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s, transform 0.2s;
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}
.book-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
  transform: translateY(-2px);
}
.book-cover {
  background: linear-gradient(135deg, #f5f1ea, #ede6d8);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  aspect-ratio: 2 / 3;
}
.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  font-size: 36px;
  color: #d0c8b4;
}
.book-title {
  font-weight: 500;
  font-size: 13px;
  color: var(--color-text);
  margin-top: 10px;
  padding: 0 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.book-author {
  font-size: 11px;
  color: var(--color-text-muted);
  margin-top: 3px;
  padding: 0 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.book-category {
  margin-top: 6px;
  padding: 2px 8px;
  background: var(--color-accent-light);
  color: var(--color-primary);
  border-radius: 10px;
  font-size: 10px;
  display: inline-block;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add reader-app/src/components/BookCard.vue
git commit -m "refactor: upgrade BookCard with larger cover, category tag, and refined shadows"
```

---

### Task 5: 升级读者端 Pagination 和 BookDetail 组件

**Files:**
- Modify: `reader-app/src/components/Pagination.vue`
- Modify: `reader-app/src/views/BookDetail.vue`

- [ ] **Step 1: 更新 Pagination.vue 样式**

修改 `<style scoped>` 块，使用新的令牌变量：

```css
<style scoped>
.pagination {
  text-align: center;
  margin-top: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.pagination button {
  padding: 6px 12px;
  border: 1px solid var(--color-border);
  background: var(--color-card-bg);
  border-radius: var(--radius);
  font-size: 12px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}
.pagination button:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.pagination button.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
  font-weight: 500;
}
.pagination button:disabled {
  color: #d0c8b4;
  cursor: not-allowed;
}
</style>
```

（仅替换 `<style scoped>` 块，`<template>` 和 `<script setup>` 保持不变）

- [ ] **Step 2: 更新 BookDetail.vue 样式**

修改 `<style scoped>` 块：

```css
<style scoped>
.detail-page { padding: 24px 32px; }
.detail-card {
  display: flex;
  gap: 28px;
  background: var(--color-card-bg);
  border: 1px solid var(--color-card-border);
  border-radius: var(--radius);
  padding: 24px;
}
.detail-cover {
  width: 260px;
  height: 360px;
  background: linear-gradient(135deg, #f5f1ea, #ede6d8);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.detail-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { font-size: 64px; color: #d0c8b4; }
.detail-info { flex: 1; }
.detail-title {
  font-size: 24px;
  font-family: var(--font-serif);
  color: var(--color-text);
  font-weight: 600;
  letter-spacing: 2px;
  margin-bottom: 20px;
}
.detail-meta { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 24px; }
.meta-item { font-size: 13px; color: var(--color-text-secondary); }
.meta-label { color: var(--color-text-muted); margin-right: 8px; font-size: 12px; }
.detail-desc-title {
  font-size: 14px;
  font-family: var(--font-serif);
  color: var(--color-text);
  margin-bottom: 8px;
  font-weight: 600;
}
.detail-desc { font-size: 13px; color: var(--color-text-secondary); line-height: 1.8; }
.back-btn {
  margin-top: 20px;
  padding: 8px 20px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s;
}
.back-btn:hover { background: var(--color-primary-hover); }
.loading-text { text-align: center; padding: 60px; color: var(--color-text-muted); }

@media (max-width: 768px) {
  .detail-page { padding: 16px; }
  .detail-card { flex-direction: column; }
  .detail-cover { width: 100%; height: auto; aspect-ratio: 2/3; max-height: 400px; margin: 0 auto; }
}
</style>
```

（仅替换 `<style scoped>` 块）

- [ ] **Step 3: 提交**

```bash
git add reader-app/src/components/Pagination.vue reader-app/src/views/BookDetail.vue
git commit -m "refactor: refine Pagination and BookDetail styles with design tokens"
```

---

### Task 6: 更新 admin-app 全局样式和 AdminHeader

**Files:**
- Modify: `admin-app/src/assets/global.css`
- Modify: `admin-app/src/components/AdminHeader.vue`

- [ ] **Step 1: 重写 admin-app global.css**

```css
/* admin-app/src/assets/global.css */
@import './design-tokens.css';

* { margin: 0; padding: 0; box-sizing: border-box; }

body {
  background: var(--color-bg);
  color: var(--color-text);
  font-family: var(--font-sans);
  font-size: 13px;
}

a { color: inherit; text-decoration: none; }

@media (max-width: 1024px) {
  :root {
    --header-padding: 8px 20px;
    --content-padding: 12px 20px;
  }
}

@media (max-width: 768px) {
  :root {
    --header-padding: 8px 16px;
    --content-padding: 10px 16px;
  }
}

@media (max-width: 480px) {
  :root {
    --header-padding: 6px 12px;
    --content-padding: 8px 12px;
  }
}
```

- [ ] **Step 2: 更新 AdminHeader.vue**

修改 `<style scoped>` 块：

```css
<style scoped>
.app-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--header-padding); background: var(--color-card-bg);
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  position: sticky; top: 0; z-index: 100;
}
.header-left { display: flex; align-items: center; gap: 8px; }
.logo-icon {
  width: 30px; height: 30px;
  background: var(--color-primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; border-radius: var(--radius);
}
.logo-text {
  font-weight: 600; font-size: 15px; color: var(--color-text);
  font-family: var(--font-serif); letter-spacing: 2px;
}
.header-right { display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--color-text-secondary); }
.nav-divider { color: var(--color-border); }
.nav-link {
  cursor: pointer;
  padding: 5px 10px;
  border-radius: var(--radius);
  transition: all 0.2s;
}
.nav-link:hover { background: var(--color-accent-light); color: var(--color-primary); }
.nav-link:last-of-type:hover { background: #fff3f3; color: var(--color-danger); }
.mobile-menu-btn { display: none; background: none; border: none; font-size: 20px; cursor: pointer; color: var(--color-text); }
.mobile-menu {
  display: none;
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--color-card-bg);
  border-bottom: 1px solid var(--color-border);
  padding: 12px;
  box-shadow: var(--shadow-md);
  flex-direction: column;
  gap: 8px;
}
.mobile-menu-item {
  display: block;
  padding: 10px 16px;
  background: var(--color-accent-light);
  color: var(--color-primary);
  border-radius: var(--radius);
  text-align: center;
  cursor: pointer;
}
.mobile-menu-item:last-child { background: #fff3f3; color: var(--color-danger); }

@media (max-width: 768px) {
  .desktop-only { display: none; }
  .mobile-only { display: block; }
  .mobile-menu { display: flex; }
  .logo-text { font-size: 13px; }
}

@media (max-width: 480px) {
  .logo-text { display: none; }
}
</style>
```

（`<template>` 和 `<script setup>` 保持不变）

- [ ] **Step 3: 提交**

```bash
git add admin-app/src/assets/global.css admin-app/src/components/AdminHeader.vue
git commit -m "refactor: update admin-app global styles and AdminHeader with design tokens"
```

---

### Task 7: 升级 admin-app 图书表格和表单

**Files:**
- Modify: `admin-app/src/views/BookTable.vue`
- Modify: `admin-app/src/views/BookForm.vue`
- Modify: `admin-app/src/views/AdminForm.vue`

- [ ] **Step 1: 更新 BookTable.vue 样式**

修改 `<style scoped>` 块：

```css
<style scoped>
.table-page { padding: var(--content-padding); }
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; flex-wrap: wrap; }
.toolbar-search {
  flex: 1; min-width: 160px; max-width: 280px; padding: 8px 12px;
  border: 1px solid var(--color-border); border-radius: var(--radius);
  font-size: 12px; outline: none; color: var(--color-text);
}
.toolbar-search::placeholder { color: var(--color-text-muted); }
.toolbar-select {
  padding: 8px 12px; border: 1px solid var(--color-border); border-radius: var(--radius);
  font-size: 12px; color: var(--color-text-secondary); outline: none; background: var(--color-card-bg);
}
.btn-add {
  margin-left: auto; padding: 8px 20px;
  background: var(--color-primary); color: #fff; border: none;
  border-radius: var(--radius); font-size: 12px; cursor: pointer;
  transition: background 0.2s;
}
.btn-add:hover { background: var(--color-primary-hover); }
.table-wrapper { overflow-x: auto; }
.data-table {
  width: 100%; border-collapse: collapse; font-size: 12px; min-width: 700px;
}
.data-table th {
  text-align: left; padding: 10px 12px; color: var(--color-text-secondary);
  font-weight: 500; border-bottom: 2px solid var(--color-border);
  background: var(--color-bg);
}
.data-table td { padding: 10px 12px; border-bottom: 1px solid var(--color-accent-light); }
.thumb {
  width: 36px; height: 48px; background: var(--color-accent-light);
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; color: var(--color-text-muted); overflow: hidden;
  border-radius: var(--radius-sm);
}
.thumb img { width: 100%; height: 100%; object-fit: cover; }
.cell-title { color: var(--color-text); font-weight: 500; }
.cell-isbn { color: var(--color-text-muted); font-size: 11px; }
.status-tag {
  padding: 2px 10px; border-radius: 12px; font-size: 11px; font-weight: 500;
}
.status-tag.up { background: #e8f5e9; color: var(--color-success); }
.status-tag.down { background: #fff3e0; color: var(--color-warning); }
.cell-actions a { cursor: pointer; color: var(--color-text-secondary); }
.cell-actions a:hover { color: var(--color-primary); }
.cell-actions a.del:hover { color: var(--color-danger); }
.cell-actions .sep { color: var(--color-border); margin: 0 4px; }
.table-pagination {
  text-align: center; margin-top: 16px;
  display: flex; align-items: center; justify-content: center; gap: 12px; font-size: 12px;
}
.table-pagination button {
  padding: 5px 14px; border: 1px solid var(--color-border);
  background: var(--color-card-bg); border-radius: var(--radius);
  color: var(--color-text-secondary); cursor: pointer;
  transition: all 0.2s;
}
.table-pagination button:hover:not(:disabled) {
  border-color: var(--color-primary); color: var(--color-primary);
}
.table-pagination button:disabled { color: #d0c8b4; cursor: not-allowed; }
.page-info { color: var(--color-text-muted); }

@media (max-width: 768px) {
  .toolbar { gap: 6px; }
  .toolbar-search { min-width: 120px; }
  .btn-add { padding: 6px 14px; font-size: 11px; }
}

@media (max-width: 480px) {
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar-search { max-width: 100%; }
  .btn-add { margin-left: 0; text-align: center; }
  .table-pagination { flex-wrap: wrap; gap: 8px; }
}
</style>
```

（`<template>` 和 `<script setup>` 保持不变）

- [ ] **Step 2: 更新 BookForm.vue 样式**

修改 `<style scoped>` 块：

```css
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
```

- [ ] **Step 3: 更新 AdminForm.vue 样式**

修改 `<style scoped>` 块：

```css
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
```

- [ ] **Step 4: 提交**

```bash
git add admin-app/src/views/BookTable.vue admin-app/src/views/BookForm.vue admin-app/src/views/AdminForm.vue
git commit -m "refactor: upgrade admin-app table, forms with unified design tokens and rounded elements"
```

---

### Task 8: 升级登录页 — 全屏背景 + 毛玻璃卡片

**Files:**
- Modify: `login-app/src/assets/global.css`
- Modify: `login-app/src/components/AuthForm.vue`

- [ ] **Step 1: 重写 login-app global.css**

```css
/* login-app/src/assets/global.css */
@import './design-tokens.css';

* { margin: 0; padding: 0; box-sizing: border-box; }

body {
  background: var(--color-bg);
  color: var(--color-text);
  font-family: var(--font-sans);
  font-size: 13px;
}

a { color: inherit; text-decoration: none; }

@media (max-width: 480px) {
  :root {
    --auth-width: 100%;
    --auth-padding: 24px 20px;
  }
}
```

- [ ] **Step 2: 更新 AuthForm.vue**

修改 `<template>` 最外层包裹和 `<style scoped>`：

```vue
<template>
  <div class="auth-wrapper">
    <!-- Background image -->
    <img :src="bgSrc" class="auth-bg" alt="" />
    <div class="auth-overlay"></div>

    <div class="auth-card">
      <div class="auth-logo">
        <span class="logo-icon">書</span>
        <span class="logo-text">云图书馆</span>
      </div>

      <div class="auth-tabs">
        <button
          :class="{ active: tab === 'login' }"
          @click="switchTab('login')"
        >登录</button>
        <button
          :class="{ active: tab === 'register' }"
          @click="switchTab('register')"
        >注册</button>
      </div>

      <div class="auth-form" v-if="tab === 'login'">
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
      </div>

      <div class="auth-form" v-else>
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
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { login, register } from '../api/auth'
import bgSrc from '../assets/login-bg.jpg'

const tab = ref('login')
const loggingIn = ref(false)
const registering = ref(false)
const loginError = ref('')
const regError = ref('')

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', confirmPassword: '' })

function switchTab(t) {
  tab.value = t
  loginError.value = ''
  regError.value = ''
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

async function onLogin() {
  if (!validateLogin()) return
  loggingIn.value = true
  try {
    const res = await login(loginForm.username, loginForm.password)
    const { token, role } = res.data
    const target = role === 'ADMIN'
      ? `http://localhost:5174/?token=${token}`
      : `http://localhost:5173/?token=${token}`
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
    const { token } = res.data
    window.location.href = `http://localhost:5173/?token=${token}`
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
    rgba(245,240,232,0.6) 0%,
    rgba(250,250,247,0.7) 50%,
    rgba(245,240,232,0.6) 100%
  );
  z-index: 1;
}
.auth-card {
  position: relative;
  z-index: 2;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.6);
  padding: var(--auth-padding);
  width: var(--auth-width);
  text-align: center;
  border-radius: var(--radius);
  box-shadow: var(--shadow-lg);
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
.auth-tabs { display: flex; gap: 0; margin-bottom: 20px; border-bottom: 1px solid var(--color-border); }
.auth-tabs button {
  flex: 1; padding: 8px 0; background: none; border: none;
  font-size: 13px; color: var(--color-text-muted); cursor: pointer;
  border-bottom: 2px solid transparent; font-family: var(--font-serif);
  transition: all 0.2s;
}
.auth-tabs button.active {
  color: var(--color-primary); border-bottom-color: var(--color-primary);
  font-weight: 500;
}
.auth-form { text-align: left; }
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

@media (max-width: 480px) {
  .auth-wrapper { padding: 20px 12px; align-items: flex-start; padding-top: 60px; }
  .logo-text { font-size: 16px; }
  .auth-tabs button { font-size: 12px; padding: 6px 0; }
}
</style>
```

- [ ] **Step 3: 提交**

```bash
git add login-app/src/assets/global.css login-app/src/components/AuthForm.vue
git commit -m "feat: add full-screen dt background and glassmorphism card to login page"
```

---

### Task 9: 最终走查 — 跨应用一致性检查

**Files:** (仅检查，无代码修改)

- [ ] **Step 1: 验证 reader-app 构建正常**

```bash
cd reader-app && npx vite build
```
Expected: 构建成功，无 CSS 相关报错。

- [ ] **Step 2: 验证 admin-app 构建正常**

```bash
cd admin-app && npx vite build
```
Expected: 构建成功，无 CSS 相关报错。

- [ ] **Step 3: 验证 login-app 构建正常**

```bash
cd login-app && npx vite build
```
Expected: 构建成功，无 CSS 相关报错。

- [ ] **Step 4: 检查跨应用颜色一致性**

Run:
```bash
grep -rn "var(--color-" reader-app/src/ admin-app/src/ login-app/src/ --include="*.vue" --include="*.css" | head -40
```
Expected: 所有 app 使用相同的 `var(--color-*)` 变量，无硬编码颜色值。

- [ ] **Step 5: 检查圆角一致性**

Run:
```bash
grep -rn "border-radius:" reader-app/src/ admin-app/src/ login-app/src/ --include="*.vue" --include="*.css" | grep -v node_modules
```
Expected: 所有 `border-radius` 值为 `var(--radius)`、`var(--radius-sm)`、`var(--radius-lg)` 或特定值（如搜索框 pill 形状的 24px）。

- [ ] **Step 6: 提交最终检查结果（如有修正）**

```bash
git add -A && git commit -m "chore: final consistency pass for UI redesign" || echo "No changes needed"
```
