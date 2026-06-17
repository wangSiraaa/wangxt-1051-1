<template>
  <div class="page-container">
    <div class="card">
      <div class="toolbar">
        <h2>现场核查</h2>
        <el-button v-if="isEnforcer" type="primary" :icon="Plus" @click="goCreate">
          新增核查
        </el-button>
      </div>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="permitNo" label="许可证号" width="160" />
        <el-table-column prop="inspectionTime" label="核查时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.inspectionTime) }}</template>
        </el-table-column>
        <el-table-column prop="inspectorName" label="执法人员" width="120" />
        <el-table-column prop="location" label="位置" min-width="200" />
        <el-table-column label="经纬度" width="180">
          <template #default="{ row }">
            {{ row.latitude?.toFixed(4) || '-' }}, {{ row.longitude?.toFixed(4) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="实际方量" width="120">
          <template #default="{ row }">{{ formatNumber(row.actualVolume) }} 方</template>
        </el-table-column>
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor('inspection', row.result)">
              {{ getResultDesc(row.result) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="abnormalDescription" label="异常描述" min-width="200" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { getInspections, getInspectionsByInspector } from '@/api/inspection'
import { getEnums } from '@/api/common'
import { getUserInfo, formatDateTime, formatNumber, getStatusColor } from '@/utils'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const resultList = ref([])
const userInfo = computed(() => getUserInfo() || {})
const isEnforcer = computed(() => userInfo.value.role === 'ENFORCER')

function getResultDesc(result) {
  return resultList.value.find(r => r.name === result)?.description || result
}

async function loadEnums() {
  const res = await getEnums()
  resultList.value = res.data?.inspectionResults || []
}

async function loadData() {
  loading.value = true
  try {
    let res
    if (isEnforcer.value) {
      res = await getInspectionsByInspector(userInfo.value.userId)
    } else {
      res = await getInspections()
    }
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function goCreate() {
  router.push('/inspections/new')
}

onMounted(() => {
  loadEnums()
  loadData()
})
</script>
