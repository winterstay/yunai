<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Button, Input, Popconfirm, Space, Table, Tag, message } from 'ant-design-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { adminListAppByPage, deleteAppByAdmin, updateAppByAdmin } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { formatRelativeTime } from '@/utils/app'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const loading = ref(false)
const tableData = ref<API.AppVO[]>([])
const total = ref(0)

const searchForm = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  appName: '',
  codeGenType: undefined,
  deployKey: '',
  userId: undefined,
  priority: undefined,
})

const pagination = computed(() => ({
  current: searchForm.pageNum || 1,
  pageSize: searchForm.pageSize || 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (value: number) => `共 ${value} 条`,
}))

const columns: TableColumnsType<API.AppVO> = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 120,
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    ellipsis: true,
  },
  {
    title: '类型',
    dataIndex: 'codeGenType',
    width: 120,
  },
  {
    title: '创建者',
    dataIndex: ['user', 'userName'],
    width: 140,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 100,
  },
  {
    title: '部署标识',
    dataIndex: 'deployKey',
    width: 140,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 140,
    customRender: ({ text }) => formatRelativeTime(text as string),
  },
  {
    title: '操作',
    key: 'action',
    width: 240,
  },
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminListAppByPage(searchForm)
    if (res.data.code === 0 && res.data.data) {
      tableData.value = res.data.data.records || []
      total.value = Number(res.data.data.totalRow || 0)
    } else {
      message.error(res.data.message || '获取应用列表失败')
    }
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id?: string) => {
  if (!id) {
    return
  }
  const res = await deleteAppByAdmin({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    await loadData()
  } else {
    message.error(res.data.message || '删除失败')
  }
}

const setFeatured = async (record: API.AppVO) => {
  const res = await updateAppByAdmin({
    id: record.id,
    appName: record.appName,
    cover: record.cover,
    priority: 99,
  })
  if (res.data.code === 0) {
    message.success('已设为精选')
    await loadData()
  } else {
    message.error(res.data.message || '操作失败')
  }
}

onMounted(async () => {
  await loginUserStore.fetchLoginUser()
  await loadData()
})
</script>

<template>
  <div class="app-manage-page">
    <div class="toolbar">
      <Input
        v-model:value="searchForm.appName"
        allow-clear
        placeholder="按应用名称搜索"
        class="search-input"
        @press-enter="loadData"
      />
      <Space>
        <Button @click="router.push('/')">返回首页</Button>
        <Button type="primary" @click="loadData">查询</Button>
      </Space>
    </div>

    <Table
      row-key="id"
      :columns="columns"
      :data-source="tableData"
      :loading="loading"
      :pagination="pagination"
      @change="(page) => {
        searchForm.pageNum = page.current
        searchForm.pageSize = page.pageSize
        loadData()
      }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'priority'">
          <Tag :color="record.priority === 99 ? 'gold' : 'default'">
            {{ record.priority === 99 ? '精选' : record.priority ?? 0 }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <Space>
            <Button size="small" @click="router.push(`/app/edit/${record.id}`)">编辑</Button>
            <Button size="small" @click="setFeatured(record)">精选</Button>
            <Popconfirm title="确定删除该应用？" @confirm="handleDelete(record.id)">
              <Button danger size="small">删除</Button>
            </Popconfirm>
          </Space>
        </template>
      </template>
    </Table>
  </div>
</template>

<style scoped>
.app-manage-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.search-input {
  width: 280px;
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }
}
</style>
