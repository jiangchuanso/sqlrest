import Axios from 'axios';
var root = process.env.API_ROOT || '';
const axios = Axios.create();

//请求拦截
axios.interceptors.request.use((config) => {
    // 确保 config 存在
    if (!config) {
        console.error('Axios request blocked: config is undefined');
        return Promise.reject(new Error('Config is undefined'));
    }
    // 确保 URL 存在
    if (!config.url) {
        console.error('Axios request blocked: missing URL', config);
        return Promise.reject(new Error('Missing URL'));
    }
    //请求之前重新拼装url，确保root有效
    if (root && config.url && typeof config.url === 'string') {
        config.url = root + config.url;
    }
    // 添加语言Header，与后端i18n对接
    const locale = localStorage.getItem('locale') || 'zh-CN';
    config.headers['Accept-Language'] = locale;
    return config;
});

export default axios;