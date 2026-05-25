<script setup lang="ts">
import { Empty, Pagination, Spin } from 'ant-design-vue'
import AppCard from './AppCard.vue'

defineProps<{
  title: string
  apps: API.AppVO[]
  loading?: boolean
  current: number
  pageSize: number
  total: number
}>()

const emit = defineEmits<{
  change: [page: number, pageSize: number]
}>()
</script>

<template>
  <section class="app-section">
    <div class="section-header">
      <h2>{{ title }}</h2>
    </div>
    <Spin :spinning="!!loading">
      <div v-if="apps.length" class="grid-list">
        <AppCard v-for="item in apps" :key="item.id" :app="item" />
      </div>
      <Empty v-else description="暂无应用" class="section-empty" />
    </Spin>
    <div class="section-pagination">
      <Pagination
        :current="current"
        :page-size="pageSize"
        :total="total"
        :show-size-changer="false"
        @change="(page, size) => emit('change', page, size)"
      />
    </div>
  </section>
</template>

<style scoped>
.app-section {
  padding: 28px 0 8px;
}

.section-header {
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  font-size: 22px;
  color: #111827;
}

.grid-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 28px 24px;
}

.section-empty {
  padding: 36px 0;
}

.section-pagination {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

@media (max-width: 1200px) {
  .grid-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .grid-list {
    grid-template-columns: minmax(0, 1fr);
    gap: 24px;
  }
}
</style>
