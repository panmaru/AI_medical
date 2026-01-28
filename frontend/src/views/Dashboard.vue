<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409EFF">
              <el-icon :size="30"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.patientCount }}</div>
              <div class="stat-label">患者总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67C23A">
              <el-icon :size="30"><DocumentChecked /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.diagnosisCount }}</div>
              <div class="stat-label">今日问诊</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #E6A23C">
              <el-icon :size="30"><ChatLineRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.aiDiagnosisCount }}</div>
              <div class="stat-label">AI辅助诊断</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #F56C6C">
              <el-icon :size="30"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.accuracy }}%</div>
              <div class="stat-label">诊断准确率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>问诊趋势</span>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>疾病类型分布</span>
            </div>
          </template>
          <div ref="pieChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近问诊记录 -->
    <el-row :gutter="20" class="recent-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近问诊记录</span>
              <el-button type="primary" size="small" @click="$router.push('/diagnosis-record')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-table :data="recentRecords" stripe>
            <el-table-column prop="recordNo" label="诊断编号" width="180" />
            <el-table-column prop="patientName" label="患者姓名" width="120" />
            <el-table-column prop="chiefComplaint" label="主诉" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 0" type="warning">待确认</el-tag>
                <el-tag v-else-if="row.status === 1" type="success">已确认</el-tag>
                <el-tag v-else type="info">已完成</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="诊断时间" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="viewDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

// 统计数据
const stats = ref({
  patientCount: 1234,
  diagnosisCount: 56,
  aiDiagnosisCount: 42,
  accuracy: 95.8
})

// 最近记录
const recentRecords = ref([
  {
    recordNo: 'DR202412010001',
    patientName: '张三',
    chiefComplaint: '头痛、发热3天',
    status: 0,
    createTime: '2024-12-01 14:30:25'
  },
  {
    recordNo: 'DR202412010002',
    patientName: '李四',
    chiefComplaint: '咳嗽、咽痛',
    status: 1,
    createTime: '2024-12-01 13:20:15'
  },
  {
    recordNo: 'DR202412010003',
    patientName: '王五',
    chiefComplaint: '腹痛、恶心',
    status: 2,
    createTime: '2024-12-01 11:45:30'
  }
])

// 图表引用
const trendChartRef = ref(null)
const pieChartRef = ref(null)

// 初始化趋势图
const initTrendChart = () => {
  const chart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['问诊人数', 'AI诊断']
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '问诊人数',
        type: 'line',
        data: [45, 52, 48, 55, 60, 42, 38],
        smooth: true
      },
      {
        name: 'AI诊断',
        type: 'line',
        data: [35, 42, 38, 45, 50, 32, 28],
        smooth: true
      }
    ]
  }
  chart.setOption(option)
}

// 初始化饼图
const initPieChart = () => {
  const chart = echarts.init(pieChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '疾病类型',
        type: 'pie',
        radius: '60%',
        data: [
          { value: 35, name: '呼吸系统' },
          { value: 25, name: '消化系统' },
          { value: 20, name: '心血管' },
          { value: 12, name: '神经系统' },
          { value: 8, name: '其他' }
        ]
      }
    ]
  }
  chart.setOption(option)
}

// 查看详情
const viewDetail = (row) => {
  console.log('查看详情:', row)
}

onMounted(() => {
  initTrendChart()
  initPieChart()
})
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.2);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 20px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.charts-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
</style>
