<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Button,
  Empty,
  Input,
  List,
  Modal,
  Select,
  Space,
  Spin,
  Tag,
  TypographyText,
  message,
} from 'ant-design-vue'
import { CloudUploadOutlined, SendOutlined } from '@ant-design/icons-vue'
import { deployApp, getAppVoById } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { createSseUrl, parseSseChunk } from '@/utils/sse'
import { getAppPreviewUrl } from '@/utils/app'

type ChatMessage = {
  id: string
  role: 'user' | 'assistant'
  content: string
}

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = computed(() => String(route.params.id))
const appInfo = ref<API.AppVO>()
const loading = ref(false)
const sending = ref(false)
const deploying = ref(false)
const inputMessage = ref('')
const messages = ref<ChatMessage[]>([])
const previewUrl = ref('')
const deployUrl = ref('')
const eventSourceRef = ref<EventSource | null>(null)
const messageListRef = ref<HTMLElement>()

const suggestionPrompts = [
  '优化首页首屏的转化文案和配色',
  '增加一个联系我们页面并补充表单',
  '把网站改成更偏科技感的企业官网',
]

const canSend = computed(() => !!inputMessage.value.trim() && !sending.value)
const appName = computed(() => appInfo.value?.appName || '应用对话')

const appendMessage = (role: 'user' | 'assistant', content: string) => {
  messages.value.push({
    id: `${Date.now()}_${Math.random()}`,
    role,
    content,
  })
  scrollToBottom()
}

