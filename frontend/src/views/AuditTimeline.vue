<template>
  <div class="page-container">
    <div class="card">
      <div class="toolbar">
        <h2>审计时间线</h2>
        <div>
          <el-select v-model="filters.businessType" placeholder="业务类型" clearable style="width: 180px; margin-right: 10px;">
            <el-option label="许可申请" value="APPLICATION" />
            <el-option label="许可证" value="PERMIT" />
            <el-option label="现场核查" value="INSPECTION" />
            <el-option label="处罚线索" value="PENALTY_CLUE" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        </div>
      </div>

      <el-timeline v-if="list.length" class="timeline-wrapper">
        <el-timeline-item
          v-for="item in list"
          :key="item.id"
          :timestamp="formatDateTime(item.operateTime)"
          :color="getColor(item)"
        >
          <el-card shadow="never" style="margin-bottom: 0;">
            <el-row :gutter="10">
              <el-col :span="4">
                <el-tag size="small" :type="getBusinessTypeTag(item.businessType)">
                  {{ getBusinessTypeDesc(item.businessType) }}
                </el-tag>
              </el-col>
              <el-col :span="16">
                <strong>{{ item.operation }}</strong>
                <span style="margin-left: 8px; color: #909399; font-size: 12px;">
                  {{ item.businessNo }}
                </span>
                <div style="margin-top: 6px; color: #606266;">{{ item.detail }}</div>
              </el-col>
              <el-col :span="4" style="text-align: right;">
                <el-tag size="small" type="info">{{ getRoleDesc(item.operatorRole) }}</el-tag>
                <div style="margin-top: 4px; color: #909399; font-size: 12px;">{{ item.operator }}</div>
              </el-col>
            </el-row>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无操作记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getLatestAuditTimeline } from '@/api/common'
import { formatDateTime } from '@/utils'

const loading = ref(false)
const list = ref([])

const filters = reactive({
  businessType: null
})

function getColor(item) {
  if (item.operation.includes('通过') || item.operation.includes('恢复') || item.operation.includes('正常') || item.operation.includes('发放')) return '#67c23a'
  if (item.operation.includes('退回') || item.operation.includes('驳回') || item.operation.includes('异常') || item.operation.includes('暂停') || item.operation.includes('处罚')) return '#f56c6c'
  if (item.operation.includes('审核') || item.operation.includes('提交') || item.operation.includes('变更')) return '#e6a23c'
  return '#409eff'
}

function getBusinessTypeDesc(type) {
  const map = {
    APPLICATION: '许可申请',
    PERMIT: '许可证',
    INSPECTION: '现场核查',
    PENALTY_CLUE: '处罚线索'
  }
  return map[type] || type
}

function getBusinessTypeTag(type) {
  const map = {
    APPLICATION: '',
    PERMIT: 'success',
    INSPECTION: 'warning',
    PENALTY_CLUE: 'danger'
  }
  return map[type] || 'info'
}

function getRoleDesc(role) {
  const map = {
    ENTERPRISE: '企业用户',
    AUDITOR: '审核员',
    ENFORCER: '执法人员',
    PENALTY_HANDLER: '处罚处理人',
    SYSTEM: '系统'
  }
  return map[role] || role
}

async function loadData() {
  loading.value = true
  try {
    const res = await getLatestAuditTimeline(50)
    let data = res.data || []
    if (filters.businessType) {
      data = data.filter(item => item.businessType === filters.businessType)
    }
    list.value = data
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
