<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="card stat-card">
          <el-icon :size="32" color="#409eff"><Document /></el-icon>
          <div class="stat-number">{{ stats.applications }}</div>
          <div class="stat-label">许可申请总数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card stat-card">
          <el-icon :size="32" color="#67c23a"><Tickets /></el-icon>
          <div class="stat-number">{{ stats.activePermits }}</div>
          <div class="stat-label">有效许可证</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card stat-card">
          <el-icon :size="32" color="#e6a23c"><View /></el-icon>
          <div class="stat-number">{{ stats.inspections }}</div>
          <div class="stat-label">现场核查次数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card stat-card">
          <el-icon :size="32" color="#f56c6c"><Warning /></el-icon>
          <div class="stat-number">{{ stats.penaltyClues }}</div>
          <div class="stat-label">处罚线索</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="14">
        <div class="card">
          <div class="page-header">
            <h2>最近动态</h2>
          </div>
          <el-timeline v-if="timeline.length">
            <el-timeline-item
              v-for="item in timeline"
              :key="item.id"
              :timestamp="formatDateTime(item.operateTime)"
              :color="getTimelineColor(item.operation)"
            >
              <el-card shadow="never" style="margin-bottom: 0;">
                <strong>{{ item.operation }}</strong>
                <span style="margin-left: 8px; color: #909399; font-size: 12px;">
                  [{{ item.operator }} - {{ getRoleDesc(item.operatorRole) }}]
                </span>
                <div style="margin-top: 8px; color: #606266;">{{ item.detail }}</div>
                <div style="margin-top: 4px; color: #909399; font-size: 12px;">
                  业务编号：{{ item.businessNo }}
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无动态" />
        </div>
      </el-col>
      <el-col :span="10">
        <div class="card">
          <div class="page-header">
            <h2>许可额度概览</h2>
          </div>
          <el-table :data="quotas" stripe>
            <el-table-column prop="riverSectionName" label="河段" min-width="140" />
            <el-table-column prop="year" label="年度" width="80" />
            <el-table-column label="总额度" width="110">
              <template #default="{ row }">{{ formatNumber(row.totalQuota) }} 方</template>
            </el-table-column>
            <el-table-column label="已使用" width="110">
              <template #default="{ row }">{{ formatNumber(row.usedQuota) }} 方</template>
            </el-table-column>
            <el-table-column label="剩余额度" width="110">
              <template #default="{ row }">
                <span :style="{ color: row.remainingQuota < 10000 ? '#f56c6c' : '#67c23a' }">
                  {{ formatNumber(row.remainingQuota) }} 方
                </span>
              </template>
            </el-table-column>
          </el-table>

          <div style="margin-top: 24px;">
            <div class="page-header">
              <h2>禁采期</h2>
            </div>
            <el-table :data="forbiddenPeriods" stripe size="small">
              <el-table-column prop="startMonthDay" label="开始" width="100" />
              <el-table-column prop="endMonthDay" label="结束" width="100" />
              <el-table-column prop="description" label="说明" />
            </el-table>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getApplications, getPermits } from '@/api/application'
import { getInspections } from '@/api/inspection'
import { getPenaltyClues } from '@/api/penaltyClue'
import { getLatestAuditTimeline } from '@/api/common'
import { getAllQuotas, getForbiddenPeriods } from '@/api/quota'
import { formatDateTime, formatNumber } from '@/utils'

const stats = ref({
  applications: 0,
  activePermits: 0,
  inspections: 0,
  penaltyClues: 0
})
const timeline = ref([])
const quotas = ref([])
const forbiddenPeriods = ref([])

function getTimelineColor(op) {
  if (op.includes('通过') || op.includes('恢复') || op.includes('正常')) return '#67c23a'
  if (op.includes('退回') || op.includes('驳回') || op.includes('异常') || op.includes('暂停')) return '#f56c6c'
  if (op.includes('审核') || op.includes('提交')) return '#e6a23c'
  return '#409eff'
}

function getRoleDesc(role) {
  const map = {
    ENTERPRISE: '企业用户',
    AUDITOR: '审核员',
    ENFORCER: '执法人员',
    PENALTY_HANDLER: '处罚线索处理人',
    SYSTEM: '系统'
  }
  return map[role] || role
}

async function loadData() {
  try {
    const [apps, permits, insp, clues, tl, qt, fp] = await Promise.all([
      getApplications(),
      getPermits(),
      getInspections(),
      getPenaltyClues(),
      getLatestAuditTimeline(10),
      getAllQuotas(),
      getForbiddenPeriods()
    ])
    stats.value.applications = apps.data?.length || 0
    stats.value.activePermits = (permits.data || []).filter(p => p.status === 'ACTIVE').length
    stats.value.inspections = insp.data?.length || 0
    stats.value.penaltyClues = clues.data?.length || 0
    timeline.value = tl.data || []
    quotas.value = qt.data || []
    forbiddenPeriods.value = fp.data || []
  } catch (e) {
    console.error(e)
  }
}

onMounted(loadData)
</script>
