<template>
  <el-card>
    <div class="container">
      <div>
        <el-button type="primary"
                   size="mini"
                   @click="handleGoBack">
          <i class="el-icon-d-arrow-left">
            返回</i>
        </el-button>
      </div>
      <el-tabs type="border-card">
        <el-tab-pane label="接口定义">
          <el-row class="detail-row">
            <el-col :span="4">
              <i class="el-icon-key">当前版本：</i>
            </el-col>
            <el-col :span="20">
              <el-tag size="small">V{{interfaceDetail.version}}</el-tag>
              <el-button size="small"
                         type="danger"
                         icon="el-icon-timer"
                         @click="handleSwitchVersion(interfaceDetail)"
                         round>切换</el-button>
            </el-col>
          </el-row>
          <el-row class="detail-row">
            <el-col :span="4">
              <i class="el-icon-mic">CommitId：</i>
            </el-col>
            <el-col :span="20">
              <el-tag size="small">{{interfaceDetail.commitId}}</el-tag>
            </el-col>
          </el-row>
          <el-row class="detail-row">
            <el-col :span="4">
              <i class="el-icon-user">接口名称：</i>
            </el-col>
            <el-col :span="20">
              <el-tag size="small">{{interfaceDetail.name}}</el-tag>
            </el-col>
          </el-row>
          <el-row class="detail-row">
            <el-col :span="4">
              <i class="el-icon-attract">接口路径：</i>
            </el-col>
            <el-col :span="20">
              <el-tag size="small"
                      type="danger">{{interfaceDetail.method}}</el-tag>
              <el-tag size="small"
                      type="warning">{{interfaceDetail.path}}</el-tag>
            </el-col>
          </el-row>
          <el-row class="detail-row">
            <el-col :span="4">
              <i class="el-icon-tickets">请求类型：</i>
            </el-col>
            <el-col :span="20">
              <el-tag size="small"
                      type="success">{{interfaceDetail.contentType}}</el-tag>
            </el-col>
          </el-row>
          <el-row class="detail-row">
            <el-col :span="4">
              <i class="el-icon-s-check">访问认证：</i>
            </el-col>
            <el-col :span="20">
              <el-tag size="small"
                      type="danger">{{boolTypeFormat(!interfaceDetail.open)}}</el-tag>
            </el-col>
          </el-row>
          <el-row class="detail-row">
            <el-col :span="4">
              <i class="el-icon-help">接口说明：</i>
            </el-col>
            <el-col :span="20">
              <el-tag size="small"
                      type="info">{{interfaceDetail.description}}</el-tag>
            </el-col>
          </el-row>
          <el-row class="detail-row">
            <el-col :span="4">
              <i class="el-icon-postcard">请求参数：</i>
            </el-col>
            <el-col :span="20">
              <el-table :data="interfaceDetail.inputParams"
                        :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                        size="mini"
                        default-expand-all
                        row-key="id"
                        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                        border>
                <template slot="empty">
                  <span>请求参数为空</span>
                </template>
                <el-table-column label="参数名"
                                 prop="name"
                                 min-width="40%">
                </el-table-column>
                <el-table-column label="参数位置"
                                 prop="location"
                                 min-width="15%">
                  <template slot-scope="scope">
                    {{enumTypeLocationFormat(scope.row.location)}}
                  </template>
                </el-table-column>
                <el-table-column label="参数类型"
                                 prop="type"
                                 min-width="15%">
                  <template slot-scope="scope">
                    {{enumTypeValueFormat(scope.row.type)}}
                  </template>
                </el-table-column>
                <el-table-column label="数组"
                                 min-width="15%">
                  <template slot-scope="scope">
                    {{boolTypeFormat(scope.row.isArray)}}
                  </template>
                </el-table-column>
                <el-table-column label="必填"
                                 min-width="15%">
                  <template slot-scope="scope">
                    {{boolTypeFormat(scope.row.required)}}
                  </template>
                </el-table-column>
                <el-table-column label="默认值"
                                 prop="defaultValue"
                                 min-width="20%">
                  <template slot-scope="scope">
                    {{scope.row.defaultValue}}
                  </template>
                </el-table-column>
                <el-table-column label="描述"
                                 prop="remark"
                                 min-width="25%">
                  <template slot-scope="scope">
                    {{scope.row.remark}}
                  </template>
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
          <el-row class="detail-row">
            <el-col :span="4">
              <i class="el-icon-chat-line-round">响应参数：</i>
            </el-col>
            <el-col :span="20">
              <el-table :data="interfaceDetail.outputParams"
                        :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                        size="mini"
                        default-expand-all
                        row-key="id"
                        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                        border>
                <template slot="empty">
                  <span>响应参数为空</span>
                </template>
                <el-table-column label="参数名"
                                 prop="name"
                                 min-width="25%">
                </el-table-column>
                <el-table-column label="参数类型"
                                 min-width="25%">
                  <template slot-scope="scope">
                    {{enumTypeValueFormat(scope.row.type)}}
                  </template>
                </el-table-column>
                <el-table-column label="描述"
                                 min-width="25%">
                  <template slot-scope="scope">
                    {{scope.row.remark}}
                  </template>
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="访问日志">
          <el-table :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                    :data="accessLogData"
                    size="small"
                    border>
            <el-table-column prop="createTime"
                             label="时间"
                             min-width="20%"></el-table-column>
            <el-table-column label="客户端地址"
                             prop="ipAddr"
                             :show-overflow-tooltip="true"
                             min-width="15%">
            </el-table-column>
            <el-table-column label="执行器地址"
                             prop="executorAddr"
                             :show-overflow-tooltip="true"
                             min-width="15%">
            </el-table-column>
            <el-table-column label="网关地址"
                             prop="gatewayAddr"
                             :show-overflow-tooltip="true"
                             min-width="15%">
            </el-table-column>
            <el-table-column label="状态码"
                             prop="status"
                             :show-overflow-tooltip="true"
                             min-width="12%">
            </el-table-column>
            <el-table-column label="耗时(ms)"
                             prop="duration"
                             :show-overflow-tooltip="true"
                             min-width="12%">
            </el-table-column>
            <el-table-column label="调用方"
                             prop="clientApp"
                             :show-overflow-tooltip="true"
                             min-width="15%">
            </el-table-column>
            <el-table-column label="UserAgent"
                             prop="userAgent"
                             :show-overflow-tooltip="true"
                             min-width="15%">
            </el-table-column>
            <el-table-column label="查看"
                             min-width="20%">
              <template slot-scope="scope">
                <el-link class="btn-text"
                         type="primary"
                         @click="handleShowParam(scope.$index, scope.row)">入参</el-link>
                <label v-if="scope.row.exception"
                       class="btn-style">&nbsp;|&nbsp;</label>
                <el-link class="btn-text"
                         v-if="scope.row.exception"
                         type="primary"
                         @click="handleShowException(scope.$index, scope.row)">异常</el-link>
              </template>
            </el-table-column>
          </el-table>
          <div class="page"
               align="right">
            <el-pagination @size-change="handleAccessSizeChange"
                           @current-change="handleAccessCurrentChange"
                           :current-page="currentAccessPageNum"
                           :page-sizes="[5, 10, 20, 40]"
                           :page-size="currentAccessPageSize"
                           layout="total, sizes, prev, pager, next, jumper"
                           :total="totalAccessItemCount"></el-pagination>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog title="接口请求入参"
               :visible.sync="showParamDialogVisible"
               :showClose="false">
      <json-viewer :value="requestParameters"
                   :expand-depth=10
                   copyable
                   boxed
                   sort></json-viewer>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="info"
                   @click="showParamDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="接口异常信息"
               :visible.sync="showExceptDialogVisible"
               :showClose="false">
      <el-input type="textarea"
                :rows="20"
                :spellcheck="false"
                v-model="exeptionText"></el-input>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="info"
                   @click="showExceptDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="切换线上版本"
               :visible.sync="versionDialogVisible"
               :showClose="false"
               width="40%"
               :before-close="handleClose">
      <el-table :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                :data="versionList"
                highlight-current-row
                size="mini"
                border>
        <template slot="empty">
          <span>版本内容为空，请点击“发版”按钮发布一个版本来</span>
        </template>
        <el-table-column label="选择版本"
                         min-width="10%">
          <template slot-scope="scope">
            <el-radio v-model="selectCommitId"
                      :label="scope.row.commitId">V{{ scope.row.version }}</el-radio>
          </template>
        </el-table-column>
        <el-table-column prop="createTime"
                         label="生成时间"
                         min-width="15%"> </el-table-column>
        <el-table-column prop="description"
                         label="版本描述"
                         show-overflow-tooltip
                         min-width="20%"></el-table-column>
      </el-table>
      <div slot="footer"
           class="dialog-footer">
        <el-button @click="versionDialogVisible = false">取 消</el-button>
        <el-button type="primary"
                   @click="handleDeployVersion">切 换</el-button>
      </div>
    </el-dialog>

  </el-card>
