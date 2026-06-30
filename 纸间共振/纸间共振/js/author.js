/* ============================================
   纸间共振 — 作者主页脚本 (author.js)
   动态加载 · IntersectionObserver 时间轴渐显
   ============================================ */

(function() {
  'use strict';

  var allBooks = window.BOOKS_DATA || [];
  var currentAuthor = '';

  function loadAuthor() {
    var params = new URLSearchParams(window.location.search);
    currentAuthor = params.get('author') || '鲁迅';

    var authorBooks = allBooks.filter(function(b) { return b.author === currentAuthor; });
    if (authorBooks.length === 0) {
      document.getElementById('authorName').textContent = currentAuthor;
      document.getElementById('authorStats').innerHTML = '暂无作品信息';
      return;
    }

    var first = authorBooks[0];
    document.getElementById('authorName').textContent = currentAuthor;
    document.getElementById('authorYears').textContent = first.authorYears || '';

    // 作者头像
    var avatarEl = document.getElementById('authorAvatar');
    if (avatarEl && first.authorAvatar) {
      avatarEl.innerHTML = '<img src="' + first.authorAvatar + '" alt="' + currentAuthor + '" style="width:100%;height:100%;object-fit:cover;border-radius:50%;" onerror="this.parentElement.innerHTML=\'✍️\'">';
    }

    // 作者简介
    var bio = first.authorBio || first.description || '';
    var quoteEl = document.getElementById('authorQuote');
    if (quoteEl) {
      quoteEl.textContent = bio.length > 60 ? '"' + bio.substring(0, 60) + '..."' : '"' + bio + '"';
    }

    var totalReads = authorBooks.reduce(function(s, b) { return s + (b.readCount || 0); }, 0);
    document.getElementById('authorStats').innerHTML = '已创作 <strong>' + authorBooks.length + '</strong> 部作品 · <strong>' + totalReads.toLocaleString() + '</strong> 位读者';
    document.title = currentAuthor + ' — 纸间共振';

    renderWorks(authorBooks);
    renderTimeline(authorBooks);
    renderAuthorTags(authorBooks);
  }

  function renderWorks(books) {
    var grid = document.getElementById('worksGrid');
    if (!grid) return;

    function render(bookList) {
      grid.innerHTML = bookList.map(function(b) {
        return '<a href="book.html?id=' + b.id + '" class="work-card">' +
          '<div class="work-card__cover"><img src="' + b.cover + '" alt="' + b.title + '" style="width:100%;height:100%;object-fit:cover;border-radius:4px;" onerror="this.parentElement.innerHTML=\'📖\'"></div>' +
          '<div class="work-card__title">' + b.title + '</div>' +
          '<div class="work-card__year">' + (b.year || '?') + '</div>' +
          '<div class="stars">' + '★'.repeat(Math.floor(b.rating || 0)) + '☆'.repeat(5 - Math.floor(b.rating || 0)) + '</div>' +
        '</a>';
      }).join('');
    }

    render(books);

    // 排序按钮事件委托
    var sortContainer = document.querySelector('.author-works__sort');
    if (sortContainer) {
      sortContainer.addEventListener('click', function(e) {
        var btn = e.target.closest('.sort-btn');
        if (!btn) return;
        e.preventDefault();
        document.querySelectorAll('.author-works__sort .sort-btn').forEach(function(b) { b.classList.remove('sort-btn--active'); });
        btn.classList.add('sort-btn--active');
        var sorted = books.slice();
        if (btn.dataset.sort === 'rating') {
          sorted.sort(function(a, b) { return (b.rating || 0) - (a.rating || 0); });
        } else {
          sorted.sort(function(a, b) { return (a.year || 0) - (b.year || 0); });
        }
        render(sorted);
      });
    }
  }

  function renderTimeline(books) {
    var tl = document.getElementById('timeline');
    if (!tl) return;

    var events = books
      .filter(function(b) { return b.year; })
      .sort(function(a, b) { return a.year - b.year; })
      .map(function(b) {
        return {
          year: b.year,
          event: '《' + b.title + '》' + (b.wordCount ? b.wordCount + '字' : '') + ' · 评分 ' + (b.rating || '?')
        };
      });

    if (events.length === 0) {
      tl.innerHTML = '<div class="timeline-empty">暂无时间线数据</div>';
      return;
    }

    var first = books[0];
    if (first.authorYears) {
      var parts = first.authorYears.split('-');
      var born = parseInt(parts[0]);
      if (!isNaN(born)) {
        var bornPlace = '';
        if (first.authorBio) {
          var match = first.authorBio.match(/（([^）]+)）/);
          if (match) bornPlace = match[1];
        }
        events.unshift({ year: born, event: '出生于' + bornPlace });
      }
    }
    var startYear = events[0].year - 5;
    events.unshift({ year: startYear, event: '开始文学创作之路' });

    tl.innerHTML = events.map(function(e) {
      return '<div class="timeline-item">' +
        '<div class="timeline-item__year">' + e.year + '</div>' +
        '<div class="timeline-item__event">' + e.event + '</div>' +
      '</div>';
    }).join('');

    var items = tl.querySelectorAll('.timeline-item');
    var observer = new IntersectionObserver(function(entries) {
      entries.forEach(function(entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add('timeline-item--visible');
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.2 });

    items.forEach(function(item) { observer.observe(item); });
  }

  function renderAuthorTags(books) {
    var cloud = document.getElementById('authorTags');
    if (!cloud) return;
    var tagSet = {};
    books.forEach(function(b) {
      (b.tags || []).forEach(function(t) { tagSet[t] = true; });
    });
    var tags = Object.keys(tagSet);
    var colors = ['#6baa7a', '#7ba8c8', '#d4b87a', '#b0a0c0', '#d49a9a'];
    cloud.innerHTML = tags.map(function(t, i) {
      return '<span class="tag" style="color:' + colors[i % colors.length] + ';">' + t + '</span>';
    }).join('');
  }

  document.addEventListener('DOMContentLoaded', loadAuthor);
})();
