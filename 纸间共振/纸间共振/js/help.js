/* ============================================
   纸间共振 — 帮助/FAQ 脚本 (help.js)
   手风琴折叠 · 关键词搜索过滤
   ============================================ */

(function() {
  'use strict';

  /* ---- 手风琴折叠 ---- */
  function initAccordion() {
    const items = document.querySelectorAll('.faq-item');

    items.forEach(item => {
      const btn = item.querySelector('.faq-question');
      btn.addEventListener('click', () => {
        const wasActive = item.classList.contains('faq-item--active');

        // 关闭所有
        items.forEach(i => i.classList.remove('faq-item--active'));

        // 如果之前未展开，则展开
        if (!wasActive) {
          item.classList.add('faq-item--active');
        }
      });
    });
  }

  /* ---- 关键词搜索过滤 ---- */
  function initSearch() {
    const input = document.getElementById('faqSearch');
    if (!input) return;

    input.addEventListener('input', function() {
      const kw = this.value.trim().toLowerCase();
      const items = document.querySelectorAll('.faq-item');
      let hasVisible = false;

      items.forEach(item => {
        const text = item.textContent.toLowerCase();
        if (!kw || text.includes(kw)) {
          item.style.display = '';
          hasVisible = true;
        } else {
          item.style.display = 'none';
        }
      });

      // 无结果提示
      const list = document.getElementById('faqList');
      let noResult = list.querySelector('.faq-no-result');

      if (!hasVisible && kw) {
        if (!noResult) {
          noResult = document.createElement('div');
          noResult.className = 'faq-no-result';
          noResult.textContent = '🍃 没有找到相关问题，试试其他关键词？';
          list.appendChild(noResult);
        }
      } else if (noResult) {
        noResult.remove();
      }
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    initAccordion();
    initSearch();
  });
})();
