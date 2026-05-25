<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Button, Input, Space, Table, Tag, message } from 'ant-design-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { listUserVoByPage } from '@/api/userController'

const loading = ref(false)
const tableData = ref<API.UserVO[]>([])
const total = ref(0)

const searchForm = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  userName: '',
  userAccount: '',
  userRole: '',
})

const columns: TableColumnsType<API.UserVO> = [
  { title: 'ID', dataIndex: 'id', width: 120 },
  { title: '账号', dataIndex: 'userAccount', width: 180 },
  { title: '昵称', dataIndex: 'userName', width: 180 },
  { title: '简介', dataIndex: 'userProfile', ellipsis: true },
  { title: '角色', dataIndex: 'userRole', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await listUserVoByPage(searchForm)
    if (res.data.code === 0 && res.data.data) {
      tableData.value = res.data.data.records || []
      total.value = Number(res.data.data.totalRow || 0)
    } else {
      message.error(res.data.message || '获取用户列表失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadData()
})
</script>

<template>
  <div class="user-manage-page">
    <div class="toolbar">
      <Input
        v-model:value="searchForm.userName"
        allow-clear
        placeholder="按昵称搜索"
        class="search-input"
        @press-enter="loadData"
      />
      <Space>
        <Button @click="loadData">重置</Button>
        <Button type="primary" @click="loadData">查询</Button>
      </Space>
    </div>

    <Table
      row-key="id"
      :columns="columns"
      :data-source="tableData"
      :loading="loading"
      :pagination="{
        current: searchForm.pageNum,
        pageSize: searchForm.pageSize,
        total,
        showSizeChanger: true,
        showTotal: (value: number) => `共 ${value} 条`,
      }"
      @change="(page) => {
        searchForm.pageNum = page.current
        searchForm.pageSize = page.pageSize
        loadData()
      }"
    >
      <template #bodyCell="{ column, text }">
        <template v-if="column.dataIndex === 'userRole'">
          <Tag :color="text === 'admin' ? 'red' : 'blue'">
            {{ text }}
          </Tag>
        </template>
      </template>
    </Table>
  </div>
</template>

<style scoped>
.user-manage-page {
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
