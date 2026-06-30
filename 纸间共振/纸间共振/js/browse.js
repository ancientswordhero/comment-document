/* ============================================
   纸间共振 — 浏览/分类脚本 (browse.js)
   实时搜索 · 标签筛选 · 排序切换 · 标签云 · 作者滚动
   ============================================ */

(function() {
  'use strict';

  var allBooks = window.BOOKS_DATA || [];
  var activeCat = '全部';
  var activeSearch = '';

  /* ---- 获取筛选值 ---- */
  function getEra() {
    var el = document.querySelector('input[name="era"]:checked');
    return el ? el.value : 'all';
  }
  function getWordCount() {
    var el = document.querySelector('input[name="wordCount"]:checked');
    return el ? el.value : 'all';
  }
  function getSort() {
    var el = document.querySelector('input[name="sort"]:checked');
    return el ? el.value : 'default';
  }

  /* ---- 过滤书籍 ---- */
  function applyFilters() {
    var books = allBooks.slice();

    // 搜索过滤
    if (activeSearch) {
      var kw = activeSearch.toLowerCase();
      books = books.filter(function(b) {
        return b.title.indexOf(kw) !== -1 ||
          b.author.indexOf(kw) !== -1 ||
          b.tags.some(function(t) { return t.indexOf(kw) !== -1; }) ||
          b.description.indexOf(kw) !== -1;
      });
    }

    // 分类过滤
    if (activeCat !== '全部') {
      books = books.filter(function(b) { return b.category === activeCat; });
    }

    // 年代过滤
    var era = getEra();
    if (era === 'modern') {
      books = books.filter(function(b) { return b.year >= 1840 && b.year < 1919; });
    } else if (era === 'modern-contemp') {
      books = books.filter(function(b) { return b.year >= 1919 && b.year < 1949; });
    } else if (era === 'contemp') {
      books = books.filter(function(b) { return b.year >= 1949; });
    }

    // 篇幅过滤
    var wc = getWordCount();
    if (wc !== 'all') {
      books = books.filter(function(b) { return b.wordCount === wc; });
    }

    // 排序
    var sort = getSort();
    if (sort === 'rating') {
      books.sort(function(a, b) { return b.rating - a.rating; });
    } else if (sort === 'newest') {
      books.sort(function(a, b) { return b.year - a.year; });
    } else if (sort === 'popular') {
      books.sort(function(a, b) { return (b.readCount || 0) - (a.readCount || 0); });
    }

    renderGrid(books);
  }

  /* ---- 渲染网格 ---- */
  function renderGrid(books) {
    var grid = document.getElementById('browseGrid');
    if (books.length === 0) {
      grid.innerHTML = '<p style="grid-column:1/-1;text-align:center;padding:60px;color:var(--text-secondary);">没有找到匹配的书籍 🍃</p>';
      return;
    }
    grid.innerHTML = books.map(function(b) {
      return '<a href="book.html?id=' + b.id + '" class="book-card">' +
        '<div class="book-card__cover"><img src="' + b.cover + '" alt="' + b.title + '" style="width:100%;height:100%;object-fit:cover;border-radius:4px;" onerror="this.style.display=\'none\';this.parentElement.innerHTML=\'📖\'"></div>' +
        '<h3 class="book-card__title">' + b.title + '</h3>' +
        '<p class="book-card__author">' + b.author + '</p>' +
        '<div class="stars">' + '★'.repeat(Math.floor(b.rating)) + '☆'.repeat(5 - Math.floor(b.rating)) + '</div>' +
        '<div class="book-card__tags">' + b.tags.slice(0, 2).map(function(t) { return '<span class="book-card__tag">' + t + '</span>'; }).join('') + '</div>' +
      '</a>';
    }).join('');
  }

  /* ---- 实时搜索 ---- */
  function initSearch() {
    var input = document.getElementById('searchInput');
    if (!input) return;

    input.addEventListener('input', function() {
      activeSearch = this.value.trim();
      applyFilters();
    });
  }

  /* ---- 分类标签筛选 ---- */
  function initFilterTabs() {
    var tabs = document.querySelectorAll('#filterTabs .filter-tab');
    tabs.forEach(function(tab) {
      tab.addEventListener('click', function() {
        tabs.forEach(function(t) { t.classList.remove('filter-tab--active'); });
        this.classList.add('filter-tab--active');
        activeCat = this.dataset.cat;
        applyFilters();
      });
    });
  }

  /* ---- 筛选器变化 ---- */
  function initFilters() {
    document.querySelectorAll('input[name="era"], input[name="wordCount"], input[name="sort"]').forEach(function(input) {
      input.addEventListener('change', applyFilters);
    });
  }

  /* ---- 标签云 ---- */
  function renderTagCloud() {
    var cloud = document.getElementById('tagCloud');
    if (!cloud) return;

    var tagCount = {};
    allBooks.forEach(function(b) {
      b.tags.forEach(function(t) { tagCount[t] = (tagCount[t] || 0) + 1; });
    });

    var colors = ['#6baa7a', '#7ba8c8', '#d4b87a', '#b0a0c0', '#d49a9a'];
    var entries = [];
    for (var tag in tagCount) {
      if (tagCount.hasOwnProperty(tag)) {
        entries.push({ tag: tag, count: tagCount[tag] });
      }
    }
    entries.sort(function(a, b) { return b.count - a.count; });

    cloud.innerHTML = entries.map(function(entry, i) {
      var size = 13 + entry.count * 2;
      var color = colors[i % colors.length];
      return '<span class="tag" style="font-size:' + size + 'px;color:' + color + ';cursor:pointer;" data-tag="' + entry.tag + '">' + entry.tag + ' (' + entry.count + ')</span>';
    }).join('');

    // 点击标签触发搜索
    cloud.querySelectorAll('.tag').forEach(function(tag) {
      tag.addEventListener('click', function() {
        var t = this.dataset.tag;
        document.getElementById('searchInput').value = t;
        activeSearch = t;
        activeCat = '全部';
        document.querySelectorAll('#filterTabs .filter-tab').forEach(function(tab) { tab.classList.remove('filter-tab--active'); });
        var allTab = document.querySelector('#filterTabs .filter-tab[data-cat="全部"]');
        if (allTab) allTab.classList.add('filter-tab--active');
        applyFilters();
      });
    });
  }

  /* ---- 推荐作者 ---- */
  function renderAuthors() {
    var scroll = document.getElementById('authorScroll');
    if (!scroll) return;

    var seen = {};
    var authors = [];
    allBooks.forEach(function(b) {
      if (!seen[b.author]) {
        seen[b.author] = true;
        authors.push(b);
      }
    });

    scroll.innerHTML = authors.map(function(a) {
      var count = allBooks.filter(function(b) { return b.author === a.author; }).length;
      return '<a href="author.html?author=' + encodeURIComponent(a.author) + '" class="author-item">' +
        '<div class="author-item__avatar"><img src="' + a.authorAvatar + '" alt="' + a.author + '" style="width:100%;height:100%;object-fit:cover;border-radius:50%;" onerror="this.parentElement.innerHTML=\'✍️\'"></div>' +
        '<span class="author-item__name">' + a.author + '</span>' +
        '<span class="author-item__count">' + count + ' 部作品</span>' +
      '</a>';
    }).join('');
  }

  /* ---- URL参数读取 ---- */
  function readUrlParams() {
    var params = new URLSearchParams(window.location.search);
    var cat = params.get('cat');
    if (cat) {
      activeCat = cat;
      document.querySelectorAll('#filterTabs .filter-tab').forEach(function(tab) {
        tab.classList.toggle('filter-tab--active', tab.dataset.cat === cat);
      });
      document.getElementById('searchInput').value = cat;
      activeSearch = cat;
    }
  }

  /* ---- 初始化 ---- */
  document.addEventListener('DOMContentLoaded', function() {
    readUrlParams();
    applyFilters();
    renderTagCloud();
    renderAuthors();
    initSearch();
    initFilterTabs();
    initFilters();
  });

})();
