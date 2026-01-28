<template>
  <div class="knowledge-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>医疗知识库管理</span>
          <el-button type="primary" size="small" @click="handleAdd">
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
            <el-option label="内分泌系统" value="内分泌系统" />
            <el-option label="泌尿系统" value="泌尿系统" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.auditStatus" placeholder="请选择状态" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="已审核" :value="1" />
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
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.auditStatus === 0"
              type="success"
              link
              size="small"
              @click="handleAudit(row)"
            >
              审核
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="疾病名称" prop="diseaseName">
          <el-input v-model="form.diseaseName" placeholder="请输入疾病名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="呼吸系统" value="呼吸系统" />
            <el-option label="消化系统" value="消化系统" />
            <el-option label="心血管系统" value="心血管系统" />
            <el-option label="神经系统" value="神经系统" />
            <el-option label="内分泌系统" value="内分泌系统" />
            <el-option label="泌尿系统" value="泌尿系统" />
          </el-select>
        </el-form-item>
        <el-form-item label="症状" prop="symptoms">
          <el-input
            v-model="form.symptoms"
            type="textarea"
            :rows="3"
            placeholder="请输入症状描述"
          />
        </el-form-item>
        <el-form-item label="病因">
          <el-input
            v-model="form.etiology"
            type="textarea"
            :rows="3"
            placeholder="请输入病因"
          />
        </el-form-item>
        <el-form-item label="诊断方法">
          <el-input
            v-model="form.diagnosisMethods"
            type="textarea"
            :rows="3"
            placeholder="请输入诊断方法"
          />
        </el-form-item>
        <el-form-item label="治疗方案">
          <el-input
            v-model="form.treatment"
            type="textarea"
            :rows="3"
            placeholder="请输入治疗方案"
          />
        </el-form-item>
        <el-form-item label="用药建议">
          <el-input
            v-model="form.medicationAdvice"
            type="textarea"
            :rows="3"
            placeholder="请输入用药建议"
          />
        </el-form-item>
        <el-form-item label="预防措施">
          <el-input
            v-model="form.prevention"
            type="textarea"
            :rows="3"
            placeholder="请输入预防措施"
          />
        </el-form-item>
        <el-form-item label="注意事项">
          <el-input
            v-model="form.precautions"
            type="textarea"
            :rows="3"
            placeholder="请输入注意事项"
          />
        </el-form-item>
        <el-form-item label="参考文献">
          <el-input
            v-model="form.references"
            type="textarea"
            :rows="2"
            placeholder="请输入参考文献"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="请输入标签，多个标签用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="知识详情" width="800px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ currentKnowledge.title }}</el-descriptions-item>
        <el-descriptions-item label="疾病名称">{{ currentKnowledge.diseaseName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentKnowledge.category }}</el-descriptions-item>
        <el-descriptions-item label="症状">{{ currentKnowledge.symptoms }}</el-descriptions-item>
        <el-descriptions-item label="病因">{{ currentKnowledge.etiology }}</el-descriptions-item>
        <el-descriptions-item label="诊断方法">{{ currentKnowledge.diagnosisMethods }}</el-descriptions-item>
        <el-descriptions-item label="治疗方案">{{ currentKnowledge.treatment }}</el-descriptions-item>
        <el-descriptions-item label="用药建议">{{ currentKnowledge.medicationAdvice }}</el-descriptions-item>
        <el-descriptions-item label="预防措施">{{ currentKnowledge.prevention }}</el-descriptions-item>
        <el-descriptions-item label="注意事项">{{ currentKnowledge.precautions }}</el-descriptions-item>
        <el-descriptions-item label="参考文献">{{ currentKnowledge.references }}</el-descriptions-item>
        <el-descriptions-item label="标签">{{ currentKnowledge.tags }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag v-if="currentKnowledge.auditStatus === 0" type="warning">待审核</el-tag>
          <el-tag v-else type="success">已审核</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentKnowledge.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditDialogVisible" title="审核知识" width="400px">
      <el-form label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="0">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAuditSubmit" :loading="auditLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getKnowledgePage,
  createKnowledge,
  updateKnowledge,
  deleteKnowledge,
  auditKnowledge
} from '@/api/knowledge'

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  diseaseName: '',
  category: '',
  auditStatus: null
})

const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 对话框相关
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const auditLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  title: '',
  diseaseName: '',
  category: '',
  symptoms: '',
  etiology: '',
  diagnosisMethods: '',
  treatment: '',
  medicationAdvice: '',
  prevention: '',
  precautions: '',
  references: '',
  tags: ''
})

const currentKnowledge = ref({})

const auditForm = reactive({
  id: null,
  auditStatus: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  diseaseName: [{ required: true, message: '请输入疾病名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  symptoms: [{ required: true, message: '请输入症状', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: page.current,
      size: page.size,
      diseaseName: searchForm.diseaseName || undefined,
      category: searchForm.category || undefined,
      auditStatus: searchForm.auditStatus
    }
    const res = await getKnowledgePage(params)
    tableData.value = res.data.records
    page.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  page.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.diseaseName = ''
  searchForm.category = ''
  searchForm.auditStatus = null
  page.current = 1
  loadData()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增知识'
  Object.assign(form, {
    id: null,
    title: '',
    diseaseName: '',
    category: '',
    symptoms: '',
    etiology: '',
    diagnosisMethods: '',
    treatment: '',
    medicationAdvice: '',
    prevention: '',
    precautions: '',
    references: '',
    tags: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑知识'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 查看
const handleView = (row) => {
  currentKnowledge.value = row
  viewDialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateKnowledge(form)
      ElMessage.success('更新成功')
    } else {
      await createKnowledge(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条知识吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteKnowledge(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  })
}

// 审核
const handleAudit = (row) => {
  auditForm.id = row.id
  auditForm.auditStatus = 1
  auditDialogVisible.value = true
}

// 提交审核
const handleAuditSubmit = async () => {
  auditLoading.value = true
  try {
    await auditKnowledge(auditForm.id, auditForm.auditStatus)
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    auditLoading.value = false
  }
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
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
