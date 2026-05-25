import axios from 'axios'
import { message } from 'ant-design-vue'

const LARGE_INTEGER_REGEX = /([:\[,]\s*)(-?\d{16,})(\s*[,}\]])/g

const parseJsonSafely = (data: string) => {
  const normalizedData = data.replace(LARGE_INTEGER_REGEX, '$1"$2"$3')
  try {
    return JSON.parse(normalizedData)
  } catch {
    return JSON.parse(data)
  }
}

// 创建 Axios 实例
const myAxios = axios.create({
  baseURL: 'http://localhost:8123/api',
  timeout: 60000,
  withCredentials: true,
  transformResponse: [
    (data) => {
      if (typeof data !== 'string' || !data) {
        return data
      }
      try {
        return parseJsonSafely(data)
      } catch {
        return data
      }
    },
  ],
})

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // Do something before request is sent
    return config
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error)
  },
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // 未登录
    if (data.code === 40100) {
      // 不是获取用户信息的请求，并且用户目前不是已经在用户登录页面，则跳转到登录页面
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    return Promise.reject(error)
  },
)

export default myAxios
