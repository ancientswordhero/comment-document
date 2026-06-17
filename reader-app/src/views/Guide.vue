<template>
  <div class="guide-page">
    <BannerHeader />
    <div class="guide-container">
      <h1 class="guide-title">凡例</h1>

      <section class="guide-section">
        <h2>使用指南</h2>

        <h3>开始阅读</h3>
        <p>在首页浏览或搜索感兴趣的书籍，点击书籍卡片进入详情页。在详情页点击"开始阅读"进入阅读器，你可以通过顶部工具栏调整字号、切换章节、翻页阅读。</p>

        <h3>记书余</h3>
        <p>阅读过程中，选中一段文字会弹出操作栏。点击"记书余"，在弹出的浮层中写下你的心得或疑问，选择类型标签后点击"留墨"即可保存。你的手记默认保存在思余中，仅自己可见。</p>

        <h3>思余与余音</h3>
        <p><strong>思余</strong>是你的私密手记空间，所有未公开的手记保存在这里，你可以随时查看、编辑或删除。当你觉得某条手记值得分享时，点击"送入余音"即可将其公开发布。</p>
        <p><strong>余音</strong>是公开手记广场，展示所有用户投递的手记。你可以浏览他人的读书心得，进行回复讨论和点赞。</p>

        <h3>写书评</h3>
        <p>在书籍详情页底部，你可以发表书评或回复他人的书评。书评支持点赞功能。</p>

        <h3>书架与收件箱</h3>
        <p>点击"我的书架"查看已收藏的书籍。点击"收件箱"查看回复和通知消息。</p>
      </section>

      <section class="guide-section">
        <h2>社区规范</h2>

        <h3>书评准则</h3>
        <ul>
          <li>尊重原著和作者，发表有实质内容的书评</li>
          <li>禁止人身攻击、辱骂或歧视性言论</li>
          <li>禁止发布广告、垃圾信息或与书籍无关的内容</li>
        </ul>

        <h3>手记发布要求</h3>
        <ul>
          <li>手记应为原创内容，与引用原文相关</li>
          <li>禁止抄袭他人手记或书评</li>
          <li>手记内容应围绕阅读讨论，不偏离主题</li>
          <li>禁止在手记中发布违法或违规内容</li>
        </ul>

        <h3>举报规则</h3>
        <p>如果你发现书评或手记存在以下问题，可以使用举报功能：</p>
        <ul>
          <li>垃圾广告</li>
          <li>人身攻击</li>
          <li>虚假信息</li>
          <li>违规内容</li>
          <li>其他（需附补充说明）</li>
        </ul>
        <p>管理团队会在收到举报后及时审核处理。请合理使用举报功能，恶意举报将受到限制。</p>

        <h3>账号规范</h3>
        <ul>
          <li>每位用户只能注册一个账号</li>
          <li>禁止冒充他人或使用不当用户名</li>
          <li>违反社区规范可能导致账号被限制或注销</li>
        </ul>
      </section>
    </div>
  </div>
</template>

<script setup>
import BannerHeader from '../components/BannerHeader.vue'
</script>

<style scoped>
.guide-page {
  min-height: 100vh;
  background: var(--color-bg, #fafaf7);
}

.guide-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 24px 64px;
}

.guide-title {
  font-family: var(--font-serif);
  font-size: 28px;
  color: var(--color-text, #4a3d2f);
  text-align: center;
  margin-bottom: 40px;
  letter-spacing: 4px;
}

.guide-section {
  margin-bottom: 40px;
}

.guide-section h2 {
  font-size: 18px;
  color: var(--color-primary, #c9a96e);
  border-bottom: 1px solid var(--color-border, #e8e4dc);
  padding-bottom: 8px;
  margin-bottom: 20px;
}

.guide-section h3 {
  font-size: 15px;
  color: var(--color-text, #4a3d2f);
  margin: 20px 0 8px;
}

.guide-section p {
  font-size: 14px;
  line-height: 1.8;
  color: var(--color-text-secondary, #8b8070);
  margin-bottom: 12px;
}

.guide-section ul {
  font-size: 14px;
  line-height: 1.8;
  color: var(--color-text-secondary, #8b8070);
  padding-left: 20px;
  margin-bottom: 12px;
}

.guide-section li {
  margin-bottom: 4px;
}
</style>
