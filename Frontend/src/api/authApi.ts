import api from './axios'

export const authApi = {
  register: (data: any) => api.post('/auth/register', data),
  login: (data: any) => api.post('/auth/login', data),
  me: () => api.get('/auth/me'),
  refresh: (token: string) => api.post(`/auth/refresh?refreshToken=${token}`),
}
