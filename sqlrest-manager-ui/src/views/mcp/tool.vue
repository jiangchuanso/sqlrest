<template>
  <div>
    <el-card>
      <div class="client-list-top">
        <div class="left-search-input-group">
          <div class="left-search-input">
            <el-input placeholder="请输入名称关键字搜索"
                      size="mini"
                      v-model="keyword"
                      @change="searchByKeyword"
                      :clearable=true
                      style="width:300px">
            </el-input>
          </div>
        </div>
        <div class="right-add-button-group">
          <el-button type="primary"
                     size="mini"
                     icon="el-icon-document-add"
                     @click="handleAdd">添加</el-button>
        </div>
      </div>

      <el-table :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                :data="tableData"
                size="small"
                border>
        <el-table-column prop="id"
                         label="编号"
                         min-width="5%"></el-table-column>
        <el-table-column prop="name"
                         label="工具名称"
                         show-overflow-tooltip
                         min-width="20%"></el-table-column>
        <el-table-column prop="description"
                         label="描述"
                         show-overflow-tooltip
                         min-width="15%"></el-table-column>
        <el-table-column prop="apiName"
                         label="链接接口"
                         :formatter="stringFormatApiInfo"
                         show-overflow-tooltip
                         min-width="15%"></el-table-column>
        <el-table-column prop="createTime"
                         label="创建时间"
                         min-width="15%">
        </el-table-column>
        <el-table-column prop="updateTime"
                         label="更新时间"
                         min-width="15%">
        </el-table-column>
        <el-table-column label="操作"
                         min-width="35%">
          <template slot-scope="scope">
            <el-button-group>
              <el-button size="small"
                         type="warning"
                         icon="el-icon-document"
                         @click="handleLink(scope.$index, scope.row)"
                         round>跳转</el-button>
              <el-button size="small"
                         type="danger"
                         icon="el-icon-edit"
                         @click="handleUpdate(scope.$index, scope.row)"
                         round>修改</el-button>
              <el-button size="small"
                         type="success"
                         icon="el-icon-delete"
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

      <el-dialog title="添加工具"
                 :visible.sync="createFormVisible"
                 :showClose="false"
                 :before-close="handleClose">
        <el-form :model="createform"
                 size="mini"
                 status-icon
                 :rules="rules"
                 ref="createform">
          <el-form-item label-width="120px"
                        :required=true
                        prop="name"
                        style="width:85%">
            <span slot="label"
                  style="display:inline-block;">
              工具名称
              <el-tooltip effect="dark"
                          content="tool的名称，即函数名，建议采用英文字母、数字及下划线命名"
                          placement="bottom">
                <i class='el-icon-question' />
              </el-tooltip>
            </span>
            <el-input v-model="createform.name"
                      auto-complete="off"></el-input>
          </el-form-item>
          <el-form-item label-width="120px"
                        :required=true
                        prop="description"
                        style="width:85%">
            <span slot="label"
                  style="display:inline-block;">
              工具描述
              <el-tooltip effect="dark"
                          content="tool的描述，请尽量详细描述此项，以便大模型能够正确使用到此tool"
                          placement="bottom">
                <i class='el-icon-question' />
              </el-tooltip>
            </span>
            <el-input type="textarea"
                      :rows="6"
                      :spellcheck="false"
                      placeholder="请输入"
                      v-model="createform.description"
                      auto-complete="off">
            </el-input>
          </el-form-item>
          <el-form-item label="模块名称"
                        label-width="120px"
                        :required=true
                        style="width:85%">
            <el-select v-model="createform.moduleId"
                       @change="selectChangedModule"
                       placeholder="请选择">
              <el-option v-for="(item,index) in moduleList"
                         :key="index"
                         :label="`[${item.id}] ${item.name}`"
                         :value="item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="接口名称"
                        label-width="120px"
                        :required=true
                        style="width:85%">
            <el-select v-model="createform.apiId"
                       placeholder="请选择">
              <el-option v-for="(item,index) in apiList"
                         :key="index"
                         :label="`[${item.id}] ${item.name}`"
                         :value="item.id"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer"
             class="dialog-footer">
          <el-button @click="createFormVisible = false">取 消</el-button>
          <el-button type="primary"
                     @click="handleCreate">确 定</el-button>
        </div>
      </el-dialog>

      <el-dialog title="修改工具"
                 :visible.sync="updateFormVisible"
                 :showClose="false"
                 :before-close="handleClose">
        <el-form :model="updateform"
                 size="mini"
                 status-icon
                 :rules="rules"
                 ref="updateform">
          <el-form-item label-width="120px"
                        :required=true
                        prop="name"
                        style="width:85%">
            <span slot="label"
                  style="display:inline-block;">
              工具名称
              <el-tooltip effect="dark"
                          content="tool的名称，即函数名，建议采用英文字母、数字及下划线命名"
                          placement="bottom">
                <i class='el-icon-question' />
              </el-tooltip>
            </span>
            <el-input v-model="updateform.name"
                      auto-complete="off"></el-input>
          </el-form-item>
          <el-form-item label-width="120px"
                        :required=true
                        prop="description"
                        style="width:85%">
            <span slot="label"
                  style="display:inline-block;">
              工具描述
              <el-tooltip effect="dark"
                          content="tool的描述，请尽量详细描述此项，以便大模型能够正确使用到此tool"
                          placement="bottom">
                <i class='el-icon-question' />
              </el-tooltip>
            </span>
            <el-input type="textarea"
                      :rows="6"
                      :spellcheck="false"
                      placeholder="请输入"
                      v-model="updateform.description"
                      auto-complete="off">
            </el-input>
          </el-form-item>
          <el-form-item label="模块名称"
                        label-width="120px"
                        :required=true
                        style="width:85%">
            <el-select v-model="updateform.moduleId"
                       @change="selectChangedModule"
                       placeholder="请选择">
              <el-option v-for="(item,index) in moduleList"
                         :key="index"
                         :label="`[${item.id}] ${item.name}`"
                         :value="item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="接口名称"
                        label-width="120px"
                        :required=true
                        style="width:85%">
            <el-select v-model="updateform.apiId"
                       placeholder="请选择">
              <el-option v-for="(item,index) in apiList"
                         :key="index"
                         :label="`[${item.id}] ${item.name}`"
                         :value="item.id"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer"
             class="dialog-footer">
          <el-button @click="updateFormVisible = false">取 消</el-button>
          <el-button type="primary"
                     @click="handleSave">确 定</el-button>
        </div>
      </el-dialog>

    </el-card>
  </div>
