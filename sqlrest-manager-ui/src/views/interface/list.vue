<template>
  <div>
    <el-card>
      <div class="assignment-list-top">
        <div class="left-search-input-group">
          <div class="left-search-input">
            <el-radio-group v-model="online"
                            @change="handleSearch"
                            size="small">
              <el-radio-button :label="false">开发中</el-radio-button>
              <el-radio-button :label="true">已上线</el-radio-button>
            </el-radio-group>
            <el-select v-model="groupId"
                       size="mini"
                       :clearable="true"
                       style="width:15%"
                       placeholder="选择授权分组">
              <el-option v-for="(item,index) in groupLists"
                         :key="index"
                         :label="item.name"
                         :value="item.id"></el-option>
            </el-select>
            <el-select v-model="moduleId"
                       size="mini"
                       :clearable="true"
                       style="width:15%"
                       placeholder="请选择模块">
              <el-option v-for="(item,index) in moduleLists"
                         :key="index"
                         :label="item.name"
                         :value="item.id"></el-option>
            </el-select>
            <el-select v-model="open"
                       size="mini"
                       :clearable="true"
                       style="width:10%"
                       placeholder="是否公开">
              <el-option :key=true
                         label="是"
                         :value=true></el-option>
              <el-option :key=false
                         label="否"
                         :value=false></el-option>
            </el-select>
            <el-input placeholder="名称搜索"
                      size="mini"
                      v-model="keyword"
                      :clearable=true
                      style="width:15%"
                      @change="searchByKeyword">
            </el-input>
            <el-button type="primary"
                       size="mini"
                       icon="el-icon-search"
                       @click="handleSearch">搜索</el-button>
            <el-switch v-model="apiDocStatus"
                       name="Swagger文档开关"
                       active-color="#13ce66"
                       inactive-color="#ff4949"
                       :active-value=true
                       :inactive-value=false
                       v-if="online"
                       active-text="文档开"
                       inactive-text="文档关"
                       @change="hanldeSwitchApiDoc()">
            </el-switch>
          </div>
        </div>
        <el-button type="warning"
                   size="mini"
                   :disabled="apiDocStatus==false"
                   v-if="online"
                   icon="el-icon-document-add"
                   @click="openOnlineApiDoc">在线文档</el-button>
        <el-button type="primary"
                   size="mini"
                   v-if="!online"
                   icon="el-icon-document-add"
                   @click="handleCreate">新建接口</el-button>
      </div>

      <el-table :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                :data="tableData"
                size="small"
                border>
        <el-table-column prop="id"
                         label="编号"
                         min-width="8%"></el-table-column>
        <el-table-column prop="name"
                         label="名称"
                         show-overflow-tooltip
                         min-width="20%">
          <template slot-scope="scope">
            <el-link class="btn-text"
                     type="primary"
                     @click="handleDetail(scope.$index, scope.row)">{{ scope.row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="路径"
                         show-overflow-tooltip
                         min-width="20%">
          <template slot-scope="scope">
            <el-tag size="medium"
                    class="name-wrapper-tag">{{ scope.row.method }}</el-tag>
            {{ scope.row.path }}
          </template>
        </el-table-column>
        <el-table-column prop="moduleName"
                         label="模块"
                         show-overflow-tooltip
                         min-width="10%"></el-table-column>
        <el-table-column prop="groupName"
                         label="授权分组"
                         show-overflow-tooltip
                         min-width="10%"></el-table-column>
        <el-table-column label="引擎"
                         min-width="10%">
          <template slot-scope="scope">
            <el-tag size="medium"
                    class="name-wrapper-tag">{{ scope.row.engine }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status"
                         label="上线"
                         :formatter="boolFormatPublish"
                         show-overflow-tooltip
                         v-if="online"
                         min-width="8%"></el-table-column>
        <el-table-column prop="open"
                         label="公开"
                         :formatter="boolFormatOpen"
                         v-if="online"
                         show-overflow-tooltip
                         min-width="8%"></el-table-column>
        <el-table-column prop="alarm"
                         label="告警"
                         :formatter="boolFormatAlarm"
                         v-if="online"
                         show-overflow-tooltip
                         min-width="8%"></el-table-column>
        <el-table-column prop="createTime"
                         label="创建时间"
                         min-width="18%"></el-table-column>
        <el-table-column label="操作"
                         min-width="40%">
          <template slot-scope="scope">
            <el-button-group>
              <el-button size="small"
                         type="primary"
                         icon="el-icon-timer"
                         v-if="scope.row.status===false"
                         @click="handleOnline(scope.$index, scope.row)"
                         round>上线</el-button>
              <el-button size="small"
                         type="info"
                         icon="el-icon-delete-location"
                         v-if="scope.row.status===true"
                         @click="handleOffline(scope.$index, scope.row)"
                         round>下线</el-button>
              <el-button size="small"
                         type="warning"
                         icon="el-icon-edit"
                         @click="handleUpdate(scope.$index, scope.row)"
                         round>修改</el-button>
              <el-button size="small"
                         type="success"
                         icon="el-icon-position"
                         @click="handlePublish(scope.$index, scope.row)"
                         round>发版</el-button>
              <el-button size="small"
                         type="danger"
                         icon="el-icon-delete"
                         v-if="scope.row.status===false"
                         @click="handleDelete(scope.$index, scope.row)"
                         round>删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      <div class="page"
           align="right">
        <el-pagination @size-change="handleSizeChange"
                       @current-change="handleCurrentChange"
                       :current-page="currentPage"
                       :page-sizes="[5, 10, 20, 40]"
                       :page-size="pageSize"
                       layout="total, sizes, prev, pager, next, jumper"
                       :total="totalCount"></el-pagination>
      </div>
    </el-card>

    <el-dialog title="发布新版本"
               :visible.sync="publishDialogVisible"
               :showClose="false"
               width="40%"
               :before-close="handleClose">
      <el-form size="mini"
               status-icon>
        <el-form-item label="版本描述"
                      label-width="120px"
                      :required=true
                      style="width:85%">
          <el-input type="textarea"
                    :autosize="{ minRows: 4, maxRows: 10 }"
                    v-model="publishVersionDesc"
                    auto-complete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button @click="publishDialogVisible = false">取 消</el-button>
        <el-button type="primary"
                   @click="handlePublishVersion">发 布</el-button>
      </div>
    </el-dialog>

    <el-dialog title="选择上线版本"
               :visible.sync="versionDialogVisible"
               :showClose="false"
               width="40%"
               :before-close="handleClose">
      <el-table :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                :data="versions"
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
                   @click="handleDeploy">上 线</el-button>
      </div>
    </el-dialog>

    <el-dialog title="请选择打开的在线文档类型"
               :visible.sync="selectOpenApiDocsDialogVisible"
               :showClose="false"
               width="20%"
               :before-close="handleClose">
      <el-select v-model="selectedOpenApiDocType"
                 size="mini"
                 style="width:95%"
                 placeholder="请选择文档类型">
        <el-option v-for="(item,index) in openApiDocs"
                   :key="index"
                   :label="item.key"
                   :value="item.key"></el-option>
      </el-select>
      <div slot="footer"
           class="dialog-footer">
        <el-button @click="selectOpenApiDocsDialogVisible = false">取 消</el-button>
        <el-button type="primary"
                   @click="handleOpenApiDoc">打 开</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
export default {

  data () {
    return {
      loading: true,
      currentPage: 1,
      pageSize: 10,
      totalCount: 2,
      keyword: null,
      groupId: null,
      moduleId: null,
      online: false,
      open: null,
      apiDocStatus: true,
      groupLists: [],
      moduleLists: [],
      tableData: [],
      publishDialogVisible: false,
      publishVersionDesc: '',
      versionDialogVisible: false,
      selectRowId: 0,
      selectCommitId: 0,
      versions: [],
      openApiDocs: [{ key: "swagger" }, { key: "knife4j" }],
      selectOpenApiDocsDialogVisible: false,
      selectedOpenApiDocType: "swagger",
    };
  },
  methods: {
    handleClose (done) {
    },
    loadData: function () {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/assignment/list",
        data: window.JSON.stringify(
          {
            groupId: this.groupId,
            moduleId: this.moduleId,
            online: this.online,
            open: this.open,
            searchText: this.keyword,
            page: this.currentPage,
            size: this.pageSize
          }
        )
      }).then(res => {
        if (0 === res.data.code) {
          this.currentPage = res.data.pagination.page;
          this.pageSize = res.data.pagination.size;
          this.totalCount = res.data.pagination.total;
          this.tableData = res.data.data;
        } else {
          alert("加载列表失败:" + res.data.message);
        }
      }
      );
    },
    loadGroupList () {
      this.groupLists = [];
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/group/listAll",
        data: JSON.stringify({
          page: 1,
          size: 2147483647,
          searchText: null
        })
      }).then(
        res => {
          if (0 === res.data.code) {
            this.groupLists = res.data.data;
          } else {
            alert("加载失败:" + res.data.message);
          }
        }
      );
    },
    loadModuleList () {
      this.moduleLists = [];
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/module/listAll",
        data: JSON.stringify({
          page: 1,
          size: 2147483647,
          searchText: null
        })
      }).then(
        res => {
          if (0 === res.data.code) {
            this.moduleLists = res.data.data;
          } else {
            alert("加载失败:" + res.data.message);
          }
        }
      );
    },
    loadApiDocOpenStatus () {
      this.$http.get(
        "/sqlrest/manager/api/v1/param/value/query?key=apiDocOpen"
      ).then(res => {
        if (0 === res.data.code) {
          this.apiDocStatus = res.data.data;
        } else {
          if (res.data.message) {
            alert("操作失败:" + res.data.message);
          }
        }
      });
    },
    searchByKeyword: function () {
      this.currentPage = 1;
      this.loadData();
    },
    hanldeSwitchApiDoc: function () {
      this.$http.post(
        "/sqlrest/manager/api/v1/param/value/update?key=apiDocOpen&value=" + this.apiDocStatus
      ).then(res => {
        if (0 === res.data.code) {
          this.loadApiDocOpenStatus();
        } else {
          if (res.data.message) {
            alert("操作失败:" + res.data.message);
          }
        }
      });
    },
    boolFormatPublish (row) {
      if (row.status === true) {
        return "V" + row.version;
      } else {
        return "否";
      }
    },
    boolFormatOpen (row) {
      if (row.open === true) {
        return "是";
      } else {
        return "否";
      }
    },
    boolFormatAlarm (row) {
      if (row.alarm === true) {
        return "开";
      } else {
        return "关";
      }
    },
    handleSearch: function () {
      this.loadData();
    },
    handleCreate: function () {
      this.$router.push('/interface/create')
    },
    openOnlineApiDoc: function () {
      this.selectedOpenApiDocType = this.openApiDocs[0].key;
      this.selectOpenApiDocsDialogVisible = true;
    },
    handleOpenApiDoc: function () {
      this.$http({
        method: "GET",
        url: "/sqlrest/manager/api/v1/node/gateway"
      }).then(
        res => {
          if (0 === res.data.code) {
            if (res.data.data && typeof res.data.data === 'string') {
              var url = res.data.data + '/apidoc/' + this.selectedOpenApiDocType + '/index.html';
              window.open(url, '_blank');
            }
          } else {
            if (res.data.message) {
              alert("操作失败:" + res.data.message);
            }
          }
          this.selectOpenApiDocsDialogVisible = false;
        }
      );
    },
    handleDetail: function (index, row) {
      this.$router.push({ path: '/interface/detail', query: { id: row.id } })
    },
    handleUpdate: function (index, row) {
      this.$router.push({ path: '/interface/update', query: { id: row.id } })
    },
    handleDelete: function (index, row) {
      this.$confirm(
        "此操作将此接口ID=" + row.id + "删除么, 是否继续?",
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }
      ).then(() => {
        this.$http.delete(
          "/sqlrest/manager/api/v1/assignment/delete/" + row.id
        ).then(res => {
          if (0 === res.data.code) {
            this.loadData();
          } else {
            if (res.data.message) {
              alert("删除失败:" + res.data.message);
            }
          }
        });
      });
    },
    handlePublish: function (index, row) {
      this.selectRowId = row.id
      this.publishDialogVisible = true
    },
    handlePublishVersion: function () {
      if (!this.publishVersionDesc) {
        this.$alert("版本描述内容不能为空", "错误信息",
          {
            confirmButtonText: "确定",
            type: "error"
          }
        );
        return;
      }
      this.$http({
        method: "PUT",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/assignment/publish",
        data: JSON.stringify({
          id: this.selectRowId,
          description: this.publishVersionDesc,
        })
      }).then(res => {
        if (0 === res.data.code) {
          this.selectRowId = null;
          this.publishVersionDesc = null;
          this.publishDialogVisible = false
          this.$message("发布版本成功");
        } else {
          if (res.data.message) {
            alert("发布版本失败," + res.data.message);
          }
        }
      });
    },
    handleOnline: function (index, row) {
      this.$http({
        method: "GET",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/version/list/" + row.id,
      }).then(res => {
        if (0 === res.data.code) {
          this.versions = res.data.data;
          this.selectRowId = row.id;
          this.versionDialogVisible = true;
        } else {
          if (res.data.message) {
            alert("获取版本列表失败," + res.data.message);
          }
        }
      });
    },
    handleDeploy: function () {
      if (!this.selectCommitId || this.selectCommitId <= 0) {
        this.$alert("请选择一个版本", "错误信息",
          {
            confirmButtonText: "确定",
            type: "error"
          }
        );
        return;
      }
      this.$http({
        method: "PUT",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/assignment/deploy/" + this.selectRowId + "?commitId=" + this.selectCommitId,
      }).then(res => {
        if (0 === res.data.code) {
          this.selectRowId = null;
          this.selectCommitId = null;
          this.versionDialogVisible = false;
          this.$message("上线成功");
          this.loadData();
        } else {
          if (res.data.message) {
            alert("上线失败," + res.data.message);
          }
        }
      });
    },
    handleOffline: function (index, row) {
      this.$http({
        method: "PUT",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/assignment/retire/" + row.id,
      }).then(res => {
        if (0 === res.data.code) {
          this.$message("下线成功");
          this.loadData();
        } else {
          if (res.data.message) {
            alert("下线失败," + res.data.message);
          }
        }
      });
    },
    handleSizeChange: function (pageSize) {
      this.loading = true;
      this.pageSize = pageSize;
      this.loadData();
    },

    handleCurrentChange: function (currentPage) {
      this.loading = true;
      this.currentPage = currentPage;
      this.loadData();
    }
  },
  created () {
    this.loadGroupList();
    this.loadModuleList();
    this.loadApiDocOpenStatus();
    this.loadData();
  },
};
</script>

<style scoped>
.el-card,
.el-message {
  width: 100%;
  height: 100%;
  overflow: auto;
}

.el-table {
  width: 100%;
  height: 100%;
}

.demo-table-expand {
  font-size: 0;
}

.demo-table-expand label {
  width: 90px;
  color: #99a9bf;
}

.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 50%;
}

.el-input.is-disabled .el-input__inner {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
  cursor: pointer;
}

.assignment-list-top {
  width: 100%;
  display: flex;
  justify-content: space-between;
  padding: 5px;
}

.left-search-input-group {
  width: calc(100% - 100px);
  margin-right: auto;
  display: flex;
  justify-content: space-between;
}
.left-search-input {
  margin-right: auto;
}
.right-add-button-group {
  width: 100px;
  margin-left: auto;
  display: flex;
  justify-content: space-between;
}
</style>
