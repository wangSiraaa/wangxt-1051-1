<template>
  <div class="page-container">
    <div class="card">
      <div class="toolbar">
        <h2>许可证管理</h2>
        <div>
          <el-button type="warning" @click="checkExpiry">
            <el-icon><RefreshRight /></el-icon> 检查许可证过期
          </el-button>
        </div>
      </div>

      <div class="search-form" style="margin-bottom: 16px;">
        <el-select v-model="filters.status" placeholder="状态" clearable style="width: 160px;">
          <el-option v-for="s in statusList" :key="s.name" :label="s.description" :value="s.name" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
      </div>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="permitNo" label="许可证号" width="160" />
        <el-table-column prop="holderName" label="持证企业" min-width="160" />
        <el-table-column prop="riverSectionName" label="河段" min-width="140" />
        <el-table-column label="有效期" width="220">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }} 至 {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column label="许可方量" width="110">
          <template #default="{ row }">{{ formatNumber(row.permittedVolume) }} 方</template>
        </el-table-column>
        <el-table-column label="已使用" width="110">
          <template #default="{ row }">{{ formatNumber(row.usedVolume) }} 方</template>
        </el-table-column>
        <el-table-column label="剩余" width="110">
          <template #default="{ row }">
            <span :style="{ color: row.remainingVolume < 500 ? '#f56c6c' : '#67c23a' }">
              {{ formatNumber(row.remainingVolume) }} 方
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor('permit', row.status)">
              {{ getStatusDesc(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh, RefreshRight } from '@element-plus/icons-vue'
import { getPermits, checkPermitExpiry } from '@/api/permit'
import { getEnums } from '@/api/common'
import { getUserInfo, formatDate, formatNumber, getStatusColor } from '@/utils'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const statusList = ref([])
const userInfo = computed(() => getUserInfo() || {})

const filters = reactive({ status: null })

function getStatusDesc(status) {
  return statusList.value.find(s => s.name === status)?.description || status
}

async function loadEnums() {
  const res = await getEnums()
  statusList.value = res.data?.permitStatuses || []
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...filters }
    if (userInfo.value.role === 'ENTERPRISE') {
      params.holderId = userInfo.value.userId
    }
    const res = await getPermits(params)
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.status = null
  loadData()
}

function goDetail(id) {
  router.push(`/permits/${id}`)
}

async function checkExpiry() {
  await checkPermitExpiry()
  ElMessage.success('已执行过期检查')
  loadData()
}

onMounted(() => {
  loadEnums()
  loadData()
})
</script>
