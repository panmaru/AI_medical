import request from '@/utils/request'

/**
 * 分页查询医疗知识库
 */
export function getKnowledgePage(params) {
  return request({
    url: '/knowledge/page',
    method: 'get',
    params
  })
}

/**
 * 获取知识详情
 */
export function getKnowledgeById(id) {
  return request({
    url: `/knowledge/${id}`,
    method: 'get'
  })
}

/**
 * 新增知识
 */
export function createKnowledge(data) {
  return request({
    url: '/knowledge',
    method: 'post',
    data
  })
}

/**
 * 更新知识
 */
export function updateKnowledge(data) {
  return request({
    url: '/knowledge',
    method: 'put',
    data
  })
}

/**
 * 删除知识
 */
export function deleteKnowledge(id) {
  return request({
    url: `/knowledge/${id}`,
    method: 'delete'
  })
}

/**
 * 审核知识
 */
export function auditKnowledge(id, auditStatus) {
  return request({
    url: `/knowledge/audit/${id}`,
    method: 'put',
    params: { auditStatus }
  })
}
