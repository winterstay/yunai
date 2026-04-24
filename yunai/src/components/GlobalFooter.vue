<script setup lang="ts">
</script>

<template>
  <div class="global-footer">
    <p class="copyright-text">为个人独立练习项目</p>
  </div>
</template>

<style scoped>
.global-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
}

.copyright-text {
  margin: 0;
  font-size: 14px;
  color: #666;
  text-align: center;
}

@media (max-width: 768px) {
  .copyright-text {
    font-size: 12px;
  }
}
</style>
