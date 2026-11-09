<template>
  <div class="login-container">
    <!-- 装饰背景（纯视觉，对辅助技术隐藏） -->
    <div class="bg-grid"
         aria-hidden="true"></div>
    <div class="bg-glow bg-glow-1"
         aria-hidden="true"></div>
    <div class="bg-glow bg-glow-2"
         aria-hidden="true"></div>
    <div class="bg-glow bg-glow-3"
         aria-hidden="true"></div>

    <!-- 左侧品牌展示区 -->
    <section class="brand-panel">
      <div class="brand-top">
        <img class="brand-logo"
             src="../../assets/LOGO.png"
             alt="SQLREST" />
        <span class="brand-name">SQLREST</span>
      </div>

      <div class="brand-main">
        <p class="brand-eyebrow">{{ $t('login.brandEyebrow') }}</p>
        <h1 class="brand-title">{{ $t('login.brandSlogan') }}</h1>
        <p class="brand-desc">{{ $t('login.brandDesc') }}</p>

        <ul class="feature-list">
          <li class="feature-item"
              v-for="feature in features"
              :key="feature.text">
            <span class="feature-icon"
                  aria-hidden="true"
                  v-html="feature.icon"></span>
            <span class="feature-text">{{ $t(feature.text) }}</span>
          </li>
        </ul>
      </div>

      <div class="brand-footer">
        <span>© {{ currentYear }} SQLREST</span>
        <span class="brand-footer-dot">·</span>
        <span>https://gitee.com/inrgihc/sqlrest</span>
      </div>
    </section>

    <!-- 右侧登录表单区 -->
    <section class="form-panel">
      <div class="login-card">
        <div class="card-brand">
          <img class="card-logo"
               src="../../assets/LOGO.png"
               alt="SQLREST" />
          <span class="card-brand-name">SQLREST</span>
        </div>

        <div class="lang-switch">
          <button type="button"
                  :class="{ active: locale === 'zh-CN' }"
                  @click="switchLang('zh-CN')">中文</button>
          <span class="lang-sep">/</span>
          <button type="button"
                  :class="{ active: locale === 'en-US' }"
                  @click="switchLang('en-US')">EN</button>
        </div>

        <h2 class="card-title">{{ $t('login.welcomeBack') }}</h2>
        <p class="card-subtitle">{{ $t('login.subtitle') }}</p>

        <form @submit.prevent="handleLogin"
              class="form-content"
              novalidate>
          <div class="input-group">
            <label for="username"
                   class="input-label">{{ $t('login.username') }}</label>
            <div class="input-wrap"
                 :class="{ 'is-error': errors.username }">
              <span class="input-icon"
                    aria-hidden="true">
                <svg viewBox="0 0 24 24"
                     fill="none"
                     stroke="currentColor"
                     stroke-width="1.6"
                     stroke-linecap="round"
                     stroke-linejoin="round">
                  <circle cx="12"
                          cy="8"
                          r="3.2" />
                  <path d="M5.5 19a6.5 6.5 0 0 1 13 0" />
                </svg>
              </span>
              <input type="text"
                     id="username"
                     v-model="loginForm.username"
                     :placeholder="$t('login.usernamePlaceholder')"
                     class="input-field"
                     autocomplete="username" />
            </div>
            <span class="error-message"
                  role="alert"
                  v-if="errors.username">{{ errors.username }}</span>
          </div>

          <div class="input-group">
            <label for="password"
                   class="input-label">{{ $t('login.password') }}</label>
            <div class="input-wrap"
                 :class="{ 'is-error': errors.password }">
              <span class="input-icon"
                    aria-hidden="true">
                <svg viewBox="0 0 24 24"
                     fill="none"
                     stroke="currentColor"
                     stroke-width="1.6"
                     stroke-linecap="round"
                     stroke-linejoin="round">
                  <rect x="5"
                        y="10.5"
                        width="14"
                        height="9.5"
                        rx="2" />
                  <path d="M8 10.5V8a4 4 0 0 1 8 0v2.5" />
                </svg>
              </span>
              <input :type="showPassword ? 'text' : 'password'"
                     id="password"
                     v-model="loginForm.password"
                     :placeholder="$t('login.passwordPlaceholder')"
                     class="input-field"
                     autocomplete="current-password" />
              <button type="button"
                      class="toggle-password"
                      @click="togglePassword"
                      :aria-label="showPassword ? $t('login.hidePassword') : $t('login.showPassword')"
                      :title="showPassword ? $t('login.hidePassword') : $t('login.showPassword')">
                <svg v-if="showPassword"
                     viewBox="0 0 24 24"
                     fill="none"
                     stroke="currentColor"
                     stroke-width="1.6"
                     stroke-linecap="round"
                     stroke-linejoin="round">
                  <path d="M3 3l18 18" />
                  <path d="M10.6 6.1A9.7 9.7 0 0 1 12 6c6 0 9.5 6 9.5 6a15.8 15.8 0 0 1-3.3 3.9" />
                  <path d="M6.2 7.2A15.6 15.6 0 0 0 2.5 12s3.5 6 9.5 6a9.6 9.6 0 0 0 3.6-.7" />
                  <path d="M9.9 9.9a3 3 0 0 0 4.2 4.2" />
                </svg>
                <svg v-else
                     viewBox="0 0 24 24"
                     fill="none"
                     stroke="currentColor"
                     stroke-width="1.6"
                     stroke-linecap="round"
                     stroke-linejoin="round">
                  <path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12z" />
                  <circle cx="12"
                          cy="12"
                          r="3" />
                </svg>
              </button>
            </div>
            <span class="error-message"
                  role="alert"
                  v-if="errors.password">{{ errors.password }}</span>
          </div>

          <button type="submit"
                  class="login-button"
                  :disabled="loading"
                  :aria-busy="loading ? 'true' : 'false'">
            <span v-if="loading"
                  class="spinner"
                  aria-hidden="true"></span>
            <span>{{ loading ? $t('login.loggingIn') : $t('login.loginBtn') }}</span>
          </button>
        </form>

        <div class="card-footer">© {{ currentYear }} SQLREST</div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'LoginPage',
  data () {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      errors: {},
      loading: false,
      showPassword: false,
      locale: 'zh-CN',
      features: [
        {
          text: 'login.featureApi',
          icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M8.5 6.5L3.5 12l5 5.5"/><path d="M15.5 6.5l5 5.5-5 5.5"/></svg>'
        },
        {
          text: 'login.featureDb',
          icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><ellipse cx="12" cy="6" rx="7" ry="3"/><path d="M5 6v6c0 1.7 3.1 3 7 3s7-1.3 7-3V6"/><path d="M5 12v6c0 1.7 3.1 3 7 3s7-1.3 7-3v-6"/></svg>'
        },
        {
          text: 'login.featureEnterprise',
          icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l7 3v6c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V6l7-3z"/><path d="M9 12l2 2 4-4"/></svg>'
        }
      ]
    }
  },
  computed: {
    currentYear () {
      return new Date().getFullYear();
    }
  },
  created () {
    this.initLocale();
  },
  methods: {
    initLocale () {
      const savedLocale = localStorage.getItem('locale');
      if (savedLocale) {
        this.locale = savedLocale;
      } else {
        const browserLang = navigator.language || 'zh-CN';
        const defaultLocale = browserLang.startsWith('en') ? 'en-US' : 'zh-CN';
        this.locale = defaultLocale;
        localStorage.setItem('locale', defaultLocale);
      }
      this.$i18n.locale = this.locale;
    },
    switchLang (lang) {
      if (this.locale === lang) {
        return;
      }
      this.locale = lang;
      this.$i18n.locale = lang;
      localStorage.setItem('locale', lang);
    },
    togglePassword () {
      this.showPassword = !this.showPassword;
    },
    validateForm () {
      this.errors = {}

      if (!this.loginForm.username.trim()) {
        this.errors.username = this.$t('login.usernameRequired')
      }

      if (!this.loginForm.password) {
        this.errors.password = this.$t('login.passwordRequired')
      }

      return Object.keys(this.errors).length === 0
    },
    resetLoading () {
      this.loading = false;
    },
    showMessageBox (msg) {
      this.$alert(msg, this.$t('common.error'), {
        type: 'error',
        confirmButtonText: this.$t('common.confirm')
      });
    },
    handleLogin () {
      if (!this.validateForm()) {
        return
      }
      this.loading = true
      this.$http({
        method: 'POST',
        url: '/user/login',
        data: {
          username: this.loginForm.username,
          password: this.loginForm.password
        },
      }).then(res => {
        if (0 === res.data.code) {
          window.sessionStorage.setItem('token', res.data.data.accessToken);
          window.sessionStorage.setItem('username', this.loginForm.username);
          window.sessionStorage.setItem('realname', res.data.data.realName);
          this.$http.get("/sqlrest/manager/api/v1/health/version").then(
            res => {
              if (0 === res.data.code) {
                window.sessionStorage.setItem('version', res.data.data);
                this.$message(this.$t('login.loginSuccess'));
                this.$router.push({ path: '/dashboard' });
              } else {
                this.$message(res.data.message);
              }
              this.resetLoading();
            }
          );
        } else {
          this.showMessageBox(this.$t('login.loginError') + res.data.message);
          this.resetLoading();
        }
      }).catch(error => {
        this.resetLoading();
        this.showMessageBox(this.$t('login.loginError') + (error || this.$t('login.connectionFailed')));
      })
    }
  }
}
</script>

