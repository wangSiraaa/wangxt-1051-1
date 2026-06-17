<template>
  <div class="page-container">
    <div class="card">
      <div class="page-header">
        <h2>{{ isEdit ? '编辑申请' : '新建采砂许可申请' }}</h2>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" style="max-width: 900px;">
        <el-form-item label="申请企业">
          <el-input v-model="form.enterpriseName" disabled />
        </el-form-item>
        <el-form-item label="采砂河段" prop="riverSectionId">
          <el-select v-model="form.riverSectionId" placeholder="请选择河段" style="width: 100%;" @change="onSectionChange">
            <el-option v-for="s in sections" :key="s.id" :label="s.sectionName" :value="s.id" />
          </el-select>
          <div v-if="form.riverSectionId" style="margin-top: 8px;">
            <el-tag size="small">年度剩余额度：{{ formatNumber(remainingQuota) }} 方</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="作业船只" prop="vesselIds">
          <el-select
            v-model="selectedVessels"
            multiple
            filterable
            placeholder="请选择作业船只"
            style="width: 100%;"
            @change="onVesselChange"
          >
            <el-option v-for="v in vessels" :key="v.id" :label="v.vesselName + '(' + v.vesselNo + ')'" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="采砂时段" required>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
            @change="onDateChange"
          />
          <el-tag v-if="forbiddenOverlap" type="danger" size="small" style="margin-top: 8px;">
            <el-icon><Warning /></el-icon> 所选时段包含禁采期（3月1日-6月30日），无法提交
          </el-tag>
        </el-form-item>
        <el-form-item label="预计采砂量" prop="estimatedVolume">
          <el-input-number v-model="form.estimatedVolume" :min="1" :precision="2" style="width: 100%;" />
          <span style="color: #909399; font-size: 12px;">单位：方（立方米）</span>
        </el-form-item>
        <el-form-item label="保证金" prop="depositAmount">
          <el-input-number v-model="form.depositAmount" :min="0" :precision="2" style="width: 100%;" />
          <span style="color: #909399; font-size: 12px;">单位：元</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item>
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="handleSaveDraft">保存草稿</el-button>
          <el-button type="success" :disabled="forbiddenOverlap" @click="handleSaveAndSubmit">
            保存并提交
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { getRiverSections, getVessels } from '@/api/common'
import { createApplication, submitApplication } from '@/api/application'
import { getRemainingQuota, checkForbiddenOverlap } from '@/api/quota'
import { getUserInfo, formatNumber } from '@/utils'

const router = useRouter()
const formRef = ref(null)
const isEdit = ref(false)
const userInfo = getUserInfo()
const sections = ref([])
const vessels = ref([])
const selectedVessels = ref([])
const dateRange = ref([])
const remainingQuota = ref(0)
const forbiddenOverlap = ref(false)

const form = reactive({
  enterpriseName: userInfo?.enterpriseName || '',
  riverSectionId: null,
  riverSectionName: '',
  vesselIds: '',
  vesselNames: '',
  startDate: null,
  endDate: null,
  estimatedVolume: 1000,
  depositAmount: 10000,
  remark: ''
})

const rules = {
  riverSectionId: [{ required: true, message: '请选择河段', trigger: 'change' }],
  vesselIds: [{ required: true, message: '请选择船只', trigger: 'change' }],
  estimatedVolume: [{ required: true, message: '请输入预计方量', trigger: 'blur' }],
  depositAmount: [{ required: true, message: '请输入保证金', trigger: 'blur' }]
}

function onSectionChange(val) {
  const section = sections.value.find(s => s.id === val)
  form.riverSectionName = section?.sectionName || ''
  if (val) {
    loadRemainingQuota()
  }
}

function onVesselChange(vals) {
  const names = vals.map(id => vessels.value.find(v => v.id === id)?.vesselName).filter(Boolean)
  form.vesselIds = vals.join(',')
  form.vesselNames = names.join(',')
}

function onDateChange(dates) {
  if (dates && dates.length === 2) {
    form.startDate = dates[0]
    form.endDate = dates[1]
    checkOverlap()
  } else {
    form.startDate = null
    form.endDate = null
    forbiddenOverlap.value = false
  }
}

async function loadRemainingQuota() {
  const res = await getRemainingQuota(new Date().getFullYear(), form.riverSectionId)
  remainingQuota.value = res.data || 0
}

async function checkOverlap() {
  if (form.startDate && form.endDate) {
    const res = await checkForbiddenOverlap(form.startDate, form.endDate)
    forbiddenOverlap.value = res.data?.hasOverlap || false
  }
}

function goBack() {
  router.back()
}

async function handleSaveDraft() {
  await formRef.value.validate()
  const res = await createApplication(form, userInfo.userId, userInfo.realName || userInfo.username)
  if (res.code === 200) {
    ElMessage.success('草稿已保存')
    router.push(`/applications/${res.data.id}`)
  }
}

async function handleSaveAndSubmit() {
  if (forbiddenOverlap.value) {
    ElMessage.error('所选时段包含禁采期，无法提交')
    return
  }
  await formRef.value.validate()
  const res = await createApplication(form, userInfo.userId, userInfo.realName || userInfo.username)
  if (res.code === 200) {
    await submitApplication(res.data.id)
    ElMessage.success('已提交审核')
    router.push(`/applications/${res.data.id}`)
  }
}

onMounted(async () => {
  const [sRes, vRes] = await Promise.all([getRiverSections(), getVessels(userInfo?.userId)])
  sections.value = sRes.data || []
  vessels.value = vRes.data || []
})
</script>
