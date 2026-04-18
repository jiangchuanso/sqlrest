// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import axios from './assets/axios.js';
import ElementUI from 'element-ui';
import VueI18n from 'vue-i18n'
import messages from './lang'
import './assets/iconfont/iconfont.css'
import './assets/dbicon/iconfont.css'
import './assets/dbicon/iconfont.js'
import './assets/sysicon/iconfont.css'
import 'element-ui/lib/theme-chalk/index.css';
import * as echarts from 'echarts'
import VueCodeMirror from 'vue-codemirror'
import 'codemirror/lib/codemirror.css'
import JsonViewer from 'vue-json-viewer'

Vue.use(VueCodeMirror)
Vue.use(ElementUI)
Vue.use(JsonViewer)

// 初始化 i18n
// 获取浏览器语言或默认中文
const browserLang = navigator.language || 'zh-CN'
const defaultLocale = browserLang.startsWith('en') ? 'en-US' : 'zh-CN'

// 从 localStorage 读取用户偏好
const savedLocale = localStorage.getItem('locale') || defaultLocale

Vue.use(VueI18n)

const i18n = new VueI18n({
  locale: savedLocale,
  messages
})

Vue.prototype.$http = axios
Vue.config.productionTip = false
Vue.prototype.$echarts = echarts


// http request 拦截器
axios.interceptors.request.use(config => {

  // 通过拦截request请求,对头部增加Authorization属性,以传递token值
  let token = sessionStorage.getItem('token');
  if (token) {
    config.headers.Authorization = 'Bearer ' + token;
  }

  // 添加语言Header，与后端i18n对接
  const locale = localStorage.getItem('locale') || 'zh-CN';
  config.headers['Accept-Language'] = locale;

  return config;
}, function (error) {
  // 对请求错误做些什么
  return Promise.reject(error)
})

//返回状态判断(添加响应拦截器)
axios.interceptors.response.use(res => {
  //对响应数据做些事
  if (res.data && (res.data.code === 401 || res.data.code === 403 || res.data.code === 404)) {
    // 只有不在登录页时才跳转到登录页
    if (router.currentRoute.path !== '/login') {
      router.push({ path: "/login" }).catch(() => {});
    }
  }

  return res
}, error => {
  // 返回 response 里的错误信息
  //console.log(error);
  return Promise.reject(error.response)
})

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  i18n,
  components: { App },
  template: '<App/>'
})
