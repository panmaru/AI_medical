<template>
  <div class="statistics-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>数据统计分析</span>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                size="small"
              />
            </div>
          </template>

          <!-- 统计图表 -->
          <el-tabs v-model="activeTab">
            <el-tab-pane label="问诊趋势" name="trend">
              <div ref="trendChartRef" style="height: 400px"></div>
            </el-tab-pane>

            <el-tab-pane label="疾病分布" name="disease">
              <div ref="diseaseChartRef" style="height: 400px"></div>
            </el-tab-pane>

            <el-tab-pane label="医生工作量" name="workload">
              <div ref="workloadChartRef" style="height: 400px"></div>
            </el-tab-pane>

            <el-tab-pane label="诊断准确率" name="accuracy">
              <div ref="accuracyChartRef" style="height: 400px"></div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const activeTab = ref('trend')
const dateRange = ref([])

const trendChartRef = ref(null)
const diseaseChartRef = ref(null)
const workloadChartRef = ref(null)
const accuracyChartRef = ref(null)

// 初始化趋势图
const initTrendChart = () => {
  const chart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['问诊总数', 'AI诊断数', '人工诊断数']
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '问诊总数',
        type: 'line',
        data: [120, 132, 101, 134, 90, 230, 210, 182, 191, 234, 290, 330],
        smooth: true
      },
      {
        name: 'AI诊断数',
        type: 'line',
        data: [80, 92, 71, 94, 70, 180, 160, 132, 141, 174, 220, 250],
        smooth: true
      },
      {
        name: '人工诊断数',
        type: 'line',
        data: [40, 40, 30, 40, 20, 50, 50, 50, 50, 60, 70, 80],
        smooth: true
      }
    ]
  }
  chart.setOption(option)
}

// 初始化疾病分布图
const initDiseaseChart = () => {
  const chart = echarts.init(diseaseChartRef.value)
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
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 1048, name: '呼吸系统' },
          { value: 735, name: '消化系统' },
          { value: 580, name: '心血管系统' },
          { value: 484, name: '神经系统' },
          { value: 300, name: '内分泌系统' },
          { value: 200, name: '其他' }
        ]
      }
    ]
  }
  chart.setOption(option)
}

// 初始化工作量图
const initWorkloadChart = () => {
  const chart = echarts.init(workloadChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {},
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: ['张医生', '李医生', '王医生', '赵医生', '刘医生']
    },
    series: [
      {
        name: 'AI辅助',
        type: 'bar',
        stack: 'total',
        label: {
          show: true
        },
        data: [320, 302, 301, 334, 390]
      },
      {
        name: '人工诊断',
        type: 'bar',
        stack: 'total',
        label: {
          show: true
        },
        data: [120, 132, 101, 134, 90]
      }
    ]
  }
  chart.setOption(option)
}

// 初始化准确率图
const initAccuracyChart = () => {
  const chart = echarts.init(accuracyChartRef.value)
  const option = {
    tooltip: {
      formatter: '{a} <br/>{b} : {c}%'
    },
    series: [
      {
        name: '诊断准确率',
        type: 'gauge',
        progress: {
          show: true,
          width: 18
        },
        axisLine: {
          lineStyle: {
            width: 18
          }
        },
        axisTick: {
          show: false
        },
        splitLine: {
          length: 15,
          lineStyle: {
            width: 2,
            color: '#999'
          }
        },
        axisLabel: {
          distance: 25,
          color: '#999',
          fontSize: 20
        },
        anchor: {
          show: true,
          showAbove: true,
          size: 25,
          itemStyle: {
            borderWidth: 10
          }
        },
        title: {
          show: false
        },
        detail: {
          valueAnimation: true,
          fontSize: 40,
          offsetCenter: [0, '70%'],
          formatter: '{value}%'
        },
        data: [
          {
            value: 95.8
          }
        ]
      }
    ]
  }
  chart.setOption(option)
}

onMounted(() => {
  initTrendChart()
  initDiseaseChart()
  initWorkloadChart()
  initAccuracyChart()
})
</script>

<style scoped>
.statistics-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
</style>
