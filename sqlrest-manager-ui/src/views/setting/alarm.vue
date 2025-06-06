<template>
  <div>
    <el-card>
      <el-form label-width="200px">
        <el-form-item label="告警配置">
          <el-switch v-model="status"
                     active-color="#13ce66"
                     active-value="ON"
                     inactive-value="OFF"
                     active-text="开启"
                     inactive-text="关闭">
          </el-switch>
        </el-form-item>
        <div v-show="status=='ON'">
          <el-form-item label="接口端点">
            <span slot="label"
                  style="display:inline-block;">
              接口端点
              <el-tooltip effect="dark"
                          content="接口端点请查阅对应的告警系统所提供的消息发送接口"
                          placement="bottom">
                <i class='el-icon-question' />
              </el-tooltip>
            </span>
            <el-input v-model="endpoint"
                      placeholder="请输入接口的路径，例如：http://127.0.0.1:8000/api/v1/message/send">
            </el-input>
          </el-form-item>
          <el-form-item label="入参格式">
            <span slot="label"
                  style="display:inline-block;">
              入参格式
              <el-tooltip effect="dark"
                          content="接口的入参数据格式，目前只支持Content-Type为application/json"
                          placement="bottom">
                <i class='el-icon-question' />
              </el-tooltip>
            </span>
            <el-select v-model="contentType">
              <el-option label="application/json"
                         value="application/json"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="入参模板">
            <span slot="label"
                  style="display:inline-block;">
              入参模板
              <el-tooltip effect="dark"
                          content="入参数据格式请查阅对应的告警系统；模板支持的所有变量参数请点击“测试”按钮查看。"
                          placement="bottom">
                <i class='el-icon-question' />
              </el-tooltip>
            </span>
            <el-input type="textarea"
                      :autosize="{ minRows: 8, maxRows: 20 }"
                      v-model="inputTemplate"
                      placeholder="请输入接口的入参内容模板，变参部分请以${xxx}占位.">
            </el-input>
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary"
                     v-show="status=='ON'"
                     @click="handleShowTest"
                     plain>测试</el-button>
          <el-button type="primary"
                     @click="handleSave"
                     plain>保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog title="测试告警配置"
               :visible.sync="testFormVisible"
               :showClose="false"
               :before-close="handleClose">
      <el-form label-width="200px">
        <el-form-item v-for="item in dataModel"
                      :key="item.key"
                      v-bind="item"
                      :label="`\$\{${item.key}\}`">
          <el-input type="text"
                    :key="item.key"
                    v-model="item.value"
                    :value="item.value"> </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary"
                   @click="handleSendTest"
                   plain>测试</el-button>
        <el-button type="primary"
                   @click="testFormVisible = false"
                   plain>关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>

export default {
  name: "alarm",
  data () {
    return {
      status: "ON",
      endpoint: "",
      contentType: "application/json",
      inputTemplate: "",
      testFormVisible: false,
      dataModel: {},
    }
  },
  methods: {
    loadData: function () {
      this.$http.get("/sqlrest/manager/api/v1/alarm/detail").then(res => {
        if (0 === res.data.code) {
          this.status = res.data.data.status;
          this.endpoint = res.data.data.endpoint;
          this.contentType = res.data.data.contentType;
          this.inputTemplate = res.data.data.inputTemplate;
        } else {
          alert("加载数据失败:" + res.data.message);
        }
      }
      );
    },
    handleShowTest () {
      this.$http.get("/sqlrest/manager/api/v1/alarm/example").then(res => {
        if (0 === res.data.code) {
          this.dataModel = res.data.data;
          this.testFormVisible = true;
        } else {
          alert("加载数据失败:" + res.data.message);
          return;
        }
      }
      );
    },
    handleClose (done) {
    },
    handleSendTest () {
      if (this.status === 'ON' && (!this.endpoint || /^\s*$/.test(this.endpoint))) {
        alert('接口端点不能为空!')
        return
      }
      if (this.status === 'ON' && (!this.contentType || /^\s*$/.test(this.contentType))) {
        alert('入参类型必须选择!')
        return
      }
      if (this.status === 'ON' && (!this.inputTemplate || /^\s*$/.test(this.inputTemplate))) {
        alert('入参模板必须输入!')
        return
      }
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/alarm/test",
        data: JSON.stringify({
          endpoint: this.endpoint,
          contentType: this.contentType,
          inputTemplate: this.inputTemplate,
          dataModel: this.dataModel
        })
      }).then(res => {
        if (0 === res.data.code) {
          this.$alert("发送测试成功，请去告警系统查看发送的告警日志信息", "提示信息",
            {
              confirmButtonText: "确定",
              type: "info"
            }
          );
        } else {
          this.$alert(res.data.message, "提示信息",
            {
              confirmButtonText: "确定",
              type: "error"
            }
          );
        }
      });
    },
    handleSave () {
      if (this.status === 'ON' && (!this.endpoint || /^\s*$/.test(this.endpoint))) {
        alert('接口端点不能为空!')
        return
      }
      if (this.status === 'ON' && (!this.contentType || /^\s*$/.test(this.contentType))) {
        alert('入参类型必须选择!')
        return
      }
      if (this.status === 'ON' && (!this.inputTemplate || /^\s*$/.test(this.inputTemplate))) {
        alert('入参模板必须输入!')
        return
      }
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/alarm/save",
        data: JSON.stringify({
          status: this.status,
          endpoint: this.endpoint,
          contentType: this.contentType,
          inputTemplate: this.inputTemplate
        })
      }).then(res => {
        if (0 === res.data.code) {
          this.$alert("告警配置保存成功", "提示信息",
            {
              confirmButtonText: "确定",
              type: "info"
            }
          );
          this.loadData();
        } else {
          alert("保存失败:" + res.data.message);
        }
      });
    }
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
</style>
