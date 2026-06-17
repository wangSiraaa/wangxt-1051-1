<template>
  <div class="page-container" v-loading="loading">
    <div v-if="permit" class="card" style="margin-bottom: 20px;">
      <div class="toolbar">
        <div>
          <h2>许可证详情 - {{ permit.permitNo }}</h2>
          <el-tag :type="getStatusColor('permit', permit.status)" style="margin-left: 12px;">
            {{ getStatusDesc(permit.status) }}
          </el-tag>
        </div>
        <el-button @click="goBack">返回</el-button>
      </div>

      <el-row :gutter="20">
        <el-col :span="8">
          <div class="detail-item">
            <span class="detail-label">持证企业：</span>
            <span class="detail-value">{{ permit.holderName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">采砂河段：</span>
            <span class="detail-value">{{ permit.riverSectionName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">作业船只：</span>
            <span class="detail-value">{{ permit.vesselNames || '-' }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="detail-item">
            <span class="detail-label">有效期：</span>
            <span class="detail-value">{{ formatDate(permit.startDate) }} 至 {{ formatDate(permit.endDate) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">发证日期：</span>
            <span class="detail-value">{{ formatDateTime(permit.issueDate) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">保证金：</span>
            <span class="detail-value">{{ formatNumber(permit.depositAmount) }} 元</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="detail-item">
            <span class="detail-label">许可方量：</span>
            <span class="detail-value">{{ formatNumber(permit.permittedVolume) }} 方</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">已使用：</span>
            <span class="detail-value">{{ formatNumber(permit.usedVolume) }} 方</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">剩余：</span>
            <span class="detail-value">
              <span :style="{ color: permit.remainingVolume < 500 ? '#f56c6c' : '#67c23a' }">
                {{ formatNumber(permit.remainingVolume) }} 方
              </span>
            </span>
          </div>
        </el-col>
      </el-row>

      <el-progress
        :percentage="calcProgress(permit.usedVolume, permit.permittedVolume)"
        :status="permit.usedVolume / permit.permittedVolume > 1 ? 'exception' : ''"
        style="margin-top: 20px;"
      />

      <div v-if="permit.suspendReason" class="detail-item" style="margin-top: 16px;">
        <span class="detail-label">暂停原因：</span>
        <span class="detail-value">{{ permit.suspendReason }}</span>
      </div>
      <div v-if="permit.changeDescription" class="detail-item">
        <span class="detail-label">变更说明：</span>
        <span class="detail-value">{{ permit.changeDescription }}</span>
      </div>
    </div>

    <div class="card" style="margin-bottom: 20px;">
      <div class="toolbar">
        <h2>现场核查记录 ({{ inspections.length }})</h2>
        <el-button
          v-if="isEnforcer && permit && permit.status === 'ACTIVE' && !isExpired"
          type="primary" @click="goCreateInspection"
        >
          <el-icon><Plus /></el-icon> 新增核查
        </el-button>
      </div>
      <el-table :data="inspections" stripe>
        <el-table-column prop="inspectionTime" label="核查时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.inspectionTime) }}</template>
        </el-table-column>
        <el-table-column prop="inspectorName" label="执法人员" width="120" />
        <el-table-column prop="location" label="位置" min-width="200" />
        <el-table-column label="实际方量" width="120">
          <template #default="{ row }">{{ formatNumber(row.actualVolume) }} 方</template>
        </el-table-column>
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor('inspection', row.result)">
              {{ getInspectionResultDesc(row.result) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="abnormalDescription" label="异常描述" min-width="200" show-overflow-tooltip />
      </el-table>
    </div>

    <div class="card" style="margin-bottom: 20px;">
      <div class="page-header">
        <h2>处罚线索 ({{ penaltyClues.length }})</h2>
      </div>
      <el-table :data="penaltyClues" stripe>
        <el-table-column prop="clueNo" label="线索编号" width="160" />
        <el-table-column prop="clueType" label="类型" width="120" />
        <el-table-column label="超采量" width="120">
          <template #default="{ row }">{{ formatNumber(row.exceedVolume) }} 方</template>
        </el-table-column>
        <el-table-column label="拟处罚金额" width="120">
          <template #default="{ row }">{{ formatNumber(row.penaltyAmount) }} 元</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor('penalty', row.status)">
              {{ getPenaltyStatusDesc(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="primary" link @click="goPenaltyDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
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
import { Plus } from '@element-plus/icons-vue'
import { getPermitById } from '@/api/permit'
import { getInspectionsByPermit } from '@/api/inspection'
import { getPenaltyCluesByPermit } from '@/api/penaltyClue'
import { getAuditTimeline, getEnums } from '@/api/common'
import { getUserInfo, formatDate, formatDateTime, formatNumber, getStatusColor } from '@/utils'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const permit = ref(null)
const inspections = ref([])
const penaltyClues = ref([])
const timeline = ref([])
const statusList = ref([])
const inspectionResultList = ref([])
const penaltyStatusList = ref([])
const userInfo = computed(() => getUserInfo() || {})
const isEnforcer = computed(() => userInfo.value.role === 'ENFORCER')
const isExpired = computed(() => {
  if (!permit.value) return false
  return new Date(permit.value.endDate) < new Date()
})

function getStatusDesc(status) {
  return statusList.value.find(s => s.name === status)?.description || status
}
function getInspectionResultDesc(status) {
  return inspectionResultList.value.find(s => s.name === status)?.description || status
}
function getPenaltyStatusDesc(status) {
  return penaltyStatusList.value.find(s => s.name === status)?.description || status
}
function calcProgress(used, total) {
  if (!total) return 0
  return Math.min(100, Math.round((used / total) * 100))
}
function goBack() {
  router.back()
}
function goCreateInspection() {
  router.push({ path: '/inspections/new', query: { permitId: permit.value.id } })
}
function goPenaltyDetail(id) {
  router.push(`/penalty-clues/${id}`)
}

async function loadData() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getPermitById(id)
    permit.value = res.data

    if (permit.value) {
      const inspRes = await getInspectionsByPermit(permit.value.id)
      inspections.value = inspRes.data || []
      const clueRes = await getPenaltyCluesByPermit(permit.value.id)
      penaltyClues.value = clueRes.data || []
    }

    const tlRes = await getAuditTimeline('PERMIT', id)
    timeline.value = tlRes.data || []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const enumRes = await getEnums()
  statusList.value = enumRes.data?.permitStatuses || []
  inspectionResultList.value = enumRes.data?.inspectionResults || []
  penaltyStatusList.value = enumRes.data?.penaltyStatuses || []
  loadData()
})
</script>
