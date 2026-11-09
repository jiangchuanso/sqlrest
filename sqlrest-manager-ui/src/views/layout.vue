<template>
  <el-container class="index-container">
    <el-aside :width="isCollapse?'64px':'250px'">
      <!-- 左侧LOGO -->
      <div class="title">
        <img src="../assets/LOGO.png" />
        <span class="title-text"
              v-if="!isCollapse">{{title}}</span>
      </div>
      <!-- 左侧导航菜单 -->
      <asideBar ref="asideBar"></asideBar>
    </el-aside>
    <el-main>
      <el-header>
        <!-- 折叠图标 -->
        <div @click="handleToggleCollapse"
             class="collapse">
          <i class="el-icon-s-unfold"
             v-if="isCollapse"></i>
          <i class="el-icon-s-fold"
             v-else></i>
        </div>

        <!-- 面包屑导航 -->
        <breadcrumb></breadcrumb>
        <!-- 右侧组件区域 -->
        <div class="header-right">
          <languageSwitch></languageSwitch>
          <userDropdown></userDropdown>
        </div>
      </el-header>

      <!-- 右侧显示区域 -->
      <viewMain></viewMain>
    </el-main>
  </el-container>
</template>

<script>
import asideBar from "@/components/asideBar/asideBar";
import breadcrumb from "@/components/breadcrumb/index";
import userDropdown from "@/components/userDropdown/index"
import viewMain from "@/views/viewer";
import languageSwitch from "@/components/languageSwitch/index";

export default {
  name: "home",
  components: {
    asideBar,
    breadcrumb,
    userDropdown,
    viewMain,
    languageSwitch
  },
  data () {
    return {
      title: "SQLREST",
      isCollapse: null
    };
  },
  computed: {},
  watch: {},
  methods: {
    handleToggleCollapse () {
      let status = !this.isCollapse;
      this.isCollapse = status;
      this.$refs.asideBar.updateCollapse(status);
    }
  },
  created () {
  },
  mounted () { }
};
</script>

<style scoped>
.index-container {
  height: 100%;
}

.el-aside {
  float: left;
  background: var(--sr-sidebar);
  color: #333;
  text-align: left;
  box-shadow: 2px 0 8px rgba(15, 23, 42, 0.06);
  transition: width 0.28s ease;
  overflow-x: hidden;
}

.el-aside .title {
  height: 60px;
  background: var(--sr-sidebar-title);
  line-height: 64px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.el-aside .title img {
  width: 36px;
  height: 36px;
  border-radius: var(--sr-radius-sm);
  vertical-align: middle;
}

.el-aside .title .title-text {
  color: #fff;
  font-weight: 600;
  font-size: 19px;
  letter-spacing: 1px;
  margin-left: 10px;
  vertical-align: middle;
}

.el-header {
  padding: 0px;
  color: var(--sr-text);
  width: 100%;
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  line-height: 60px;
  border-bottom: 1px solid var(--sr-border);
  box-shadow: var(--sr-shadow-sm);
  position: relative;
  z-index: 5;
}

.el-header .header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  padding-right: 20px;
}

.el-header .header-right > * {
  vertical-align: middle;
}

.el-header .collapse {
  display: flex;
  align-items: center;
  height: 100%;
  padding-left: 12px;
  font-size: 22px;
  color: var(--sr-text-secondary);
  margin-right: 20px;
  cursor: pointer;
  transition: var(--sr-transition);
}

.el-header .collapse:hover {
  color: var(--sr-primary);
}

.el-main {
  padding: 0px;
  float: left;
  background-color: var(--sr-bg);
}
</style>