<style scoped>
/* ------------------------------------------------------------------
 * 布局
 * ------------------------------------------------------------------ */
.login-container {
  position: relative;
  display: flex;
  min-height: 100vh;
  overflow: hidden;
  background: #0b1220;
  color: #e2e8f0;
  color-scheme: dark;
}

/* ------------------------------------------------------------------
 * 装饰背景
 * ------------------------------------------------------------------ */
.bg-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: linear-gradient(rgba(148, 163, 184, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.06) 1px, transparent 1px);
  background-size: 48px 48px;
  -webkit-mask-image: radial-gradient(circle at 30% 40%, #000 0%, transparent 72%);
  mask-image: radial-gradient(circle at 30% 40%, #000 0%, transparent 72%);
}

.bg-glow {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(90px);
  opacity: 0.5;
}

.bg-glow-1 {
  width: 520px;
  height: 520px;
  top: -160px;
  left: -120px;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.55), transparent 70%);
  animation: float-a 14s ease-in-out infinite;
}

.bg-glow-2 {
  width: 460px;
  height: 460px;
  bottom: -180px;
  left: 22%;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.35), transparent 70%);
  animation: float-b 18s ease-in-out infinite;
}

.bg-glow-3 {
  width: 420px;
  height: 420px;
  top: 18%;
  right: -140px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.35), transparent 70%);
  animation: float-a 16s ease-in-out infinite;
}

