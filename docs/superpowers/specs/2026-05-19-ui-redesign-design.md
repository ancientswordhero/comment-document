# UI 重设计规范

## 目标

统一三个前端应用（reader-app、admin-app、login-app）的视觉风格，提升协调性和美观度。在现有暖色书卷风基础上做深度精炼，并引入 Bilibili 风格 Header Banner。

## 设计原则

- **暖色书卷风**：米色背景、金色点缀、温暖质感，契合图书馆/阅读场景
- **统一设计令牌**：三个 app 共享同一套 CSS 变量，消除当前不一致
- **克制圆角**：统一 `--radius: 4px`，小型元素用 `--radius-sm: 2px`
- **素材驱动**：使用 `C:\Users\36295\Desktop\dt\` 中的插画素材丰富视觉层次

---

## 1. 共享设计令牌

三个 app 各自在 `src/assets/design-tokens.css` 放置一份内容相同的副本（独立 Vite 项目，不引入跨项目依赖）。

### 颜色

```css
--color-bg:            #fafaf7   /* 页面背景 */
--color-card-bg:       #fff      /* 卡片/表格背景 */
--color-accent-light:  #f0ebe0   /* 浅金色背景 */
--color-primary:       #c9a96e   /* 主色 - 金色 */
--color-primary-hover: #b8944d   /* hover 深金色 */
--color-text:          #4a3d2f   /* 主文字 - 深棕 */
--color-text-secondary:#8b8070   /* 次要文字 */
--color-text-muted:    #a09880   /* 辅助文字 */
--color-border:        #e8e4dc   /* 边框 */
--color-card-border:   #ece8df   /* 卡片边框 */
--color-danger:        #c04040   /* 危险/错误 */
--color-success:       #5b8c5a   /* 成功 */
--color-warning:       #c08840   /* 警告 */
```

### 圆角

```css
--radius-sm: 2px   /* 极小元素 */
--radius:     4px   /* 默认 */
--radius-lg:  8px   /* Logo/特殊元素 */
```

### 间距

```
--space-xs:  4px
--space-sm:  8px
--space-md:  12px
--space-lg:  16px
--space-xl:  24px
--space-2xl: 32px
```

### 阴影

```css
--shadow-sm:  0 1px 3px rgba(74,61,47,0.06)   /* 卡片默认 */
--shadow-md:  0 4px 16px rgba(74,61,47,0.1)    /* hover/浮层 */
--shadow-lg:  0 8px 40px rgba(74,61,47,0.15)   /* 模态/登录卡 */
```

### 字体层级

| 用途 | 大小 | 字重 |
|------|------|------|
| 辅助/标签 | 11px | 400 |
| 正文 | 13px | 400 |
| 小标题 | 15px | 500 |
| 页面标题 | 18px | 600 |
| 大标题 | 24-28px | 600-700 |
| Banner 标题 | 26-28px | 700 |

---

## 2. 读者端改造

### 2.1 Header Banner（替换 AppHeader + BookCarousel）

- 通栏 Banner 作为页面顶部，高度约 280-320px
- 背景使用 dt 素材中的插画（File0001.jpg / File0007.jpg），叠加半透明渐变过渡到底部内容区
- 左侧放置 Q 版角色装饰（File0002.png 风格）
- 右侧放置大型角色立绘（File0011.png / File0012.png 风格）
- 中央：Logo + "云图书馆" 标题（26-28px，字间距 4px）
- 搜索框居中置于标题下方：白色背景、圆角 24px（保持 pill 形状）、阴影 `--shadow-md`，宽度 ~480px
- 搜索框下方为标语文字（12px，辅助色）
- 半透明导航栏叠加在 Banner 上方（`backdrop-filter: blur(8px)`），包含首页/分类浏览等链接 + 管理后台入口

### 2.2 分类导航

保持不变。

### 2.3 图书卡片

- 封面区域扩大（aspect-ratio 保持 2:3），背景渐变微调
- 卡片圆角 `--radius`（4px）
- 默认状态：`box-shadow: var(--shadow-sm)`
- Hover：`box-shadow: var(--shadow-md)` + `translateY(-2px)` + 边框变色
- 新增分类标签胶囊（可选，显示图书所属分类）
- 卡片内边距略增，标题字号提升至 13px

### 2.4 分页

- 按钮圆角统一为 4px
- 与整体风格一致，间距微调

### 2.5 图书详情页

- 封面容器微调圆角
- 元数据区域间距优化
- 返回按钮样式与其他按钮统一

---

## 3. 管理后台改造

### 3.1 Header

- 圆角统一 4px
- Logo 图标 `border-radius: 4px`
- 导航链接样式改为胶囊 hover 态（`border-radius: 4px`，hover 时金色背景）
- "退出"按钮红色文字

### 3.2 图书表格

- 表格外层包裹圆角容器（`border-radius: 4px; border: 1px solid var(--color-card-border)`）
- 表头背景 `--color-bg`
- 行分隔线使用 `--color-accent-light`
- 状态标签改为圆角胶囊（4px），上架绿/下架橙
- 操作列分隔符从 `|` 改为 `·` 居中点
- 工具栏控件（搜索框、下拉框、按钮）统一 4px 圆角

### 3.3 表单页

- 表单卡片化：白色背景 + 边框 + 4px 圆角
- 输入框统一 4px 圆角，focus 时边框色 `--color-primary`
- 按钮统一样式：主按钮金色背景白色文字，次按钮白色背景边框
- 上传区域圆角 4px，虚线边框

---

## 4. 登录页改造

### 4.1 背景

- 全屏背景使用 dt 素材（File0001.jpg / File0007.jpg 大图）
- 图片上方叠加半透明渐变遮罩
- 左右放置角色装饰元素

### 4.2 卡片

- 半透明毛玻璃效果：`background: rgba(255,255,255,0.92); backdrop-filter: blur(12px)`
- 圆角 4px
- 阴影 `--shadow-lg`
- 四角可选装饰框线（File0008.png 风格）

### 4.3 Tab 切换

- 保持底部边框指示器样式
- Active 态金色下划线 + 金色文字
- 输入框 focus 时边框变金色
- 提交按钮圆角 4px

---

## 5. 素材文件规划

| 素材 | 用途 | 位置 |
|------|------|------|
| File0001.jpg / File0007.jpg | Banner/登录页背景大图 | Banner 背景层、登录页全屏背景 |
| File0002.png | Q版角色装饰 | Banner 左侧、登录页左侧 |
| File0011.png / File0012.png | 大型角色立绘 | Banner 右侧、登录页右侧 |
| File0008.png | UI装饰框 | 登录卡四角装饰 |
| File0003-0006, 0009-0010, 0014-0015 | 小图标/徽章 | 功能按钮装饰（可选） |

素材文件复制到各 app 的 `src/assets/` 目录下使用。

---

## 6. 实施顺序

1. 创建共享 `design-tokens.css`，三个 app 同步引用
2. 读者端：Header Banner 重构（最高优先级，视觉冲击力最强）
3. 读者端：图书卡片、分页、详情页打磨
4. 管理后台：表格、工具栏、表单、Header 统一
5. 登录页：全屏背景 + 毛玻璃卡片
6. 整体走查：检查跨 app 一致性、移动端响应式

---

## 7. 不变项

- 分类导航组件保持现状
- API 接口不变
- 路由结构不变
- 功能逻辑不变
