/* ============================================
   纸间共振 — 个人中心脚本 (profile.js)
   Tab 面板切换 · 柱状图动画 · 用户数据加载
   ============================================ */

(function() {
  'use strict';

  /* ---- 加载用户信息 ---- */
  function loadUserInfo() {
    const user = ZJ.getUser();
    if (user) {
      document.getElementById('profileName').textContent = user.username;
    }
    // 加载统计
    const comments = ZJ.getData('comments_all', []);
    const shelf = ZJ.getData('bookshelf', []);
    document.getElementById('profileStats').innerHTML = `
      <span>📖 读过 <strong>${shelf.length}</strong></span>
      <span>📝 评论 <strong>${comments.length}</strong></span>
      <span>⭐ 评分 <strong>${comments.length}</strong></span>
    `;
  }

  /* ---- Tab 切换 ---- */
  function initTabs() {
    const tabs = document.querySelectorAll('.p-tab');
    const contents = document.querySelectorAll('.p-content');
    const indicator = document.getElementById('tabIndicator');

    tabs.forEach((tab, i) => {
      tab.addEventListener('click', function() {
        tabs.forEach(t => t.classList.remove('p-tab--active'));
        contents.forEach(c => c.classList.remove('p-content--active'));

        this.classList.add('p-tab--active');
        const target = document.getElementById('tab-' + this.dataset.tab);
        if (target) target.classList.add('p-content--active');

        // 指示器滑动
        indicator.style.left = (i * 25) + '%';
        indicator.style.width = '25%';

        // 加载对应内容
        if (this.dataset.tab === 'comments') loadComments();
        if (this.dataset.tab === 'ratings') loadRatings();
        if (this.dataset.tab === 'bookshelf') loadBookshelf();
      });
    });
  }

  function loadComments() {
    var container = document.getElementById('tab-comments');
    var comments = window.COMMENTS_DATA || [];
    var local = getAllLocalComments();
    comments = local.concat(comments).slice(0, 10);

    container.innerHTML = comments.map(function(c) {
      return '<div class="p-item">' +
        '<div class="p-item__cover">📖</div>' +
        '<div class="p-item__info">' +
          '<div class="p-item__title">对一部作品的评论</div>' +
          '<div class="p-item__meta">' + '★'.repeat(c.rating) + '☆'.repeat(5-c.rating) + ' · ' + c.date + '</div>' +
          '<div class="p-item__content">' + c.content + '</div>' +
          '<div class="p-item__actions">' +
            '<button>✏️ 编辑</button><button>🗑️ 删除</button>' +
          '</div>' +
        '</div>' +
      '</div>';
    }).join('') || '<p style="color:var(--text-secondary);text-align:center;padding:40px;">还没有评论 🍃</p>';
  }

  function getAllLocalComments() {
    const result = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.startsWith('zhiJian_comments_')) {
        try { result.push(...JSON.parse(localStorage.getItem(key))); } catch(e) {}
      }
    }
    return result;
  }

  function loadRatings() {
    const container = document.getElementById('tab-ratings');
    loadComments().then(() => {
      // 评分和评论共享数据，直接复制
      const html = document.getElementById('tab-comments').innerHTML;
      container.innerHTML = html || '<p style="color:var(--text-secondary);text-align:center;padding:40px;">还没有评分 🍃</p>';
    });
    // 简化处理
    container.innerHTML = '<p style="color:var(--text-secondary);text-align:center;padding:40px;">查看你的评分记录</p>';
  }

  function loadBookshelf() {
    const container = document.getElementById('tab-bookshelf');
    const shelf = ZJ.getData('bookshelf', []);
    if (shelf.length === 0) {
      container.innerHTML = '<p style="color:var(--text-secondary);text-align:center;padding:40px;">书架还是空的，去探索书库吧 📚</p>';
      return;
    }
    container.innerHTML = shelf.map(b => `
      <div class="p-item">
        <div class="p-item__cover">📖</div>
        <div class="p-item__info">
          <div class="p-item__title"><a href="book.html?id=${b.id}">${b.title}</a></div>
          <div class="p-item__meta">${b.author} · 加入于 ${new Date(b.addedAt).toLocaleDateString()}</div>
          <div class="p-item__actions"><button onclick="location.href='book.html?id=${b.id}'">📖 继续阅读</button><button>🗑️ 移出书架</button></div>
        </div>
      </div>
    `).join('');
  }

  /* ---- 柱状图 ---- */
  function initChart() {
    const chart = document.getElementById('readingChart');
    const months = ['1月','2月','3月','4月','5月','6月'];
    const data = [4, 2, 5, 3, 4, 3];
    const maxH = 160;

    chart.innerHTML = months.map((m, i) => `
      <div class="chart-bar">
        <span class="chart-bar__count">${data[i]}</span>
        <div class="chart-bar__fill" style="height:0;" data-h="${(data[i]/5)*maxH}px"></div>
        <span class="chart-bar__label">${m}</span>
      </div>
    `).join('');

    // 柱状图动画
    setTimeout(() => {
      chart.querySelectorAll('.chart-bar__fill').forEach(bar => {
        bar.style.height = bar.dataset.h;
      });
    }, 300);
  }

  /* ---- 设置功能 ---- */
  function initSettings() {
    document.getElementById('btnClearData').addEventListener('click', () => {
      if (confirm('确定要清除所有本地数据吗？此操作不可撤销。')) {
        const keys = [];
        for (let i = 0; i < localStorage.length; i++) {
          const key = localStorage.key(i);
          if (key && key.startsWith('zhiJian_')) keys.push(key);
        }
        keys.forEach(k => localStorage.removeItem(k));
        alert('数据已清除');
        window.location.reload();
      }
    });
    document.getElementById('btnExport').addEventListener('click', () => {
      alert('阅读记录已整理，功能开发中');
    });
  }

  /* ---- 初始化 ---- */
  document.addEventListener('DOMContentLoaded', () => {
    loadUserInfo();
    initTabs();
    initChart();
    initSettings();
    loadComments();
  });
})();