/* ------------------------------------------------------------------
 * 品牌区
 * ------------------------------------------------------------------ */
.brand-panel {
  position: relative;
  z-index: 1;
  flex: 1 1 58%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 40px;
  padding: 56px 64px;
}

.brand-top {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-logo {
  width: 44px;
  height: 44px;
  padding: 6px;
  border-radius: 12px;
  object-fit: contain;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-name {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #fff;
}

.brand-main {
  max-width: 560px;
  animation: card-in 0.55s ease both;
}

.brand-eyebrow {
  display: inline-block;
  margin: 0 0 22px;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 3px;
  text-transform: uppercase;
  color: #60a5fa;
  background: rgba(37, 99, 235, 0.14);
  border: 1px solid rgba(96, 165, 250, 0.3);
}

.brand-title {
  margin: 0 0 18px;
  font-size: 44px;
  line-height: 1.2;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #fff;
}

.brand-desc {
  max-width: 480px;
  margin: 0 0 36px;
  font-size: 16px;
  line-height: 1.8;
  color: #94a3b8;
}

.feature-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 15px;
  color: #cbd5e1;
}

.feature-icon {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  color: #60a5fa;
  background: rgba(37, 99, 235, 0.14);
  border: 1px solid rgba(96, 165, 250, 0.22);
}

.brand-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748b;
}

.brand-footer-dot {
  color: #334155;
}

/* ------------------------------------------------------------------
 * 表单区
 * ------------------------------------------------------------------ */
