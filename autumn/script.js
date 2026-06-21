// 各层视差配置
const initConfig = [
  {
    aspect: 1,
    blur: 4,
    x: 0,
    y: 0,
    blurEffect: (blur, p) => blur + p * blur,
    parallaxX: (x) => x,
  },
  {
    aspect: 0.6,
    blur: 0,
    x: 0,
    y: 0,
    blurEffect: (blur, p) => Math.abs(p * 10),
    parallaxX: (x, p) => x - p * 10,
  },
  {
    aspect: 1,
    blur: 1,
    x: -50,
    y: 0,
    blurEffect: (blur, p) => Math.abs(blur - p * 4),
    parallaxX: (x, p) => x - p * 30,
  },
  {
    aspect: 0.6,
    blur: 4,
    x: 0,
    y: 4.2,
    blurEffect: (blur, p) => Math.abs(blur - p * 8),
    parallaxX: (x, p) => x - p * 45,
  },
  {
    aspect: 0.6,
    blur: 5,
    x: 0,
    y: -1.8,
    blurEffect: (blur, p) => Math.abs(blur - p * 8),
    parallaxX: (x, p) => x - p * 95,
  },
  {
    aspect: 0.65,
    blur: 6,
    x: 0,
    y: 0,
    blurEffect: (blur, p) => Math.abs(blur - p * 4),
    parallaxX: (x, p) => x - p * 118,
  },
];

const breakpoint = 1658;
let endpoint = { width: 0, x: 0 };

// DOM 元素
const banner = document.getElementById("animateBanner");
const girlLayer = document.getElementById("girlLayer");
const girlImg = girlLayer.querySelector("img");

// 图片眨眼效果
const eyeOpen = "images/girl-eye-open.png";
const eyeNapping = "images/girl-eye-napping.png";
const eyeClosed = "images/girl-eye-closed.png";

function sleep(time) {
  return new Promise((resolve) => setTimeout(resolve, time));
}

async function makeBlink() {
  if (!girlImg) return;
  await sleep(50);
  girlImg.src = eyeNapping;
  await sleep(50);
  girlImg.src = eyeClosed;
  await sleep(350);
  girlImg.src = eyeOpen;
  setTimeout(makeBlink, 5000);
}

// 生成 transform 样式
function movementTemplate(blur, x, y) {
  return `filter: blur(${blur}px); transform: translate(${x}px, ${y}px) translateZ(0);`;
}

// 获取初始样式
function getInitStyle(key) {
  const cfg = initConfig[key];
  return movementTemplate(cfg.blur, cfg.x, cfg.y);
}

// 初始化各层尺寸
function initRect(key, img, bannerWidth) {
  const cfg = initConfig[key];
  const originWidth = parseInt(img.dataset.width, 10);
  const originHeight = parseInt(img.dataset.height, 10);

  let width, height;

  if (bannerWidth < breakpoint) {
    width = cfg.aspect * originWidth;
    height = cfg.aspect * originHeight;
  } else {
    const extra = Math.floor((bannerWidth - breakpoint) / 10);
    width = cfg.aspect * originWidth + extra * 12;
    height = cfg.aspect * originHeight + extra * 1;
  }

  img.width = width;
  img.height = height;
  img.style = getInitStyle(key);
}

// 应用视差效果
function makeEffect(img, key, parallax) {
  const cfg = initConfig[key];
  const blur = cfg.blurEffect(cfg.blur, parallax);
  const x = cfg.parallaxX(cfg.x, parallax);
  img.style = movementTemplate(blur, x, cfg.y);
}

// 重置效果
function resetEffect() {
  endpoint = { width: 0, x: 0 };
  const layers = banner.querySelectorAll(".layer");
  layers.forEach((layer, key) => {
    const img = layer.querySelector("img");
    img.style = `transition-duration: 0.2s; ${getInitStyle(key)}`;
  });
}

// 初始化 Banner
function initBanner(parallax = null) {
  const bannerRect = banner.getBoundingClientRect();
  const layers = banner.querySelectorAll(".layer");

  layers.forEach((layer, key) => {
    const img = layer.querySelector("img");
    initRect(key, img, bannerRect.width);

    if (parallax !== null) {
      makeEffect(img, key, parallax);
    }
  });
}

// 事件处理
banner.addEventListener("mouseenter", (e) => {
  const { width } = banner.getBoundingClientRect();
  endpoint.x = e.clientX;
  endpoint.width = width;
});

banner.addEventListener("mousemove", (e) => {
  if (endpoint.width === 0) return;
  const parallax = e.clientX - endpoint.x;
  const parallaxRatio = parallax / endpoint.width;
  initBanner(parallaxRatio);
});

banner.addEventListener("mouseleave", () => {
  resetEffect();
});

// 响应式
window.addEventListener("resize", () => {
  initBanner();
});

// 启动
initBanner();
makeBlink();
