<template>
  <div class="carousel-wrapper">
    <div class="carousel-main">
      <div class="carousel-slide">
        <div class="carousel-text">
          <h2>翰 墨 书 香</h2>
          <p>万卷古今消永日，一窗昏晓送流年</p>
        </div>
        <div class="carousel-dots">
          <span class="dot active"></span>
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </div>
      </div>
      <button class="carousel-btn prev">◂</button>
      <button class="carousel-btn next">▸</button>
    </div>
    <div class="carousel-side">
      <div class="side-card">推荐位 1</div>
      <div class="side-card">推荐位 2</div>
    </div>
  </div>
</template>

<style scoped>
.carousel-wrapper { display: flex; gap: 16px; padding: 16px 36px; }
.carousel-main {
  flex: 1; height: 220px; position: relative;
  background: linear-gradient(135deg, #f5f1ea, #ede6d8, #e8e0d0);
  border: 1px solid var(--border); border-radius: 2px;
  display: flex; align-items: center; justify-content: center;
}
.carousel-text { text-align: center; }
.carousel-text h2 {
  font-size: 26px; color: var(--text); font-weight: 600;
  font-family: var(--font-serif); letter-spacing: 6px;
}
.carousel-text p { font-size: 12px; color: var(--text-muted); margin-top: 6px; }
.carousel-dots { display: flex; gap: 6px; justify-content: center; margin-top: 14px; }
.dot { width: 6px; height: 6px; border-radius: 50%; background: #d8d0c0; }
.dot.active { background: var(--accent); }
.carousel-btn {
  position: absolute; top: 50%; transform: translateY(-50%);
  background: none; border: none; font-size: 18px; color: #c0b8a8; cursor: pointer;
}
.carousel-btn.prev { left: 10px; }
.carousel-btn.next { right: 10px; }
.carousel-side { width: 130px; display: flex; flex-direction: column; gap: 10px; }
.side-card {
  flex: 1; background: #f5f1ea; border: 1px solid var(--border);
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; color: var(--text-muted);
}
</style>
