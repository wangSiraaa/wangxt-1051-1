<template>
  <div class="login-wrapper">
    <div class="login-box">
      <h2 class="login-title">河道采砂许可管理系统</h2>
      <p class="login-subtitle">River Sand Mining Permit System</p>
      <el-form ref="loginForm" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-tips">
        <p><strong>演示账号：</strong></p>
        <p>企业用户：enterprise1 / 123456</p>
        <p>审核员：auditor / 123456</p>
        <p>执法人员：enforcer / 123456</p>
        <p>处罚处理人：penalty / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/common'
import { setUserInfo } from '@/utils'

const router = useRouter()
const loading = ref(false)
const loginForm = ref(null)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await loginForm.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    if (res.code === 200) {
      setUserInfo(res.data)
      ElMessage.success('登录成功')
      router.push('/')
    }
  } finally {
    loading.value = false
  }
}
</script>