</template>

<script>
import '@/assets/sysicon/iconfont.js'
import JsonViewer from 'vue-json-viewer';

export default {
  data () {
    return {
      paramLocation: [
        { name: "Query", value: "REQUEST_FORM" },
        { name: "Body", value: "REQUEST_BODY" },
        { name: "Header", value: "REQUEST_HEADER" }
      ],
      paramTypeList: [
        { name: "整型", value: "LONG" },
        { name: "浮点型", value: "DOUBLE" },
        { name: "字符串", value: "STRING" },
        { name: "日期", value: "DATE" },
        { name: "时间", value: "TIME" },
        { name: "对象", value: "OBJECT" }
      ],
      showDetail: false,
      tableData: [],
      currentModuleId: 0,
      currentPageNum: 1,
      currentPageSize: 10,
      totalItemCount: 0,
      interfaceDetail: {},
      gatewayApiPrefix: null,
      currentInterfaceId: 0,
      currentCommitId: 0,
      accessLogData: [],
      currentAccessPageNum: 1,
      currentAccessPageSize: 10,
      totalAccessItemCount: 0,
      showParamDialogVisible: false,
      requestParameters: null,
      showExceptDialogVisible: false,
      exeptionText: null,
      versionDialogVisible: false,
      versionList: [],
      selectCommitId: null,
    };
  },
  components: { JsonViewer },
  methods: {
    handleClose () { },
    handleGoBack: function () {
      this.$router.go(-1);
    },
    loadGetwayApiPrefix: function () {
      this.$http({
        method: "GET",
        url: "/sqlrest/manager/api/v1/node/prefix"
      }).then(
        res => {
          if (0 === res.data.code) {
            if (res.data.data && typeof res.data.data === 'string') {
              this.gatewayApiPrefix = res.data.data || "";
            }
          }
        }
      );
    },
    reloadIntefaceDetail: function () {
      if (!this.gatewayApiPrefix) {
        this.loadGetwayApiPrefix();
      }
      this.$http.get(
        "/sqlrest/manager/api/v1/version/show/" + this.currentCommitId
      ).then(res => {
        if (0 === res.data.code) {
          let detail = res.data.data.detail;
          this.interfaceDetail = {
            id: detail.id,
            version: detail.version,
            commitId: detail.commitId,
            name: detail.name,
            description: detail.description,
            method: detail.method,
            path: this.gatewayApiPrefix + detail.path,
            contentType: detail.contentType,
            open: detail.open,
            group: detail.groupId,
            module: detail.moduleId,
            dataSourceId: detail.datasourceId,
            engine: detail.engine,
            inputParams: detail.params,
            outputParams: detail.outputs || [],
          }
        }
      });
    },
    reloadAccessLogList: function () {
      this.$http.get(
        "/sqlrest/manager/api/v1/overview/log/" + this.currentInterfaceId
        + "?page=" + this.currentAccessPageNum + "&size=" + this.currentAccessPageSize
      ).then(res => {
        if (0 === res.data.code) {
          this.totalAccessItemCount = res.data.pagination.total
          this.accessLogData = res.data.data;
        }
      });
    },
    handleShowParam: function (index, row) {
      this.requestParameters = row.parameters;
      this.showParamDialogVisible = true;
    },
    handleShowException: function (index, row) {
      this.exeptionText = row.exception;
      this.showExceptDialogVisible = true;
    },
    handleSizeChange: function (pageSize) {
      this.currentPageSize = pageSize;
      this.reloadInterfaceList()
    },
    handleCurrentChange: function (currentPage) {
      this.currentPageNum = currentPage;
      this.reloadInterfaceList()
    },
    handleAccessSizeChange: function (pageSize) {
      this.currentAccessPageSize = pageSize;
      this.reloadAccessLogList()
    },
    handleAccessCurrentChange: function (currentPage) {
      this.currentAccessPageNum = currentPage;
      this.reloadAccessLogList()
    },
    boolTypeFormat (value) {
      if (value === true) {
        return "是";
      } else {
        return "否";
      }
    },
    returnUnknownValue () {
      return "未知";
    },
    enumTypeLocationFormat (value) {
      for (const item of this.paramLocation) {
        if (item.value === value) {
          return item.name;
        }
      }
      return this.returnUnknownValue();
    },
    enumTypeValueFormat (value) {
      for (const item of this.paramTypeList) {
        if (item.value === value) {
          return item.name;
        }
      }
      return this.returnUnknownValue();
    },
    handleSwitchVersion (detail) {
      this.$http({
        method: "GET",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/version/list/" + detail.id,
      }).then(res => {
        if (0 === res.data.code) {
          this.versionList = res.data.data;
          this.versionDialogVisible = true;
        } else {
          if (res.data.message) {
            alert("获取版本列表失败," + res.data.message);
          }
        }
      });
    },
    handleDeployVersion () {
      if (!this.selectCommitId || this.selectCommitId <= 0) {
        this.$alert("请选择一个版本", "错误信息",
          {
            confirmButtonText: "确定",
            type: "error"
          }
        );
        return;
      }
      if (this.selectCommitId == this.currentCommitId) {
        this.$alert("您选择的版本与当前线上的版本一样,无需进行线上版本切换", "错误信息",
          {
            confirmButtonText: "确定",
            type: "error"
          }
        );
        return;
      }
      console.log("selectCommitId=" + this.selectCommitId + ",currentCommitId=" + this.currentCommitId);
      this.$http({
        method: "PUT",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/assignment/deploy/" + this.interfaceDetail.id + "?commitId=" + this.selectCommitId,
      }).then(res => {
        if (0 === res.data.code) {
          this.currentCommitId = this.selectCommitId
          this.selectCommitId = null;
          this.versionDialogVisible = false;
          this.reloadIntefaceDetail();
          this.$message("线上版本切换成功");
        } else {
          if (res.data.message) {
            alert("上线失败," + res.data.message);
          }
        }
      });
    }
  },
  created () {
    this.currentInterfaceId = this.$route.query.id;
    this.currentCommitId = this.$route.query.commitId;
    this.reloadIntefaceDetail();
    this.reloadAccessLogList();
  }
};
</script>

<style scoped>
.el-card {
  width: 100%;
  height: 100%;
  overflow: auto;
}

.tree-node-text {
  overflow: hidden; /* 隐藏溢出的内容 */
  white-space: nowrap; /* 防止文本换行 */
  text-overflow: ellipsis; /* 显示省略号表示溢出 */
}

.box {
  width: 100%;
  height: 100%;
  display: flex;
  vertical-align: top; /* 确保元素顶部对齐 */
  align-items: flex-start;
}

.resizable {
  height: 100%;
  padding: 0px;
  display: inline-block;
}

.resizer {
  width: 5px;
  height: 200px;
  cursor: ew-resize;
  display: inline-block;
  border-left: 1px solid #dcdfe6;
  margin-left: 5px;
  margin-right: 2px;
}

.resizer:hover {
  background-color: #699eff;
}

.detail-row {
  font-size: 13px;
  padding: 2px;
}

.btn-style {
  color: #e9e9f3;
}

.btn-text {
  font-size: 12px;
  color: #6873ce;
}
</style>
