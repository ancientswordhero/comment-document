(function() {
'use strict';

// ============================================================
// 图书数据 — 从 books.json 异步加载
// ============================================================
var LIBRARY = [];

// ============================================================
// Canvas 初始化
// ============================================================
var canvas = document.getElementById('scene');
var ctx = canvas.getContext('2d');
var W = 0, H = 0, mouseX = 0, mouseY = 0;
var time = 0, dt = 0, lastTime = 0, running = true;

function resize() {
  W = canvas.width = window.innerWidth || 1024;
  H = canvas.height = window.innerHeight || 768;
}
resize();

canvas.addEventListener('mousemove', function(e) { mouseX = e.clientX; mouseY = e.clientY; });
canvas.addEventListener('touchmove', function(e) {
  e.preventDefault();
  if (e.touches.length > 0) { mouseX = e.touches[0].clientX; mouseY = e.touches[0].clientY; }
}, { passive: false });
canvas.addEventListener('click', function(e) { handleClick(e.clientX, e.clientY); });
canvas.addEventListener('touchend', function(e) {
  var t = e.changedTouches[0];
  if (t) handleClick(t.clientX, t.clientY);
});

// ============================================================
// 场景数据
// ============================================================
var clouds = [];
for (var ci = 0; ci < 8; ci++) {
  clouds.push({ x: Math.random(), y: 0.15 + Math.random() * 0.4,
    w: 0.2 + Math.random() * 0.5, h: 0.03 + Math.random() * 0.06,
    speed: (Math.random() - 0.5) * 0.00012, opacity: 0.3 + Math.random() * 0.4 });
}

var mountains = [];
for (var layer = 0; layer < 4; layer++) {
  var pts = [], count = 50, amp = 0.04 + layer * 0.04, baseY = 0.28 + layer * 0.1, seed = layer * 2.7;
  for (var pi = 0; pi <= count; pi++) {
    var px = pi / count, py = baseY;
    for (var oct = 1; oct <= 5; oct++) py += Math.sin(px * Math.PI * oct * 2.1 + seed + oct * 0.7) * amp / oct;
    if (Math.abs(py - baseY) > amp * 0.6) py = baseY + (py - baseY) * 1.4;
    pts.push({ x: px, y: py });
  }
  mountains.push({ points: pts, opacity: [0.18, 0.32, 0.5, 0.68][layer], layer: layer });
}

var trees = [];
for (var ti = 0; ti < 15; ti++) {
  trees.push({ x: 0.02 + Math.random() * 0.96, y: 0.38 + Math.random() * 0.18,
    h: 20 + Math.random() * 50, w: 8 + Math.random() * 20 });
}

var grassBlades = [];
for (var gi = 0; gi < 350; gi++) {
  var gy = 0.48 + Math.random() * 0.5;
  grassBlades.push({ x: Math.random(), y: gy,
    h: 10 + Math.random() * 35 * (gy - 0.48) * 0.5,
    sway: Math.random() * Math.PI * 2, curve: (Math.random() - 0.5) * 0.3,
    width: 0.5 + Math.random() * 1.2, shade: 0.3 + Math.random() * 0.5 });
}

var birds = [];
function addBird() {
  birds.push({ x: -0.05, y: 0.08 + Math.random() * 0.22,
    vx: 0.0003 + Math.random() * 0.0006, vy: (Math.random() - 0.5) * 0.00006,
    size: 2.5 + Math.random() * 5, wingPhase: Math.random() * Math.PI * 2 });
}
for (var bi = 0; bi < 3; bi++) addBird();

// --- 墨点 ---
var inkSplatters = [];
function spawnInk(x, y, r, g, b) {
  var n = 3 + Math.floor(Math.random() * 5);
  for (var i = 0; i < n; i++) {
    var a = Math.random() * Math.PI * 2, d = 10 + Math.random() * 30;
    inkSplatters.push({ x:x, y:y, vx:Math.cos(a)*(0.8+Math.random()*2), vy:Math.sin(a)*(0.8+Math.random()*2)-1.5,
      life:1, decay:0.018+Math.random()*0.03, radius:1+Math.random()*2.5, r:r, g:g, b:b });
  }
}

// --- 植物 ---
var plants = [];
var PLANT_LIFESPAN = 45;
var MAX_PLANTS = 30;
var PLANT_COLORS = { plum:[184,58,58], bamboo:[74,90,56], pine:[58,72,68] };

function spawnPlant(x, y, type) {
  if (plants.length >= MAX_PLANTS) return; // 植物数量上限
  var types = ['plum','bamboo','pine'];
  var t = type || types[Math.floor(Math.random()*types.length)];
  var c = PLANT_COLORS[t] || [58,48,40];
  spawnInk(x, y, c[0], c[1], c[2]);
  plants.push({ x:x, y:y, type:t, age:0, maxAge:PLANT_LIFESPAN, sway:Math.random()*Math.PI*2,
    size:0.85+Math.random()*0.55, r:c[0], g:c[1], b:c[2] });
}

// --- 图书位置 ---
var discoveredBooks = {};
var bookPositions = [];
var bookGlowPhases = [];
var BOOK_SPOTS = [
  { x:0.18, y:0.58 }, { x:0.50, y:0.68 }, { x:0.75, y:0.55 }, { x:0.88, y:0.72 }, { x:0.35, y:0.80 }
];

// 由 books.json 加载完成后调用
function initBookPositions() {
  bookPositions = [];
  bookGlowPhases = [];
  LIBRARY.forEach(function(book, i) {
    if (i < BOOK_SPOTS.length) {
      bookPositions.push({ x: BOOK_SPOTS[i].x, y: BOOK_SPOTS[i].y, book: book, found: false });
      bookGlowPhases.push(Math.random() * Math.PI * 2);
    }
  });
}

// ============================================================
// UI 工具
// ============================================================
function rgba(r,g,b,a) { return 'rgba('+r+','+g+','+b+','+Math.max(0,Math.min(1,a))+')'; }

function updateBadge() {
  var found = Object.keys(discoveredBooks).length;
  document.getElementById('catalog-badge').textContent = found;
}

function showBookDetail(book) {
  document.getElementById('detail-emoji').textContent = book.emoji;
  document.getElementById('detail-title').textContent = '《' + book.title + '》';
  document.getElementById('detail-author').textContent = book.author;
  document.getElementById('detail-desc').textContent = book.desc;
  var q = book.quote || '';
  document.getElementById('detail-quote').textContent = q ? '「' + q + '」' : '';
  document.getElementById('detail-tags').innerHTML =
    book.tags.map(function(t){return'<span class="book-tag">'+t+'</span>';}).join('');
  document.getElementById('book-detail').classList.add('active');
}

function closeDetail() {
  document.getElementById('book-detail').classList.remove('active');
}

// ============================================================
// 目录面板
// ============================================================
function renderCatalog() {
  var list = document.getElementById('cat-list');
  var foundBooks = LIBRARY.filter(function(book) { return !!discoveredBooks[book.id]; });
  if (foundBooks.length === 0) {
    list.innerHTML = '<div style="padding:40px 20px;text-align:center;color:rgba(60,45,30,0.3);font-size:14px;letter-spacing:0.08em;">尚未寻得任何藏书<br><span style="font-size:12px;">在草地中寻觅微光闪烁之处</span></div>';
    return;
  }
  var html = '';
  foundBooks.forEach(function(book) {
    html += '<div class="cat-item" data-id="' + book.id + '">';
    html += '<div class="ci-top">';
    html += '<span class="ci-emoji">' + book.emoji + '</span>';
    html += '<div class="ci-info">';
    html += '<div class="ci-title">《' + book.title + '》</div>';
    html += '<div class="ci-author">' + book.author + '</div>';
    html += '</div></div>';
    html += '<div class="ci-desc">' + book.desc.substring(0, 80) + '……</div>';
    html += '<div class="ci-tags">' + book.tags.map(function(t){return'<span class="ci-tag">'+t+'</span>';}).join('') + '</div>';
    html += '<div class="ci-status found">✓ 已寻得 — 点击查看详情</div>';
    html += '</div>';
  });
  list.innerHTML = html;

  list.querySelectorAll('.cat-item').forEach(function(el) {
    el.addEventListener('click', function() {
      var id = parseInt(el.getAttribute('data-id'));
      showBookDetail(LIBRARY[id]);
      closeCatalog();
    });
  });
}

function openCatalog() {
  document.getElementById('catalog-panel').classList.add('open');
  document.getElementById('catalog-backdrop').classList.add('show');
  renderCatalog();
}

function closeCatalog() {
  document.getElementById('catalog-panel').classList.remove('open');
  document.getElementById('catalog-backdrop').classList.remove('show');
}

document.getElementById('catalog-btn').addEventListener('click', openCatalog);
document.getElementById('catalog-panel').querySelector('.cat-close').addEventListener('click', closeCatalog);
document.getElementById('catalog-backdrop').addEventListener('click', closeCatalog);

document.getElementById('detail-close-btn').addEventListener('click', function(e) {
  e.stopPropagation();
  closeDetail();
});
document.getElementById('book-detail').addEventListener('click', function(e) {
  if (e.target === this || e.target.classList.contains('backdrop')) closeDetail();
});
document.addEventListener('keydown', function(e) {
  if (e.key === 'Escape') { closeDetail(); closeCatalog(); }
});

// ============================================================
// 交互
// ============================================================
function handleClick(cx, cy) {
  var rx = cx / W, ry = cy / H;
  if (ry < 0.42 || ry > 0.93) return;

  var hitBook = null;
  for (var i = 0; i < bookPositions.length; i++) {
    var bp = bookPositions[i];
    var dx = cx - bp.x*W, dy = cy - bp.y*H;
    if (Math.sqrt(dx*dx+dy*dy) < (bp.found ? 50 : 75)) { hitBook = bp; break; }
  }

  if (hitBook) {
    if (!hitBook.found) {
      hitBook.found = true;
      discoveredBooks[hitBook.book.id] = true;
      var b = hitBook.book;
      spawnInk(hitBook.x*W, hitBook.y*H, b.color[0], b.color[1], b.color[2]);
      for (var j = 0; j < 5; j++) {
        (function(idx) {
          var a = (idx/5)*Math.PI*2, d = 30 + Math.random()*40;
          setTimeout(function() {
            spawnPlant(hitBook.x*W+Math.cos(a)*d, hitBook.y*H+Math.sin(a)*d,
              idx%3===0?'plum':idx%3===1?'bamboo':'pine');
          }, idx*80);
        })(j);
      }
      setTimeout(function() { showBookDetail(hitBook.book); }, 1000);
      updateBadge();
    } else {
      showBookDetail(hitBook.book);
    }
  } else {
    spawnInk(cx, cy, 58, 48, 40);
    spawnPlant(cx, cy, null);
    if (Math.random() < 0.4) {
      setTimeout(function() {
        spawnPlant(cx+(Math.random()-0.5)*80, cy+(Math.random()-0.5)*60, null);
      }, 200);
    }
  }
}

// ============================================================
// 植物绘制
// ============================================================

// --- 梅花 ---
// 枝干数据: [起点x, 起点y, 控制点x, 控制点y, 终点x, 终点y, 线宽系数, 最小ease]
// 主干用折线+曲线模拟苍劲虬曲，侧枝穿插交错
function drawPlum(size, ease) {
  var bloom = Math.min(Math.max((ease-0.28)/0.72, 0), 1);
  if (ease < 0.06) return;

  var s = size;
  var sway = Math.sin(time*0.0006 + size*3) * 1.5;

  // ── 枝段绘制辅助函数 ──
  function branch(x1, y1, cx, cy, x2, y2, lw, minEase, alphaBonus) {
    if (ease < minEase) return;
    var segEase = Math.min((ease - minEase) / (1 - minEase), 1);
    // 飞白效果: 用多条细线叠加
    var strokes = lw > 2.2 ? 3 : lw > 1.5 ? 2 : 1;
    var baseAlpha = (0.35 + (alphaBonus||0)) * segEase;
    for (var st = 0; st < strokes; st++) {
      var offsetX = st === 0 ? 0 : (st-1.5)*0.7*s;
      var offsetY = st === 0 ? 0 : (st-1.5)*0.4*s;
      ctx.beginPath();
      ctx.moveTo(x1+offsetX, y1+offsetY);
      ctx.quadraticCurveTo(cx+offsetX, cy+offsetY, x2+offsetX, y2+offsetY);
      ctx.strokeStyle = rgba(38, 28, 20, baseAlpha * (1 - st*0.25));
      ctx.lineWidth = lw * s * (1 - st*0.2);
      ctx.lineCap = 'round';
      ctx.stroke();
    }
  }

  // ── 主干: 自右下向左上斜出，三段折笔 ──
  // 第一段: 根部 → 向右上横出
  branch(0, 0, 6+sway, -10, 12+sway, -20, 3.8, 0.04, 0.2);
  // 第二段: 转折向左上
  branch(12+sway, -20, 8+sway, -32, -4+sway, -42, 3.0, 0.12, 0.18);
  // 第三段: 再向右上挺出
  branch(-4+sway, -42, -10+sway, -52, -2+sway, -62, 2.0, 0.20, 0.15);

  // ── 侧枝1 (从第一段中段分出，向左下回折) ──
  branch(6+sway, -12, -4, -18, -16+sway, -26, 2.4, 0.10, 0.15);
  // 侧枝1的次级小枝
  branch(-16+sway, -26, -20, -32, -26, -36, 1.4, 0.20, 0.1);
  branch(-10+sway*0.5, -20, -6, -28, -8, -38, 1.1, 0.25, 0.1);

  // ── 侧枝2 (从第一段末端分出，向右上发展) ──
  branch(12+sway, -18, 22, -24, 28, -34, 2.2, 0.14, 0.15);
  // 侧枝2次级
  branch(22, -28, 28, -32, 34, -38, 1.2, 0.24, 0.1);
  branch(18, -24, 14, -34, 16, -44, 1.0, 0.28, 0.1);

  // ── 侧枝3 (从第二段分出，向右横穿) ──
  branch(2+sway, -30, 12, -34, 20+sway*0.5, -40, 1.8, 0.16, 0.12);
  branch(12, -36, 18, -42, 22, -50, 1.0, 0.26, 0.1);

  // ── 侧枝4 (从第三段分出，向左探出) ──
  branch(-8+sway, -50, -18, -54, -24, -58, 1.5, 0.22, 0.12);
  branch(-18, -54, -24, -58, -28, -64, 0.9, 0.30, 0.1);

  // ── 枯梢棘枝 (短小锐角折线) ──
  var deadTwigs = [
    [0, -4, -7, -8, -10, -6, 0.8, 0.08],
    [14, -16, 20, -18, 22, -14, 0.7, 0.18],
    [-6, -30, -12, -28, -16, -22, 0.7, 0.22],
    [-14, -20, -22, -18, -26, -14, 0.9, 0.15],
    [8, -38, 14, -36, 18, -32, 0.6, 0.30],
    [-20, -52, -26, -48, -30, -42, 0.6, 0.32],
    [26, -30, 30, -26, 34, -22, 0.5, 0.34],
  ];
  deadTwigs.forEach(function(dt) {
    if (ease < dt[7]) return;
    var de = Math.min((ease - dt[7]) / (1 - dt[7]), 1);
    ctx.beginPath();
    ctx.moveTo(dt[0], dt[1]);
    ctx.lineTo(dt[2], dt[3]);
    ctx.lineTo(dt[4], dt[5]);
    ctx.strokeStyle = rgba(40, 30, 20, 0.45 * de);
    ctx.lineWidth = dt[6] * s;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.stroke();
  });

  // ── 树干皴擦纹理 ──
  if (ease > 0.2) {
    var barkLines = [
      [2, -8, 7, -10], [8, -14, 12, -16], [6, -24, 0, -26],
      [-2, -28, -8, -30], [0, -36, -6, -38], [-6, -44, -10, -46],
      [-10, -52, -14, -54], [4, -20, 10, -22], [18, -24, 22, -26],
      [-10, -18, -14, -20], [-12, -22, -16, -24],
    ];
    barkLines.forEach(function(bl) {
      var be = Math.min(Math.max((ease-0.2)/0.4, 0), 1);
      if (Math.random() < 0.3) return; // 疏密有致
      ctx.beginPath();
      ctx.moveTo(bl[0], bl[1]);
      ctx.lineTo(bl[2], bl[3]);
      ctx.strokeStyle = rgba(35, 25, 18, 0.2 * be);
      ctx.lineWidth = 0.5 * s;
      ctx.stroke();
      // 偶尔加一点小墨点模拟苔点
      if (Math.random() < 0.3) {
        ctx.beginPath();
        ctx.arc(bl[2], bl[3], 0.8 * s, 0, Math.PI*2);
        ctx.fillStyle = rgba(35, 25, 18, 0.18 * be);
        ctx.fill();
      }
    });
  }

  // ── 节疤 (枝干分叉处略微膨大) ──
  var knots = [
    [6+sway, -12, 2.2], [12+sway, -20, 2.8], [-4+sway, -42, 2.2],
    [-16+sway, -26, 1.8], [2+sway, -30, 1.6], [-8+sway, -50, 1.4],
  ];
  knots.forEach(function(k) {
    if (ease < 0.1) return;
    var ke = Math.min((ease-0.1)/0.3, 1);
    ctx.beginPath();
    ctx.arc(k[0], k[1], k[2]*s*ke, 0, Math.PI*2);
    ctx.fillStyle = rgba(35, 25, 18, 0.25*ke);
    ctx.fill();
  });

  // ── 花朵 ──
  if (bloom <= 0) return;

  // 花簇位置 — 沿枝干散布，聚散有致
  var flowerClusters = [
    {x: 2+sway, y: -28, n: 2, minB: 0.0},
    {x: -10+sway, y: -38, n: 2, minB: 0.05},
    {x: -20, y: -34, n: 1, minB: 0.08},
    {x: 18, y: -24, n: 2, minB: 0.1},
    {x: -8, y: -52, n: 1, minB: 0.15},
    {x: 10, y: -40, n: 2, minB: 0.08},
    {x: -24, y: -56, n: 1, minB: 0.2},
    {x: -18, y: -50, n: 1, minB: 0.18},
  ];

  flowerClusters.forEach(function(cl) {
    var cb = Math.min(Math.max((bloom - cl.minB) / 0.55, 0), 1);
    if (cb <= 0) return;
    for (var fi = 0; fi < cl.n; fi++) {
      // 花朵在簇心周围微散
      var ox = cl.x + (fi - cl.n/2 + 0.5) * 2.2 * s;
      var oy = cl.y + (fi - cl.n/2 + 0.5) * 1.8 * s + Math.sin(fi*2.3)*1.0*s;
      var fb = Math.min(Math.max((cb - fi*0.08) / 0.9, 0), 1);
      if (fb <= 0) continue;

      drawPlumFlower(ox, oy, fb, s, fi);
    }
  });

  // ── 花苞 (沿枝梢零星点缀) ──
  var budSpots = [
    [16, -14], [-22, -22], [6, -22], [-8, -14],
    [-14, -30], [-22, -50], [-4, -34],
  ];
  budSpots.forEach(function(bd) {
    var bb = Math.min(Math.max((bloom - 0.06) / 0.6, 0), 1);
    if (bb <= 0.01) return;
    // 花苞: 小椭圆，深红色
    ctx.save();
    ctx.translate(bd[0], bd[1]);
    ctx.rotate(Math.sin(bd[0]*bd[1])*0.5);
    ctx.beginPath();
    ctx.ellipse(0, 0, 0.6*s*bb, 0.9*s*bb, 0, 0, Math.PI*2);
    ctx.fillStyle = rgba(175, 45, 45, 0.55*bb);
    ctx.fill();
    // 萼片
    ctx.beginPath();
    ctx.moveTo(0, -0.9*s*bb);
    ctx.lineTo(-0.6*s*bb, -1.4*s*bb);
    ctx.lineTo(0.6*s*bb, -1.4*s*bb);
    ctx.closePath();
    ctx.fillStyle = rgba(90, 55, 40, 0.5*bb);
    ctx.fill();
    ctx.restore();
  });
}

// 单朵梅花: 五瓣 + 花蕊 + 花托
function drawPlumFlower(ox, oy, bloom, s, seed) {
  // 花瓣 — 每瓣略有大小和方向变化
  var petalCount = 5;
  for (var pi = 0; pi < petalCount; pi++) {
    var angle = (pi/petalCount)*Math.PI*2 + seed*0.35;
    // 花瓣椭圆: 长轴放射方向
    var petalR = 0.8 * s * bloom;
    var petalLen = petalR * 1.3;
    var petalWid = petalR * 0.55;
    var cx = ox + Math.cos(angle) * petalLen * 0.45;
    var cy = oy + Math.sin(angle) * petalLen * 0.45;

    ctx.save();
    ctx.translate(cx, cy);
    ctx.rotate(angle);
    // 用椭圆模拟花瓣，略微不规则
    ctx.beginPath();
    ctx.moveTo(0, 0);
    ctx.bezierCurveTo(
      petalWid*0.6, -petalLen*0.65,
      petalWid*0.3, -petalLen*0.95,
      0, -petalLen
    );
    ctx.bezierCurveTo(
      -petalWid*0.3, -petalLen*0.95,
      -petalWid*0.6, -petalLen*0.65,
      0, 0
    );
    // 墨梅: 淡墨勾边 + 略施粉红
    ctx.fillStyle = rgba(200, 60, 55, 0.55 * bloom);
    ctx.fill();
    ctx.strokeStyle = rgba(160, 40, 35, 0.35 * bloom);
    ctx.lineWidth = 0.4 * s;
    ctx.stroke();
    ctx.restore();
  }

  // 花蕊
  var stamenCount = 6 + Math.floor(seed*3);
  for (var si = 0; si < stamenCount; si++) {
    var sa = (si/stamenCount)*Math.PI*2 + seed*0.8;
    var sl = 0.55 * s * bloom * (0.6 + Math.random()*0.4);
    ctx.beginPath();
    ctx.moveTo(ox, oy);
    ctx.lineTo(ox + Math.cos(sa)*sl, oy + Math.sin(sa)*sl);
    ctx.strokeStyle = rgba(180, 160, 100, 0.5 * bloom);
    ctx.lineWidth = 0.3 * s;
    ctx.stroke();
    // 蕊头小点
    ctx.beginPath();
    ctx.arc(ox + Math.cos(sa)*sl, oy + Math.sin(sa)*sl, 0.2*s*bloom, 0, Math.PI*2);
    ctx.fillStyle = rgba(220, 190, 90, 0.7 * bloom);
    ctx.fill();
  }
  // 花心
  ctx.beginPath();
  ctx.arc(ox, oy, 0.55*s*bloom, 0, Math.PI*2);
  ctx.fillStyle = rgba(200, 170, 70, 0.6 * bloom);
  ctx.fill();
}

// --- 竹 ---
function drawBamboo(size, ease) {
  var h = 65*size*ease, segs = 4, segH = h/segs;

  var mainSway = 3*Math.sin(time*0.0005);
  for (var s = 0; s < segs; s++) {
    if (s/segs > ease) break;
    var sy = -(s*segH);
    var sg = Math.min(Math.max((ease-s/segs)*segs, 0), 1);
    var sx = mainSway * (s/segs);

    ctx.beginPath();
    ctx.moveTo(sx, sy+3);
    ctx.lineTo(sx+mainSway*0.1, sy+3-segH*sg);
    ctx.strokeStyle = rgba(68,82,52, 0.45+sg*0.35);
    ctx.lineWidth = 3.2*size;
    ctx.lineCap = 'round';
    ctx.stroke();

    if (s > 0 && sg > 0.5) {
      ctx.beginPath();
      var nx = sx;
      ctx.moveTo(nx-4.5*size, sy+3);
      ctx.lineTo(nx+4.5*size, sy+3);
      ctx.strokeStyle = rgba(48,62,38, 0.55);
      ctx.lineWidth = 1.6*size;
      ctx.stroke();
      ctx.beginPath();
      ctx.moveTo(nx-4.5*size, sy+1);
      ctx.lineTo(nx-4.5*size, sy+5);
      ctx.strokeStyle = rgba(48,62,38, 0.3);
      ctx.lineWidth = 1.1*size;
      ctx.stroke();
      ctx.beginPath();
      ctx.moveTo(nx+4.5*size, sy+1);
      ctx.lineTo(nx+4.5*size, sy+5);
      ctx.stroke();
    }
  }

  if (ease > 0.5) {
    var lb = Math.min((ease-0.5)/0.5, 1);
    for (var si = 1; si < segs; si++) {
      if (si/segs > ease) break;
      var ny = -(si*segH) + 3;
      var nx = mainSway*(si/segs);

      for (var li = 0; li < 4; li++) {
        var la = -1.2 + li*0.7;
        var ll = (14+li*3)*size*lb;
        var lx = nx + Math.cos(la)*ll*0.3;
        var ly = ny + Math.sin(la)*ll*0.3;
        var tipX = nx + Math.cos(la)*ll;
        var tipY = ny + Math.sin(la)*ll;

        ctx.beginPath();
        ctx.moveTo(nx, ny);
        ctx.quadraticCurveTo(lx, ly, tipX, tipY);
        ctx.quadraticCurveTo(nx+Math.cos(la+0.3)*ll*0.6, ny+Math.sin(la+0.3)*ll*0.6, nx, ny);
        ctx.fillStyle = rgba(55,75,40, 0.35*lb);
        ctx.fill();
      }
    }

    var topY = -h;
    var topX = mainSway;
    for (var ti = 0; ti < 3; ti++) {
      var ta = -0.6 + ti*0.6;
      var tl = 20*size*lb;
      ctx.beginPath();
      ctx.moveTo(topX, topY);
      ctx.quadraticCurveTo(topX+Math.cos(ta)*tl*0.4, topY+Math.sin(ta)*tl*0.4,
                           topX+Math.cos(ta)*tl, topY+Math.sin(ta)*tl);
      ctx.quadraticCurveTo(topX+Math.cos(ta+0.25)*tl*0.5, topY+Math.sin(ta+0.25)*tl*0.5, topX, topY);
      ctx.fillStyle = rgba(50,70,38, 0.4*lb);
      ctx.fill();
    }
  }
}

// --- 松 ---
function drawPine(size, ease) {
  var h = 70*size*ease;

  ctx.beginPath();
  ctx.moveTo(0, 0);
  ctx.quadraticCurveTo(-6*size, -h*0.35, -8*size, -h*0.7);
  ctx.quadraticCurveTo(-9*size, -h*0.85, -8*size, -h);
  ctx.strokeStyle = rgba(52,45,35, 0.55);
  ctx.lineWidth = 4.5*size;
  ctx.lineCap = 'round';
  ctx.stroke();

  if (ease > 0.3) {
    for (var bi = 0; bi < 8; bi++) {
      var by = -h*(0.15+bi*0.1)*ease;
      var bx = -4*size + Math.sin(bi*2.1)*3*size;
      ctx.beginPath();
      ctx.moveTo(bx-3*size, by);
      ctx.lineTo(bx+4*size, by+1);
      ctx.strokeStyle = rgba(45,38,28, 0.25);
      ctx.lineWidth = 0.7*size;
      ctx.stroke();
    }
  }

  var branches = [
    { y:-h*0.25, dx:10, dy:-8, len:22, curve:0.5 },
    { y:-h*0.42, dx:-12, dy:-6, len:18, curve:-0.4 },
    { y:-h*0.55, dx:14, dy:-5, len:20, curve:0.6 },
    { y:-h*0.68, dx:-10, dy:-4, len:16, curve:-0.3 },
    { y:-h*0.78, dx:8, dy:-3, len:14, curve:0.4 },
    { y:-h*0.85, dx:-6, dy:-2, len:10, curve:-0.5 },
  ];

  branches.forEach(function(br, bi) {
    if (ease < 0.15+bi*0.08) return;
    var bbx = -8*size*(br.y/-h);
    var bby = br.y;
    ctx.beginPath();
    ctx.moveTo(bbx, bby);
    ctx.quadraticCurveTo(
      bbx+br.dx*size*0.3, bby+br.dy*size*0.5,
      bbx+br.dx*size*br.curve, bby+br.dy*size
    );
    ctx.strokeStyle = rgba(50,42,32, 0.45);
    ctx.lineWidth = (2.2-bi*0.18)*size;
    ctx.lineCap = 'round';
    ctx.stroke();
  });

  if (ease > 0.35) {
    var lb = Math.min((ease-0.35)/0.65, 1);
    var clusters = [
      { x:14*size, y:-h*0.32, r:13 }, { x:-19*size, y:-h*0.49, r:11 },
      { x:18*size, y:-h*0.6, r:12 }, { x:-14*size, y:-h*0.74, r:10 },
      { x:12*size, y:-h*0.82, r:10 }, { x:-8*size, y:-h*0.89, r:8 },
      { x:4*size, y:-h*0.92, r:9 }, { x:-3*size, y:-h*0.94, r:7 },
    ];

    clusters.forEach(function(cl) {
      if (lb < 0.1) return;
      var clb = Math.min(lb*1.3, 1);
      var cg = ctx.createRadialGradient(cl.x, cl.y, 0, cl.x, cl.y, cl.r*size*clb);
      cg.addColorStop(0, rgba(40,55,42, 0.4*clb));
      cg.addColorStop(0.6, rgba(48,62,48, 0.25*clb));
      cg.addColorStop(1, 'rgba(0,0,0,0)');
      ctx.beginPath();
      ctx.arc(cl.x, cl.y, cl.r*size*clb, 0, Math.PI*2);
      ctx.fillStyle = cg;
      ctx.fill();

      var needleCount = 10;
      for (var ni = 0; ni < needleCount; ni++) {
        var na = (ni/needleCount)*Math.PI*2 + bi*0.4;
        var nl = cl.r*size*clb*1.1;
        ctx.beginPath();
        ctx.moveTo(cl.x, cl.y);
        ctx.lineTo(cl.x+Math.cos(na)*nl, cl.y+Math.sin(na)*nl);
        ctx.strokeStyle = rgba(40,55,40, 0.3*clb);
        ctx.lineWidth = 0.6*size;
        ctx.stroke();
      }
    });
  }
}

function drawPlant(p) {
  var gp = Math.min(p.age/2, 1);
  var ease = 1 - Math.pow(1-gp, 3);
  ctx.save();
  ctx.translate(p.x, p.y);
  ctx.rotate(Math.sin(time*0.001+p.sway)*0.025);
  switch (p.type) {
    case 'plum': drawPlum(p.size, ease); break;
    case 'bamboo': drawBamboo(p.size, ease); break;
    case 'pine': drawPine(p.size, ease); break;
  }
  ctx.restore();
}

// ============================================================
// 场景渲染
// ============================================================
function drawPaperBackground() {
  ctx.fillStyle = '#f5efe0';
  ctx.fillRect(0, 0, W, H);
  var dotCount = Math.min(800, Math.floor(W*H/2500));
  for (var i = 0; i < dotCount; i++) {
    var fx = (i*7919+127)%W, fy = (i*6271+359)%H;
    ctx.fillStyle = (i%2===0) ? rgba(180,170,150,0.01+(i%3)*0.01) : rgba(200,190,170,0.01+(i%3)*0.01);
    ctx.fillRect(fx, fy, (i%3)+0.5, ((i%2)+0.3));
  }
  var ew = W*0.03;
  var el = ctx.createLinearGradient(0,0,ew,0);
  el.addColorStop(0, rgba(200,185,160,0.2)); el.addColorStop(1,'rgba(0,0,0,0)');
  ctx.fillStyle = el; ctx.fillRect(0,0,ew,H);
  var er = ctx.createLinearGradient(W,0,W-ew,0);
  er.addColorStop(0, rgba(200,185,160,0.2)); er.addColorStop(1,'rgba(0,0,0,0)');
  ctx.fillStyle = er; ctx.fillRect(W-ew,0,ew,H);
}

function drawSky() {
  var sg = ctx.createLinearGradient(0,0,0,H*0.35);
  sg.addColorStop(0, rgba(240,230,210,0.3)); sg.addColorStop(0.5, rgba(245,238,220,0.1)); sg.addColorStop(1,'rgba(0,0,0,0)');
  ctx.fillStyle = sg; ctx.fillRect(0,0,W,H*0.35);
  var sx=W*0.82, sy=H*0.1, sr=Math.min(W,H)*0.04;
  var sgl=ctx.createRadialGradient(sx,sy,sr*0.2,sx,sy,sr*2.5);
  sgl.addColorStop(0,rgba(200,100,80,0.25)); sgl.addColorStop(0.4,rgba(200,100,80,0.08)); sgl.addColorStop(1,'rgba(0,0,0,0)');
  ctx.fillStyle=sgl; ctx.fillRect(sx-sr*3,sy-sr*3,sr*6,sr*6);
  ctx.beginPath(); ctx.arc(sx,sy,sr,0,Math.PI*2); ctx.fillStyle=rgba(195,110,85,0.55); ctx.fill();
}

function drawClouds() {
  for (var i=0;i<clouds.length;i++) {
    var c=clouds[i]; c.x+=c.speed;
    if(c.x>1.3)c.x=-0.3; if(c.x<-0.3)c.x=1.3;
    var cx=c.x*W, cy=c.y*H, cw=c.w*W, ch=c.h*H;
    for (var j=0;j<4;j++) {
      ctx.beginPath();
      ctx.ellipse(cx+(j-1.5)*cw*0.25, cy+Math.sin(j*1.8+time*0.0003)*ch*0.5,
        cw*(0.2+0.15*Math.sin(j*1.3)), ch*(0.6+0.4*Math.sin(j*0.9)), 0,0,Math.PI*2);
      ctx.fillStyle=rgba(235,225,210,c.opacity*0.6); ctx.fill();
    }
  }
}

function drawMountains() {
  for (var idx=0;idx<mountains.length;idx++) {
    var m=mountains[idx], pts=m.points, bottomY=H*0.6+idx*H*0.04, gray=60-idx*12;
    ctx.beginPath(); ctx.moveTo(pts[0].x*W,pts[0].y*H);
    for(var i=1;i<pts.length;i++) ctx.lineTo(pts[i].x*W,pts[i].y*H+Math.sin(time*0.0002+idx*0.8)*3);
    ctx.lineTo(W,bottomY); ctx.lineTo(0,bottomY); ctx.closePath();
    var gy=pts.reduce(function(s,p){return s+p.y;},0)/pts.length*H;
    var g=ctx.createLinearGradient(0,gy-30,0,bottomY);
    g.addColorStop(0,rgba(gray,gray-3,gray-5,m.opacity*0.9));
    g.addColorStop(0.3,rgba(gray+10,gray+7,gray+4,m.opacity*0.7));
    g.addColorStop(0.7,rgba(gray+25,gray+22,gray+18,m.opacity*0.3));
    g.addColorStop(1,'rgba(0,0,0,0)');
    ctx.fillStyle=g; ctx.fill();
    ctx.beginPath(); ctx.moveTo(pts[0].x*W,pts[0].y*H);
    for(var j=1;j<pts.length;j++) ctx.lineTo(pts[j].x*W,pts[j].y*H+Math.sin(time*0.0002+idx*0.8)*3);
    ctx.strokeStyle=rgba(50-idx*8,48-idx*8,43-idx*8,m.opacity*1.2); ctx.lineWidth=1.2+idx*0.3; ctx.stroke();
  }
}

function drawTrees() {
  for(var ti=0;ti<trees.length;ti++) {
    var t=trees[ti], tx=t.x*W, ty=t.y*H, th=t.h*0.45;
    ctx.beginPath(); ctx.moveTo(tx,ty);
    ctx.quadraticCurveTo(tx+3,ty-th*0.5,tx+5+Math.sin(t.x*10)*4,ty-th);
    ctx.strokeStyle=rgba(60,55,45,0.55); ctx.lineWidth=2; ctx.lineCap='round'; ctx.stroke();
    var cb=ty-th;
    for(var i=0;i<12;i++){
      var cx=tx+(i-5.5)*t.w*0.12, cy=cb-Math.abs(i-5.5)*t.h*0.04+Math.sin(i*1.3)*5, cr=t.w*0.22*(1-Math.abs(i-5.5)*0.06);
      ctx.beginPath(); ctx.arc(cx,cy,cr,0,Math.PI*2);
      ctx.fillStyle=rgba(55+i*3,50+i*3,42+i*3,0.2+0.3*(1-Math.abs(i-5.5)*0.1)); ctx.fill();
    }
  }
}

function drawGrass() {
  for(var i=0;i<grassBlades.length;i++){
    var g=grassBlades[i], gx=g.x*W, gy=g.y*H;
    if(gx<-20||gx>W+20)continue;
    var sway=Math.sin(time*0.001+g.sway)*4;
    var tipX=gx+sway+g.curve*g.h*0.4, tipY=gy-g.h, midX=gx+sway*0.4, midY=gy-g.h*0.45;
    ctx.beginPath(); ctx.moveTo(gx,gy); ctx.quadraticCurveTo(midX,midY,tipX,tipY);
    var gray=Math.floor(40+g.shade*40);
    ctx.strokeStyle=rgba(gray,gray-3,gray-6,0.25+g.shade*0.35); ctx.lineWidth=g.width; ctx.lineCap='round'; ctx.stroke();
  }
}

function drawInkSplatters() {
  for(var i=0;i<inkSplatters.length;i++){
    var s=inkSplatters[i]; if(s.life<=0)continue;
    s.x+=s.vx;s.y+=s.vy;s.vy+=0.03;s.life-=s.decay;
    ctx.beginPath(); ctx.arc(s.x,s.y,s.radius*Math.max(0,s.life),0,Math.PI*2);
    ctx.fillStyle=rgba(s.r,s.g,s.b,s.life*0.7); ctx.fill();
  }
}

function drawBookMarkers() {
  for(var i=0;i<bookPositions.length;i++){
    var bp=bookPositions[i], bx=bp.x*W, by=bp.y*H;
    bookGlowPhases[i]+=0.018; var pulse=0.5+0.5*Math.sin(bookGlowPhases[i]);
    if(!bp.found){
      var alpha=0.15+pulse*0.2;
      var glow=ctx.createRadialGradient(bx,by,3,bx,by,28+pulse*10);
      glow.addColorStop(0,rgba(bp.book.color[0],bp.book.color[1],bp.book.color[2],alpha));
      glow.addColorStop(1,'rgba(0,0,0,0)');
      ctx.fillStyle=glow; ctx.fillRect(bx-35,by-35,70,70);
      ctx.beginPath(); ctx.arc(bx,by,2+pulse*1.2,0,Math.PI*2); ctx.fillStyle=rgba(120,100,80,alpha+0.15); ctx.fill();
    }else{
      ctx.save(); ctx.translate(bx,by-8);
      var bw=18,bh=13;
      ctx.fillStyle='#f0e8d8'; ctx.fillRect(-bw/2,-bh,bw,bh);
      ctx.strokeStyle=rgba(100,80,60,0.5); ctx.lineWidth=0.8; ctx.strokeRect(-bw/2,-bh,bw,bh);
      ctx.beginPath(); ctx.moveTo(-bw/2+3,-bh); ctx.lineTo(-bw/2+3,0);
      ctx.strokeStyle=rgba(80,60,40,0.4); ctx.stroke();
      ctx.fillStyle=rgba(60,45,30,0.6); ctx.font='9px "KaiTi","STKaiti",serif'; ctx.fillText(bp.book.title.charAt(0),-bw/2+6,-bh+10);
      ctx.beginPath(); ctx.moveTo(-bw/2+6,bh); ctx.lineTo(-bw/2+2,bh+10);
      ctx.strokeStyle=rgba(184,58,58,0.4); ctx.lineWidth=1.2; ctx.stroke();
      ctx.restore();
    }
  }
}

function drawBirds() {
  if(birds.length<5&&Math.random()<0.005)addBird();
  for(var i=0;i<birds.length;i++){
    var b=birds[i]; b.x+=b.vx; b.y+=b.vy+Math.sin(time*0.002+i)*0.00005; b.wingPhase+=0.05;
    if(b.x>1.1||b.y<0.02||b.y>0.4){b.x=-0.05;b.y=0.08+Math.random()*0.22;}
    var bx=b.x*W, by=b.y*H, wf=Math.sin(b.wingPhase)*b.size*0.8;
    ctx.beginPath(); ctx.moveTo(bx,by);
    ctx.quadraticCurveTo(bx-b.size,by-wf,bx-b.size*1.8,by-wf*0.5);
    ctx.strokeStyle=rgba(50,45,38,0.45); ctx.lineWidth=0.8; ctx.stroke();
    ctx.beginPath(); ctx.moveTo(bx,by);
    ctx.quadraticCurveTo(bx+b.size,by-wf,bx+b.size*1.8,by-wf*0.5); ctx.stroke();
  }
  while(birds.length>8)birds.shift();
}

function getPX(){var v=((mouseX/W)-0.5)*10; return isNaN(v)?0:v;}
function getPY(){var v=((mouseY/H)-0.5)*5; return isNaN(v)?0:v;}

// ============================================================
// 主循环
// ============================================================
function animate(timestamp) {
  if(!running)return;
  if(!lastTime)lastTime=timestamp;
  dt=Math.min((timestamp-lastTime)/1000,0.1); lastTime=timestamp; time=timestamp;
  try{
    ctx.clearRect(0,0,W,H);
    var px=getPX(), py=getPY();
    if(isNaN(px))px=0; if(isNaN(py))py=0;

    drawPaperBackground();

    ctx.save(); ctx.translate(px*0.1,py*0.1); drawSky(); ctx.restore();
    drawBirds();
    ctx.save(); ctx.translate(px*0.15,0); drawClouds(); ctx.restore();
    ctx.save(); ctx.translate(px*0.3,py*0.1); drawMountains(); ctx.restore();
    ctx.save(); ctx.translate(px*0.5,py*0.1); drawTrees(); ctx.restore();
    ctx.save(); ctx.translate(px*0.7,py*0.05); drawGrass(); ctx.restore();

    drawInkSplatters();
    drawBookMarkers();

    ctx.save(); ctx.translate(px*1.1,py*0.15);
    for(var pi=0;pi<plants.length;pi++)drawPlant(plants[pi]);
    ctx.restore();

    for(var i=plants.length-1;i>=0;i--){plants[i].age+=dt; if(plants[i].age>plants[i].maxAge)plants.splice(i,1);}
    for(var j=inkSplatters.length-1;j>=0;j--){if(inkSplatters[j].life<=0)inkSplatters.splice(j,1);}
  }catch(e){}
  requestAnimationFrame(animate);
}

// ============================================================
// 从 books.js 中读取图书数据并初始化
// ============================================================
LIBRARY = (typeof BOOKS_RAW !== 'undefined') ? BOOKS_RAW : [];
initBookPositions();
updateBadge();
renderCatalog();

// 启动动画
requestAnimationFrame(animate);

var resizeTimer=null;
window.addEventListener('resize',function(){resize();clearTimeout(resizeTimer);resizeTimer=setTimeout(function(){},300);});

})();
