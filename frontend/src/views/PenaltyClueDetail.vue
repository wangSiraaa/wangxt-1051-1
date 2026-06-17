<template>
  <div class="page-container" v-loading="loading">
    <div v-if="clue" class="card" style="margin-bottom: 20px;">
      <div class="toolbar">
        <div>
          <h2>处罚线索详情 - {{ clue.clueNo }}</h2>
          <el-tag :type="getStatusColor('penalty', clue.status)" style="margin-left: 12px;">
            {{ getStatusDesc(clue.status) }}
          </el-tag>
        </div>
        <div>
          <el-button @click="goBack">返回</el-button>
          <el-button
            v-if="isEnforcer && clue.status === 'PENDING'"
            type="success" @click="handleSubmit"
          >
            提交审核
          </el-button>
          <el-button
            v-if="isAuditor && clue.status === 'REVIEWING'"
            type="warning" @click="handleReview(true)"
          >
            审核通过
          </el-button>
          <el-button
            v-if="isAuditor && clue.status === 'REVIEWING'"
            type="danger" @click="handleReview(false)"
          >
            审核驳回
          </el-button>
          <el-button
            v-if="isPenaltyHandler && clue.status === 'CONFIRMED'"
            type="success" @click="handleProcess"
          >
            处理完成
          </el-button>
        </div>
      </div>

      <el-row :gutter="20">
        <el-col :span="8">
          <div class="detail-item">
            <span class="detail-label">线索类型：</span>
            <span class="detail-value">{{ clue.clueType }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">来源：</span>
            <span class="detail-value">{{ clue.clueSource }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">企业名称：</span>
            <span class="detail-value">{{ clue.enterpriseName }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="detail-item">
            <span class="detail-label">关联许可证：</span>
            <span class="detail-value">
              <el-link type="primary" v-if="clue.permitId" @click="goPermitDetail">
                {{ clue.permitNo }}
              </el-link>
              <span v-else>-</span>
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">超采量：</span>
            <span class="detail-value" style="color: #f56c6c;">{{ formatNumber(clue.exceedVolume) }} 方</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">拟处罚金额：</span>
            <span class="detail-value" style="color: #f56c6c;">{{ formatNumber(clue.penaltyAmount) }} 元</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="detail-item">
            <span class="detail-label">创建时间：</span>
            <span class="detail-value">{{ formatDateTime(clue.createTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">审核时间：</span>
            <span class="detail-value">{{ formatDateTime(clue.reviewTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">处理时间：</span>
            <span class="detail-value">{{ formatDateTime(clue.handleTime) }}</span>
          </div>
        </el-col>
      </el-row>

      <el-alert
        :title="clue.description"
        type="warning"
        :closable="false"
        style="margin-top: 16px;"
      />

      <div v-if="clue.reviewOpinion" class="detail-item" style="margin-top: 16px;">
        <span class="detail-label">审核意见：</span>
        <span class="detail-value">{{ clue.reviewOpinion }}</span>
      </div>
      <div v-if="clue.handleResult" class="detail-item">
        <span class="detail-label">处理结果：</span>
        <span class="detail-value">{{ clue.handleResult }}</span>
      </div>
    </div>

    <div class="card">
      <div class="page-header">
        <h2>审计时间线</h2>
      </div>
      <el-timeline v-if="timeline.length" class="timeline-wrapper">
        <el-timeline-item
          v-for="item in timeline"
          :key="item.id"
          :timestamp="formatDateTime(item.operateTime)"
        >
          <strong>{{ item.operation }}</strong>
          <span style="margin-left: 8px; color: #909399; font-size: 12px;">[{{ item.operator }}]</span>
          <div style="margin-top: 4px; color: #606266;">{{ item.detail }}</div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无操作记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPenaltyClueById, submitPenaltyClueForReview, reviewPenaltyClue, processPenaltyClue
} from '@/api/penaltyClue'
import { getAuditTimeline, getEnums } from '@/api/common'
import { getUserInfo, formatDateTime, formatNumber, getStatusColor } from '@/utils'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const clue = ref(null)
const timeline = ref([])
const statusList = ref([])
const userInfo = computed(() => getUserInfo() || {})
const isEnforcer = computed(() => userInfo.value.role === 'ENFORCER')
const isAuditor = computed(() => userInfo.value.role === 'AUDITOR')
const isPenaltyHandler = computed(() => userInfo.value.role === 'PENALTY_HANDLER')

function getStatusDesc(status) {
  return statusList.value.find(s => s.name === status)?.description || status
}
function goBack() {
  router.back()
}
function goPermitDetail() {
  router.push(`/permits/${clue.value.permitId}`)
}

async function handleSubmit() {
  await ElMessageBox.confirm('确定提交审核吗？', '提示', { type: 'warning' })
  await submitPenaltyClueForReview(clue.value.id)
  ElMessage.success('已提交审核')
  loadData()
}

async function handleReview(approved) {
  const title = approved ? '审核通过' : '审核驳回'
  const { value } = await ElMessageBox.prompt(`请输入${title}意见`, title, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '请输入审核意见'
  })
  await reviewPenaltyClue(clue.value.id, { approved, opinion: value })
  ElMessage.success(title + '成功')
  loadData()
}

async function handleProcess() {
  const { value } = await ElMessageBox.prompt('请输入处理结果', '处罚处理', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '请输入处理结果'
  })
  await processPenaltyClue(clue.value.id, value)
  ElMessage.success('处理完成')
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getPenaltyClueById(id)
    clue.value = res.data
    const tlRes = await getAuditTimeline('PENALTY_CLUE', id)
    timeline.value = tlRes.data || []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const enumRes = await getEnums()
  statusList.value = enumRes.data?.penaltyStatuses || []
  loadData()
})
</script>
