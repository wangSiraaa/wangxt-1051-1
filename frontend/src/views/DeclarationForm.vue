<template>
  <div class="page-container">
    <div class="card">
      <div class="page-header">
        <h2>{{ isEdit ? '申报详情' : '新建申报' }}</h2>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" style="max-width: 800px;" v-if="isEdit || permitList.length">
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
            :title="`剩余可用：${formatNumber(selectedPermit.remainingVolume)} 方 / 许可总量：${formatNumber(selectedPermit.permittedVolume)} 方`"
            type="info"
            :closable="false"
            style="margin-bottom: 20px;"
          >
            <template #default>
              <div>已采：{{ formatNumber(selectedPermit.usedVolume) }} 方</div>
              <div>冻结待查：{{ formatNumber(selectedPermit.frozenVolume) }} 方</div>
              <div>处罚中：{{ formatNumber(selectedPermit.penaltyVolume) }} 方</div>
            </template>
          </el-alert>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
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
          </el-col>
          <el-col :span="12">
            <el-form-item label="申报日期" prop="declarationDate">
              <el-date-picker
                v-model="form.declarationDate"
                type="date"
                placeholder="选择申报日期"
                style="width: 100%;"
                :disabled="isEdit"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="采区" prop="miningArea">
          <el-input v-model="form.miningArea" placeholder="例如：A区、B区等" :disabled="isEdit" />
        </el-form-item>

        <el-form-item label="申报方量" prop="declaredVolume">
          <el-input-number
            v-model="form.declaredVolume"
            :min="0"
            :precision="2"
            :step="10"
            style="width: 100%;"
            :disabled="isEdit"
          />
          <span style="color: #909399; font-size: 12px;">单位：方（立方米）</span>
        </el-form-item>

        <template v-if="isEdit && form.weighedVolume != null">
          <el-form-item label="称重方量">
            <el-input-number
              :model-value="form.weighedVolume"
              :precision="2"
              style="width: 100%;"
              disabled
            />
            <span style="color: #909399; font-size: 12px;">现场称重实际方量</span>
          </el-form-item>
        </template>

        <template v-if="isEdit && form.exceedVolume && form.exceedVolume > 0">
          <el-alert
            :title="`超采量：${formatNumber(form.exceedVolume)} 方`"
            type="error"
            :closable="false"
            style="margin-bottom: 20px;"
          />
        </template>

        <el-form-item v-if="!isEdit" label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>

        <el-form-item v-if="isEdit && form.verifyRemark" label="核验备注">
          <el-input :model-value="form.verifyRemark" type="textarea" :rows="2" disabled />
        </el-form-item>

        <el-form-item>
          <el-button @click="goBack">返回</el-button>
          <template v-if="!isEdit">
            <el-button :loading="submitting" @click="handleSave">
              保存草稿
            </el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              提交申报
            </el-button>
          </template>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createDeclaration, submitDeclaration, getDeclarationById } from '@/api/declaration'
import { getPermits, getPermitById } from '@/api/permit'
import { getVessels } from '@/api/common'
import { formatNumber, formatDate } from '@/utils'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const permitList = ref([])
const vesselList = ref([])
const selectedPermit = ref(null)
const isEdit = computed(() => !!route.params.id)

const form = reactive({
  permitId: null,
  vesselId: null,
  declarationDate: new Date().toISOString().split('T')[0],
  miningArea: '',
  declaredVolume: 0,
  remark: '',
  weighedVolume: null,
  exceedVolume: null,
  verifyRemark: ''
})

const rules = {
  permitId: [{ required: true, message: '请选择许可证', trigger: 'change' }],
  vesselId: [{ required: true, message: '请选择作业船只', trigger: 'change' }],
  declarationDate: [{ required: true, message: '请选择申报日期', trigger: 'change' }],
  declaredVolume: [{ required: true, message: '请输入申报方量', trigger: 'blur' }]
}

function onPermitChange(id) {
  selectedPermit.value = permitList.value.find(p => p.id === id) || null
}

function goBack() {
  router.back()
}

async function handleSave() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await createDeclaration(form)
    ElMessage.success('已保存草稿')
    router.push('/declarations')
  } finally {
    submitting.value = false
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const res = await createDeclaration(form)
    if (res.data?.id) {
      await submitDeclaration(res.data.id)
      ElMessage.success('申报已提交，请等待核验')
      router.push('/declarations')
    }
  } finally {
    submitting.value = false
  }
}

async function loadDetail(id) {
  const res = await getDeclarationById(id)
  const data = res.data
  Object.assign(form, data)
  if (data.permitId) {
    const permitRes = await getPermitById(data.permitId)
    selectedPermit.value = permitRes.data
  }
}

onMounted(async () => {
  const [permitsRes, vesselsRes] = await Promise.all([
    getPermits({ status: 'ACTIVE' }),
    getVessels()
  ])
  permitList.value = permitsRes.data || []
  vesselList.value = vesselsRes.data || []

  if (isEdit.value) {
    await loadDetail(route.params.id)
  }
})
</script>
