import request from '@/utils/request'

/**
 * AI对话问诊
 */
export const aiChat = (data) => {
  return request({
    url: '/diagnosis/chat',
    method: 'post',
    data
  })
}

/**
 * 分页查询诊断记录
 */
export const getDiagnosisRecordPage = (params) => {
  return request({
    url: '/diagnosis/page',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询诊断记录
 */
export const getDiagnosisRecordById = (id) => {
  return request({
    url: `/diagnosis/${id}`,
    method: 'get'
  })
}

/**
 * 医生确认诊断
 */
export const confirmDiagnosis = (data) => {
  return request({
    url: '/diagnosis/confirm',
    method: 'put',
    data
  })
}

/**
 * 完成诊断
 */
export const completeDiagnosis = (id) => {
  return request({
    url: `/diagnosis/complete/${id}`,
    method: 'put'
  })
}
