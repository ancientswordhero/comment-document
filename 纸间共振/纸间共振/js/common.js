/* ============================================
   纸间共振 — 公共脚本 (common.js)
   导航栏、页脚注入 · 吸顶 · 状态管理
   ============================================ */

(function() {
  'use strict';

  /* ---- 页面标识 ---- */
  const pageMap = {
    'index':   { name: '首页',     href: 'index.html' },
    'browse':  { name: '浏览书库', href: 'browse.html' },
    'book':    { name: '阅读',     href: 'book.html' },
    'author':  { name: '作者',     href: 'author.html' },
    'creator': { name: '创作',     href: 'creator.html' },
    'login':   { name: '登录',     href: 'login.html' },
    'profile': { name: '个人中心', href: 'profile.html' },
    'about':   { name: '关于我们', href: 'about.html' },
    'help':    { name: '帮助',     href: 'help.html' }
  };

  /* 当前页面标识（从 body data-page 读取） */
  const currentPage = document.body.dataset.page || 'index';

  /* ---- 检查登录状态 ---- */
  function isLoggedIn() {
    return localStorage.getItem('zhiJian_user') !== null;
  }

  function getUser() {
    const data = localStorage.getItem('zhiJian_user');
    return data ? JSON.parse(data) : null;
  }

  /* ---- 注入导航栏 ---- */
  function injectNav() {
    const nav = document.createElement('nav');
    nav.className = 'nav';
    nav.id = 'mainNav';

    const publicLinks = ['login','index', 'browse', 'creator', 'about', 'help'];
    const linksHTML = publicLinks.map(key => {
      const p = pageMap[key];
      const activeClass = (currentPage === key) ? ' nav__link--active' : '';
      return `<a href="${p.href}" class="nav__link${activeClass}">${p.name}</a>`;
    }).join('\n          ');

    const user = getUser();
    const actionsHTML = user
      ? `<a href="profile.html" class="nav__avatar-link">
           <img src="${user.avatar || '../assets/images/avatars/default1.jpg'}" alt="头像" class="nav__avatar" title="${user.username}">
         </a>`
      : `<a href="login.html" class="nav__btn-login">登 录</a>`;

    nav.innerHTML = `
        <a href="index.html" class="nav__logo">
          <svg class="nav__logo-icon" viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18 4C12 4 6 10 6 18s6 14 12 14c2 0 4-1 6-2l6 4-2-6c1-2 2-4 2-6 0-8-6-14-12-14z"
                  stroke="currentColor" stroke-width="1.5" fill="none" opacity="0.7"/>
            <circle cx="14" cy="16" r="1.5" fill="currentColor" opacity="0.5"/>
            <circle cx="22" cy="16" r="1.5" fill="currentColor" opacity="0.5"/>
            <path d="M14 22c2 2 6 2 8 0" stroke="currentColor" stroke-width="1" fill="none" opacity="0.5"/>
          </svg>
          纸间共振
        </a>
        <div class="nav__links">
          ${linksHTML}
        </div>
        <div class="nav__actions">
          ${actionsHTML}
        </div>
      `;

    document.body.prepend(nav);
  }

  /* ---- 注入页脚 ---- */
  function injectFooter() {
    const footer = document.createElement('footer');
    footer.className = 'footer';
    footer.innerHTML = `
        <div class="footer__brand">纸间共振</div>
        <p>在文字之间，遇见共鸣</p>
        <p style="margin-top:8px;">&copy; 2024 纸间共振 · 为爱阅读的人留一盏灯</p>
      `;
    document.body.appendChild(footer);
  }

  /* ---- 导航吸顶效果 ---- */
  function initStickyNav() {
    const nav = document.getElementById('mainNav');
    if (!nav) return;

    function onScroll() {
      if (window.scrollY > 80) {
        nav.classList.add('nav--scrolled');
      } else {
        nav.classList.remove('nav--scrolled');
      }
    }

    window.addEventListener('scroll', onScroll, { passive: true });
    onScroll(); // 初始化时检查
  }

  /* ---- 页面主体顶部间距 ---- */
  function adjustBodyPadding() {
    // 为固定导航留出空间
    document.body.style.paddingTop = 'var(--nav-height)';
  }

  /* ---- localStorage 工具 ---- */
  window.ZJ = {
    isLoggedIn,
    getUser,

    login(user) {
      localStorage.setItem('zhiJian_user', JSON.stringify(user));
      localStorage.setItem('zhiJian_loginTime', Date.now());
    },

    logout() {
      localStorage.removeItem('zhiJian_user');
      localStorage.removeItem('zhiJian_loginTime');
      window.location.href = 'index.html';
    },

    saveData(key, data) {
      localStorage.setItem('zhiJian_' + key, JSON.stringify(data));
    },

    getData(key, fallback) {
      const item = localStorage.getItem('zhiJian_' + key);
      return item ? JSON.parse(item) : fallback;
    },

    getBooks() {
      // 同步读取预置数据（由页面自行 fetch）
      return null;
    }
  };

  /* ---- 初始化 ---- */
  document.addEventListener('DOMContentLoaded', () => {
    injectNav();
    injectFooter();
    initStickyNav();
    adjustBodyPadding();
  });

})();
