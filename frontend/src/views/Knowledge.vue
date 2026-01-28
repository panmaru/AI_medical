<template>
  <div class="knowledge-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>医疗知识库管理</span>
          <el-button type="primary" size="small">
            <el-icon><Plus /></el-icon>
            新增知识
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="疾病名称">
          <el-input v-model="searchForm.diseaseName" placeholder="请输入疾病名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option label="呼吸系统" value="呼吸系统" />
            <el-option label="消化系统" value="消化系统" />
            <el-option label="心血管系统" value="心血管系统" />
            <el-option label="神经系统" value="神经系统" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="diseaseName" label="疾病名称" width="150" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="symptoms" label="症状" show-overflow-tooltip />
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus === 0" type="warning">待审核</el-tag>
            <el-tag v-else type="success">已审核</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small">查看</el-button>
            <el-button type="primary" link size="small">编辑</el-button>
            <el-button type="danger" link size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([
  {
    id: 1,
    title: '急性上呼吸道感染',
    diseaseName: '急性上呼吸道感染',
    category: '呼吸系统',
    symptoms: '鼻塞、流涕、咳嗽、发热、头痛、全身不适',
    auditStatus: 1
  },
  {
    id: 2,
    title: '急性胃炎',
    diseaseName: '急性胃炎',
    category: '消化系统',
    symptoms: '上腹部疼痛、恶心、呕吐、食欲不振',
    auditStatus: 1
  }
])

const searchForm = reactive({
  diseaseName: '',
  category: ''
})

const page = reactive({
  current: 1,
  size: 10,
  total: 2
})

const loadData = () => {
  ElMessage.info('功能开发中...')
}

const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  searchForm.diseaseName = ''
  searchForm.category = ''
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.knowledge-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.search-form {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
