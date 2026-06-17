<template>
  <div class="page-container">
    <div class="card">
      <div class="toolbar">
        <h2>许可申请</h2>
        <div>
          <el-button v-if="isEnterprise" type="primary" :icon="Plus" @click="goCreate">
            新建申请
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
        <el-table-column prop="applicationNo" label="申请编号" width="160" />
        <el-table-column prop="enterpriseName" label="申请企业" min-width="160" />
        <el-table-column prop="riverSectionName" label="河段" min-width="140" />
        <el-table-column prop="vesselNames" label="船只" min-width="140" show-overflow-tooltip />
        <el-table-column label="采砂时段" width="220">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }} 至 {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column label="预计方量" width="120">
          <template #default="{ row }">{{ formatNumber(row.estimatedVolume) }} 方</template>
        </el-table-column>
        <el-table-column label="保证金" width="120">
          <template #default="{ row }">{{ formatNumber(row.depositAmount) }} 元</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor('application', row.status)">
              {{ getStatusDesc(statusList, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.id)">详情</el-button>
            <el-button
              v-if="isEnterprise && (row.status === 'DRAFT' || row.status === 'RETURNED')"
              type="success" link
              @click="handleSubmit(row)"
            >
              提交
            </el-button>
            <el-button
              v-if="isAuditor && row.status === 'PENDING_REVIEW'"
              type="warning" link
              @click="goDetail(row.id)"
            >
              审核
            </el-button>
            <el-button
              v-if="isAuditor && (row.status === 'APPROVED' || row.status === 'CHANGED')"
              type="danger" link
              @click="handleSuspend(row)"
            >
              暂停
            </el-button>
            <el-button
              v-if="isAuditor && row.status === 'SUSPENDED'"
              type="success" link
              @click="handleResume(row)"
            >
              恢复
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getApplications, submitApplication, suspendApplication, resumeApplication } from '@/api/application'
import { getEnums } from '@/api/common'
import { getUserInfo, formatDate, formatNumber, getStatusColor } from '@/utils'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const statusList = ref([])
const userInfo = computed(() => getUserInfo() || {})
const isEnterprise = computed(() => userInfo.value.role === 'ENTERPRISE')
const isAuditor = computed(() => userInfo.value.role === 'AUDITOR')

const filters = reactive({
  status: null
})

function getStatusDesc(list, status) {
  return list.find(s => s.name === status)?.description || status
}

async function loadEnums() {
  const res = await getEnums()
  statusList.value = res.data?.applicationStatuses || []
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...filters }
    if (isEnterprise.value) {
      params.applicantId = userInfo.value.userId
    }
    const res = await getApplications(params)
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.status = null
  loadData()
}

function goCreate() {
  router.push('/applications/new')
}

function goDetail(id) {
  router.push(`/applications/${id}`)
}

async function handleSubmit(row) {
  await ElMessageBox.confirm('确定提交审核吗？', '提示', { type: 'warning' })
  await submitApplication(row.id)
  ElMessage.success('提交成功')
  loadData()
}

async function handleSuspend(row) {
  const { value } = await ElMessageBox.prompt('请输入暂停原因', '暂停许可', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '请输入暂停原因'
  })
  await suspendApplication(row.id, value)
  ElMessage.success('已暂停')
  loadData()
}

async function handleResume(row) {
  await ElMessageBox.confirm('确定恢复该许可吗？', '提示', { type: 'warning' })
  await resumeApplication(row.id)
  ElMessage.success('已恢复')
  loadData()
}

onMounted(() => {
  loadEnums()
  loadData()
})
</script>
