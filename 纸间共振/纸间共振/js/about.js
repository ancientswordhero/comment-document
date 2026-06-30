/* ============================================
   纸间共振 — 关于我们脚本 (about.js)
   数字滚动计数器 · IntersectionObserver
   ============================================ */

(function() {
  'use strict';

  /* ---- 数字滚动计数器 ---- */
  function animateNumbers() {
    const nums = document.querySelectorAll('.milestone-card__num');
    if (!nums.length) return;

    let animated = false;

    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && !animated) {
          animated = true;
          nums.forEach(el => {
            const target = parseInt(el.dataset.target);
            const duration = 2000;
            const start = performance.now();

            function update(now) {
              const elapsed = now - start;
              const progress = Math.min(elapsed / duration, 1);
              const eased = 1 - Math.pow(1 - progress, 3); // ease-out
              el.textContent = Math.floor(eased * target).toLocaleString();

              if (progress < 1) {
                requestAnimationFrame(update);
              } else {
                el.textContent = target.toLocaleString();
              }
            }

            requestAnimationFrame(update);
          });
        }
      });
    }, { threshold: 0.5 });

    const grid = document.querySelector('.milestones-grid');
    if (grid) observer.observe(grid);
  }

  /* ---- 联系表单 ---- */
  function initContact() {
    const btn = document.getElementById('btnContactSend');
    const textarea = document.getElementById('contactMsg');

    if (!btn || !textarea) return;

    btn.addEventListener('click', () => {
      const msg = textarea.value.trim();
      if (!msg) return alert('请输入留言内容');

      const messages = ZJ.getData('contactMessages', []);
      messages.push({ text: msg, time: Date.now() });
      ZJ.saveData('contactMessages', messages);

      textarea.value = '';
      btn.textContent = '✓ 已发送';
      btn.style.background = 'var(--accent-hover)';
      setTimeout(() => {
        btn.textContent = '发 送';
        btn.style.background = '';
      }, 2000);
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    animateNumbers();
    initContact();
  });
})();
