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
  getOrgPage(params) {
    return api.get('/organization/page', { params })
  },
  getOrg(id) {
    return api.get(`/organization/${id}`)
  }
}