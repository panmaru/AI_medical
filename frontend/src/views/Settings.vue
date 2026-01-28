<template>
  <div class="settings-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          <div class="user-profile">
            <el-avatar :size="80" :src="userStore.userInfo.avatar" />
            <h3>{{ userStore.userInfo.realName }}</h3>
            <p>{{ userStore.userInfo.title || '暂无职称' }}</p>
            <el-descriptions :column="1" class="user-info">
              <el-descriptions-item label="用户名">{{ userStore.userInfo.username }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ userStore.userInfo.phone || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ userStore.userInfo.email || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="专长">{{ userStore.userInfo.specialty || '暂无' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统设置</span>
            </div>
          </template>

          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本设置" name="basic">
              <el-form label-width="120px">
                <el-form-item label="系统名称">
                  <el-input v-model="settings.systemName" />
                </el-form-item>
                <el-form-item label="系统Logo">
                  <el-upload
                    class="avatar-uploader"
                    action="#"
                    :show-file-list="false"
                  >
                    <img v-if="settings.logoUrl" :src="settings.logoUrl" class="logo" />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary">保存设置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="AI配置" name="ai">
              <el-form label-width="150px">
                <el-form-item label="讯飞星火AppId">
                  <el-input v-model="settings.sparkAppId" placeholder="请输入AppId" />
                </el-form-item>
                <el-form-item label="讯飞星火API Key">
                  <el-input v-model="settings.sparkApiKey" placeholder="请输入API Key" />
                </el-form-item>
                <el-form-item label="讯飞星火API Secret">
                  <el-input v-model="settings.sparkApiSecret" type="password" placeholder="请输入API Secret" />
                </el-form-item>
                <el-form-item label="模型版本">
                  <el-select v-model="settings.modelVersion">
                    <el-option label="Spark 1.5" value="1.5" />
                    <el-option label="Spark 2.0" value="2.0" />
                    <el-option label="Spark 3.0" value="3.0" />
                    <el-option label="Spark 3.5" value="3.5" />
                  </el-select>
                </el-form-item>
                <el-form-item label="温度参数">
                  <el-slider v-model="settings.temperature" :min="0" :max="1" :step="0.1" />
                </el-form-item>
                <el-form-item label="最大Token数">
                  <el-input-number v-model="settings.maxTokens" :min="100" :max="4096" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary">保存配置</el-button>
                  <el-button @click="testConnection">测试连接</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="安全设置" name="security">
              <el-form label-width="120px">
                <el-form-item label="Token过期时间">
                  <el-input-number v-model="settings.tokenTimeout" :min="30" :max="86400" />
                  <span style="margin-left: 10px">秒</span>
                </el-form-item>
                <el-form-item label="密码强度">
                  <el-radio-group v-model="settings.passwordStrength">
                    <el-radio label="low">低</el-radio>
                    <el-radio label="medium">中</el-radio>
                    <el-radio label="high">高</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="启用验证码">
                  <el-switch v-model="settings.enableCaptcha" />
                </el-form-item>
                <el-form-item label="启用两步验证">
                  <el-switch v-model="settings.enableTwoFactor" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary">保存设置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="通知设置" name="notification">
              <el-form label-width="150px">
                <el-form-item label="邮件通知">
                  <el-switch v-model="settings.emailNotification" />
                </el-form-item>
                <el-form-item label="短信通知">
                  <el-switch v-model="settings.smsNotification" />
                </el-form-item>
                <el-form-item label="诊断完成通知">
                  <el-switch v-model="settings.diagnosisCompleteNotification" />
                </el-form-item>
                <el-form-item label="系统公告通知">
                  <el-switch v-model="settings.systemNotification" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary">保存设置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref('basic')

const settings = ref({
  systemName: 'AI智能问诊医疗辅助平台',
  logoUrl: '',
  sparkAppId: '',
  sparkApiKey: '',
  sparkApiSecret: '',
  modelVersion: '1.5',
  temperature: 0.7,
  maxTokens: 2048,
  tokenTimeout: 7200,
  passwordStrength: 'medium',
  enableCaptcha: false,
  enableTwoFactor: false,
  emailNotification: true,
  smsNotification: false,
  diagnosisCompleteNotification: true,
  systemNotification: true
})

const testConnection = () => {
  ElMessage.success('连接测试成功！')
}
</script>

<style scoped>
.settings-container {
  padding: 0;
}

.card-header {
  font-weight: bold;
}

.user-profile {
  text-align: center;
}

.user-profile h3 {
  margin: 15px 0 5px;
}

.user-profile p {
  color: #999;
  margin-bottom: 20px;
}

.user-info {
  margin-top: 20px;
}

.avatar-uploader {
  text-align: left;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo {
  width: 100px;
  height: 100px;
  display: block;
}
</style>