.form-panel {
  position: relative;
  z-index: 1;
  flex: 0 0 auto;
  width: min(500px, 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-card {
  position: relative;
  width: 100%;
  padding: 40px 38px;
  border-radius: 20px;
  background: linear-gradient(160deg, rgba(30, 41, 59, 0.82) 0%, rgba(15, 23, 42, 0.9) 100%);
  border: 1px solid rgba(148, 163, 184, 0.16);
  box-shadow: 0 24px 60px rgba(2, 6, 23, 0.55), inset 0 1px 0 rgba(255, 255, 255, 0.06);
  -webkit-backdrop-filter: blur(20px);
  backdrop-filter: blur(20px);
  animation: card-in 0.5s ease both;
}

.card-brand {
  display: none;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}

.card-logo {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  object-fit: contain;
}

.card-brand-name {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #fff;
}

.lang-switch {
  position: absolute;
  top: 22px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.lang-switch button {
  padding: 2px 4px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 13px;
  color: #64748b;
  transition: color 0.2s ease;
}

.lang-switch button:hover {
  color: #93c5fd;
}

.lang-switch button.active {
  color: #60a5fa;
  font-weight: 600;
}

.lang-sep {
  color: #334155;
}

.card-title {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;
  color: #f8fafc;
}

.card-subtitle {
  margin: 0 0 28px;
  font-size: 14px;
  color: #94a3b8;
}

.form-content {
  position: relative;
  z-index: 1;
}

.input-group {
  margin-bottom: 20px;
}

.input-label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #cbd5e1;
}

.input-wrap {
  display: flex;
  align-items: center;
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.6);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.input-wrap:hover {
  border-color: rgba(148, 163, 184, 0.4);
}

.input-wrap:focus-within {
  border-color: #3b82f6;
  background: rgba(15, 23, 42, 0.85);
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.18);
}

.input-wrap.is-error {
  border-color: #ef4444;
}

.input-icon {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  color: #64748b;
}

.input-icon svg {
  width: 18px;
  height: 18px;
}

.input-field {
  flex: 1;
  min-width: 0;
  height: 46px;
  padding: 0;
  border: none;
  outline: none;
  -webkit-appearance: none;
  appearance: none;
  background-color: transparent;
  font-size: 15px;
  color: #f1f5f9;
  caret-color: #60a5fa;
}

.input-field::placeholder {
  color: #64748b;
}

/*
 * 覆盖浏览器自动填充（Chrome/Safari）默认的浅色背景，
 * 避免输入内容后输入框变为白色。
 */
.input-field:-webkit-autofill,
.input-field:-webkit-autofill:hover,
.input-field:-webkit-autofill:focus,
.input-field:-webkit-autofill:active {
  -webkit-text-fill-color: #f1f5f9;
  caret-color: #60a5fa;
  border-radius: 9px;
  -webkit-box-shadow: 0 0 0 1000px #0f172a inset;
  box-shadow: 0 0 0 1000px #0f172a inset;
  transition: background-color 5000s ease-in-out 0s;
}

.toggle-password {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 46px;
  border: none;
  border-radius: 8px;
  background: none;
  color: #64748b;
  cursor: pointer;
  transition: color 0.2s ease;
}

.toggle-password:hover {
  color: #93c5fd;
}

.toggle-password:focus-visible {
  outline: 2px solid #3b82f6;
  outline-offset: 2px;
}

.toggle-password svg {
  width: 18px;
  height: 18px;
}

.error-message {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #f87171;
}

.login-button {
  width: 100%;
  height: 48px;
  margin-top: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
  color: #fff;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.35);
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.45);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-button:disabled {
  opacity: 0.75;
  cursor: not-allowed;
}

.login-button:focus-visible {
  outline: 2px solid #93c5fd;
  outline-offset: 2px;
}

.spinner {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  animation: spin 0.7s linear infinite;
}

.card-footer {
  margin-top: 28px;
  padding-top: 18px;
  border-top: 1px solid rgba(148, 163, 184, 0.12);
  text-align: center;
  font-size: 12px;
  color: #64748b;
}

/* ------------------------------------------------------------------
 * 动画
 * ------------------------------------------------------------------ */
@keyframes card-in {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float-a {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(30px, 40px);
  }
}

@keyframes float-b {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(-40px, -30px);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ------------------------------------------------------------------
 * 响应式
 * ------------------------------------------------------------------ */
@media (max-width: 1024px) {
  .brand-panel {
    padding: 44px 40px;
  }

  .brand-title {
    font-size: 36px;
  }

  .form-panel {
    width: min(460px, 100%);
  }
}

@media (max-width: 860px) {
  .brand-panel {
    display: none;
  }

  .card-brand {
    display: flex;
  }

  .form-panel {
    width: 100%;
    padding: 24px;
  }
}

@media (max-width: 480px) {
  .form-panel {
    padding: 16px;
  }

  .login-card {
    padding: 28px 20px;
    border-radius: 16px;
  }

  .card-title {
    font-size: 22px;
  }
}

/* ------------------------------------------------------------------
 * 无障碍：尊重「减少动态效果」偏好
 * ------------------------------------------------------------------ */
@media (prefers-reduced-motion: reduce) {
  .brand-main,
  .login-card {
    animation: none;
  }

  .bg-glow,
  .spinner {
    animation: none;
  }

  .login-button {
    transition: none;
  }
}
</style>
