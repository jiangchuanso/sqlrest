<template>
  <div>
    <el-card>
      <div class="group-list-top">
        <div class="left-search-input-group">
          <div class="left-search-input">
            <el-input placeholder="名称搜索"
                      size="mini"
                      v-model="searchText"
                      :clearable=true
                      style="width:300px"
                      @change="searchByKeyword">
            </el-input>
          </div>
        </div>
        <div class="right-add-button-group">
          <el-button type="primary"
                     size="mini"
                     icon="el-icon-document-add"
                     @click="addGroup">添加</el-button>
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
                         label="名称"
                         show-overflow-tooltip
                         min-width="20%"></el-table-column>
        <el-table-column prop="createTime"
                         label="创建时间"
                         min-width="20%"></el-table-column>
        <el-table-column prop="updateTime"
                         label="更新时间"
                         show-overflow-tooltip
                         min-width="20%"></el-table-column>
        <el-table-column label="操作"
                         min-width="35%">
          <template slot-scope="scope">
            <el-button-group>
              <el-button size="small"
                         type="danger"
                         icon="el-icon-document"
                         @click="handleShowToken(scope.$index, scope.row)"
                         round>查看</el-button>
              <el-button size="small"
                         type="warning"
                         icon="el-icon-edit"
                         @click="handleUpdate(scope.$index, scope.row)"
                         round>编辑</el-button>
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
                       :current-page="currentPageNum"
                       :page-sizes="[5, 10, 20, 40]"
                       :page-size="currentPageSize"
                       layout="total, sizes, prev, pager, next, jumper"
                       :total="totalItemCount"></el-pagination>
      </div>

      <el-dialog title="查看"
                 :visible.sync="ShowTokenDialog">
        <el-form size="mini">
          <el-form-item label="MCP访问令牌"
                        label-width="100px"
                        style="width:100%">
            <el-input type="input"
                      style="width:60%"
                      :spellcheck="false"
                      id="tokenTextInput"
                      v-model="clientTokenValue"></el-input>
            <el-button @click="handleCopyTokenText">点击复制</el-button>
          </el-form-item>
          <el-form-item label="SSE服务地址"
                        label-width="100px"
                        style="width:100%">
            <el-tooltip effect="dark"
                        content="服务器发送事件(SSE)"
                        placement="bottom">
              <i class='el-icon-question' />
            </el-tooltip>
            <el-input type="input"
                      style="width:80%"
                      :spellcheck="false"
                      id="sseAddressTextInput"
                      v-model="serverSseUrlAddress"></el-input>
            <el-button style="width:10%"
                       @click="handleCopySseAddressText">点击复制</el-button>
          </el-form-item>
          <el-form-item label="流式Http地址"
                        label-width="100px"
                        style="width:100%">
            <el-tooltip effect="dark"
                        content="可流式传输的HTTP(StreamableHttp)"
                        placement="bottom">
              <i class='el-icon-question' />
            </el-tooltip>
            <el-input type="input"
                      style="width:80%"
                      :spellcheck="false"
                      id="streamAddressTextInput"
                      v-model="serverStreamUrlAddress"></el-input>
            <el-button style="width:10%"
                       @click="handleCopyStreamAddressText">点击复制</el-button>
          </el-form-item>
        </el-form>
        <span slot="footer">
          <el-button @click="ShowTokenDialog = false">取消</el-button>
        </span>
      </el-dialog>

      <el-dialog title="添加信息"
                 :visible.sync="createFormVisible"
                 :showClose="false"
                 :before-close="handleClose">
        <el-form :model="createform"
                 size="mini"
                 status-icon
                 :rules="rules"
                 ref="createform">
          <el-form-item label="令牌名称"
                        label-width="120px"
                        :required=true
                        prop="name"
                        style="width:85%">
            <el-input v-model="createform.name"
                      auto-complete="off"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer"
             class="dialog-footer">
          <el-button @click="createFormVisible = false">取 消</el-button>
          <el-button type="primary"
                     @click="handleCreate">确 定</el-button>
        </div>
      </el-dialog>

      <el-dialog title="修改信息"
                 :visible.sync="updateFormVisible"
                 :showClose="false"
                 :before-close="handleClose">
        <el-form :model="updateform"
                 size="mini"
                 status-icon
                 :rules="rules"
                 ref="updateform">
          <el-form-item label="令牌名称"
                        label-width="120px"
                        :required=true
                        prop="name"
                        style="width:85%">
            <el-input v-model="updateform.name"
                      auto-complete="off"></el-input>
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
import qs from "qs";