const scrollToBottom = async () => {
  await nextTick()
  const el = messageListRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

const closeStream = () => {
  eventSourceRef.value?.close()
  eventSourceRef.value = null
}

const loadApp = async () => {
  if (!appId.value) {
    message.error('应用不存在')
    await router.push('/')
    return
  }
  loading.value = true
  try {
    const res = await getAppVoById({ id: appId.value })
    if (res.data.code !== 0 || !res.data.data) {
      message.error(res.data.message || '获取应用失败')
      return
    }
    appInfo.value = res.data.data
    previewUrl.value = getAppPreviewUrl(res.data.data)
    deployUrl.value = res.data.data.deployKey ? `http://localhost/${res.data.data.deployKey}/` : ''
  } finally {
    loading.value = false
  }
}

const startSseChat = (content: string) => {
  closeStream()
  sending.value = true
  appendMessage('user', content)
  const assistantMessage: ChatMessage = {
    id: `${Date.now()}_assistant`,
    role: 'assistant',
    content: '',
  }
  messages.value.push(assistantMessage)
  scrollToBottom()
  const url = createSseUrl('/app/chat/gen/code', {
    appId: appId.value,
    message: content,
  })
  const es = new EventSource(url, { withCredentials: true })
  eventSourceRef.value = es
  es.onmessage = (event) => {
    assistantMessage.content += parseSseChunk(event.data)
    messages.value = [...messages.value]
    scrollToBottom()
  }
  es.addEventListener('done', () => {
    sending.value = false
    closeStream()
    previewUrl.value = getAppPreviewUrl(appInfo.value)
  })
  es.onerror = () => {
    sending.value = false
    closeStream()
    message.error('生成中断，请稍后重试')
  }
}

const sendMessage = async (preset?: string) => {
  const content = (preset ?? inputMessage.value).trim()
  if (!content || sending.value) {
    return
  }
  inputMessage.value = ''
  startSseChat(content)
}

const deployCurrentApp = async () => {
  if (!appId.value || deploying.value) {
    return
  }
  deploying.value = true
  try {
    const res = await deployApp({ appId: appId.value })
    if (res.data.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      Modal.success({
        title: '部署完成',
        content: res.data.data,
      })
    } else {
      message.error(res.data.message || '部署失败')
    }
  } finally {
    deploying.value = false
  }
}

const openDeploySite = () => {
  if (!deployUrl.value) {
    return
  }
  window.open(deployUrl.value, '_blank')
}

watch(
  appInfo,
  (value) => {
    if (value?.initPrompt && messages.value.length === 0) {
      startSseChat(value.initPrompt)
    }
  },
  { immediate: true },
)

onMounted(() => {
  void loginUserStore.fetchLoginUser()
  void loadApp()
})

onBeforeUnmount(() => {
  closeStream()
})
</script>

<template>
  <div class="chat-page">
    <Spin :spinning="loading" class="page-spin">
      <header class="chat-header">
        <div class="header-left">
          <Button type="text" @click="router.push('/')">返回</Button>
          <div class="title-block">
            <h1>{{ appName }}</h1>
            <div class="subline">
              <Tag color="blue">{{ appInfo?.codeGenType || 'multi_file' }}</Tag>
              <TypographyText type="secondary">
                {{ deployUrl ? '已部署' : '未部署' }}
              </TypographyText>
            </div>
          </div>
        </div>
        <Space>
          <Button :disabled="!deployUrl" @click="openDeploySite">
            打开部署站点
          </Button>
          <Button type="primary" :loading="deploying" @click="deployCurrentApp">
            <CloudUploadOutlined />
            部署
          </Button>
        </Space>
      </header>

      <main class="chat-content">
        <section class="dialog-panel">
          <div ref="messageListRef" class="message-list">
            <div
              v-for="item in messages"
              :key="item.id"
              class="message-row"
              :class="item.role"
            >
              <div class="message-bubble">
                <pre class="message-text">{{ item.content }}</pre>
              </div>
            </div>
            <Empty v-if="messages.length === 0 && !sending" description="开始描述你想生成的页面" />
          </div>

          <div class="composer">
            <div class="suggestions">
              <Button
                v-for="prompt in suggestionPrompts"
                :key="prompt"
                size="small"
                @click="sendMessage(prompt)"
              >
                {{ prompt }}
              </Button>
            </div>
            <Input.TextArea
              v-model:value="inputMessage"
              :auto-size="{ minRows: 3, maxRows: 6 }"
              placeholder="继续描述要修改的页面、功能或样式"
              @press-enter.prevent="sendMessage()"
            />
            <div class="composer-action">
              <Button type="primary" :loading="sending" :disabled="!canSend" @click="sendMessage()">
                <SendOutlined />
                发送
              </Button>
            </div>
          </div>
        </section>

        <aside class="preview-panel">
          <div class="preview-toolbar">
            <span>网页展示</span>
            <Select :value="previewUrl ? 'ready' : 'empty'" class="preview-status" disabled>
              <Select.Option value="ready">已生成</Select.Option>
              <Select.Option value="empty">待生成</Select.Option>
            </Select>
          </div>
          <div class="preview-frame-shell">
            <iframe
              v-if="previewUrl"
              :src="previewUrl"
              title="应用预览"
              class="preview-frame"
            />
            <Empty v-else description="生成完成后会在这里展示" class="preview-empty" />
          </div>
        </aside>
      </main>
    </Spin>
  </div>
</template>

<style scoped>
.chat-page {
  min-height: calc(100vh - 64px - 49px);
}

.page-spin :deep(.ant-spin-container) {
  min-height: inherit;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.title-block h1 {
  margin: 0;
  font-size: 24px;
  color: #111827;
}

.subline {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.chat-content {
  display: grid;
  grid-template-columns: minmax(360px, 1.05fr) minmax(420px, 1.35fr);
  gap: 16px;
  min-height: calc(100vh - 64px - 49px - 72px);
}

.dialog-panel,
.preview-panel {
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.message-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f8fafc;
}

.message-row {
  display: flex;
  margin-bottom: 14px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 88%;
  padding: 14px 16px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.message-row.user .message-bubble {
  background: #111827;
  color: #ffffff;
}

.message-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.7;
}

.composer {
  border-top: 1px solid rgba(15, 23, 42, 0.08);
  padding: 16px;
  background: #ffffff;
}

.suggestions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.composer-action {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.preview-toolbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.preview-status {
  width: 110px;
}

.preview-frame-shell {
  flex: 1;
  min-height: 0;
  background: #f8fafc;
}

.preview-frame {
  width: 100%;
  height: 100%;
  border: 0;
  display: block;
  background: #ffffff;
}

.preview-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 1100px) {
  .chat-content {
    grid-template-columns: minmax(0, 1fr);
  }

  .preview-panel {
    min-height: 560px;
  }
}

@media (max-width: 768px) {
  .chat-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-left {
    align-items: flex-start;
  }

  .title-block h1 {
    font-size: 20px;
  }
}
</style>
