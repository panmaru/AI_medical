<template>
  <div class="settings-container">
    <el-row justify="center">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
              <el-button type="primary" size="small" @click="openEditDialog">编辑</el-button>
            </div>
          </template>
          <div class="user-profile">
            <div class="avatar-wrapper">
              <el-avatar :size="120" :src="userStore.userInfo.avatar" />
              <el-upload
                class="avatar-upload"
                action="#"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="uploadAvatar"
              >
                <el-icon class="avatar-upload-icon"><Camera /></el-icon>
              </el-upload>
            </div>
            <h3>{{ userStore.userInfo.realName }}</h3>
            <p>{{ userStore.userInfo.title || '暂无职称' }}</p>
            <el-descriptions :column="1" class="user-info" border>
              <el-descriptions-item label="用户名">{{ userStore.userInfo.username }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ userStore.userInfo.phone || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ userStore.userInfo.email || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="专长">{{ userStore.userInfo.specialty || '暂无' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>

        <!-- 编辑个人信息对话框 -->
        <el-dialog
          v-model="editDialogVisible"
          title="编辑个人信息"
          width="500px"
          :close-on-click-modal="false"
        >
          <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="100px">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="职称" prop="title">
              <el-input v-model="profileForm.title" placeholder="请输入职称" />
            </el-form-item>
            <el-form-item label="专长" prop="specialty">
              <el-input v-model="profileForm.specialty" type="textarea" :rows="3" placeholder="请输入专长" />
            </el-form-item>
            <el-form-item label="头像" prop="avatar">
              <div class="avatar-upload-wrapper">
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :http-request="uploadAvatarForEdit"
                >
                  <img v-if="profileForm.avatar" :src="profileForm.avatar" class="avatar-preview" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div class="avatar-tip">支持上传jpg、png格式的图片，文件大小不超过5MB</div>
              </div>
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="editDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleUpdateProfile" :loading="updateLoading">保存</el-button>
          </template>
        </el-dialog>

        <!-- 患者信息卡片（仅普通用户显示） -->
        <el-card v-if="isPatient" style="margin-top: 20px;" v-loading="patientInfoLoading">
          <template #header>
            <div class="card-header">
              <span>患者信息</span>
              <el-button type="primary" size="small" @click="goToPatientProfile">管理患者信息</el-button>
            </div>
          </template>
          
          <div v-if="patientInfo">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="患者编号">{{ patientInfo.patientNo || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="姓名">{{ patientInfo.name || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ patientInfo.gender === 1 ? '男' : patientInfo.gender === 0 ? '女' : '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ patientInfo.age || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="出生日期">{{ patientInfo.birthday || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ patientInfo.phone || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="身份证号" :span="2">{{ patientInfo.idCard || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="地址" :span="2">{{ patientInfo.address || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="过敏史" :span="2">{{ patientInfo.allergyHistory || '无' }}</el-descriptions-item>
              <el-descriptions-item label="既往病史" :span="2">{{ patientInfo.pastHistory || '无' }}</el-descriptions-item>
              <el-descriptions-item label="家族病史" :span="2">{{ patientInfo.familyHistory || '无' }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">{{ patientInfo.remark || '无' }}</el-descriptions-item>
            </el-descriptions>
          </div>
          
          <el-empty v-else description="暂无患者信息" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { updateProfile } from '@/api/user'
import { getMyPatientInfo } from '@/api/patient'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()
const editDialogVisible = ref(false)
const updateLoading = ref(false)
const profileFormRef = ref(null)

// 患者信息
const patientInfo = ref(null)
const patientInfoLoading = ref(false)

// 判断是否是普通用户（患者）
const isPatient = computed(() => {
  return userStore.userInfo.role === 2
})

// 跳转到患者信息页面
const goToPatientProfile = () => {
  router.push('/patient-profile')
}

// 加载患者信息
const loadPatientInfo = async () => {
  if (!isPatient.value) return
  
  patientInfoLoading.value = true
  try {
    const res = await getMyPatientInfo()
    if (res.code === 200 && res.data) {
      patientInfo.value = res.data
    }
  } catch (error) {
    console.error('加载患者信息失败:', error)
  } finally {
    patientInfoLoading.value = false
  }
}

// 个人信息表单
const profileForm = reactive({
  realName: '',
  phone: '',
  email: '',
  title: '',
  specialty: '',
  avatar: ''
})

// 表单验证规则
const profileRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9][0-9]{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 打开编辑对话框
const openEditDialog = () => {
  profileForm.realName = userStore.userInfo.realName || ''
  profileForm.phone = userStore.userInfo.phone || ''
  profileForm.email = userStore.userInfo.email || ''
  profileForm.title = userStore.userInfo.title || ''
  profileForm.specialty = userStore.userInfo.specialty || ''
  profileForm.avatar = userStore.userInfo.avatar || ''
  editDialogVisible.value = true
}

// 更新个人信息
const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      updateLoading.value = true
      try {
        await updateProfile(profileForm)
        ElMessage.success('更新个人信息成功')
        editDialogVisible.value = false
        // 更新用户信息
        await userStore.getInfo()
      } catch (error) {
        ElMessage.error(error.message || '更新个人信息失败')
      } finally {
        updateLoading.value = false
      }
    }
  })
}

// 上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB!')
    return false
  }
  return true
}

// 上传头像（直接更新）
const uploadAvatar = async (options) => {
  const { file } = options
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await request({
      url: '/file/upload/avatar',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    // 更新用户头像
    await updateProfile({
      ...profileForm,
      avatar: res.data.url
    })

    ElMessage.success('头像上传成功')
    // 刷新用户信息
    await userStore.getInfo()
  } catch (error) {
    ElMessage.error(error.message || '头像上传失败')
  }
}

// 上传头像（编辑对话框）
const uploadAvatarForEdit = async (options) => {
  const { file } = options
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await request({
      url: '/file/upload/avatar',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    profileForm.avatar = res.data.url
    ElMessage.success('头像上传成功')
  } catch (error) {
    ElMessage.error(error.message || '头像上传失败')
  }
}

// 组件挂载时加载患者信息
onMounted(() => {
  loadPatientInfo()
})
</script>

<style scoped>
.settings-container {
  padding: 20px;
}

.card-header {
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-profile {
  text-align: center;
  padding: 20px 0;
}

.user-profile h3 {
  margin: 15px 0 5px;
  font-size: 20px;
}

.user-profile p {
  color: #999;
  margin-bottom: 20px;
}

.user-info {
  margin-top: 30px;
  text-align: left;
}

/* 头像上传 */
.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
}

.avatar-upload {
  position: absolute;
  bottom: 0;
  right: 0;
  background: #409EFF;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.avatar-upload:hover {
  background: #66b1ff;
}

.avatar-upload-icon {
  color: #fff;
  font-size: 16px;
}

/* 编辑对话框头像上传 */
.avatar-upload-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-uploader {
  text-align: center;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}
</style>
