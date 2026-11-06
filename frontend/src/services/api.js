import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 12000,
  headers: { 'Content-Type': 'application/json' },
})

apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('lumiere-token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export function apiError(error) {
  return error.response?.data?.error || error.response?.data?.message ||
    (error.code === 'ERR_NETWORK' ? 'Cannot reach Spring Boot. Is the backend running on port 8080?' : error.message) || 'Request failed'
}

export const productApi = {
  async getAll() { return (await apiClient.get('/products')).data },
  async getById(id) { return (await apiClient.get(`/products/${encodeURIComponent(id)}`)).data },
  async create(data) { return (await apiClient.post('/products', data)).data },
  async update(id, data) { return (await apiClient.put(`/products/${encodeURIComponent(id)}`, data)).data },
  async remove(id) { await apiClient.delete(`/products/${encodeURIComponent(id)}`) },
}
export const brandApi = {
  async getAll() { return (await apiClient.get('/brands')).data },
}
export const categoryApi = {
  async getAll() { return (await apiClient.get('/categories')).data },
}
export const authApi = {
  async login(data) { return (await apiClient.post('/auth/login', data)).data },
  async register(data) { return (await apiClient.post('/auth/register', data)).data },
  async me() { return (await apiClient.get('/auth/me')).data },
  async logout() { await apiClient.post('/auth/logout') },
}
export const orderApi = {
  async create(data) { return (await apiClient.post('/orders', data)).data },
  async getMyOrders() { return (await apiClient.get('/orders/my')).data },
  async getAllForAdmin() { return (await apiClient.get('/admin/orders')).data },
}
export const adminUserApi = {
  async getAll() { return (await apiClient.get('/admin/users')).data },
  async create(data) { return (await apiClient.post('/admin/users', data)).data },
  async update(id, data) { return (await apiClient.patch(`/admin/users/${id}`, data)).data },
  async remove(id) { await apiClient.delete(`/admin/users/${id}`) },
}
export default apiClient
