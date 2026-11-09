# 前端 Vue 约定

> 本文档描述 `sqlrest-manager-ui` 前端项目的约定。

## 项目概况

- **Vue 2 + webpack** 项目，独立于 Maven 构建。
- 位于 `sqlrest-manager-ui/`，Node >= v14.15.4。
- **不被 Maven 构建**——构建产物需手动复制到 `sqlrest-manager/src/main/resources/`。

## 目录结构

```
sqlrest-manager-ui/src/
├── assets/      静态资源
├── components/  公共组件
├── lang/        i18n 国际化（中英文）
├── router/      路由
└── views/       页面视图
```

## 构建与部署流程

```bash
cd sqlrest-manager-ui
npm install
npm run build
```

构建后**手动复制** `sqlrest-manager-ui/dist/*` 到 `sqlrest-manager/src/main/resources/`（覆盖 `static/`、`index.html`）。

- 预构建产物已提交到 `sqlrest-manager/src/main/resources/static/`，正常 Maven 构建会直接使用。
- **修改前端后必须重新构建并复制产物，否则 Maven 打包不会包含最新前端。**

## 技术栈

- Vue 2.5+
- Element UI 2.15+
- vue-router 3
- vue-i18n 8（中英文双语）
- ECharts 5（图表）
- vue-codemirror（代码编辑器）
- axios（HTTP 请求）

## 约定

- **i18n 必须同时维护中英文翻译**——新增文案时在 `lang/` 下同时添加 `zh` 和 `en` 条目。
- API 请求通过 axios 封装，统一处理 token 和错误。
- 视图组件放在 `views/` 下按功能模块组织。
- 开发模式：`npm run dev`（热更新，端口 8080）。
