<template>
  <div class="page-container">
    <div class="card">
      <div class="toolbar">
        <h2>处罚线索</h2>
      </div>

      <div class="search-form" style="margin-bottom: 16px;">
        <el-select v-model="filters.status" placeholder="状态" clearable style="width: 160px;">
          <el-option v-for="s in statusList" :key="s.name" :label="s.description" :value="s.name" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
      </div>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="clueNo" label="线索编号" width="160" />
        <el-table-column prop="clueType" label="线索类型" width="120" />
        <el-table-column prop="clueSource" label="来源" width="100" />
        <el-table-column prop="permitNo" label="许可证号" width="160" />
        <el-table-column prop="enterpriseName" label="企业名称" min-width="160" />
        <el-table-column label="超采量" width="120">
          <template #default="{ row }">{{ formatNumber(row.exceedVolume) }} 方</template>
        </el-table-column>
        <el-table-column label="拟处罚金额" width="120">
          <template #default="{ row }">{{ formatNumber(row.penaltyAmount) }} 元</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor('penalty', row.status)">
              {{ getStatusDesc(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.id)">详情</el-button>
            <el-button
              v-if="isEnforcer && row.status === 'PENDING'"
              type="success" link
              @click="handleSubmit(row)"
            >
              提交审核
            </el-button>
            <el-button
              v-if="isAuditor && row.status === 'REVIEWING'"
              type="warning" link
              @click="handleReview(row, true)"
            >
              审核通过
            </el-button>
            <el-button
              v-if="isAuditor && row.status === 'REVIEWING'"
              type="danger" link
              @click="handleReview(row, false)"
            >
              审核驳回
            </el-button>
            <el-button
              v-if="isPenaltyHandler && row.status === 'CONFIRMED'"
              type="success" link
              @click="handleProcess(row)"
            >
              处理完成
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
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  getPenaltyClues, getPenaltyCluesByEnterprise, getPenaltyCluesByStatus,
  submitPenaltyClueForReview, reviewPenaltyClue, processPenaltyClue
} from '@/api/penaltyClue'
import { getEnums } from '@/api/common'
import { getUserInfo, formatDateTime, formatNumber, getStatusColor } from '@/utils'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const statusList = ref([])
const userInfo = computed(() => getUserInfo() || {})
const isEnforcer = computed(() => userInfo.value.role === 'ENFORCER')
const isAuditor = computed(() => userInfo.value.role === 'AUDITOR')
const isPenaltyHandler = computed(() => userInfo.value.role === 'PENALTY_HANDLER')

const filters = reactive({ status: null })

function getStatusDesc(status) {
  return statusList.value.find(s => s.name === status)?.description || status
}

async function loadEnums() {
  const res = await getEnums()
  statusList.value = res.data?.penaltyStatuses || []
}

async function loadData() {
  loading.value = true
  try {
    let res
    if (filters.status) {
      res = await getPenaltyCluesByStatus(filters.status)
    } else if (userInfo.value.role === 'ENTERPRISE') {
      res = await getPenaltyCluesByEnterprise(userInfo.value.userId)
    } else {
      res = await getPenaltyClues()
    }
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
  router.push(`/penalty-clues/${id}`)
}

async function handleSubmit(row) {
  await ElMessageBox.confirm('确定提交审核吗？', '提示', { type: 'warning' })
  await submitPenaltyClueForReview(row.id)
  ElMessage.success('已提交审核')
  loadData()
}

async function handleReview(row, approved) {
  const title = approved ? '审核通过' : '审核驳回'
  const { value } = await ElMessageBox.prompt(`请输入${title}意见`, title, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '请输入审核意见'
  })
  await reviewPenaltyClue(row.id, { approved, opinion: value })
  ElMessage.success(title + '成功')
  loadData()
}

async function handleProcess(row) {
  const { value } = await ElMessageBox.prompt('请输入处理结果', '处罚处理', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '请输入处理结果'
  })
  await processPenaltyClue(row.id, value)
  ElMessage.success('处理完成')
  loadData()
}

onMounted(() => {
  loadEnums()
  loadData()
})
</script>
