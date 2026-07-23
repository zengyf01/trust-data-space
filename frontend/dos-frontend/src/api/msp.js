import axios from 'axios'

const api = axios.create({
  baseURL: '/api/msp',
  timeout: 30000
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('msp_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('msp_token')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export const taskAPI = {
  list: (params) => api.get('/task/page', { params }),
  create: (data) => api.post('/task', data),
  save: (data) => api.post('/task/save', data),
  get: (taskId) => api.get(`/task/${taskId}`),
  execute: (taskId) => api.post(`/task/${taskId}/execute`),
  cancel: (taskId) => api.delete(`/task/${taskId}/cancel`),
  delete: (taskId) => api.delete(`/task/${taskId}`),
  result: (taskId) => api.get(`/task/${taskId}/result`)
}

export const nodeAPI = {
  register: (data) => api.post('/node/register', data),
  list: (params) => api.get('/node/page', { params }),
  get: (nodeId) => api.get(`/node/${nodeId}`),
  heartbeat: (nodeId) => api.post(`/node/${nodeId}/heartbeat`),
  unregister: (nodeId) => api.delete(`/node/${nodeId}`)
}

export const dataSourceAPI = {
  create: (data) => api.post('/datasource', data),
  list: (params) => api.get('/datasource/page', { params }),
  get: (datasourceId) => api.get(`/datasource/${datasourceId}`),
  update: (datasourceId, data) => api.put(`/datasource/${datasourceId}`, data),
  delete: (datasourceId) => api.delete(`/datasource/${datasourceId}`),
  test: (data) => api.post('/datasource/test-connection', data)
}

export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout')
}

export default api