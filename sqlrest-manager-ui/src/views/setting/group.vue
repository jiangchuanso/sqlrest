<template>
  <div>
    <el-card>
      <div class="group-list-top">
        <div class="left-search-input-group">
          <div class="left-search-input">
            <el-input placeholder="请输入名称关键字搜索"
                      size="mini"
                      v-model="searchText"
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
                         label="分组名称"
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
                         @click="handleRelation(scope.$index, scope.row)"
                         round>关联</el-button>
              <el-button size="small"
                         type="warning"
                         icon="el-icon-edit"
                         @click="handleUpdate(scope.$index, scope.row)"
                         round>编辑</el-button>
              <el-button size="small"
                         type="success"
                         v-if="scope.row.id!==1"
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

      <el-dialog title="添加信息"
                 :visible.sync="createFormVisible"
                 :showClose="false"
                 :before-close="handleClose">
        <el-form :model="createform"
                 size="mini"
                 status-icon
                 :rules="rules"
                 ref="createform">
          <el-form-item label="分组名称"
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
          <el-form-item label="分组名称"
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

      <el-dialog title="修改关联信息"
                 :visible.sync="relationFormVisible"
                 :showClose="false"
                 :before-close="handleClose">
        <el-alert title="取消勾选后执行修改操作时，被取消勾选项将被修改关联到id=1的“默认分组”中."
                  type="warning"
                  show-icon>
        </el-alert>
        <el-tree :data="moduleAssignments"
                 :show-checkbox="true"
                 ref="relationTree"
                 node-key="id"
                 :default-checked-keys="initCheckedKeys"
                 highlight-current
                 :props="defaultProps">
        </el-tree>
        <div slot="footer"
             class="dialog-footer">
          <el-button type="primary"
                     @click="relationFormVisible = false">关 闭</el-button>
          <el-button type="danger"
                     v-if="currentGroupId!==1"
                     @click="handleRelationSave">修 改</el-button>
        </div>
      </el-dialog>

    </el-card>
  </div>
</template>

<script>
import qs from "qs";
import Vue from "vue";

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
      createform: {
        title: "",
      },
      updateform: {
        id: 0,
        title: "",
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
      updateFormVisible: false,
      relationFormVisible: false,
      moduleAssignments: [],
      initCheckedKeys: [],
      currentGroupId: 0,
      defaultProps: {
        children: 'children',
        label: 'name'
      }
    }
  },
  methods: {
    uuid: function () {
      var s = [];
      var hexDigits = "0123456789abcdef";
      for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
      }
      s[14] = "4"; // bits 12-15 of the time_hi_and_version field to 0010
      s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1); // bits 6-7 of the clock_seq_hi_and_reserved to 01
      s[8] = s[13] = s[18] = s[23] = "-";

      var uuid = s.join("");
      return uuid;
    },
    loadData: function () {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/group/listAll",
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
    handleClose (done) {
    },
    handleDelete: function (index, row) {
      this.$confirm(
        "此操作将此分组ID=" + row.id + "删除么, 是否继续?",
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }
      ).then(() => {
        this.$http.delete(
          "/sqlrest/manager/api/v1/group/delete/" + row.id
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
            url: "/sqlrest/manager/api/v1/group/create",
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
    handleRelation: function (index, row) {
      this.moduleAssignments = [];
      this.$http.get(
        "/sqlrest/manager/api/v1/module/moduleTree/" + row.id
      ).then(res => {
        if (0 === res.data.code) {
          this.initCheckedKeys = [];
          this.currentGroupId = row.id;
          this.moduleAssignments = res.data.data;
          for (let item of this.moduleAssignments) {
            Vue.set(item, 'id', this.uuid());
            if (item.children) {
              for (let one of item.children) {
                //Vue.set(one, 'disabled', true);
                if (one.selected) {
                  this.initCheckedKeys.push(one.id);
                }
              }
            }
          }
        }
      });
      this.relationFormVisible = true;
    },
    handleRelationSave: function () {
      let checkedKeys = this.$refs.relationTree.getCheckedKeys(true);
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/assignment/group/" + this.currentGroupId,
        data: checkedKeys
      }).then(res => {
        if (0 === res.data.code) {
          this.relationFormVisible = false;
          this.$message("修改关联信息成功.");
        } else {
          alert("修改失败:" + res.data.message);
        }
      });
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
            url: "/sqlrest/manager/api/v1/group/update/" + this.updateform.id,
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
