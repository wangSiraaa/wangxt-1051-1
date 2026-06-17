<template>
  <div class="page-container">
    <div class="card" v-loading="loading">
      <div class="page-header">
        <h2>现场称重核验</h2>
      </div>

      <div v-if="declaration" class="verify-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申报编号">{{ declaration.declarationNo }}</el-descriptions-item>
          <el-descriptions-item label="许可证号">{{ declaration.permitNo }}</el-descriptions-item>
          <el-descriptions-item label="作业船只">{{ declaration.vesselName }}</el-descriptions-item>
          <el-descriptions-item label="河段">{{ declaration.riverSectionName }}</el-descriptions-item>
          <el-descriptions-item label="采区">{{ declaration.miningArea || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申报日期">{{ formatDate(declaration.declarationDate) }}</el-descriptions-item>
          <el-descriptions-item label="申报方量">
            <span style="font-weight: bold;">{{ formatNumber(declaration.declaredVolume) }} 方</span>
          </el-descriptions-item>
          <el-descriptions-item label="持证企业">{{ declaration.holderName }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-divider content-position="left">现场称重核验</el-divider>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" style="max-width: 600px;">
        <el-form-item label="现场称重方量" prop="weighedVolume">
          <el-input-number
            v-model="form.weighedVolume"
            :min="0"
            :precision="2"
            :step="10"
            style="width: 100%;"
          />
          <span style="color: #909399; font-size: 12px;">单位：方（立方米）</span>
        </el-form-item>

        <el-form-item label="核验备注" prop="verifyRemark">
          <el-input v-model="form.verifyRemark" type="textarea" :rows="3" placeholder="请输入核验备注" />
        </el-form-item>

        <el-form-item label="现场照片">
          <el-input
            v-model="form.photoPlaceholders"
            type="textarea"
            :rows="2"
            placeholder="多个照片名用逗号分隔，例如：photo1.jpg,photo2.jpg"
          />
          <span style="color: #909399; font-size: 12px;">演示环境使用占位符代替实际照片</span>
        </el-form-item>

        <el-alert
          v-if="form.weighedVolume > 0 && selectedPermit && form.weighedVolume > selectedPermit.remainingVolume"
          title="注意：称重方量超过剩余可用额度，系统将自动标记为异常并生成处罚线索"
          type="warning"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div>超采量：{{ formatNumber(form.weighedVolume - selectedPermit.remainingVolume) }} 方</div>
            <div style="font-size: 12px; color: #909399; margin-top: 4px;">
              该处罚仅关联本船次，不影响许可证的其他合规船次正常作业
            </div>
          </template>
        </el-alert>

        <el-form-item>
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" :loading="submitting" @click="handleVerify">
            确认核验
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
import { getDeclarationById, verifyDeclaration } from '@/api/declaration'
import { getPermitById } from '@/api/permit'
import { formatDate, formatNumber } from '@/utils'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const declaration = ref(null)
const selectedPermit = ref(null)

const form = reactive({
  weighedVolume: 0,
  verifyRemark: '',
  photoPlaceholders: ''
})

const rules = {
  weighedVolume: [{ required: true, message: '请输入称重方量', trigger: 'blur' }]
}

function goBack() {
  router.back()
}

async function handleVerify() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await verifyDeclaration(route.params.id, {
      weighedVolume: form.weighedVolume,
      verifyRemark: form.verifyRemark
    })
    ElMessage.success('核验完成')
    if (form.weighedVolume > selectedPermit.value.remainingVolume) {
      ElMessage.warning('超量开采，已自动生成处罚线索')
    }
    router.push('/declarations')
  } finally {
    submitting.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getDeclarationById(id)
    declaration.value = res.data
    form.weighedVolume = res.data.declaredVolume || 0

    if (res.data.permitId) {
      const permitRes = await getPermitById(res.data.permitId)
      selectedPermit.value = permitRes.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.verify-info {
  margin-bottom: 20px;
}
</style>
