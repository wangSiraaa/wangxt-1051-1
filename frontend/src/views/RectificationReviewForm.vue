<template>
  <div class="page-container">
    <div class="card" v-loading="loading">
      <div class="page-header">
        <h2>整改复查</h2>
      </div>

      <div v-if="detention" class="review-info" style="margin-bottom: 20px;">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="暂扣单号">{{ detention.detentionNo }}</el-descriptions-item>
          <el-descriptions-item label="许可证号">{{ detention.permitNo }}</el-descriptions-item>
          <el-descriptions-item label="申报单号">{{ detention.declarationNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="作业船只">{{ detention.vesselName }}</el-descriptions-item>
          <el-descriptions-item label="暂扣方量">{{ formatNumber(detention.detainedVolume) }} 方</el-descriptions-item>
          <el-descriptions-item label="暂扣时间">{{ formatDateTime(detention.detentionTime) }}</el-descriptions-item>
          <el-descriptions-item label="暂扣原因" :span="2">{{ detention.detentionReason }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-divider content-position="left">整改复查记录</el-divider>

      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="120px"
        style="max-width: 800px;"
      >
        <el-form-item label="整改要求" prop="rectificationRequirement">
          <el-input
            v-model="form.rectificationRequirement"
            type="textarea"
            :rows="3"
            placeholder="请填写整改要求"
            :disabled="isSubmitted"
          />
        </el-form-item>

        <el-form-item label="整改结果" prop="rectificationResult">
          <el-input
            v-model="form.rectificationResult"
            type="textarea"
            :rows="3"
            placeholder="请描述整改完成情况"
            :disabled="isSubmitted"
          />
        </el-form-item>

        <el-form-item label="是否整改合格" prop="isRectified">
          <el-radio-group v-model="form.isRectified" :disabled="isSubmitted">
            <el-radio :label="true">整改合格</el-radio>
            <el-radio :label="false">整改不合格</el-radio>
          </el-radio-group>
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            合格后可解除暂扣或确认违规；不合格需继续整改
          </div>
        </el-form-item>

        <el-form-item label="复查意见" prop="reviewOpinion">
          <el-input
            v-model="form.reviewOpinion"
            type="textarea"
            :rows="3"
            placeholder="请填写复查意见"
            :disabled="isSubmitted"
          />
        </el-form-item>

        <el-form-item label="现场照片">
          <el-input
            v-model="form.photoPlaceholders"
            type="textarea"
            :rows="2"
            placeholder="多个照片名用逗号分隔，例如：photo1.jpg,photo2.jpg"
            :disabled="isSubmitted"
          />
          <span style="color: #909399; font-size: 12px;">演示环境使用占位符代替实际照片</span>
        </el-form-item>

        <el-form-item v-if="form.remark" label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" :disabled="isSubmitted" />
        </el-form-item>

        <el-form-item v-if="!isSubmitted">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交复查
          </el-button>
        </el-form-item>
        <el-form-item v-else>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>

      <div v-if="reviewList.length" class="card" style="margin-top: 20px;">
        <div class="page-header">
          <h2>历史复查记录 ({{ reviewList.length }})</h2>
        </div>
        <el-timeline>
          <el-timeline-item
            v-for="item in reviewList"
            :key="item.id"
            :timestamp="formatDateTime(item.reviewTime)"
          >
            <el-card shadow="never" style="margin-bottom: 0;">
              <div style="margin-bottom: 8px;">
                <el-tag :type="item.isRectified ? 'success' : 'danger'">
                  {{ item.isRectified ? '整改合格' : '整改不合格' }}
                </el-tag>
                <span style="margin-left: 8px; color: #909399; font-size: 12px;">
                  复查人：{{ item.reviewerName }}
                </span>
              </div>
              <div v-if="item.rectificationRequirement" style="margin-bottom: 4px;">
                <strong>整改要求：</strong>{{ item.rectificationRequirement }}
              </div>
              <div v-if="item.rectificationResult" style="margin-bottom: 4px;">
                <strong>整改结果：</strong>{{ item.rectificationResult }}
              </div>
              <div v-if="item.reviewOpinion">
                <strong>复查意见：</strong>{{ item.reviewOpinion }}
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createRectificationReview, getRectificationReviews } from '@/api/rectificationReview'
import { getDetentionById } from '@/api/detention'
import { formatNumber, formatDateTime } from '@/utils'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const detention = ref(null)
const reviewList = ref([])
const isSubmitted = computed(() => reviewList.value.length > 0 && reviewList.value.some(r => r.isRectified))

const form = reactive({
  detentionId: null,
  penaltyClueId: null,
  rectificationRequirement: '',
  rectificationResult: '',
  isRectified: true,
  reviewOpinion: '',
  photoPlaceholders: '',
  remark: ''
})

const rules = {
  rectificationRequirement: [{ required: true, message: '请填写整改要求', trigger: 'blur' }],
  rectificationResult: [{ required: true, message: '请填写整改结果', trigger: 'blur' }],
  isRectified: [{ required: true, message: '请选择是否整改合格', trigger: 'change' }],
  reviewOpinion: [{ required: true, message: '请填写复查意见', trigger: 'blur' }]
}

function goBack() {
  router.back()
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await createRectificationReview(form)
    ElMessage.success('复查记录已提交')
    router.push('/detentions')
  } finally {
    submitting.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    const detentionId = route.params.id
    form.detentionId = detentionId

    const [detentionRes, reviewsRes] = await Promise.all([
      getDetentionById(detentionId),
      getRectificationReviews({ detentionId })
    ])
    detention.value = detentionRes.data
    reviewList.value = reviewsRes.data || []

    if (reviewList.value.length > 0) {
      const latest = reviewList.value[0]
      form.rectificationRequirement = latest.rectificationRequirement
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
