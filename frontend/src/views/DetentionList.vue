<template>
  <div class="page-container">
    <div class="card">
      <div class="toolbar">
        <h2>现场暂扣</h2>
        <div>
          <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 140px; margin-right: 12px;" @change="loadData">
            <el-option v-for="s in statusList" :key="s.name" :label="s.description" :value="s.name" />
          </el-select>
          <el-button v-if="isEnforcer" type="primary" :icon="Plus" @click="goCreate">
            新增暂扣
          </el-button>
        </div>
      </div>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="detentionNo" label="暂扣单号" width="160" />
        <el-table-column prop="permitNo" label="许可证号" width="160" />
        <el-table-column prop="declarationNo" label="申报单号" width="160">
          <template #default="{ row }">{{ row.declarationNo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="vesselName" label="船只" width="120" />
        <el-table-column prop="detentionReason" label="暂扣原因" min-width="200" show-overflow-tooltip />
        <el-table-column label="暂扣方量" width="120">
          <template #default="{ row }">{{ formatNumber(row.detainedVolume) }} 方</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusColor('detention', row.status)">
              {{ getStatusDesc(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="officerName" label="执法人员" width="100" />
        <el-table-column prop="detentionTime" label="暂扣时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.detentionTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.id)">详情</el-button>
            <el-button
              v-if="isEnforcer && row.status === 'DETAINED'"
              type="warning" link @click="handleStartRectify(row)"
            >
              启动整改
            </el-button>
            <el-button
              v-if="isEnforcer && row.status === 'RECTIFYING'"
              type="success" link @click="goReview(row.id)"
            >
              复查
            </el-button>
            <el-button
              v-if="isEnforcer && (row.status === 'DETAINED' || row.status === 'REVIEWED')"
              type="success" link @click="handleRelease(row)"
            >
              解除
            </el-button>
            <el-button
              v-if="isEnforcer && row.status === 'REVIEWED'"
              type="danger" link @click="handleConfirmViolation(row)"
            >
              确认违规
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDetections, startRectification, releaseDetention, confirmViolation
} from '@/api/detention'
import { getEnums } from '@/api/common'
import { getUserInfo, formatDateTime, formatNumber, getStatusColor } from '@/utils'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const statusList = ref([])
const filterStatus = ref('')
const userInfo = computed(() => getUserInfo() || {})
const isEnforcer = computed(() => userInfo.value.role === 'ENFORCER')

function getStatusDesc(status) {
  return statusList.value.find(s => s.name === status)?.description || status
}

async function loadEnums() {
  const res = await getEnums()
  statusList.value = res.data?.detentionStatuses || []
}

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (filterStatus.value) {
      params.status = filterStatus.value
    }
    if (isEnforcer.value) {
      params.officerId = userInfo.value.userId
    }
    const res = await getDetections(params)
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function goCreate() {
  router.push('/detentions/new')
}

function goDetail(id) {
  router.push(`/detentions/${id}`)
}

function goReview(id) {
  router.push(`/detentions/${id}/review`)
}

async function handleStartRectify(row) {
  try {
    const { value: requirement } = await ElMessageBox.prompt('请输入整改要求', '启动整改', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入整改要求',
      inputType: 'textarea',
      inputRows: 3
    })
    if (requirement) {
      await startRectification(row.id, requirement)
      ElMessage.success('已启动整改')
      loadData()
    }
  } catch {
  }
}

async function handleRelease(row) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入解除原因', '解除暂扣', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入解除原因'
    })
    if (reason) {
      await releaseDetention(row.id, reason)
      ElMessage.success('已解除暂扣')
      loadData()
    }
  } catch {
  }
}

async function handleConfirmViolation(row) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入确认违规原因', '确认违规', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入确认违规原因',
      type: 'warning'
    })
    if (reason) {
      await confirmViolation(row.id, reason)
      ElMessage.success('已确认违规')
      loadData()
    }
  } catch {
  }
}

onMounted(() => {
  loadEnums()
  loadData()
})
</script>
