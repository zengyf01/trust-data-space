import axios from 'axios'

const api = axios.create({
  baseURL: '/api/tds',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加签名
api.interceptors.request.use(config => {
  const timestamp = Date.now()
  const appId = 'default-app-id'
  config.headers['Timestamp'] = timestamp
  config.headers['Authorization'] = `${appId}:${timestamp}:mock-signature`
  return config
})

// 响应拦截器
api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export default {
  // 获取合约列表（分页）
  getContractPage(params) {
    return api.post('/digitalContract/contractList', null, { params })
  },

  // 获取所有合约
  getAllContracts(connectorNumber) {
    return api.post('/digitalContract/getAllDigitalContract', null, {
      params: { connectorNumber }
    })
  },

  // 获取合约详情
  getContract(id) {
    return api.get(`/digitalContract/${id}`)
  },

  // 创建合约
  createContract(data) {
    return api.post('/digitalContract', data)
  },

  // 供方签名
  providerSign(contractId, signature) {
    return api.post('/digitalContract/providerSign', null, {
      params: { contractId, signature }
    })
  },

  // 需方签名
  consumerSign(contractId, signature) {
    return api.post('/digitalContract/consumerSign', null, {
      params: { contractId, signature }
    })
  },

  // 拒绝合约
  rejectContract(contractId, reason) {
    return api.post('/digitalContract/reject', null, {
      params: { contractId, reason }
    })
  },

  // 终止合约
  terminateContract(contractId, reason) {
    return api.post('/digitalContract/terminate', null, {
      params: { contractId, reason }
    })
  }
}