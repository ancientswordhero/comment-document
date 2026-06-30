/* ============================================
   纸间共振 — 登录/注册脚本 (login.js)
   Canvas 竹叶粒子 · 水墨晕染 · 3D 翻转卡片
   ============================================ */

(function() {
  'use strict';

  /* ==========================================
     1. Canvas 竹叶粒子系统
     ========================================== */
  const canvas = document.getElementById('particleCanvas');
  const ctx = canvas.getContext('2d');
  let particles = [];
  let mouseX = -100, mouseY = -100;
  let inkSpots = [];

  function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  }
  window.addEventListener('resize', resizeCanvas);
  resizeCanvas();

  /* 粒子类 */
  class Particle {
    constructor() {
      this.reset();
      this.y = Math.random() * canvas.height;
    }
    reset() {
      this.x = Math.random() * canvas.width;
      this.y = -20;
      this.size = Math.random() * 4 + 2;
      this.speedY = Math.random() * 0.6 + 0.3;
      this.speedX = Math.random() * 0.4 - 0.2;
      this.opacity = Math.random() * 0.4 + 0.1;
      this.rotation = Math.random() * Math.PI * 2;
      this.rotSpeed = Math.random() * 0.02 - 0.01;
      this.hue = Math.random() < 0.5 ? '#6baa7a' : '#7ba8c8';
    }
    update() {
      this.y += this.speedY;
      this.x += this.speedX;
      this.rotation += this.rotSpeed;

      // 鼠标排斥
      const dx = this.x - mouseX;
      const dy = this.y - mouseY;
      const dist = Math.sqrt(dx * dx + dy * dy);
      if (dist < 120) {
        this.x += (dx / dist) * 1.5;
        this.y += (dy / dist) * 1.5;
      }

      if (this.y > canvas.height + 20) { this.reset(); this.y = -20; }
      if (this.x < -20) this.x = canvas.width + 20;
      if (this.x > canvas.width + 20) this.x = -20;
    }
    draw() {
      ctx.save();
      ctx.translate(this.x, this.y);
      ctx.rotate(this.rotation);
      ctx.globalAlpha = this.opacity;
      ctx.fillStyle = this.hue;
      // 竹叶形状
      ctx.beginPath();
      ctx.ellipse(0, 0, this.size, this.size * 0.35, 0, 0, Math.PI * 2);
      ctx.fill();
      // 叶脉
      ctx.strokeStyle = this.hue;
      ctx.lineWidth = 0.3;
      ctx.globalAlpha = this.opacity * 0.5;
      ctx.beginPath();
      ctx.moveTo(-this.size, 0);
      ctx.lineTo(this.size, 0);
      ctx.stroke();
      ctx.restore();
    }
  }

  /* 水墨晕染点 */
  class InkSpot {
    constructor(x, y) {
      this.x = x; this.y = y;
      this.radius = 0;
      this.maxRadius = 40 + Math.random() * 30;
      this.opacity = 0.08;
      this.fading = false;
    }
    update() {
      if (!this.fading) {
        this.radius += 1.5;
        if (this.radius >= this.maxRadius) this.fading = true;
      } else {
        this.opacity -= 0.0004;
        this.radius += 0.5;
      }
      return this.opacity > 0;
    }
    draw() {
      const gradient = ctx.createRadialGradient(this.x, this.y, 0, this.x, this.y, this.radius);
      gradient.addColorStop(0, `rgba(107,170,122,${this.opacity})`);
      gradient.addColorStop(0.5, `rgba(107,170,122,${this.opacity * 0.5})`);
      gradient.addColorStop(1, 'rgba(107,170,122,0)');
      ctx.fillStyle = gradient;
      ctx.beginPath();
      ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
      ctx.fill();
    }
  }

  /* 初始化粒子 */
  function initParticles(count) {
    particles = [];
    for (let i = 0; i < count; i++) {
      particles.push(new Particle());
    }
  }
  initParticles(60);

  /* 动画循环 */
  function animate() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    particles.forEach(p => { p.update(); p.draw(); });

    inkSpots = inkSpots.filter(s => {
      const alive = s.update();
      if (alive) s.draw();
      return alive;
    });

    requestAnimationFrame(animate);
  }
  animate();

  /* 鼠标事件 — 水墨晕染 + 粒子排斥 */
  document.addEventListener('mousemove', function(e) {
    mouseX = e.clientX;
    mouseY = e.clientY;
    // 每移动一定距离产生水墨点
    if (Math.random() < 0.3) {
      inkSpots.push(new InkSpot(e.clientX, e.clientY));
    }
  });


  

  /* ==========================================
     2. 3D 翻转卡片
     ========================================== */
  const loginCard = document.getElementById('loginCard');
  let isFlipped = false;

  function flipCard() {
    isFlipped = !isFlipped;
    if (isFlipped) {
      loginCard.classList.add('login-card--flipped');
    } else {
      loginCard.classList.remove('login-card--flipped');
    }
  }

  document.getElementById('switchToRegister').addEventListener('click', function(e) {
    e.preventDefault();
    flipCard();
  });

  document.getElementById('switchToLogin').addEventListener('click', function(e) {
    e.preventDefault();
    flipCard();
  });

  /* ==========================================
     3. 密码显示切换
     ========================================== */
  document.getElementById('togglePass').addEventListener('click', function() {
    const pass = document.getElementById('loginPass');
    const isPass = pass.type === 'password';
    pass.type = isPass ? 'text' : 'password';
    this.textContent = isPass ? '🙈' : '👁️';
  });

  /* ==========================================
     4. 表单提交
     ========================================== */
  document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const username = document.getElementById('loginUser').value.trim();
    const password = document.getElementById('loginPass').value.trim();
    if (!username || !password) return alert('请填写完整信息');

    // 模拟登录
    const user = {
      username: username,
      avatar: '../assets/images/avatars/default1.jpg',
      joinedAt: Date.now()
    };
    ZJ.login(user);

    if (document.getElementById('rememberMe').checked) {
      localStorage.setItem('zhiJian_rememberedUser', username);
    }

    alert(`欢迎回来，${username}！`);
    window.location.href = 'index.html';
  });

  document.getElementById('registerForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const username = document.getElementById('regUser').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const pass = document.getElementById('regPass').value.trim();
    const confirm = document.getElementById('regPassConfirm').value.trim();

    if (!username || !email || !pass) return alert('请填写完整信息');
    if (pass !== confirm) return alert('两次密码不一致');
    if (pass.length < 4) return alert('密码至少4位');

    const user = {
      username: username,
      email: email,
      avatar: '../assets/images/avatars/default1.jpg',
      joinedAt: Date.now()
    };
    ZJ.login(user);
    alert(`注册成功！欢迎加入纸间共振，${username}！`);
    window.location.href = 'index.html';
  });

  /* 记住的用户名填充 */
  const remembered = localStorage.getItem('zhiJian_rememberedUser');
  if (remembered) {
    document.getElementById('loginUser').value = remembered;
    document.getElementById('rememberMe').checked = true;
  }

})();
