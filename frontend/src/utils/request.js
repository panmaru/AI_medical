import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data

    // 打印响应数据，方便调试
    console.log('Response:', res)

    if (!res) {
      ElMessage.error('服务器无响应')
      return Promise.reject(new Error('服务器无响应'))
    }

    if (res.code === 200) {
      return res
    } else if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      router.push('/login')
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
  },
  error => {
    console.error('Request Error:', error)

    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      if (status === 404) {
        ElMessage.error('接口不存在，请检查后端是否启动')
      } else if (status === 500) {
        ElMessage.error('服务器错误')
      } else if (status === 401) {
        ElMessage.error('未授权，请重新登录')
        localStorage.removeItem('token')
        router.push('/login')
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      ElMessage.error('网络错误，请检查后端是否启动')
    } else {
      ElMessage.error(error.message || '网络错误')
    }

    return Promise.reject(error)
  }
)

export default request
