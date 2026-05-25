<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Button, Card, Form, Input, InputNumber, Spin, message } from 'ant-design-vue'
import { adminGetAppById, getAppById, editApp, updateAppByAdmin } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { isAdmin } from '@/utils/app'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const loading = ref(false)
const saving = ref(false)
const formState = reactive<API.AppAdminUpdateRequest>({
  id: String(route.params.id),
  appName: '',
  cover: '',
  priority: 0,
})

const adminMode = computed(() => isAdmin(loginUserStore.loginUser))

const loadApp = async () => {
  loading.value = true
  try {
    const res = adminMode.value
      ? await adminGetAppById({ id: String(route.params.id) })
      : await getAppById({ id: String(route.params.id) })
    if (res.data.code === 0 && res.data.data) {
      Object.assign(formState, res.data.data)
    } else {
      message.error(res.data.message || '获取应用失败')
      await router.push('/')
    }
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  saving.value = true
  try {
    const res = adminMode.value
      ? await updateAppByAdmin(formState)
      : await editApp({
          id: formState.id,
          appName: formState.appName,
        })
    if (res.data.code === 0) {
      message.success('保存成功')
      await router.push(adminMode.value ? '/admin/appManage' : `/app/chat/${formState.id}`)
    } else {
      message.error(res.data.message || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loginUserStore.fetchLoginUser()
  await loadApp()
})
</script>

<template>
  <div class="edit-page">
    <Card title="应用信息修改" :bordered="false">
      <Spin :spinning="loading">
        <Form layout="vertical" @finish="handleSubmit">
          <Form.Item label="应用名称" name="appName" :rules="[{ required: true, message: '请输入应用名称' }]">
            <Input v-model:value="formState.appName" placeholder="请输入应用名称" />
          </Form.Item>
          <template v-if="adminMode">
            <Form.Item label="应用封面" name="cover">
              <Input v-model:value="formState.cover" placeholder="请输入封面图片地址" />
            </Form.Item>
            <Form.Item label="优先级" name="priority">
              <InputNumber v-model:value="formState.priority" :min="0" style="width: 100%" />
            </Form.Item>
          </template>
          <div class="actions">
            <Button @click="router.back()">取消</Button>
            <Button type="primary" html-type="submit" :loading="saving">保存</Button>
          </div>
        </Form>
      </Spin>
    </Card>
  </div>
</template>

<style scoped>
.edit-page {
  max-width: 760px;
  margin: 0 auto;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
