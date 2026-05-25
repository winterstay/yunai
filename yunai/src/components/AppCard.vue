<script setup lang="ts">
import { computed } from 'vue'
import { RocketOutlined } from '@ant-design/icons-vue'
import { getAppPreviewUrl, formatRelativeTime, getAppTypeLabel } from '@/utils/app'

const props = defineProps<{
  app: API.AppVO
  compact?: boolean
}>()

const previewUrl = computed(() => getAppPreviewUrl(props.app))
const coverImage = computed(() => props.app.cover || previewUrl.value)
const createdText = computed(() => formatRelativeTime(props.app.createTime))
const typeLabel = computed(() => getAppTypeLabel(props.app.codeGenType))
</script>

<template>
  <router-link :to="`/app/chat/${app.id}`" class="app-card-link">
    <article class="app-card" :class="{ compact }">
      <div class="preview-shell">
        <img v-if="coverImage" :src="coverImage" :alt="app.appName" class="preview-image" />
        <div v-else class="preview-empty">
          <RocketOutlined />
          <span>等待生成预览</span>
        </div>
      </div>
      <div class="card-body">
        <div class="card-main">
          <h3 class="title">{{ app.appName || '未命名应用' }}</h3>
          <div class="meta">{{ createdText }}</div>
        </div>
        <div class="card-tags">
          <span class="type-tag">{{ typeLabel }}</span>
          <span v-if="app.priority === 99" class="featured-tag">精选</span>
        </div>
      </div>
    </article>
  </router-link>
</template>

<style scoped>
.app-card-link {
  display: block;
  color: inherit;
  text-decoration: none;
}

.app-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.preview-shell {
  width: 100%;
  aspect-ratio: 16 / 10;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: linear-gradient(180deg, #f8fafc 0%, #eef2ff 100%);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.preview-empty {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #64748b;
  font-size: 14px;
}

.card-body {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.card-main {
  min-width: 0;
}

.title {
  margin: 0 0 6px;
  font-size: 28px;
  line-height: 1.2;
  color: #111827;
  word-break: break-word;
}

.meta {
  color: #6b7280;
  font-size: 14px;
}

.card-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.type-tag,
.featured-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  white-space: nowrap;
}

.type-tag {
  background: #eff6ff;
  color: #1d4ed8;
}

.featured-tag {
  background: #fef3c7;
  color: #92400e;
}

.compact .title {
  font-size: 22px;
}

@media (max-width: 768px) {
  .title {
    font-size: 22px;
  }

  .card-body {
    flex-direction: column;
  }

  .card-tags {
    justify-content: flex-start;
  }
}
</style>
