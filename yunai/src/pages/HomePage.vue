<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Button, Input, message } from 'ant-design-vue'
import { ArrowUpOutlined } from '@ant-design/icons-vue'
import { addApp, listGoodAppVoByPage, listMyAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import AppSection from '@/components/AppSection.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const creating = ref(false)
const prompt = ref('')
const myLoading = ref(false)
const featuredLoading = ref(false)
const myApps = ref<API.AppVO[]>([])
const featuredApps = ref<API.AppVO[]>([])

const myQuery = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 6,
  appName: '',
  sortField: 'createTime',
  sortOrder: 'descend',
})

const featuredQuery = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 6,
  appName: '',
  sortField: 'createTime',
  sortOrder: 'descend',
})

const myTotal = ref(0)
const featuredTotal = ref(0)

const ideaExamples = [
  '波普风电商页面',
  '企业官网',
  '电商运营后台',
  '暗黑话题社区',
]

const canCreate = computed(() => !!prompt.value.trim() && !!loginUserStore.loginUser.id)

const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    myApps.value = []
    myTotal.value = 0
    return
  }
  myLoading.value = true
  try {
    const res = await listMyAppVoByPage(myQuery)
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myTotal.value = Number(res.data.data.totalRow || 0)
    }
  } finally {
    myLoading.value = false
  }
}

const loadFeaturedApps = async () => {
  featuredLoading.value = true
  try {
    const res = await listGoodAppVoByPage(featuredQuery)
    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredTotal.value = Number(res.data.data.totalRow || 0)
    }
  } finally {
    featuredLoading.value = false
  }
}

const createAppAndRedirect = async (preset?: string) => {
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }
  const initPrompt = (preset ?? prompt.value).trim()
  if (!initPrompt || creating.value) {
    return
  }
  creating.value = true
  try {
    const res = await addApp({ initPrompt })
    if (res.data.code === 0 && res.data.data) {
      prompt.value = ''
      message.success('应用创建成功')
      await router.push(`/app/chat/${res.data.data}`)
    } else {
      message.error(res.data.message || '创建应用失败')
    }
  } finally {
    creating.value = false
  }
}

onMounted(async () => {
  await loginUserStore.fetchLoginUser()
  await Promise.all([loadMyApps(), loadFeaturedApps()])
})

watch(
  () => loginUserStore.loginUser.id,
  () => {
    void loadMyApps()
  },
)
</script>

<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-inner">
        <h1>一句话 呈所想</h1>
        <p>与 AI 对话轻松创建应用和网站</p>
        <div class="prompt-shell">
          <Input.TextArea
            v-model:value="prompt"
            :auto-size="{ minRows: 6, maxRows: 8 }"
            class="prompt-input"
            placeholder="使用 NoCode 创建一个高效的小工具，帮我计算……"
          />
          <div class="prompt-toolbar">
            <div class="chips">
              <Button v-for="item in ideaExamples" :key="item" shape="round" @click="createAppAndRedirect(item)">
                {{ item }}
              </Button>
            </div>
            <Button
              type="primary"
              shape="circle"
              size="large"
              :loading="creating"
              :disabled="!canCreate"
              @click="createAppAndRedirect()"
            >
              <ArrowUpOutlined />
            </Button>
          </div>
        </div>
      </div>
    </section>

    <section class="content-section">
      <div class="content-inner">
        <AppSection
          title="我的作品"
          :apps="myApps"
          :loading="myLoading"
          :current="myQuery.pageNum || 1"
          :page-size="myQuery.pageSize || 6"
          :total="myTotal"
          @change="
            (page, size) => {
              myQuery.pageNum = page
              myQuery.pageSize = size
              loadMyApps()
            }
          "
        />
        <AppSection
          title="精选案例"
          :apps="featuredApps"
          :loading="featuredLoading"
          :current="featuredQuery.pageNum || 1"
          :page-size="featuredQuery.pageSize || 6"
          :total="featuredTotal"
          @change="
            (page, size) => {
              featuredQuery.pageNum = page
              featuredQuery.pageSize = size
              loadFeaturedApps()
            }
          "
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  margin: -24px;
  background:
    radial-gradient(circle at top right, rgba(103, 232, 249, 0.35), transparent 28%),
    radial-gradient(circle at bottom center, rgba(34, 211, 238, 0.28), transparent 32%),
    linear-gradient(180deg, #f8fafc 0%, #ffffff 42%);
}

.hero-section {
  padding: 56px 24px 48px;
}

.hero-inner,
.content-inner {
  max-width: 1280px;
  margin: 0 auto;
}

.hero-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.hero-inner h1 {
  margin: 0;
  font-size: clamp(44px, 6vw, 72px);
  line-height: 1.04;
  color: #111827;
  font-weight: 700;
}

.hero-inner p {
  margin: 20px 0 0;
  font-size: 18px;
  color: #6b7280;
}

.prompt-shell {
  width: min(100%, 1120px);
  margin-top: 48px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 30px 80px rgba(15, 23, 42, 0.08);
  border-radius: 28px;
  padding: 24px;
  backdrop-filter: blur(18px);
}

.prompt-input :deep(textarea) {
  font-size: 18px;
  color: #111827;
  background: transparent;
  resize: none;
}

.prompt-toolbar {
  margin-top: 24px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.chips {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.content-section {
  background: #ffffff;
  border-top-left-radius: 28px;
  border-top-right-radius: 28px;
  padding: 20px 24px 56px;
}

@media (max-width: 768px) {
  .home-page {
    margin: -16px;
  }

  .hero-section,
  .content-section {
    padding-left: 16px;
    padding-right: 16px;
  }

  .prompt-shell {
    border-radius: 20px;
    padding: 18px;
  }

  .prompt-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .chips {
    justify-content: flex-start;
  }
}
</style>