export default {
  name: "group",
  components: {
  },
  data () {
    return {
      loading: true,
      lists: [],
      tableData: [
      ],
      currentPageNum: 1,
      currentPageSize: 10,
      totalItemCount: 0,
      searchText: '',
      ShowTokenDialog: false,
      clientTokenValue: '',
      serverSseUrlAddress: '',
      serverStreamUrlAddress: '',
      createform: {
        name: "",
      },
      updateform: {
        id: 0,
        name: "",
      },
      rules: {
        name: [
          {
            required: true,
            message: "名称不能为空",
            trigger: "blur"
          }
        ]
      },
      createFormVisible: false,
      updateFormVisible: false
    }
  },
  methods: {
    loadData: function () {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1//mcp/client/listAll",
        data: JSON.stringify({
          page: this.currentPageNum,
          size: this.currentPageSize,
          searchText: this.searchText
        })
      }).then(res => {
        if (0 === res.data.code) {
          this.totalItemCount = res.data.pagination.total
          this.tableData = res.data.data;
        } else {
          alert("加载数据失败:" + res.data.message);
        }
      }
      );
    },
    loadManagerAddress: function (token) {
      this.$http({
        method: "GET",
        url: "/sqlrest/manager/api/v1/mcp/client/endpoint"
      }).then(
        res => {
          this.serverSseUrlAddress = '';
          this.serverStreamUrlAddress = '';
          if (0 === res.data.code) {
            this.serverSseUrlAddress = res.data.data.sseAddrPrefix + token;
            this.serverStreamUrlAddress = res.data.data.streamAddrPrefix + token;
          } else {
            if (res.data.message) {
              alert("加载数据失败:" + res.data.message);
            }
          }
        }
      );
    },
    handleClose (done) {
    },
    handleDelete: function (index, row) {
      this.$confirm(
        "此操作将此令牌ID=" + row.id + "删除么, 是否继续?",
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }
      ).then(() => {
        this.$http.delete(
          "/sqlrest/manager/api/v1/mcp/client/delete/" + row.id
        ).then(res => {
          if (0 === res.data.code) {
            this.loadData();
          } else {
            alert("删除失败:" + res.data.message);
          }
        });
      });
    },
    addGroup: function () {
      this.createFormVisible = true;
      this.createform = {};
    },
    handleCreate: function () {
      this.$refs['createform'].validate(valid => {
        if (valid) {
          this.$http({
            method: "POST",
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            },
            url: "/sqlrest/manager/api/v1/mcp/client/create",
            data: qs.stringify({
              name: this.createform.name
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
    handleShowToken: function (index, row) {
      this.loadManagerAddress(row.token);
      this.clientTokenValue = row.token;
      this.ShowTokenDialog = true
    },
    handleCopyTokenText: function () {
      document.getElementById("tokenTextInput").select()
      document.execCommand("copy")
      this.$message.success("复制令牌成功")
    },
    handleCopySseAddressText: function () {
      document.getElementById("sseAddressTextInput").select()
      document.execCommand("copy")
      this.$message.success("复制SSE地址成功")
    },
    handleCopyStreamAddressText: function () {
      document.getElementById("streamAddressTextInput").select()
      document.execCommand("copy")
      this.$message.success("复制StreamableHttp地址成功")
    },
    handleUpdate: function (index, row) {
      this.updateform = JSON.parse(JSON.stringify(row));
      this.updateFormVisible = true;
    },
    handleSave: function () {
      this.$refs['updateform'].validate(valid => {
        if (valid) {
          this.$http({
            method: "POST",
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            },
            url: "/sqlrest/manager/api/v1/mcp/client/update/" + this.updateform.id,
            data: qs.stringify({
              name: this.updateform.name,
            })
          }).then(res => {
            if (0 === res.data.code) {
              this.updateFormVisible = false;
              this.$message("修改成功");
              this.loadData();
              this.updateform = {};
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
      this.currentPageSize = pageSize;
      this.loadData();
    },
    handleCurrentChange: function (currentPage) {
      this.currentPageNum = currentPage;
      this.loadData();
    },
    searchByKeyword: function () {
      this.currentPage = 1;
      this.loadData();
    },
  },
  mounted () {
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
.group-list-top {
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
  margin-right: 5px;
  margin: 10px 5px;
}
</style>
