/* ============================================
   纸间共振 — 阅读器脚本 (book.js)
   字号/行距/主题切换 · 评论加载 · localStorage 记忆
   ============================================ */

(function() {
  'use strict';

  var currentBook = null;
  var currentFontSize = 18;
  var currentLineHeight = 2.0;
  var currentTheme = 'light';
  var commentRating = 0;

  /* ---- 加载书籍 ---- */
  function loadBook() {
    var params = new URLSearchParams(window.location.search);
    var id = parseInt(params.get('id')) || 1;

    var books = window.BOOKS_DATA || [];
    currentBook = books.find(function(b) { return b.id === id; }) || books[0];

    if (!currentBook) {
      document.getElementById('bookContent').innerHTML = '<p>未找到该书籍，请返回书库浏览。</p>';
      return;
    }

    document.getElementById('bookTitle').textContent = currentBook.title;
    document.getElementById('bookAuthor').innerHTML = '<a href="author.html?author=' + encodeURIComponent(currentBook.author) + '">' + currentBook.author + '</a>';
    document.getElementById('bookRating').innerHTML =
      '<span class="stars">' + '★'.repeat(Math.floor(currentBook.rating)) + '☆'.repeat(5 - Math.floor(currentBook.rating)) + '</span>' +
      '<span style="font-size:14px;color:var(--text-secondary);margin-left:6px;">' + currentBook.rating + '</span>';
    document.getElementById('bookMeta').textContent =
      currentBook.wordCount + ' · ' + currentBook.year + ' · ' + (currentBook.readCount ? currentBook.readCount.toLocaleString() : 0) + ' 次阅读';
    document.getElementById('bookDesc').textContent = currentBook.description;

    // 封面图片
    var coverEl = document.getElementById('bookCover');
    if (coverEl && currentBook.cover) {
      coverEl.innerHTML = '<img src="' + currentBook.cover + '" alt="' + currentBook.title + '" style="width:100%;height:100%;object-fit:cover;border-radius:8px;" onerror="this.parentElement.innerHTML=\'<span style=font-size:56px;>📖</span>\'">';
    }

    // 正文（按句号分段）
    var content = currentBook.content || '';
    var paragraphs = content.split('。').filter(function(p) { return p.trim(); }).map(function(p) { return p.trim() + '。'; });
    document.getElementById('bookContent').innerHTML = paragraphs.map(function(p) { return '<p>' + p + '</p>'; }).join('\n');

    document.title = currentBook.title + ' — 纸间共振';
    loadComments();
  }

  /* ---- 恢复阅读偏好 ---- */
  function restorePreferences() {
    var saved = ZJ.getData('readerPrefs', {});
    if (saved.fontSize) currentFontSize = saved.fontSize;
    if (saved.lineHeight) currentLineHeight = saved.lineHeight;
    if (saved.theme) currentTheme = saved.theme;
    applyPreferences();
  }

  function savePreferences() {
    ZJ.saveData('readerPrefs', {
      fontSize: currentFontSize,
      lineHeight: currentLineHeight,
      theme: currentTheme
    });
  }

  function applyPreferences() {
    document.getElementById('bookContent').style.fontSize = currentFontSize + 'px';
    document.getElementById('bookContent').style.lineHeight = currentLineHeight;
    document.getElementById('fontSizeVal').textContent = currentFontSize + 'px';

    // 主题
    document.body.classList.remove('theme-light', 'theme-mild', 'theme-dark');
    document.body.classList.add('theme-' + currentTheme);

    // 工具栏按钮状态
    document.querySelectorAll('.tool-btn').forEach(function(b) { b.classList.remove('tool-btn--active'); });
    var lhBtn = document.querySelector('[data-action="lineHeight"][data-value="' + currentLineHeight + '"]');
    var thBtn = document.querySelector('[data-action="theme"][data-value="' + currentTheme + '"]');
    if (lhBtn) lhBtn.classList.add('tool-btn--active');
    if (thBtn) thBtn.classList.add('tool-btn--active');

    savePreferences();
  }

  /* ---- 工具栏交互 ---- */
  function initToolbar() {
    document.querySelectorAll('.tool-btn').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var action = this.dataset.action;
        var value = this.dataset.value;

        if (action === 'fontSize') {
          currentFontSize = Math.max(14, Math.min(24, currentFontSize + parseInt(value)));
        } else if (action === 'lineHeight') {
          currentLineHeight = parseFloat(value);
        } else if (action === 'theme') {
          currentTheme = value;
        }

        applyPreferences();
      });
    });

    // 开始阅读按钮
    document.getElementById('btnStartRead').addEventListener('click', function() {
      document.getElementById('readerContent').scrollIntoView({ behavior: 'smooth' });
    });

    // 加入书架
    document.getElementById('btnBookmark').addEventListener('click', function() {
      var shelf = ZJ.getData('bookshelf', []);
      if (!shelf.find(function(b) { return b.id === currentBook.id; })) {
        shelf.push({ id: currentBook.id, title: currentBook.title, author: currentBook.author, addedAt: Date.now() });
        ZJ.saveData('bookshelf', shelf);
        var btn = document.getElementById('btnBookmark');
        btn.textContent = '✅ 已加入书架';
        btn.classList.add('btn--primary');
        btn.classList.remove('btn--outline');
        setTimeout(function() {
          btn.textContent = '🔖 加入书架';
          btn.classList.remove('btn--primary');
          btn.classList.add('btn--outline');
        }, 2000);
      }
    });
  }

  /* ---- 评论 ---- */
  function loadComments() {
    var list = document.getElementById('commentsList');
    if (!list || !currentBook) return;

    var comments = (window.COMMENTS_DATA || []).filter(function(c) { return c.bookId === currentBook.id; });

    // 合并 localStorage 中的评论
    var local = ZJ.getData('comments_' + currentBook.id, []);
    comments = comments.concat(local);

    if (comments.length === 0) {
      list.innerHTML = '<p style="color:var(--text-secondary);text-align:center;padding:40px;">暂无评论，来写第一条吧 🍃</p>';
      return;
    }

    list.innerHTML = comments.map(function(c) {
      return '<div class="comment-card">' +
        '<div class="comment-card__header">' +
          '<div class="comment-card__avatar">🧑</div>' +
          '<span class="comment-card__name">' + c.username + '</span>' +
          '<span class="comment-card__date">' + c.date + '</span>' +
        '</div>' +
        '<div class="comment-card__rating">' + '★'.repeat(c.rating) + '☆'.repeat(5 - c.rating) + '</div>' +
        '<div class="comment-card__content">' + c.content + '</div>' +
      '</div>';
    }).join('');
  }

  function initCommentForm() {
    var btnAdd = document.getElementById('btnAddComment');
    var form = document.getElementById('commentForm');
    var stars = document.querySelectorAll('#commentRating span');
    var btnSubmit = document.getElementById('btnSubmitComment');

    btnAdd.addEventListener('click', function() {
      form.style.display = form.style.display === 'none' ? 'block' : 'none';
    });

    // 星级选择
    stars.forEach(function(star) {
      star.addEventListener('click', function() {
        commentRating = parseInt(this.dataset.v);
        stars.forEach(function(s, i) {
          s.classList.toggle('active', i < commentRating);
        });
      });
      star.addEventListener('mouseenter', function() {
        var v = parseInt(this.dataset.v);
        stars.forEach(function(s, i) { s.style.color = i < v ? 'var(--apricot)' : ''; });
      });
    });
    document.getElementById('commentRating').addEventListener('mouseleave', function() {
      stars.forEach(function(s, i) { s.style.color = i < commentRating ? 'var(--apricot)' : ''; });
    });

    // 提交
    btnSubmit.addEventListener('click', function() {
      var text = document.getElementById('commentText').value.trim();
      if (!text) return alert('请输入评论内容');
      if (commentRating === 0) return alert('请给出星级评分');

      var comment = {
        id: Date.now(),
        bookId: currentBook.id,
        username: ZJ.getUser() ? ZJ.getUser().username : '匿名读者',
        rating: commentRating,
        content: text,
        date: new Date().toISOString().split('T')[0]
      };

      var local = ZJ.getData('comments_' + currentBook.id, []);
      local.unshift(comment);
      ZJ.saveData('comments_' + currentBook.id, local);

      document.getElementById('commentText').value = '';
      commentRating = 0;
      stars.forEach(function(s) { s.classList.remove('active'); });
      form.style.display = 'none';
      loadComments();
    });
  }

  /* ---- 初始化 ---- */
  document.addEventListener('DOMContentLoaded', function() {
    restorePreferences();
    loadBook();
    initToolbar();
    initCommentForm();
  });

})();
