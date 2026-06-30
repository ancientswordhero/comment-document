/* ============================================
   纸间共振 — 首页脚本 (home.js)
   视差微动 · 动态内容填充
   ============================================ */

(function() {
  'use strict';

  /* ---- 1. 水墨层视差微动 ---- */
  function initParallax() {
    const hero = document.querySelector('.hero');
    const layers = document.querySelectorAll('.hero__layer');
    if (!hero || !layers.length) return;

    hero.addEventListener('mousemove', function(e) {
      const rect = hero.getBoundingClientRect();
      const x = (e.clientX - rect.left) / rect.width - 0.5;
      const y = (e.clientY - rect.top) / rect.height - 0.5;

      const speeds = [15, 25, 35];
      layers.forEach((layer, i) => {
        layer.style.transform = `translate(${x * speeds[i]}px, ${y * speeds[i]}px)`;
      });
    });

    hero.addEventListener('mouseleave', function() {
      layers.forEach(layer => {
        layer.style.transform = 'translate(0, 0)';
      });
    });
  }

  /* ---- 2. 加载精选推荐 ---- */
  function loadFeatured() {
    var grid = document.getElementById('featuredGrid');
    if (!grid) return;

    var books = window.BOOKS_DATA || [];
    var featured = books.filter(function(b) { return b.rating >= 4.8; }).slice(0, 3);

    grid.innerHTML = featured.map(function(b) {
      return '<a href="book.html?id=' + b.id + '" class="featured-card">' +
        '<div class="featured-card__cover"><img src="' + b.cover + '" alt="' + b.title + '" style="width:100%;height:100%;object-fit:cover;border-radius:4px;" onerror="this.parentElement.innerHTML=\'📖\'"></div>' +
        '<h3 class="featured-card__title">' + b.title + '</h3>' +
        '<p class="featured-card__author">' + b.author + '</p>' +
        '<div class="featured-card__rating">' +
          '<span class="stars">' + '★'.repeat(Math.floor(b.rating)) + '☆'.repeat(5 - Math.floor(b.rating)) + '</span>' +
          '<span style="font-size:14px;color:var(--text-secondary);">' + b.rating + '</span>' +
        '</div>' +
      '</a>';
    }).join('');
  }

  /* ---- 4. 加载最近更新 ---- */
  function loadRecent() {
    var timeline = document.getElementById('recentTimeline');
    if (!timeline) return;

    var books = window.BOOKS_DATA || [];
    var recent = books.slice().sort(function(a, b) { return b.year - a.year; }).slice(0, 5);

    timeline.innerHTML = recent.map(function(b) {
      return '<a href="book.html?id=' + b.id + '" class="timeline-item">' +
        '<div class="timeline-item__dot"></div>' +
        '<span class="timeline-item__title">' + b.title + '</span>' +
        '<span class="timeline-item__author">' + b.author + '</span>' +
      '</a>';
    }).join('');
  }

  /* ---- 5. 名言轮换 ---- */
  function initQuotes() {
    const quotes = [
      { text: '读书的意义大概就是用生活所感去读书，用读书所得去生活。', author: '杨绛' },
      { text: '读书多了，容颜自然改变。许多时候，自己可能以为许多看过的书籍都成过眼烟云，不复记忆，其实它们仍是潜在的。', author: '三毛' },
      { text: '喜欢读书，就等于把生活中寂寞的辰光换成巨大享受的时刻。', author: '孟德斯鸠' },
      { text: '一本书像一艘船，带领我们从狭隘的地方，驶向生活无限广阔的海洋。', author: '凯勒' },
      { text: '读一本好书，就是和许多高尚的人谈话。', author: '歌德' }
    ];

    let idx = 0;
    const textEl = document.getElementById('quoteContent');
    const authorEl = document.getElementById('quoteAuthor');

    if (!textEl || !authorEl) return;

    setInterval(() => {
      idx = (idx + 1) % quotes.length;
      textEl.style.opacity = '0';
      authorEl.style.opacity = '0';

      setTimeout(() => {
        textEl.textContent = quotes[idx].text;
        authorEl.textContent = '—— ' + quotes[idx].author;
        textEl.style.opacity = '1';
        authorEl.style.opacity = '1';
      }, 400);
    }, 8000);

    // 初始过渡样式
    textEl.style.transition = 'opacity 0.4s ease';
    authorEl.style.transition = 'opacity 0.4s ease';
  }

  /* ---- 初始化 ---- */
  document.addEventListener('DOMContentLoaded', () => {
    initParallax();
    loadFeatured();
    loadRecent();
    initQuotes();
  });

})();
