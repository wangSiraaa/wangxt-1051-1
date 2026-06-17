<template>
  <div class="page-container">
    <div class="card">
      <div class="page-header">
        <h2>新增现场核查</h2>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" style="max-width: 800px;">
        <el-form-item label="许可证" prop="permitId">
          <el-select
            v-model="form.permitId"
            placeholder="请选择许可证"
            filterable
            style="width: 100%;"
            @change="onPermitChange"
          >
            <el-option
              v-for="p in activePermits"
              :key="p.id"
              :label="`${p.permitNo} - ${p.holderName} - ${p.riverSectionName}`"
              :value="p.id"
            />
          </el-select>
        </el-form-item>

        <template v-if="form.permitId && selectedPermit">
          <el-alert
            :title="`许可剩余量：${formatNumber(selectedPermit.remainingVolume)} 方 / 总计：${formatNumber(selectedPermit.permittedVolume)} 方`"
            type="info"
            :closable="false"
            style="margin-bottom: 20px;"
          />
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number v-model="form.latitude" :precision="6" :step="0.0001" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number v-model="form.longitude" :precision="6" :step="0.0001" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="位置描述" prop="location">
          <el-input v-model="form.location" placeholder="例如：长江武汉段采砂点A区" />
        </el-form-item>

        <el-form-item label="实际采砂量" prop="actualVolume">
          <el-input-number v-model="form.actualVolume" :min="0" :precision="2" style="width: 100%;" />
          <span style="color: #909399; font-size: 12px;">单位：方（立方米）</span>
        </el-form-item>

        <el-form-item label="照片占位符">
          <el-input
            v-model="form.photoPlaceholders"
            type="textarea"
            :rows="2"
            placeholder="多个照片名用逗号分隔，例如：photo1.jpg,photo2.jpg"
          />
          <span style="color: #909399; font-size: 12px;">演示环境使用占位符代替实际照片</span>
        </el-form-item>

        <el-form-item label="核查结果">
          <el-radio-group v-model="form.result">
            <el-radio v-for="r in resultList" :key="r.name" :label="r.name">
              {{ r.description }}
            </el-radio>
          </el-radio-group>
          <span style="color: #e6a23c; margin-left: 12px; font-size: 12px;">
            注：若实际方量超过许可剩余量，系统将自动标记为异常并生成处罚线索
          </span>
        </el-form-item>

        <el-form-item v-if="form.result !== 'NORMAL'" label="异常描述" prop="abnormalDescription">
          <el-input v-model="form.abnormalDescription" type="textarea" :rows="3" placeholder="请描述异常情况" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>

        <el-form-item>
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交核查
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createInspection } from '@/api/inspection'
import { getPermits } from '@/api/permit'
import { getEnums } from '@/api/common'
import { formatNumber } from '@/utils'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const activePermits = ref([])
const resultList = ref([])
const selectedPermit = ref(null)

const form = reactive({
  permitId: null,
  latitude: 30.5,
  longitude: 114.3,
  location: '',
  actualVolume: 0,
  photoPlaceholders: '',
  result: 'NORMAL',
  abnormalDescription: '',
  remark: ''
})

const rules = {
  permitId: [{ required: true, message: '请选择许可证', trigger: 'change' }],
  latitude: [{ required: true, message: '请输入纬度', trigger: 'blur' }],
  longitude: [{ required: true, message: '请输入经度', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置描述', trigger: 'blur' }],
  actualVolume: [{ required: true, message: '请输入实际方量', trigger: 'blur' }]
}

function onPermitChange(id) {
  selectedPermit.value = activePermits.value.find(p => p.id === id) || null
}

function goBack() {
  router.back()
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.result !== 'NORMAL' && !form.abnormalDescription) {
    ElMessage.warning('请填写异常描述')
    return
  }
  submitting.value = true
  try {
    await createInspection(form)
    ElMessage.success('核查已提交')
    if (form.result === 'ABNORMAL') {
      ElMessage.warning('由于超量开采，已自动生成处罚线索')
    }
    router.push('/inspections')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const [permitsRes, enumRes] = await Promise.all([getPermits({ status: 'ACTIVE' }), getEnums()])
  activePermits.value = permitsRes.data || []
  resultList.value = enumRes.data?.inspectionResults || []

  if (route.query.permitId) {
    form.permitId = Number(route.query.permitId)
    onPermitChange(form.permitId)
  }
})
</script>
