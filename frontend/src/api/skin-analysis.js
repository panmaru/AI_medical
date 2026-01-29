import request from '@/utils/request'

/**
 * 上传皮肤图片并进行分析
 */
export const analyzeSkinImages = (data) => {
  const formData = new FormData()
  formData.append('patientId', data.patientId)
  if (data.description) {
    formData.append('description', data.description)
  }
  data.images.forEach(image => {
    formData.append('images', image.raw)
  })

  return request({
    url: '/skin-analysis/analyze',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 单独上传图片
 */
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('image', file)

  return request({
    url: '/skin-analysis/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传图片
 */
export const uploadImages = (files) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('images', file)
  })

  return request({
    url: '/skin-analysis/upload/batch',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
