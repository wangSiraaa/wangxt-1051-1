<template>
  <div class="page-container" v-loading="loading">
    <div v-if="application">
      <div class="card" style="margin-bottom: 20px;">
        <div class="toolbar">
          <div>
            <h2>申请详情 - {{ application.applicationNo }}</h2>
            <el-tag :type="getStatusColor('application', application.status)" style="margin-left: 12px;">
              {{ getStatusDesc(application.status) }}
            </el-tag>
          </div>
          <div>
            <el-button @click="goBack">返回</el-button>
            <el-button
              v-if="isEnterprise && (application.status === 'DRAFT' || application.status === 'RETURNED')"
              type="success" @click="handleSubmit"
            >
              提交审核
            </el-button>
            <el-button
              v-if="isEnterprise && (application.status === 'APPROVED' || application.status === 'CHANGED')"
              type="primary" @click="showChange = true"
            >
              变更许可
            </el-button>
            <el-button
              v-if="isAuditor && application.status === 'PENDING_REVIEW'"
              type="success" @click="handleReview(true)"
            >
              审核通过
            </el-button>
            <el-button
              v-if="isAuditor && application.status === 'PENDING_REVIEW'"
              type="danger" @click="handleReview(false)"
            >
              审核退回
            </el-button>
            <el-button
              v-if="isAuditor && (application.status === 'APPROVED' || application.status === 'CHANGED')"
              type="warning" @click="handleSuspend"
            >
              暂停许可
            </el-button>
            <el-button
              v-if="isAuditor && application.status === 'SUSPENDED'"
              type="success" @click="handleResume"
            >
              恢复许可
            </el-button>
          </div>
        </div>

        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <span class="detail-label">申请企业：</span>
              <span class="detail-value">{{ application.enterpriseName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">采砂河段：</span>
              <span class="detail-value">{{ application.riverSectionName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">作业船只：</span>
              <span class="detail-value">{{ application.vesselNames || '-' }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <span class="detail-label">采砂时段：</span>
              <span class="detail-value">{{ formatDate(application.startDate) }} 至 {{ formatDate(application.endDate) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">预计方量：</span>
              <span class="detail-value">{{ formatNumber(application.estimatedVolume) }} 方</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">保证金：</span>
              <span class="detail-value">{{ formatNumber(application.depositAmount) }} 元</span>
            </div>
          </el-col>
        </el-row>

        <div class="detail-item">
          <span class="detail-label">备注：</span>
          <span class="detail-value">{{ application.remark || '-' }}</span>
        </div>

        <div v-if="application.reviewOpinion" class="detail-item">
          <span class="detail-label">审核意见：</span>
          <span class="detail-value">{{ application.reviewOpinion }}</span>
        </div>
      </div>

      <div v-if="permit" class="card" style="margin-bottom: 20px;">
        <div class="page-header">
          <h2>关联许可证 - {{ permit.permitNo }}</h2>
          <el-tag :type="getStatusColor('permit', permit.status)" style="margin-left: 12px;">
            {{ getPermitStatusDesc(permit.status) }}
          </el-tag>
          <el-button type="primary" link style="margin-left: auto;" @click="goPermitDetail">
            查看详情
          </el-button>
        </div>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="detail-item">
              <span class="detail-label">许可方量：</span>
              <span class="detail-value">{{ formatNumber(permit.permittedVolume) }} 方</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="detail-item">
              <span class="detail-label">已使用：</span>
              <span class="detail-value">{{ formatNumber(permit.usedVolume) }} 方</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="detail-item">
              <span class="detail-label">剩余：</span>
              <span class="detail-value">
                <span :style="{ color: permit.remainingVolume < 500 ? '#f56c6c' : '' }">
                  {{ formatNumber(permit.remainingVolume) }} 方
                </span>
              </span>
            </div>
          </el-col>
        </el-row>
      </div>

      <div v-if="inspections.length" class="card" style="margin-bottom: 20px;">
        <div class="page-header">
          <h2>现场核查记录 ({{ inspections.length }})</h2>
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
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        </el-table>
      </div>

      <div v-if="penaltyClues.length" class="card" style="margin-bottom: 20px;">
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
            <span style="margin-left: 8px; color: #909399; font-size: 12px;">
              [{{ item.operator }}]
            </span>
            <div style="margin-top: 4px; color: #606266;">{{ item.detail }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无操作记录" />
      </div>
    </div>

    <el-dialog v-model="showChange" title="变更许可信息" width="600px">
      <el-form :model="changeForm" :rules="changeRules" ref="changeFormRef" label-width="120px">
        <el-form-item label="变更后时段" required>
          <el-date-picker
            v-model="changeDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
            @change="onChangeDate"
          />
        </el-form-item>
        <el-form-item label="变更后方量" prop="estimatedVolume">
          <el-input-number v-model="changeForm.estimatedVolume" :min="1" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="变更船只">
          <el-select
            v-model="changeSelectedVessels"
            multiple
            filterable
            placeholder="请选择作业船只"
            style="width: 100%;"
            @change="onChangeVessel"
          >
            <el-option v-for="v in vessels" :key="v.id" :label="v.vesselName + '(' + v.vesselNo + ')'" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更说明" prop="remark">
          <el-input v-model="changeForm.remark" type="textarea" :rows="3" placeholder="请填写变更原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChange = false">取消</el-button>
        <el-button type="primary" @click="confirmChange">确认变更</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApplicationById, submitApplication, reviewApplication, changeApplication, suspendApplication, resumeApplication } from '@/api/application'
import { getPermitByApplicationId } from '@/api/permit'
import { getInspectionsByPermit } from '@/api/inspection'
import { getPenaltyCluesByPermit } from '@/api/penaltyClue'
import { getAuditTimeline, getRiverSections, getVessels, getEnums } from '@/api/common'
import { getUserInfo, formatDate, formatDateTime, formatNumber, getStatusColor } from '@/utils'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const application = ref(null)
const permit = ref(null)
const inspections = ref([])
const penaltyClues = ref([])
const timeline = ref([])
const statusList = ref([])
const permitStatusList = ref([])
const inspectionResultList = ref([])
const penaltyStatusList = ref([])
const sections = ref([])
const vessels = ref([])
const showChange = ref(false)
const changeFormRef = ref(null)
const changeDateRange = ref([])
const changeSelectedVessels = ref([])

const userInfo = computed(() => getUserInfo() || {})
const isEnterprise = computed(() => userInfo.value.role === 'ENTERPRISE')
const isAuditor = computed(() => userInfo.value.role === 'AUDITOR')

const changeForm = reactive({
  startDate: null,
  endDate: null,
  estimatedVolume: 0,
  vesselIds: '',
  vesselNames: '',
  remark: ''
})

const changeRules = {
  estimatedVolume: [{ required: true, message: '请输入预计方量', trigger: 'blur' }],
  remark: [{ required: true, message: '请填写变更说明', trigger: 'blur' }]
}

function getStatusDesc(status) {
  return statusList.value.find(s => s.name === status)?.description || status
}
function getPermitStatusDesc(status) {
  return permitStatusList.value.find(s => s.name === status)?.description || status
}
function getInspectionResultDesc(status) {
  return inspectionResultList.value.find(s => s.name === status)?.description || status
}
function getPenaltyStatusDesc(status) {
  return penaltyStatusList.value.find(s => s.name === status)?.description || status
}

function goBack() {
  router.back()
}
function goPermitDetail() {
  router.push(`/permits/${permit.value.id}`)
}
function goPenaltyDetail(id) {
  router.push(`/penalty-clues/${id}`)
}

function onChangeDate(dates) {
  if (dates && dates.length === 2) {
    changeForm.startDate = dates[0]
    changeForm.endDate = dates[1]
  }
}
function onChangeVessel(vals) {
  const names = vals.map(id => vessels.value.find(v => v.id === id)?.vesselName).filter(Boolean)
  changeForm.vesselIds = vals.join(',')
  changeForm.vesselNames = names.join(',')
}

async function handleSubmit() {
  await ElMessageBox.confirm('确定提交审核吗？', '提示', { type: 'warning' })
  await submitApplication(application.value.id)
  ElMessage.success('提交成功')
  loadData()
}

async function handleReview(approved) {
  const title = approved ? '审核通过' : '审核退回'
  const { value } = await ElMessageBox.prompt(`请输入${title}意见`, title, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: approved ? 'textarea' : 'textarea',
    inputPattern: /.+/,
    inputErrorMessage: '请输入审核意见'
  })
  await reviewApplication(application.value.id, { approved, opinion: value })
  ElMessage.success(approved ? '审核通过' : '已退回')
  loadData()
}

async function handleSuspend() {
  const { value } = await ElMessageBox.prompt('请输入暂停原因', '暂停许可', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '请输入暂停原因'
  })
  await suspendApplication(application.value.id, value)
  ElMessage.success('已暂停')
  loadData()
}

async function handleResume() {
  await ElMessageBox.confirm('确定恢复该许可吗？', '提示', { type: 'warning' })
  await resumeApplication(application.value.id)
  ElMessage.success('已恢复')
  loadData()
}

async function confirmChange() {
  await changeFormRef.value.validate()
  if (!changeForm.startDate || !changeForm.endDate) {
    ElMessage.error('请选择变更后时段')
    return
  }
  await changeApplication(application.value.id, changeForm)
  ElMessage.success('变更成功')
  showChange.value = false
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getApplicationById(id)
    application.value = res.data

    if (application.value) {
      changeForm.startDate = application.value.startDate
      changeForm.endDate = application.value.endDate
      changeForm.estimatedVolume = application.value.estimatedVolume
      changeDateRange.value = [application.value.startDate, application.value.endDate]
      changeForm.vesselIds = application.value.vesselIds
      changeForm.vesselNames = application.value.vesselNames
      changeSelectedVessels.value = application.value.vesselIds
        ? application.value.vesselIds.split(',').map(Number)
        : []
    }

    try {
      const permitRes = await getPermitByApplicationId(id)
      permit.value = permitRes.data
      if (permit.value) {
        const inspRes = await getInspectionsByPermit(permit.value.id)
        inspections.value = inspRes.data || []
        const clueRes = await getPenaltyCluesByPermit(permit.value.id)
        penaltyClues.value = clueRes.data || []
      }
    } catch (e) {}

    const tlRes = await getAuditTimeline('APPLICATION', id)
    timeline.value = tlRes.data || []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const enumRes = await getEnums()
  statusList.value = enumRes.data?.applicationStatuses || []
  permitStatusList.value = enumRes.data?.permitStatuses || []
  inspectionResultList.value = enumRes.data?.inspectionResults || []
  penaltyStatusList.value = enumRes.data?.penaltyStatuses || []
  const [sRes, vRes] = await Promise.all([getRiverSections(), getVessels()])
  sections.value = sRes.data || []
  vessels.value = vRes.data || []
  loadData()
})
</script>
