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
            <span class="detail-label">许可总量：</span>
            <span class="detail-value" style="font-weight: bold; color: #409eff;">
              {{ formatNumber(permit.permittedVolume) }} 方
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">已采：</span>
            <span class="detail-value" style="color: #67c23a;">
              {{ formatNumber(permit.usedVolume) }} 方
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">冻结待查：</span>
            <span class="detail-value" style="color: #e6a23c;">
              {{ formatNumber(permit.frozenVolume) }} 方
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">处罚中：</span>
            <span class="detail-value" style="color: #f56c6c;">
              {{ formatNumber(permit.penaltyVolume) }} 方
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">剩余可用：</span>
            <span class="detail-value">
              <span
                :style="{
                  color: permit.remainingVolume < 500 ? '#f56c6c' : '#67c23a',
                  fontWeight: 'bold'
                }"
              >
                {{ formatNumber(permit.remainingVolume) }} 方
              </span>
            </span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="4">
          <div class="quota-card quota-total">
            <div class="quota-label">许可总量</div>
            <div class="quota-value">{{ formatNumber(permit.permittedVolume) }}</div>
            <div class="quota-unit">方</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="quota-card quota-used">
            <div class="quota-label">已采</div>
            <div class="quota-value">{{ formatNumber(permit.usedVolume) }}</div>
            <div class="quota-unit">方 ({{ calcProgress(permit.usedVolume, permit.permittedVolume) }}%)</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="quota-card quota-frozen">
            <div class="quota-label">冻结待查</div>
            <div class="quota-value">{{ formatNumber(permit.frozenVolume) }}</div>
            <div class="quota-unit">方 ({{ calcProgress(permit.frozenVolume, permit.permittedVolume) }}%)</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="quota-card quota-penalty">
            <div class="quota-label">处罚中</div>
            <div class="quota-value">{{ formatNumber(permit.penaltyVolume) }}</div>
            <div class="quota-unit">方 ({{ calcProgress(permit.penaltyVolume, permit.permittedVolume) }}%)</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="quota-card quota-remaining">
            <div class="quota-label">剩余可用</div>
            <div class="quota-value">{{ formatNumber(permit.remainingVolume) }}</div>
            <div class="quota-unit">方 ({{ calcProgress(permit.remainingVolume, permit.permittedVolume) }}%)</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="quota-card quota-available">
            <div class="quota-label">使用率</div>
            <div class="quota-value">{{ calcUsedPercent() }}</div>
            <div class="quota-unit">%</div>
          </div>
        </el-col>
      </el-row>

      <el-progress
        :percentage="calcUsedPercent()"
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
        <h2>采砂申报记录 ({{ declarations.length }})</h2>
        <el-button
          v-if="isEnterprise && permit && permit.status === 'ACTIVE' && !isExpired"
          type="primary" @click="goCreateDeclaration"
        >
          <el-icon><Plus /></el-icon> 新建申报
        </el-button>
      </div>
      <el-alert
        v-if="declarations.some(d => d.status === 'ABNORMAL')"
        title="存在异常船次申报，已生成处罚线索，但不影响其他合规船次的正常作业"
        type="warning"
        :closable="false"
        style="margin-bottom: 12px;"
      />
      <el-table :data="declarations" stripe>
        <el-table-column prop="declarationNo" label="申报编号" width="160" />
        <el-table-column prop="vesselName" label="作业船只" width="140" />
        <el-table-column prop="riverSectionName" label="河段" width="140" />
        <el-table-column prop="miningArea" label="采区" width="100" />
        <el-table-column prop="declarationDate" label="申报日期" width="120">
          <template #default="{ row }">{{ formatDate(row.declarationDate) }}</template>
        </el-table-column>
        <el-table-column label="申报方量" width="110">
          <template #default="{ row }">{{ formatNumber(row.declaredVolume) }} 方</template>
        </el-table-column>
        <el-table-column label="称重方量" width="110">
          <template #default="{ row }">
            {{ row.weighedVolume != null ? formatNumber(row.weighedVolume) + ' 方' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="超采量" width="100">
          <template #default="{ row }">
            <span v-if="row.exceedVolume && row.exceedVolume > 0" style="color: #f56c6c;">
              {{ formatNumber(row.exceedVolume) }} 方
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor('declaration', row.status)">
              {{ getDeclarationStatusDesc(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDeclarationDetail(row.id)">详情</el-button>
            <el-button
              v-if="isEnforcer && row.status === 'SUBMITTED'"
              type="warning" link @click="goDeclarationVerify(row.id)"
            >
              核验
            </el-button>
            <el-button
              v-if="row.hasPenalty && row.relatedPenaltyId"
              type="danger" link @click="goPenaltyDetail(row.relatedPenaltyId)"
            >
              处罚线索
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" style="margin-bottom: 20px;">
      <div class="toolbar">
        <h2>现场暂扣记录 ({{ detentions.length }})</h2>
        <el-button
          v-if="isEnforcer && permit && permit.status === 'ACTIVE' && !isExpired"
          type="primary" @click="goCreateDetention"
        >
          <el-icon><Plus /></el-icon> 新增暂扣
        </el-button>
      </div>
      <el-table :data="detentions" stripe>
        <el-table-column prop="detentionNo" label="暂扣单号" width="160" />
        <el-table-column prop="vesselName" label="船只" width="120" />
        <el-table-column prop="declarationNo" label="关联申报" width="160">
          <template #default="{ row }">{{ row.declarationNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="暂扣方量" width="110">
          <template #default="{ row }">{{ formatNumber(row.detainedVolume) }} 方</template>
        </el-table-column>
        <el-table-column prop="detentionReason" label="暂扣原因" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusColor('detention', row.status)">
              {{ getDetentionStatusDesc(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="officerName" label="执法人员" width="100" />
        <el-table-column prop="detentionTime" label="暂扣时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.detentionTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetentionDetail(row.id)">详情</el-button>
            <el-button
              v-if="isEnforcer && row.status === 'RECTIFYING'"
              type="success" link @click="goReview(row.id)"
            >
              复查
            </el-button>
          </template>
        </el-table-column>
      </el-table>
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
        <el-table-column prop="clueSource" label="来源" width="120" />
        <el-table-column label="超采量" width="110">
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
import { getDeclarations } from '@/api/declaration'
import { getDetections } from '@/api/detention'
import { getInspectionsByPermit } from '@/api/inspection'
import { getPenaltyCluesByPermit } from '@/api/penaltyClue'
import { getAuditTimeline, getEnums } from '@/api/common'
import { getUserInfo, formatDate, formatDateTime, formatNumber, getStatusColor } from '@/utils'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const permit = ref(null)
const declarations = ref([])
const detentions = ref([])
const inspections = ref([])
const penaltyClues = ref([])
const timeline = ref([])
const statusList = ref([])
const declarationStatusList = ref([])
const detentionStatusList = ref([])
const inspectionResultList = ref([])
const penaltyStatusList = ref([])
const userInfo = computed(() => getUserInfo() || {})
const isEnforcer = computed(() => userInfo.value.role === 'ENFORCER')
const isEnterprise = computed(() => userInfo.value.role === 'ENTERPRISE')
const isExpired = computed(() => {
  if (!permit.value) return false
  return new Date(permit.value.endDate) < new Date()
})

function getStatusDesc(status) {
  return statusList.value.find(s => s.name === status)?.description || status
}
function getDeclarationStatusDesc(status) {
  return declarationStatusList.value.find(s => s.name === status)?.description || status
}
function getDetentionStatusDesc(status) {
  return detentionStatusList.value.find(s => s.name === status)?.description || status
}
function getInspectionResultDesc(status) {
  return inspectionResultList.value.find(s => s.name === status)?.description || status
}
function getPenaltyStatusDesc(status) {
  return penaltyStatusList.value.find(s => s.name === status)?.description || status
}
function calcProgress(value, total) {
  if (!total) return 0
  return Math.round((value / total) * 100)
}
function calcUsedPercent() {
  if (!permit.value) return 0
  const used = permit.value.usedVolume
  const frozen = permit.value.frozenVolume
  const penalty = permit.value.penaltyVolume
  const total = permit.value.permittedVolume
  if (!total) return 0
  return Math.min(100, Math.round(((used + frozen + penalty) / total) * 100))
}
function goBack() {
  router.back()
}
function goCreateDeclaration() {
  router.push({ path: '/declarations/new', query: { permitId: permit.value.id } })
}
function goDeclarationDetail(id) {
  router.push(`/declarations/${id}`)
}
function goDeclarationVerify(id) {
  router.push(`/declarations/${id}/verify`)
}
function goCreateDetention() {
  router.push({ path: '/detentions/new', query: { permitId: permit.value.id } })
}
function goDetentionDetail(id) {
  router.push(`/detentions/${id}`)
}
function goReview(id) {
  router.push(`/detentions/${id}/review`)
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
      const [declRes, detRes, inspRes, clueRes] = await Promise.all([
        getDeclarations({ permitId: permit.value.id }),
        getDetections({ permitId: permit.value.id }),
        getInspectionsByPermit(permit.value.id),
        getPenaltyCluesByPermit(permit.value.id)
      ])
      declarations.value = declRes.data || []
      detentions.value = detRes.data || []
      inspections.value = inspRes.data || []
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
  declarationStatusList.value = enumRes.data?.declarationStatuses || []
  detentionStatusList.value = enumRes.data?.detentionStatuses || []
  inspectionResultList.value = enumRes.data?.inspectionResults || []
  penaltyStatusList.value = enumRes.data?.penaltyStatuses || []
  loadData()
})
</script>

<style scoped>
.quota-card {
  padding: 16px;
  border-radius: 8px;
  text-align: center;
  color: #fff;
}
.quota-total {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}
.quota-used {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}
.quota-frozen {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
}
.quota-penalty {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}
.quota-remaining {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
}
.quota-available {
  background: linear-gradient(135deg, #9b59b6 0%, #a569bd 100%);
}
.quota-label {
  font-size: 13px;
  opacity: 0.9;
  margin-bottom: 4px;
}
.quota-value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1.2;
}
.quota-unit {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 4px;
}
</style>