</template>

<script>

export default {
  name: "client",
  components: {
  },
  data () {
    return {
      loading: true,
      keyword: null,
      lists: [],
      currentPage: 1,
      pageSize: 10,
      totalCount: 0,
      tableData: [
      ],
      groups: [],
      clientId: 0,
      selectList: [],
      moduleList: [],
      apiList: [],
      createform: {
        name: "",
        description: "",
        moduleId: null,
        apiId: null,
      },
      updateform: {
        id: 0,
        name: "",
        description: "",
        moduleId: null,
        apiId: null,
      },
      rules: {
        name: [
          {
            required: true,
            message: "名称不能为空",
            trigger: "blur"
          }
        ],
        description: [
          {
            required: true,
            message: "描述不能为空",
            trigger: "blur"
          }
        ]
      },
      createFormVisible: false,
      updateFormVisible: false,
    }
  },
  methods: {
    loadData: function () {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/mcp/tool/listAll",
        data: JSON.stringify({
          searchText: this.keyword,
          page: this.currentPage,
          size: this.pageSize
        })
      }).then(res => {
        if (0 === res.data.code) {
          this.currentPage = res.data.pagination.page;
          this.pageSize = res.data.pagination.size;
          this.totalCount = res.data.pagination.total;
          this.tableData = res.data.data;
        } else {
          alert("加载数据失败:" + res.data.message);
        }
      });
    },
    loadModules: function () {
      this.moduleList = [];
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
            this.moduleList = res.data.data || [];
          } else {
            if (res.data.message) {
              alert("加载数据失败:" + res.data.message);
              this.moduleList = [];
            }
          }
        }
      );
    },
    stringFormatApiInfo (row, column) {
      return "[" + row.apiMethod + "]" + row.apiPath;
    },
    handleClose (done) {
    },
    handleLink: function (index, row) {
      this.$router.push('/interface/detail?id=' + row.apiId);
    },
    handleAdd: function () {
      this.loadModules();
      this.createFormVisible = true;
    },
    handleUpdate: function (index, row) {
      this.loadModules();
      this.selectChangedModule(row.moduleId);
      this.updateform = {
        id: row.id,
        name: row.name,
        description: row.description,
        moduleId: row.moduleId,
        apiId: row.apiId,
      }
      this.updateFormVisible = true;
    },
    selectChangedModule: function (value) {
      this.apiList = [];
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/assignment/list",
        data: window.JSON.stringify(
          {
            moduleId: value,
            publish: true,
            page: 1,
            size: 2147483647
          }
        )
      }).then(res => {
        if (0 === res.data.code) {
          this.apiList = res.data.data;
        } else {
          alert("加载列表失败:" + res.data.message);
        }
      }
      );
    },
    handleDelete: function (index, row) {
      this.$confirm(
        "此操作将此工具ID=" + row.id + "删除么, 是否继续?",
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }
      ).then(() => {
        this.$http.delete(
          "/sqlrest/manager/api/v1/mcp/tool/delete/" + row.id
        ).then(res => {
          if (0 === res.data.code) {
            this.loadData();
          } else {
            alert("删除失败:" + res.data.message);
          }
        });
      });
    },
    handleCreate: function () {
      this.$refs['createform'].validate(valid => {
        if (valid) {
          this.$http({
            method: "POST",
            headers: {
              'Content-Type': 'application/json'
            },
            url: "/sqlrest/manager/api/v1/mcp/tool/create",
            data: JSON.stringify({
              name: this.createform.name,
              description: this.createform.description,
              apiId: this.createform.apiId,
            })
          }).then(res => {
            if (0 === res.data.code) {
              this.createFormVisible = false;
              this.$message("添加成功");
              this.createform = {};
              this.loadData();
            } else {
              alert("添加失败:" + res.data.message);
            }
          });
        } else {
          alert("请检查输入");
        }
      });
    },
    handleSave: function () {
      this.$refs['updateform'].validate(valid => {
        if (valid) {
          this.$http({
            method: "POST",
            headers: {
              'Content-Type': 'application/json'
            },
            url: "/sqlrest/manager/api/v1/mcp/tool/update",
            data: JSON.stringify({
              id: this.updateform.id,
              name: this.updateform.name,
              description: this.updateform.description,
              apiId: this.updateform.apiId,
            })
          }).then(res => {
            if (0 === res.data.code) {
              this.updateFormVisible = false;
              this.$message("修改成功");
              this.updateform = {};
              this.loadData();
            } else {
              alert("修改失败:" + res.data.message);
            }
          });
        } else {
          alert("请检查输入");
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
    },
    searchByKeyword: function () {
      this.currentPage = 1;
      this.loadData();
    },
  },
  created () {
    this.loadData();
  }
};
</script>

<style scoped>
.el-table {
  width: 100%;
  height: 100%;
}
.el-card,
.el-message {
  width: 100%;
  height: 100%;
  overflow: auto;
}
.client-list-top {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

.left-search-input-group {
  width: calc(100% - 100px);
  margin-right: auto;
  display: flex;
  justify-content: space-between;
}
.left-search-input {
  width: 300px;
  margin-right: auto;
  margin: 10px 5px;
}
.right-add-button-group {
  width: 100px;
  margin-left: auto;
  margin: 10px 5px;
}
</style>
