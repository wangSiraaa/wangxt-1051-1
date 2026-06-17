export function getUserInfo() {
  const info = localStorage.getItem('userInfo')
  return info ? JSON.parse(info) : null
}

export function setUserInfo(info) {
  localStorage.setItem('userInfo', JSON.stringify(info))
}

export function clearUserInfo() {
  localStorage.removeItem('userInfo')
}

export function hasRole(role) {
  const user = getUserInfo()
  return user && user.role === role
}

export function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const pad = n => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

export function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const pad = n => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export function formatNumber(num) {
  if (num === null || num === undefined || num === '') return '-'
  return Number(num).toLocaleString('zh-CN')
}

export function getStatusColor(type, status) {
  const statusMap = {
    application: {
      DRAFT: 'info',
      PENDING_REVIEW: 'warning',
      RETURNED: 'danger',
      APPROVED: 'success',
      CHANGED: '',
      SUSPENDED: 'warning',
      EXPIRED: 'info',
      CANCELLED: 'info'
    },
    permit: {
      ACTIVE: 'success',
      SUSPENDED: 'warning',
      EXPIRED: 'info',
      CANCELLED: 'info'
    },
    inspection: {
      NORMAL: 'success',
      PARTIAL_ABNORMAL: 'warning',
      ABNORMAL: 'danger'
    },
    penalty: {
      PENDING: 'info',
      REVIEWING: 'warning',
      CONFIRMED: 'success',
      REJECTED: 'danger',
      PROCESSED: ''
    }
  }
  return statusMap[type]?.[status] || 'info'
}
