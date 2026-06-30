/* ============================================
   纸间共振 — 创作后台脚本 (creator.js)
   实时字数统计 · localStorage 自动保存 · 灵感轮换
   ============================================ */

(function() {
  'use strict';

  let works = [];
  let currentWorkId = null;
  let autoSaveTimer = null;

  /* ---- 加载作品 ---- */
  function loadWorks() {
    works = ZJ.getData('works', []);
    updateStats();
    renderWorksList();
    if (works.length > 0 && !currentWorkId) {
      selectWork(works[0].id);
    }
  }

  function updateStats() {
    document.getElementById('statPublished').textContent = works.filter(w => w.published).length;
    document.getElementById('statWords').textContent = works.reduce((s, w) => s + w.wordCount, 0).toLocaleString();
    document.getElementById('statReads').textContent = works.reduce((s, w) => s + (w.reads || 0), 0).toLocaleString();
    document.getElementById('statLikes').textContent = works.reduce((s, w) => s + (w.likes || 0), 0);
  }

  function renderWorksList() {
    const list = document.getElementById('worksList');
    const activeTab = document.querySelector('.ws-tab--active')?.dataset?.tab || 'published';
    let filtered = activeTab === 'published' ? works.filter(w => w.published) : works.filter(w => !w.published);

    if (filtered.length === 0) {
      list.innerHTML = '<li style="color:var(--text-secondary);justify-content:center;">暂无作品</li>';
      return;
    }

    list.innerHTML = filtered.map(w => `
      <li class="${w.id === currentWorkId ? 'active' : ''}" data-id="${w.id}">
        <span>${w.title || '未命名'}</span>
        <span class="item-badge">${w.published ? '已发布' : '草稿'}</span>
      </li>
    `).join('');

    list.querySelectorAll('li[data-id]').forEach(li => {
      li.addEventListener('click', () => selectWork(li.dataset.id));
    });
  }

  function selectWork(id) {
    currentWorkId = id;
    const work = works.find(w => w.id == id);
    if (!work) return;

    document.getElementById('editorTitle').value = work.title || '';
    document.getElementById('editorBody').value = work.content || '';
    updateWordCount();
    renderWorksList();
  }

  /* ---- 字数统计 ---- */
  function updateWordCount() {
    const text = document.getElementById('editorBody').value;
    const count = text.replace(/\s/g, '').length;
    document.getElementById('wordCount').textContent = count.toLocaleString();
  }

  /* ---- 自动保存 ---- */
  function autoSave() {
    if (!currentWorkId) return;

    const title = document.getElementById('editorTitle').value.trim();
    const content = document.getElementById('editorBody').value;
    const wordCount = content.replace(/\s/g, '').length;

    const idx = works.findIndex(w => w.id == currentWorkId);
    if (idx >= 0) {
      works[idx].title = title;
      works[idx].content = content;
      works[idx].wordCount = wordCount;
      works[idx].updatedAt = Date.now();
    }

    ZJ.saveData('works', works);

    const status = document.getElementById('saveStatus');
    status.textContent = '💾 已自动保存 ✓';
    status.style.opacity = '1';
    status.style.color = 'var(--accent)';
    setTimeout(() => { status.style.opacity = '0.5'; }, 2000);

    updateStats();
    renderWorksList();
  }

  /* ---- 新建作品 ---- */
  function createWork() {
    const work = {
      id: Date.now().toString(),
      title: '',
      content: '',
      wordCount: 0,
      published: false,
      reads: 0,
      likes: 0,
      createdAt: Date.now(),
      updatedAt: Date.now()
    };
    works.unshift(work);
    ZJ.saveData('works', works);
    selectWork(work.id);
    updateStats();
  }

  /* ---- 发布 ---- */
  function publishWork() {
    if (!currentWorkId) return alert('请先选择或创建一个作品');
    const idx = works.findIndex(w => w.id == currentWorkId);
    if (idx >= 0) {
      if (!works[idx].title.trim()) return alert('请输入作品标题');
      if (!works[idx].content.trim()) return alert('请输入作品内容');
      works[idx].published = true;
      works[idx].publishedAt = Date.now();
      ZJ.saveData('works', works);
      updateStats();
      renderWorksList();
      alert(`《${works[idx].title}》发布成功！`);
    }
  }

  /* ---- 灵感 ---- */
  function initInspiration() {
    const quotes = [
      { text: '"写作是一条认识自己、认识真理的路，你只要喜欢写，就应该随时动笔去写。"', author: '—— 罗兰' },
      { text: '"我写作，不是为了增添世上的书，而是为了减少世上的空白。"', author: '—— 张爱玲' },
      { text: '"一个作家，如果他还想写出好的作品，他就得不断地和自己过不去。"', author: '—— 老舍' },
      { text: '"写作的艺术，就是把裤子放在椅子上，把屁股放在椅子上，然后写。"', author: '—— 佚名' },
      { text: '"好文章是改出来的，不是写出来的。"', author: '—— 鲁迅' }
    ];

    let idx = 0;
    document.getElementById('btnInspo').addEventListener('click', () => {
      idx = (idx + 1) % quotes.length;
      const t = document.getElementById('inspoText');
      const a = document.getElementById('inspoAuthor');
      t.style.opacity = '0'; a.style.opacity = '0';
      setTimeout(() => {
        t.textContent = quotes[idx].text;
        a.textContent = quotes[idx].author;
        t.style.opacity = '1'; a.style.opacity = '1';
      }, 300);
    });
    document.getElementById('inspoText').style.transition = 'opacity 0.3s ease';
    document.getElementById('inspoAuthor').style.transition = 'opacity 0.3s ease';
  }

  /* ---- 初始化 ---- */
  document.addEventListener('DOMContentLoaded', () => {
    loadWorks();

    document.getElementById('editorBody').addEventListener('input', () => {
      updateWordCount();
      clearTimeout(autoSaveTimer);
      autoSaveTimer = setTimeout(autoSave, 3000);
    });

    document.getElementById('editorTitle').addEventListener('input', () => {
      clearTimeout(autoSaveTimer);
      autoSaveTimer = setTimeout(autoSave, 2000);
    });

    document.getElementById('btnNewWork').addEventListener('click', createWork);
    document.getElementById('btnSaveDraft').addEventListener('click', autoSave);
    document.getElementById('btnPublish').addEventListener('click', publishWork);

    document.querySelectorAll('.ws-tab').forEach(tab => {
      tab.addEventListener('click', function() {
        document.querySelectorAll('.ws-tab').forEach(t => t.classList.remove('ws-tab--active'));
        this.classList.add('ws-tab--active');
        renderWorksList();
      });
    });

    initInspiration();
  });
})();
