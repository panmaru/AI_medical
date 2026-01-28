import request from '@/utils/request'

/**
 * 分页查询患者列表
 */
export const getPatientPage = (params) => {
  return request({
    url: '/patient/page',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询患者
 */
export const getPatientById = (id) => {
  return request({
    url: `/patient/${id}`,
    method: 'get'
  })
}

/**
 * 新增患者
 */
export const addPatient = (data) => {
  return request({
    url: '/patient',
    method: 'post',
    data
  })
}

/**
 * 更新患者
 */
export const updatePatient = (data) => {
  return request({
    url: '/patient',
    method: 'put',
    data
  })
}

/**
 * 删除患者
 */
export const deletePatient = (id) => {
  return request({
    url: `/patient/${id}`,
    method: 'delete'
  })
}
