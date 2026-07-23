import axios from 'axios'

const api = axios.create({
  baseURL: '/api/tds',
  timeout: 10000
})

api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export default {
  getDataSpacePage(params) {
    return api.get('/dataspace/page', { params })
  },
  getDataSpace(id) {
    return api.get(`/dataspace/${id}`)
  }
}