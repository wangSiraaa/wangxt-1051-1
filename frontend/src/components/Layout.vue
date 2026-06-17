<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon :size="28"><WaterWave /></el-icon>
        <span class="logo-text">河道采砂许可系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#b7bec9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/applications">
          <el-icon><Document /></el-icon>
          <span>许可申请</span>
        </el-menu-item>
        <el-menu-item index="/permits">
          <el-icon><Tickets /></el-icon>
          <span>许可证管理</span>
        </el-menu-item>
        <el-menu-item index="/declarations">
          <el-icon><List /></el-icon>
          <span>采砂申报</span>
        </el-menu-item>
        <el-menu-item index="/detentions">
          <el-icon><Lock /></el-icon>
          <span>现场暂扣</span>
        </el-menu-item>
        <el-menu-item index="/inspections">
          <el-icon><View /></el-icon>
          <span>现场核查</span>
        </el-menu-item>
        <el-menu-item index="/penalty-clues">
          <el-icon><Warning /></el-icon>
          <span>处罚线索</span>
        </el-menu-item>
        <el-menu-item index="/audit-timeline">
          <el-icon><Clock /></el-icon>
          <span>审计时间线</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="breadcrumb-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-tag :type="getRoleTagType(userInfo.role)" effect="light" size="small">
            {{ userInfo.roleDesc }}
          </el-tag>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userInfo.realName || userInfo.username }}
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getUserInfo, clearUserInfo } from '@/utils'

const route = useRoute()
const router = useRouter()

const userInfo = computed(() => getUserInfo() || {})
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '河道采砂许可管理系统')

function getRoleTagType(role) {
  const map = {
    ENTERPRISE: '',
    AUDITOR: 'warning',
    ENFORCER: 'success',
    PENALTY_HANDLER: 'danger'
  }
  return map[role] || 'info'
}

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      clearUserInfo()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background: #001529;
  overflow: auto;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.logo-text {
  font-size: 16px;
  font-weight: 600;
}
.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}
.breadcrumb-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #606266;
}
.user-info:hover {
  color: #409eff;
}
.main {
  background: #f0f2f5;
  overflow: auto;
}
:deep(.el-menu) {
  border-right: none;
}
</style>
