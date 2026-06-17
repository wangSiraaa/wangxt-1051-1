<template>
  <div class="page-container">
    <div class="card">
      <div class="toolbar">
        <h2>采砂申报</h2>
        <div>
          <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 140px; margin-right: 12px;" @change="loadData">
            <el-option v-for="s in statusList" :key="s.name" :label="s.description" :value="s.name" />
          </el-select>
          <el-button v-if="isEnterprise" type="primary" :icon="Plus" @click="goCreate">
            新建申报
          </el-button>
        </div>
      </div>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="declarationNo" label="申报编号" width="160" />
        <el-table-column prop="permitNo" label="许可证号" width="160" />
        <el-table-column prop="vesselName" label="作业船只" width="140" />
        <el-table-column prop="riverSectionName" label="河段" min-width="150" />
        <el-table-column prop="miningArea" label="采区" width="120" />
        <el-table-column prop="declarationDate" label="申报日期" width="120">
          <template #default="{ row }">{{ formatDate(row.declarationDate) }}</template>
        </el-table-column>
        <el-table-column label="申报方量" width="120">
          <template #default="{ row }">{{ formatNumber(row.declaredVolume) }} 方</template>
        </el-table-column>
        <el-table-column label="称重方量" width="120">
          <template #default="{ row }">
            {{ row.weighedVolume != null ? formatNumber(row.weighedVolume) + ' 方' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor('declaration', row.status)">
              {{ getStatusDesc(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitterName" label="申报人" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.id)">详情</el-button>
            <el-button
              v-if="isEnterprise && row.status === 'DRAFT'"
              type="success" link @click="handleSubmit(row)"
            >
              提交
            </el-button>
            <el-button
              v-if="isEnforcer && row.status === 'SUBMITTED'"
              type="warning" link @click="goVerify(row.id)"
            >
              核验
            </el-button>
            <el-button
              v-if="isEnterprise && (row.status === 'DRAFT' || row.status === 'SUBMITTED')"
              type="danger" link @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeclarations, submitDeclaration, cancelDeclaration } from '@/api/declaration'
import { getEnums } from '@/api/common'
import { getUserInfo, formatDate, formatNumber, getStatusColor } from '@/utils'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const statusList = ref([])
const filterStatus = ref('')
const userInfo = computed(() => getUserInfo() || {})
const isEnterprise = computed(() => userInfo.value.role === 'ENTERPRISE')
const isEnforcer = computed(() => userInfo.value.role === 'ENFORCER')

function getStatusDesc(status) {
  return statusList.value.find(s => s.name === status)?.description || status
}

async function loadEnums() {
  const res = await getEnums()
  statusList.value = res.data?.declarationStatuses || []
}

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (filterStatus.value) {
      params.status = filterStatus.value
    }
    if (isEnterprise.value) {
      params.holderId = userInfo.value.userId
    }
    const res = await getDeclarations(params)
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function goCreate() {
  router.push('/declarations/new')
}

function goDetail(id) {
  router.push(`/declarations/${id}`)
}

function goVerify(id) {
  router.push(`/declarations/${id}/verify`)
}

async function handleSubmit(row) {
  try {
    await ElMessageBox.confirm('确定提交该申报单吗？提交后将等待执法人员核验。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await submitDeclaration(row.id)
    ElMessage.success('提交成功')
    loadData()
  } catch {
  }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确定取消该申报单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelDeclaration(row.id)
    ElMessage.success('已取消')
    loadData()
  } catch {
  }
}

onMounted(() => {
  loadEnums()
  loadData()
})
</script>
