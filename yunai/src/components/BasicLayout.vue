<script setup lang="ts">
import { Layout } from 'ant-design-vue'
import { RouterView } from 'vue-router'
import GlobalHeader from './GlobalHeader.vue'
import GlobalFooter from './GlobalFooter.vue'
</script>

<template>
  <Layout class="basic-layout">
    <Layout.Header class="layout-header">
      <GlobalHeader />
    </Layout.Header>
    
    <Layout.Content class="layout-content">
      <RouterView />
    </Layout.Content>
    
    <Layout.Footer class="layout-footer">
      <GlobalFooter />
    </Layout.Footer>
  </Layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-header {
  background: #fff;
  padding: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.layout-content {
  flex: 1;
  padding: 24px;
  background: #f5f5f5;
  min-height: calc(100vh - 64px - 70px);
}

.layout-footer {
  background: #fff;
  padding: 24px 50px;
  text-align: center;
  border-top: 1px solid #e8e8e8;
}

@media (max-width: 768px) {
  .layout-content {
    padding: 16px;
  }
  
  .layout-footer {
    padding: 16px;
  }
}
</style>
