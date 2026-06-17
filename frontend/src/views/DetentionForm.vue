<template>
  <div class="page-container">
    <div class="card" v-loading="loading">
      <div class="page-header">
        <h2>{{ isEdit ? '暂扣详情' : '新增现场暂扣' }}</h2>
      </div>

      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="120px"
        style="max-width: 800px;"
        v-if="isEdit || permitList.length"
      >
        <el-form-item label="许可证" prop="permitId">
          <el-select
            v-model="form.permitId"
            placeholder="请选择许可证"
            filterable
            style="width: 100%;"
            :disabled="isEdit"
            @change="onPermitChange"
          >
            <el-option
              v-for="p in permitList"
              :key="p.id"
              :label="`${p.permitNo} - ${p.holderName} - ${p.riverSectionName}`"
              :value="p.id"
            />
          </el-select>
        </el-form-item>

        <template v-if="form.permitId && selectedPermit">
          <el-alert
            :title="`额度概览 - 许可：${formatNumber(selectedPermit.permittedVolume)} 方 / 已采：${formatNumber(selectedPermit.usedVolume)} 方 / 冻结：${formatNumber(selectedPermit.frozenVolume)} 方 / 处罚：${formatNumber(selectedPermit.penaltyVolume)} 方 / 剩余：${formatNumber(selectedPermit.remainingVolume)} 方`"
            type="info"
            :closable="false"
            style="margin-bottom: 20px;"
          />
        </template>

        <el-form-item label="关联申报单" prop="declarationId">
          <el-select
            v-model="form.declarationId"
            placeholder="可选，选择关联的申报单"
            filterable
            clearable
            style="width: 100%;"
            :disabled="isEdit"
          >
            <el-option
              v-for="d in declarationList"
              :key="d.id"
              :label="`${d.declarationNo} - ${d.vesselName} - ${formatNumber(d.declaredVolume)}方`"
              :value="d.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="作业船只" prop="vesselId">
          <el-select
            v-model="form.vesselId"
            placeholder="请选择作业船只"
            filterable
            style="width: 100%;"
            :disabled="isEdit"
          >
            <el-option
              v-for="v in vesselList"
              :key="v.id"
              :label="`${v.vesselName} (${v.vesselNo})`"
              :value="v.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="暂扣方量" prop="detainedVolume">
          <el-input-number
            v-model="form.detainedVolume"
            :min="0"
            :precision="2"
            :step="10"
            style="width: 100%;"
            :disabled="isEdit"
          />
          <span style="color: #909399; font-size: 12px;">单位：方（立方米），该方量将被冻结待查</span>
        </el-form-item>

        <el-form-item label="暂扣原因" prop="detentionReason">
          <el-input
            v-model="form.detentionReason"
            type="textarea"
            :rows="3"
            placeholder="例如：涉嫌超量开采、证件不齐、违规作业等"
            :disabled="isEdit"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number
                v-model="form.latitude"
                :precision="6"
                :step="0.0001"
                style="width: 100%;"
                :disabled="isEdit"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number
                v-model="form.longitude"
                :precision="6"
                :step="0.0001"
                style="width: 100%;"
                :disabled="isEdit"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="位置描述" prop="location">
          <el-input
            v-model="form.location"
            placeholder="例如：长江武汉段A区采砂点"
            :disabled="isEdit"
          />
        </el-form-item>

        <el-form-item label="现场照片">
          <el-input
            v-model="form.photoPlaceholders"
            type="textarea"
            :rows="2"
            placeholder="多个照片名用逗号分隔，例如：photo1.jpg,photo2.jpg"
            :disabled="isEdit"
          />
          <span style="color: #909399; font-size: 12px;">演示环境使用占位符代替实际照片</span>
        </el-form-item>

        <template v-if="isEdit">
          <el-form-item label="当前状态">
            <el-tag :type="getStatusColor('detention', form.status)">
              {{ getStatusDesc(form.status) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="执法人员">
            <span>{{ form.officerName }}</span>
          </el-form-item>
          <el-form-item label="暂扣时间">
            <span>{{ formatDateTime(form.detentionTime) }}</span>
          </el-form-item>
        </template>

        <el-form-item v-if="!isEdit" label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item v-else-if="form.remark" label="备注">
          <el-input :model-value="form.remark" type="textarea" :rows="2" disabled />
        </el-form-item>

        <el-form-item>
          <el-button @click="goBack">返回</el-button>
          <el-button
            v-if="!isEdit"
            type="primary"
            :loading="submitting"
            @click="handleSubmit"
          >
            提交暂扣
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createDetention, getDetentionById } from '@/api/detention'
import { getPermits, getPermitById } from '@/api/permit'
import { getDeclarations } from '@/api/declaration'
import { getVessels, getEnums } from '@/api/common'
import { formatNumber, formatDateTime, getStatusColor } from '@/utils'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const permitList = ref([])
const vesselList = ref([])
const declarationList = ref([])
const selectedPermit = ref(null)
const statusList = ref([])
const isEdit = computed(() => !!route.params.id)

const form = reactive({
  permitId: null,
  declarationId: null,
  vesselId: null,
  detainedVolume: 0,
  detentionReason: '',
  latitude: 30.5,
  longitude: 114.3,
  location: '',
  photoPlaceholders: '',
  remark: '',
  status: '',
  officerName: '',
  detentionTime: null
})

const rules = {
  permitId: [{ required: true, message: '请选择许可证', trigger: 'change' }],
  vesselId: [{ required: true, message: '请选择作业船只', trigger: 'change' }],
  detainedVolume: [{ required: true, message: '请输入暂扣方量', trigger: 'blur' }],
  detentionReason: [{ required: true, message: '请输入暂扣原因', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置描述', trigger: 'blur' }]
}

function getStatusDesc(status) {
  return statusList.value.find(s => s.name === status)?.description || status
}

function onPermitChange(id) {
  selectedPermit.value = permitList.value.find(p => p.id === id) || null
  loadDeclarations(id)
}

async function loadDeclarations(permitId) {
  if (!permitId) {
    declarationList.value = []
    return
  }
  try {
    const res = await getDeclarations({ permitId })
    declarationList.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

function goBack() {
  router.back()
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await createDetention(form)
    ElMessage.success('暂扣记录已提交，相关方量已冻结待查')
    router.push('/detentions')
  } finally {
    submitting.value = false
  }
}

async function loadDetail(id) {
  loading.value = true
  try {
    const res = await getDetentionById(id)
    const data = res.data
    Object.assign(form, data)
    if (data.permitId) {
      const permitRes = await getPermitById(data.permitId)
      selectedPermit.value = permitRes.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const [permitsRes, vesselsRes, enumRes] = await Promise.all([
    getPermits({ status: 'ACTIVE' }),
    getVessels(),
    getEnums()
  ])
  permitList.value = permitsRes.data || []
  vesselList.value = vesselsRes.data || []
  statusList.value = enumRes.data?.detentionStatuses || []

  if (isEdit.value) {
    await loadDetail(route.params.id)
  } else if (route.query.permitId) {
    form.permitId = Number(route.query.permitId)
    onPermitChange(form.permitId)
  }
})
</script>
