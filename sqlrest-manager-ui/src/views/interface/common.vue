<template>
  <el-card>
    <el-row>
      <el-col :span="2">
        <el-button type="primary"
                   size="mini"
                   @click="handleShowTree">
          元数据查看
          <i class="el-icon-d-arrow-left"
             v-if="showTree"></i>
          <i class="el-icon-d-arrow-right"
             v-else></i>
        </el-button>
      </el-col>
      <el-col :span="22">
        <div style="float:right">
          <el-button type="warning"
                     size="mini"
                     @click="handleHelp">
            <i class="el-icon-question">
              帮助</i>
          </el-button>
          <el-button type="primary"
                     size="mini"
                     @click="handleGoBack">
            <i class="el-icon-d-arrow-left">
              返回</i>
          </el-button>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter=2>
      <el-col :span="4"
              v-if="showTree">
        <div class="grid-content bg-purple">
          <el-select placeholder="请选择数据源"
                     v-model="createParam.dataSourceId"
                     @change="loadTreeData">
            <el-option v-for="(item,index) in connectionList"
                       :key="index"
                       :label="`[${item.id}]${item.name}`"
                       :value="item.id"></el-option>
          </el-select>
          <el-tree ref="tree"
                   empty-text="请选择数据源后查看"
                   style="min-height: 500px; max-height: 800px; overflow: auto;"
                   :indent=6
                   :data="treeData"
                   :props="props"
                   :load="loadNode"
                   :expand-on-click-node="true"
                   :highlight-current="true"
                   :render-content="renderContent"
                   lazy>
          </el-tree>
        </div>
      </el-col>
      <el-col :span='showTree?20:24'>
        <div class="grid-content bg-purple">
          <el-form size="mini"
                   :model="createParam"
                   :rules="rules"
                   label-position='left'
                   ref="form">
            <el-tabs type="border-card"
                     v-model="tabActiveName">
              <el-tab-pane label="SQL配置"
                           name="basic">
                <el-row>
                  <el-col :span="6">
                    <el-form-item label="执行"
                                  label-width="65px">
                      <el-radio-group size="small"
                                      @change="agreeEngineChange"
                                      v-model="createParam.engine">
                        <el-radio-button label="SQL"
                                         :disabled="$route.query.id>0 && createParam.engine==='SCRIPT'">SQL语句</el-radio-button>
                        <el-radio-button label="SCRIPT"
                                         :disabled="$route.query.id>0 && createParam.engine==='SQL'">Groovy脚本</el-radio-button>
                      </el-radio-group>
                    </el-form-item>
                  </el-col>
                  <el-col :span="6">
                    <el-input-number v-model="editorHeightNum"
                                     size="small"
                                     :step="20"
                                     step-strictly></el-input-number>
                  </el-col>
                </el-row>
                <el-row v-if="createParam.engine==='SQL'">
                  <el-col :span="24">
                    <el-form-item label-width="65px">
                      <span slot="label"
                            style="display:inline-block;">
                        语句
                        <el-tooltip effect="dark"
                                    content="每个SQL窗口中至多写一条SQL语句，支持Mybatis的动态SQL语法"
                                    placement="bottom">
                          <i class='el-icon-question' />
                        </el-tooltip>
                      </span>
                      <multi-sql-editer ref="sqlEditors"
                                        :editorHeightNum="editorHeightNum"
                                        :tableHints="tableHints"
                                        :tabSqls="createParam.sqls"
                                        :canAddSql="!isOnlyShowDetail"></multi-sql-editer>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row v-if="createParam.engine==='SCRIPT'">
                  <el-col :span="24">
                    <el-form-item label-width="65px">
                      <span slot="label"
                            style="display:inline-block;">
                        脚本
                        <el-tooltip effect="dark"
                                    content="可以编写符合groovy语法格式的脚本内容"
                                    placement="bottom">
                          <i class='el-icon-question' />
                        </el-tooltip>
                      </span>
                      <script-editer ref="scriptEditer"
                                     :editorHeightNum="editorHeightNum"
                                     :content="createParam.script"></script-editer>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-tabs type="border-card"
                         tab-position="left">
                  <el-tab-pane label="入参">
                    <el-row>
                      <el-col :span="3"
                              v-if="createParam.engine==='SQL'">
                        <el-button type="primary"
                                   size="mini"
                                   icon="el-icon-arrow-down"
                                   v-if="!isOnlyShowDetail"
                                   @click="handleParseInputParams">
                          入参解析
                        </el-button>
                      </el-col>
                      <el-col :span="3">
                        <el-button type="primary"
                                   size="mini"
                                   icon="el-icon-arrow-down"
                                   v-if="!isOnlyShowDetail"
                                   @click="handleAddInputParams">
                          添加入参
                        </el-button>
                      </el-col>
                      <el-col :span="3">
                        <el-button type="primary"
                                   size="mini"
                                   icon="el-icon-arrow-down"
                                   v-if="!isOnlyShowDetail"
                                   @click="handleAddPagableParams">
                          分页参数
                        </el-button>
                      </el-col>
                      <el-col :span="15">
                      </el-col>
                    </el-row>
                    <el-table :data="inputParams"
                              :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                              size="mini"
                              default-expand-all
                              row-key="id"
                              :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                              border>
                      <!--如果不是懒加载的话，后端不要设置hasChildren 这个属性，要不然不能树形显示； -->
                      <template slot="empty">
                        <span>请输入sql后点击"入参解析"按钮后解析出这里的入参</span>
                      </template>
                      <el-table-column label="参数名"
                                       min-width="35%">
                        <template slot-scope="scope">
                          <el-input v-model="scope.row.name"
                                    type="string"
                                    :disabled="isOnlyShowDetail"> </el-input>
                        </template>
                      </el-table-column>
                      <el-table-column label="参数位置"
                                       min-width="25%">
                        <template slot-scope="scope">
                          <el-select v-model="scope.row.location"
                                     :disabled="isOnlyShowDetail">
                            <el-option label='header'
                                       value='REQUEST_HEADER'></el-option>
                            <el-option label='body'
                                       value='REQUEST_BODY'></el-option>
                            <el-option label='query'
                                       value='REQUEST_FORM'></el-option>
                          </el-select>
                        </template>
                      </el-table-column>
                      <el-table-column label="参数类型"
                                       min-width="25%">
                        <template slot-scope="scope">
                          <el-select v-model="scope.row.type"
                                     :disabled="isOnlyShowDetail">
                            <el-option v-for="(item,index) in paramTypeList"
                                       :key="index"
                                       :label="item.name"
                                       :value="item.value"
                                       v-if="shouldInputShowOption(item,scope.row)"></el-option>
                          </el-select>
                        </template>
                      </el-table-column>
                      <el-table-column label="数组"
                                       min-width="10%">
                        <template slot-scope="scope">
                          <el-checkbox v-model="scope.row.isArray"
                                       :disabled="isOnlyShowDetail"></el-checkbox>
                        </template>
                      </el-table-column>
                      <el-table-column label="必填"
                                       min-width="10%">
                        <template slot-scope="scope">
                          <el-checkbox v-model="scope.row.required"
                                       :disabled="isOnlyShowDetail"></el-checkbox>
                        </template>
                      </el-table-column>
                      <el-table-column label="默认值"
                                       min-width="25%">
                        <template slot-scope="scope">
                          <el-input v-model="scope.row.defaultValue"
                                    type="string"
                                    :disabled="isOnlyShowDetail "></el-input>
                        </template>
                      </el-table-column>
                      <el-table-column label="描述"
                                       min-width="25%">
                        <template slot-scope="scope">
                          <el-input v-model="scope.row.remark"
                                    type="string"
                                    :disabled="isOnlyShowDetail"></el-input>
                        </template>
                      </el-table-column>
                      <el-table-column label="操作"
                                       v-if="!isOnlyShowDetail"
                                       min-width="25%">
                        <template slot-scope="scope">
                          <el-link icon="el-icon-plus"
                                   v-if="scope.row.type=='OBJECT' && scope.row.location=='REQUEST_BODY'"
                                   @click="addInputSubParamsItem(scope.row)"></el-link>
                          &nbsp;&nbsp;&nbsp;&nbsp;
                          <el-link icon="el-icon-delete"
                                   @click="deleteInputParamsItem(scope.$index,scope.row)"></el-link>
                        </template>
                      </el-table-column>
                    </el-table>
                  </el-tab-pane>
                  <el-tab-pane label="出参">
                    <el-button type="primary"
                               size="mini"
                               icon="el-icon-arrow-down"
                               v-if="!isOnlyShowDetail"
                               @click="handleAddOutputParams">
                      添加出参
                    </el-button>
                    <el-table :data="outputParams"
                              :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                              size="mini"
                              default-expand-all
                              row-key="id"
                              :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                              border>
                      <template slot="empty">
                        <span>请输入sql后成功执行"调试"按钮后解析出这里的出参</span>
                      </template>
                      <el-table-column label="参数名"
                                       min-width="25%">
                        <template slot-scope="scope">
                          <el-input v-model="scope.row.name"
                                    type="string"
                                    :disabled="isOnlyShowDetail"> </el-input>
                        </template>
                      </el-table-column>
                      <el-table-column label="参数类型"
                                       min-width="25%">
                        <template slot-scope="scope">
                          <el-select v-model="scope.row.type"
                                     :disabled="isOnlyShowDetail">
                            <el-option v-for="(item,index) in paramTypeList"
                                       :key="index"
                                       :label="item.name"
                                       :value="item.value"
                                       v-if="shouldOutputShowOption(item,scope.row)"></el-option>
                          </el-select>
                        </template>
                      </el-table-column>
                      <el-table-column label="描述"
                                       min-width="25%">
                        <template slot-scope="scope">
                          <el-input v-model="scope.row.remark"
                                    type="string"
                                    :disabled="isOnlyShowDetail"></el-input>
                        </template>
                      </el-table-column>
                      <el-table-column label="操作"
                                       v-if="!isOnlyShowDetail"
                                       min-width="25%">
                        <template slot-scope="scope">
                          <el-link icon="el-icon-plus"
                                   v-if="scope.row.type=='OBJECT'"
                                   @click="addOutputSubParamsItem(scope.row)"></el-link>
                          &nbsp;&nbsp;&nbsp;&nbsp;
                          <el-link icon="el-icon-delete"
                                   @click="deleteOutputParamsItem(scope.$index,scope.row)"></el-link>
                        </template>
                      </el-table-column>
                    </el-table>
                  </el-tab-pane>
                </el-tabs>
              </el-tab-pane>
              <el-tab-pane label="接口配置"
                           name="detail">
                <el-row>
                  <el-col :span="12">
                    <el-form-item label="路径"
                                  label-width="65px"
                                  style="width:80%"
                                  :required=true
                                  prop="path">
                      <el-input v-model="createParam.path"
                                :disabled="isOnlyShowDetail">
                        <template slot="prepend">{{gatewayApiPrefix}}</template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="方法"
                                  label-width="65px"
                                  style="width:50%"
                                  :required=true
                                  prop="method">
                      <el-select v-model="createParam.method"
                                 :disabled="isOnlyShowDetail">
                        <el-option label="GET"
                                   value="GET"></el-option>
                        <el-option label="PUT"
                                   value="PUT"></el-option>
                        <el-option label="POST"
                                   value="POST"></el-option>
                        <el-option label="DELETE"
                                   value="DELETE"></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="12">
                    <el-form-item label="名称"
                                  label-width="65px"
                                  :required=true
                                  prop="name">
                      <el-input v-model="createParam.name"
                                auto-complete="off"
                                style="width:75%"
                                :disabled="isOnlyShowDetail"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="类型"
                                  style="width:80%"
                                  label-width="65px"
                                  :required=true
                                  prop="contentType">
                      <el-select v-model="createParam.contentType"
                                 :disabled="isOnlyShowDetail">
                        <el-option v-for="(item,index) in contentTypes"
                                   :key="index"
                                   :label="item"
                                   :value="item"></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="12">
                    <el-form-item label="模块"
                                  label-width="65px"
                                  :required=true
                                  style="width:80%"
                                  prop="module">
                      <el-select v-model="createParam.module"
                                 placeholder="请选择"
                                 :disabled="isOnlyShowDetail">
                        <el-option v-for="(item,index) in moduleList"
                                   :key="index"
                                   :label="`[${item.id}]${item.name}`"
                                   :value="item.id"></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="授权"
                                  label-width="65px"
                                  style="width:80%"
                                  :required=true
                                  prop="group">
                      <el-select v-model="createParam.group"
                                 placeholder="请选择"
                                 :disabled="isOnlyShowDetail">
                        <el-option v-for="(item,index) in groupList"
                                   :key="index"
                                   :label="`[${item.id}]${item.name}`"
                                   :value="item.id"></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="24">
                    <el-form-item label="描述"
                                  label-width="60px"
                                  prop="description"
                                  style="width:100%">
                      <el-input type="textarea"
                                v-model="createParam.description"
                                auto-complete="off"></el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-tab-pane>
              <el-tab-pane label="出参格式"
                           name="outputParams">
                <el-row>
                  <el-col :span="12">
                    <div>
                      <el-form-item label-width="120px"
                                    prop="namingStrategy"
                                    style="width:60%">
                        <span slot="label"
                              style="display:inline-block;">
                          命名策略
                          <el-tooltip effect="dark"
                                      content="修改命名策略后，需再次执行“调试”操作以纠正出参列表"
                                      placement="top">
                            <i class='el-icon-question' />
                          </el-tooltip>
                        </span>
                        <el-select v-model="createParam.namingStrategy"
                                   placeholder="请选择"
                                   :disabled="isOnlyShowDetail">
                          <el-option v-for="(item,index) in responseNamingStrategy"
                                     :key="index"
                                     :label="`[${item.key}]${item.value}`"
                                     :value="`${item.key}`"></el-option>
                        </el-select>
                      </el-form-item>
                    </div>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="12">
                    <el-form-item label="数据格式"
                                  label-width="120px"
                                  prop="formatMap"
                                  style="width:60%">
                      <div v-for="item in createParam.formatMap"
                           :key="item.key"
                           v-bind="item">
                        {{item.remark}}:
                        <el-input type="text"
                                  :key="item.key"
                                  v-model="item.value"
                                  :value="item.value"> </el-input>
                      </div>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-tab-pane>
              <el-tab-pane label="缓存配置"
                           name="cacheConfig">
                <el-form-item label="缓存方法"
                              label-width="120px"
                              style="width:60%">
                  <el-select v-model="createParam.cacheKeyType"
                             style="width:40%"
                             :disabled="isOnlyShowDetail">
                    <el-option v-for="item in cacheKeyTypeList"
                               :key="item.value"
                               :label="item.name"
                               :value="item.value"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label-width="120px"
                              style="width:60%"
                              v-show="createParam.cacheKeyType==='SPEL'">
                  <span slot="label"
                        style="display:inline-block;">
                    SpEL表达式
                    <el-tooltip effect="dark"
                                content="必填，可以使用Spring表达式语言(Spring Expression Language，SpEL)计算入参值的缓存KEY,例如：#deptNo或#dept.deptNo等形式"
                                placement="top">
                      <i class='el-icon-question' />
                    </el-tooltip>
                  </span>
                  <el-input v-model="createParam.cacheKeyExpr"
                            auto-complete="off"
                            style="width:75%"
                            :disabled="isOnlyShowDetail"></el-input>
                </el-form-item>
                <el-form-item label="过期时长(秒)"
                              label-width="120px"
                              style="width:60%"
                              v-show="createParam.cacheKeyType==='SPEL'|| createParam.cacheKeyType==='AUTO'">
                  <el-input-number v-model="createParam.cacheExpireSeconds"
                                   size="small"
                                   :min="10"
                                   :step="1"
                                   :disabled="isOnlyShowDetail"
                                   step-strictly></el-input-number>
                </el-form-item>
              </el-tab-pane>
              <el-tab-pane label="流量控制"
                           name="flowControl">
                <el-row>
                  <el-col :span="24">
                    <el-form-item label="是否开启">
                      <el-switch v-model="createParam.flowStatus"
                                 active-color="#13ce66"
                                 :active-value="true"
                                 :inactive-value="false"
                                 active-text="开启"
                                 inactive-text="关闭"
                                 :disabled="isOnlyShowDetail">
                      </el-switch>
                    </el-form-item>
                    <div v-show="createParam.flowStatus">
                      <el-form-item label="阈值类型">
                        <el-radio-group size="small"
                                        v-model="createParam.flowGrade"
                                        :disabled="isOnlyShowDetail"
                                        border>
                          <el-radio :label="1">QPS</el-radio>
                          <el-radio :label="0">并发线程数</el-radio>
                        </el-radio-group>
                      </el-form-item>
                      <el-form-item label="单机阈值">
                        <el-input-number v-model="createParam.flowCount"
                                         size="small"
                                         :step="1"
                                         :disabled="isOnlyShowDetail"
                                         step-strictly></el-input-number>
                      </el-form-item>
                    </div>
                  </el-col>
                </el-row>
              </el-tab-pane>
            </el-tabs>
          </el-form>
        </div>
      </el-col>
    </el-row>
    <el-row v-if="!isOnlyShowDetail">
      <el-col>
        <div style="float: right; padding: 5px">
          <el-button type="primary"
                     size="mini"
                     icon="el-icon-arrow-left"
                     @click="handleDebug">
            调试
          </el-button>
          <el-button type="primary"
                     size="mini"
                     icon="el-icon-arrow-left"
                     @click="handleSave">
            保存
          </el-button>
        </div>
      </el-col>
    </el-row>
    <el-drawer title="接口调试"
               :visible.sync="showDebugDrawer"
               direction="ltr"
               size="65%"
               :with-header="true">
      <el-card>
        <el-row>
          <el-col :span="24">
            <el-table :data="debugParams"
                      :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                      row-key="id"
                      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                      default-expand-all
                      size="mini"
                      border>
              <el-table-column label="参数名"
                               min-width="35%">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.name"
                            :disabled="true"
                            type="string"> </el-input>
                </template>
              </el-table-column>
              <el-table-column label="参数类型"
                               min-width="25%">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.type"
                             :disabled="true">
                    <el-option v-for="(item,index) in paramTypeList"
                               :key="index"
                               :label="item.name"
                               :value="item.value"></el-option>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="数组"
                               min-width="10%">
                <template slot-scope="scope">
                  <el-checkbox v-model="scope.row.isArray"
                               :disabled="true"></el-checkbox>
                </template>
              </el-table-column>
              <el-table-column label="必填"
                               min-width="10%">
                <template slot-scope="scope">
                  <el-checkbox v-model="scope.row.required"
                               :disabled="true"></el-checkbox>
                </template>
              </el-table-column>
              <el-table-column label="描述"
                               min-width="25%">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.remark"
                            :disabled="true"
                            type="string"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="值"
                               min-width="50%">
                <template slot-scope="scope">
                  <div v-if="scope.row.isArray">
                    <el-row v-if="scope.row.type=='OBJECT'">
                      <el-input v-model="scope.row.value"
                                :disabled="true"
                                type="string"></el-input>
                    </el-row>
                    <el-row v-else
                            :gutter="24">
                      <div style="display: inline-flex;justify-content: center;"
                           v-for="(arrayItemValue,arrayItemIndex) in scope.row.arrayValues"
                           :key="arrayItemIndex">
                        <el-col :span="4"><button @click="delArrayValuesItem(scope.row.arrayValues,arrayItemIndex)">-</button></el-col>
                        <el-col :span="16"><el-input v-model="scope.row.arrayValues[arrayItemIndex]"
                                    :disabled="scope.row.type=='OBJECT'"
                                    type="string"></el-input></el-col>
                      </div>
                      <el-col :span="4"><button @click="addArrayValuesItem(scope.row)">+</button></el-col>
                    </el-row>
                  </div>
                  <div v-else>
                    <el-input v-model="scope.row.value"
                              :disabled="scope.row.type=='OBJECT'"
                              type="string"></el-input>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作"
                               v-if="!isOnlyShowDetail"
                               min-width="15%">
                <template slot-scope="scope">
                  <el-link icon="el-icon-delete"
                           @click="deleteDebugParamsItem(scope.$index)"></el-link>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-tooltip effect="dark"
                        content="暂不支持对象数组的入参调试功能，但接口支持对象数组入参"
                        placement="bottom">
              <i class='el-icon-question' />
            </el-tooltip>
            <div style="float: right; padding: 25px">
              <el-button type="primary"
                         size="mini"
                         icon="el-icon-arrow-left"
                         @click="handleExecuteDebug">
                执行调试
              </el-button>
            </div>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-tabs type="border-card">
              <el-tab-pane label="执行结果">
                <json-viewer :value="debugResponse"
                             :expand-depth=5
                             copyable
                             boxed
                             sort></json-viewer>
              </el-tab-pane>
              <el-tab-pane label="执行信息">
                <div class="debug-console-log-text">
                  {{debugConsoleLog}}<br />
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-col>
        </el-row>
      </el-card>
    </el-drawer>
  </el-card>
</template>

<script>
import multiSqlEditer from '@/components/codeEditer/multiSqlEditer'
import scriptEditer from '@/components/codeEditer/scriptEditer'
import urlencode from "urlencode";
import qs from "qs";
import JsonViewer from 'vue-json-viewer';
import Vue from "vue";

export default {
  name: "common",
  data () {
    return {
      tabActiveName: 'basic',
      groupList: [],
      moduleList: [],
      connectionList: [],
      paramTypeList: [
        { name: "整型", value: "LONG" },
        { name: "浮点型", value: "DOUBLE" },
        { name: "字符串", value: "STRING" },
        { name: "日期", value: "DATE" },
        { name: "时间", value: "TIME" },
        { name: "布尔", value: "BOOLEAN" },
        { name: "对象", value: "OBJECT" }
      ],
      contentTypes: ['application/x-www-form-urlencoded', 'application/json'],
      cacheKeyTypeList: [
        { name: "关闭缓存功能", value: "NONE" },
        { name: "哈希生存缓存KEY", value: "AUTO" },
        { name: "SpEL生成缓存KEY", value: "SPEL" },
      ],
      showTree: true,
      editorHeightNum: 300,
      createParam: {
        id: null,
        name: null,
        description: null,
        dataSourceId: null,
        group: null,
        module: null,
        method: null,
        path: null,
        contentType: null,
        engine: 'SQL',
        sqls: [],
        script: '',
        open: false,
        namingStrategy: 'CAMEL_CASE',
        formatMap: null,
        flowStatus: false,
        flowGrade: 1,
        flowCount: 5,
        cacheKeyType: 'NONE',
        cacheKeyExpr: '',
        cacheExpireSeconds: '300',
      },
      showDebugDrawer: false,
      gatewayApiPrefix: 'http://127.0.0.1:8081/api/',
      treeData: [],
      props: {
        label: 'label',
        children: 'children',
        disabled: false,
        isLeaf: false
      },
      tableHints: {
        'mysql': ['user']
      },
      keywordHints: [],
      inputParams: [],
      debugParams: [],
      debugResponse: {},
      debugConsoleLog: "",
      outputParams: [],
      responseNamingStrategy: [],
      responseTypeFormat: [],
      rules: {
        name: [
          {
            required: true,
            message: "名称不能为空",
            trigger: "blur"
          }
        ],
        dataSourceId: [
          {
            required: true,
            message: "数据源必须选择",
            trigger: "change"
          }
        ],
        group: [
          {
            required: true,
            message: "分组必须选择",
            trigger: "change"
          }
        ],
        module: [
          {
            required: true,
            message: "模块必须选择",
            trigger: "change"
          }
        ],
        method: [
          {
            required: true,
            message: "方法必须选择",
            trigger: "change"
          }
        ],
        path: [
          {
            required: true,
            message: "路径必须提供",
            trigger: "blur"
          }
        ],
        contentType: [
          {
            required: true,
            message: "内容类型必须选择",
            trigger: "change"
          }
        ],
      },
    }
  },
  props: {
    isOnlyShowDetail: {
      type: Boolean,
      default: false
    }
  },
  components: { multiSqlEditer, scriptEditer, JsonViewer },
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
    isUpdatePage: function () {
      if (this.$route.query.id) {
        return true;
      }
      return false;
    },
    loadAssignmentDetail: function () {
      if (!this.isUpdatePage()) {
        return;
      }
      this.$http.get(
        "/sqlrest/manager/api/v1/assignment/detail/" + this.$route.query.id
      ).then(res => {
        if (0 === res.data.code) {
          this.showTree = false;
          let detail = res.data.data;
          this.createParam = {
            id: detail.id,
            name: detail.name,
            description: detail.description,
            method: detail.method,
            path: detail.path,
            contentType: detail.contentType,
            open: detail.open,
            group: detail.groupId,
            module: detail.moduleId,
            dataSourceId: detail.datasourceId,
            engine: detail.engine,
            sqls: [],
            script: "",
            namingStrategy: detail.namingStrategy,
            formatMap: detail.formatMap,
            flowStatus: detail.flowStatus,
            flowGrade: detail.flowGrade,
            flowCount: detail.flowCount,
            cacheKeyType: detail.cacheKeyType,
            cacheKeyExpr: detail.cacheKeyExpr,
            cacheExpireSeconds: detail.cacheExpireSeconds,
          }
          this.inputParams = []
          if (detail.params) {
            this.inputParams = detail.params
            for (let item of this.inputParams) {
              if (!item.id) {
                Vue.set(item, 'id', this.uuid());
              }
            }
          }
          this.outputParams = detail.outputs || [];
          if (detail.sqlList && detail.sqlList.length > 0) {
            if (this.createParam.engine === 'SQL') {
              this.createParam.sqls = detail.sqlList.map(obj => obj['sqlText'])
            } else {
              this.createParam.script = detail.sqlList[0].sqlText
            }
          }
        } else {
          if (res.data.message) {
            alert("查询失败," + res.data.message);
          }
        }
      });
    },
    loadConnections: function () {
      this.connectionList = [];
      this.$http({
        method: "GET",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/datasource/list/name",
      }).then(
        res => {
          if (0 === res.data.code) {
            this.connectionList = res.data.data || [];
          } else {
            if (res.data.message) {
              alert("加载数据失败:" + res.data.message);
              this.connectionList = [];
            }
          }
        }
      );
    },
    loadGroups: function () {
      this.groupList = [];
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
            this.groupList = res.data.data || [];
          } else {
            if (res.data.message) {
              alert("加载数据失败:" + res.data.message);
              this.groupList = [];
            }
          }
        }
      );
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
    loadGateway: function () {
      this.$http({
        method: "GET",
        url: "/sqlrest/manager/api/v1/node/prefix"
      }).then(
        res => {
          if (0 === res.data.code) {
            if (res.data.data && typeof res.data.data === 'string') {
              this.gatewayApiPrefix = res.data.data;
            }
          } else {
            if (res.data.message) {
              alert("加载数据失败:" + res.data.message);
            }
          }
        }
      );
    },
    loadKeywordHints: function () {
      this.$http.get(
        "/sqlrest/manager/api/v1/assignment/completions"
      ).then(res => {
        if (0 === res.data.code) {
          this.keywordHints = res.data.data;
        }
      });
    },
    loadResponseNamingStrategy: function () {
      this.$http.get(
        "/sqlrest/manager/api/v1/assignment/response-naming-strategy"
      ).then(res => {
        if (0 === res.data.code) {
          this.responseNamingStrategy = res.data.data;
        }
      });
    },
    loadResponseTypeFormat: function () {
      this.$http.get(
        "/sqlrest/manager/api/v1/assignment/response-type-format"
      ).then(res => {
        if (0 === res.data.code) {
          this.responseTypeFormat = res.data.data;
          if (!this.createParam.formatMap) {
            this.createParam.formatMap = res.data.data;
          }
        }
      });
    },
    loadTreeData: function () {
      if (this.createParam.dataSourceId && this.createParam.dataSourceId > 0 && this.showTree) {
        this.treeData = []
        setTimeout(() => {
          this.$http({
            method: "GET",
            url: "/sqlrest/manager/api/v1/datasource/schemas/get/" + this.createParam.dataSourceId
          }).then(
            res => {
              if (0 === res.data.code) {
                for (let element of res.data.data) {
                  let obj = new Object();
                  obj['label'] = element;
                  obj['parent'] = null;
                  obj['value'] = element;
                  obj['hasChild'] = true;
                  obj['type'] = 'DATABASE';
                  this.treeData.push(obj);
                }
              } else {
                this.$alert("加载失败，原因：" + res.data.message, '数据加载失败');
              }
            }
          );
        }, 500);
      }
    },
    loadNode: function (node, resolve) {
      setTimeout(() => {
        if (node.level === 1) {
          let tableView = [
            {
              'label': '表',
              'parent': this.createParam.dataSourceId,
              'value': node.label,
              'hasChild': true,
              'type': 'TABLE',
            },
            {
              'label': '视图',
              'parent': this.createParam.dataSourceId,
              'value': node.label,
              'hasChild': true,
              'type': 'VIEW',
            }
          ]
          resolve(tableView);
        } else if (node.level === 2) {
          this.loadTablesList(resolve, this.createParam.dataSourceId, node.data.value, node.data.type)
        } else if (node.level === 3) {
          this.loadColumnList(resolve, this.createParam.dataSourceId, node.data.value, node.data.label)
        } else {
          resolve([]);
        }
      }, 500);
    },
    loadTablesList: function (resolve, id, schema, type) {
      var tableType = 'VIEW' === type ? 'views' : 'tables'
      this.$http({
        method: "GET",
        url: "/sqlrest/manager/api/v1/datasource/" + tableType + "/get/" + id + "?schema=" + urlencode(schema)
      }).then(
        res => {
          if (0 === res.data.code) {
            let tableList = []
            let nameList = []
            for (let element of res.data.data) {
              let obj = new Object();
              obj['label'] = element;
              obj['parent'] = id;
              obj['value'] = schema;
              obj['hasChild'] = true;
              obj['type'] = type;
              tableList.push(obj);
              nameList.push(element)
            }

            // SQL关键词提示
            if (this.tableHints[schema] && Array.isArray(this.tableHints[schema])) {
              this.tableHints[schema].push(nameList)
            } else {
              this.tableHints[schema] = nameList;
            }
            if (this.$refs.sqlEditors) {
              this.$refs.sqlEditors.setTableHints(this.tableHints)
            }
            return resolve(tableList);
          } else {
            this.$alert("加载失败，原因：" + res.data.message, '数据加载失败');
          }
        }
      );
    },
    loadColumnList: function (resolve, id, schema, table) {
      this.$http({
        method: "GET",
        url: "/sqlrest/manager/api/v1/datasource/columns/get/" + id + "?schema=" + urlencode(schema) + "&table=" + urlencode(table)
      }).then(
        res => {
          if (0 === res.data.code) {
            let columnList = []
            for (let element of res.data.data) {
              let obj = new Object();
              obj['label'] = element.name;
              obj['parent'] = table;
              obj['value'] = element.name;
              obj['hasChild'] = false;
              obj['type'] = element.type;
              columnList.push(obj);
            }
            return resolve(columnList);
          } else {
            this.$alert("加载失败，原因：" + res.data.message, '数据加载失败');
          }
        }
      );
    },
    renderContent (h, { node, data, store }) {
      // https://www.cnblogs.com/zhoushuang0426/p/11059989.html
      if (node.level === 1) {
        return (
          <div class="custom-tree-node">
            <i class="iconfont icon-shujuku1"></i>
            <el-tooltip class="item" effect="light" placement="left">
              <div slot="content">{node.label}</div>
              <span>{data.label}</span>
            </el-tooltip>
          </div>
        );
      } else if (node.level === 2) {
        var icon_pic = "iconfont icon-shitu_biaoge";
        if (data.type === 'VIEW') {
          icon_pic = "iconfont icon-viewList"
        }
        return (
          <div class="custom-tree-node">
            <i class={icon_pic}></i>
            <span>{data.label}</span>
          </div>
        );
      } else if (node.level === 3) {
        var icon_pic = "iconfont icon-shitu_biaoge";
        if (data.type === 'VIEW') {
          icon_pic = "iconfont icon-viewList"
        }
        return (
          <div class="custom-tree-node">
            <i class={icon_pic}></i>
            <el-tooltip class="item" effect="light" placement="left">
              <div slot="content">{node.label}</div>
              <span>{data.label}</span>
            </el-tooltip>
          </div>
        );
      } else {
        return (
          <div class="custom-tree-node">
            <i class="el-icon-set-up"></i>
            <el-tooltip class="item" effect="light" placement="left">
              <div slot="content">{data.type}</div>
              <span>{data.label}({data.type})</span>
            </el-tooltip>
          </div>
        );
      }

    },
    handleNodeClick: function () {

    },
    handleHelp: function () {
      const url = 'https://www.yuque.com/sanpang-jq7te/nys82g/hur636mthgyhaodb#Wkpmx';
      window.open(url, '_blank');
    },
    handleGoBack: function () {
      this.$router.go(-1);
    },
    handleShowTree: function () {
      let status = !this.showTree;
      this.showTree = status;
      if (this.showTree) {
        this.loadTreeData();
      }
    },
    handleParseInputParams: function () {
      var currTabSql = this.$refs.sqlEditors.queryCurrentTabSql()
      if (/^\s*$/.test(currTabSql)) {
        alert("SQL内容不能为空")
        return
      }
      this.$http({
        method: "POST",
        url: "/sqlrest/manager/api/v1/assignment/parse",
        data: qs.stringify({
          sql: currTabSql
        }),
      }).then(
        res => {
          if (0 === res.data.code) {
            if (res.data.data && res.data.data.length === 0) {
              this.$alert("解析的入参为空", "错误信息",
                {
                  confirmButtonText: "确定",
                  type: "error"
                }
              );
              return
            }
            for (let item of res.data.data) {
              if (!this.inputParams.find(i => i.name === item.name)) {
                let type = "STRING";
                let children = [];
                if (item.children) {
                  for (let child of item.children) {
                    children.push(
                      {
                        id: this.uuid(),
                        name: child.name,
                        location: 'REQUEST_BODY',
                        type: "STRING",
                        isArray: child.isArray,
                        required: true,
                        defaultValue: "",
                        remark: "",
                      }
                    );
                    type = "OBJECT";
                  }
                }
                this.inputParams.push(
                  {
                    id: this.uuid(),
                    name: item.name,
                    location: 'REQUEST_BODY',
                    type: type,
                    isArray: item.isArray,
                    required: true,
                    defaultValue: "",
                    remark: "",
                    children: children,
                  }
                )
              }
            };
          } else {
            if (res.data.message) {
              this.$alert(res.data.message, "错误信息",
                {
                  confirmButtonText: "确定",
                  type: "error"
                }
              );
            }
          }
        }
      );
    },
    agreeEngineChange: function () {
      if (this.createParam.engine === 'SCRIPT') {
        this.$refs.scriptEditer.setTableHints(this.keywordHints);
      }
    },
    handleAddInputParams: function () {
      this.inputParams.push(
        {
          id: this.uuid(),
          name: "",
          location: 'REQUEST_BODY',
          type: "STRING",
          isArray: false,
          required: true,
          defaultValue: "",
          remark: ""
        }
      )
    },
    handleAddPagableParams: function () {
      var add = false
      if (!this.inputParams.find(item => item.name === 'apiPageNum')) {
        add = true
        this.inputParams.push(
          {
            id: this.uuid(),
            name: "apiPageNum",
            type: "LONG",
            location: 'REQUEST_FORM',
            isArray: false,
            required: true,
            defaultValue: "1",
            remark: "页号"
          }
        );
      }
      if (!this.inputParams.find(item => item.name === 'apiPageSize')) {
        add = true
        this.inputParams.push(
          {
            id: this.uuid(),
            name: "apiPageSize",
            type: "LONG",
            location: 'REQUEST_FORM',
            isArray: false,
            required: true,
            defaultValue: "10",
            remark: "页大小"
          },
        )
      }
      if (!add) {
        this.$alert("已经存在分页参数了！", "提示信息",
          {
            confirmButtonText: "确定",
            type: "info"
          }
        );
      }
    },
    shouldInputShowOption: function (item, row) {
      if (this.getInputParamsParentRow(row)) {
        return item.value != 'OBJECT';
      }
      return true;
    },
    getInputParamsParentRow: function (childRow) {
      for (const row of this.inputParams) {
        if (row.children && row.children.includes(childRow)) {
          return row;
        }
      }
      return null;
    },
    deleteInputParamsItem: function (idx, row) {
      const index = this.inputParams.indexOf(row);
      if (index !== -1) {
        this.inputParams.splice(index, 1);
      } else {
        this.deleteInputSubParamsItem(idx, row);
      }
    },
    deleteInputSubParamsItem: function (index, childRow) {
      // 通过 childRow 访问父级行数据
      const parentRow = this.getInputParamsParentRow(childRow);
      if (parentRow) {
        const childIndex = parentRow.children.indexOf(childRow);
        if (childIndex !== -1) {
          parentRow.children.splice(childIndex, 1);
        } else {
          console.warn('Child not found');
        }
      }
    },
    addInputSubParamsItem: function (row) {
      const index = this.inputParams.findIndex(item => row == item)
      if (index !== -1) {
        if (!this.inputParams[index].children) {
          // 如果还没有 children 数组，则创建它
          // 使用 Vue.set 来确保响应性
          Vue.set(this.inputParams[index], 'children', []);
        }
        this.inputParams[index].location = 'REQUEST_BODY';
        this.inputParams[index].type = 'OBJECT';
        this.inputParams[index].children.push(
          {
            id: this.uuid(),
            name: "",
            type: "STRING",
            location: 'REQUEST_BODY',
            isArray: false,
            required: true,
            defaultValue: "",
            remark: ""
          },
        );
      } else {
        row.type = 'STRING';
        this.$alert('只允许嵌套一层，类型被还原为字符串类型', "操作提示",
          {
            confirmButtonText: "确定",
            type: "info"
          }
        );
      }
    },
    checkSqlsOrScriptEmpty: function (sqls) {
      if (sqls === null || sqls === undefined || !Array.isArray(sqls) || sqls.length === 0 || sqls.includes('')) {
        return true
      }
      for (let str in sqls) {
        if (str === null || str === undefined || str.trim() === '' || str.trim().length === 0) {
          return true
        }
      }
      return false
    },
    handleSave: function () {
      this.$refs.form.validate(valid => {
        if (valid) {
          var sqls = []
          var isSql = true;
          if (this.createParam.engine === 'SQL') {
            isSql = true
            sqls = this.$refs.sqlEditors.queryContent()
          } else {
            isSql = false
            sqls = this.$refs.scriptEditer.queryContent()
          }

          if (!this.createParam.dataSourceId) {
            this.$alert('请选择一个数据源来', "错误信息",
              {
                confirmButtonText: "确定",
                type: "error"
              }
            );
            return
          }

          if (this.checkSqlsOrScriptEmpty(sqls)) {
            this.$alert(isSql ? '请检查SQL窗口内容' : '请检查脚本内容', "错误信息",
              {
                confirmButtonText: "确定",
                type: "error"
              }
            );
          } else {
            if (this.isUpdatePage()) {
              this.handleUpdateSave(sqls);
            } else {
              this.handleCreateSave(sqls);
            }
          }
        } else {
          alert("请检查输入");
        }
      });
    },
    handleCreateSave: function (sqls) {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/assignment/create",
        data: JSON.stringify({
          groupId: this.createParam.group,
          moduleId: this.createParam.module,
          datasourceId: this.createParam.dataSourceId,
          name: this.createParam.name,
          description: this.createParam.description,
          method: this.createParam.method,
          contentType: this.createParam.contentType,
          path: this.createParam.path,
          open: this.createParam.open,
          namingStrategy: this.createParam.namingStrategy,
          formatMap: this.createParam.formatMap,
          flowStatus: this.createParam.flowStatus,
          flowGrade: this.createParam.flowGrade,
          flowCount: this.createParam.flowCount,
          cacheKeyType: this.createParam.cacheKeyType,
          cacheKeyExpr: this.createParam.cacheKeyExpr,
          cacheExpireSeconds: this.createParam.cacheExpireSeconds,
          engine: this.createParam.engine,
          contextList: sqls,
          params: this.inputParams,
          outputs: this.outputParams
        })
      }).then(
        res => {
          if (0 === res.data.code) {
            this.$router.push({ path: '/interface/list' });
            this.$message("添加信息成功");
          } else {
            if (res.data.message) {
              this.$alert(res.data.message, "错误信息",
                {
                  confirmButtonText: "确定",
                  type: "error"
                }
              );
            }
          }
        }
      );
    },
    handleUpdateSave: function (sqls) {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/assignment/update",
        data: JSON.stringify({
          id: this.createParam.id,
          groupId: this.createParam.group,
          moduleId: this.createParam.module,
          datasourceId: this.createParam.dataSourceId,
          name: this.createParam.name,
          description: this.createParam.description,
          method: this.createParam.method,
          contentType: this.createParam.contentType,
          path: this.createParam.path,
          open: this.createParam.open,
          namingStrategy: this.createParam.namingStrategy,
          formatMap: this.createParam.formatMap,
          flowStatus: this.createParam.flowStatus,
          flowGrade: this.createParam.flowGrade,
          flowCount: this.createParam.flowCount,
          cacheKeyType: this.createParam.cacheKeyType,
          cacheKeyExpr: this.createParam.cacheKeyExpr,
          cacheExpireSeconds: this.createParam.cacheExpireSeconds,
          engine: this.createParam.engine,
          contextList: sqls,
          params: this.inputParams,
          outputs: this.outputParams
        })
      }).then(
        res => {
          if (0 === res.data.code) {
            this.$router.push({ path: '/interface/list' });
            this.$message("更新信息成功");
          } else {
            if (res.data.message) {
              this.$alert(res.data.message, "错误信息",
                {
                  confirmButtonText: "确定",
                  type: "error"
                }
              );
            }
          }
        }
      );
    },
    handleDebug: function () {
      this.debugResponse = {}
      this.debugConsoleLog = ""
      var sqls = []
      var isSql = true;
      if (this.createParam.engine === 'SQL') {
        isSql = true
        sqls = this.$refs.sqlEditors.queryContent()
      } else {
        isSql = false
        sqls = this.$refs.scriptEditer.queryContent()
      }

      if (!this.createParam.dataSourceId) {
        this.$alert('请选择一个数据源来', "错误信息",
          {
            confirmButtonText: "确定",
            type: "error"
          }
        );
        return
      }

      if (this.checkSqlsOrScriptEmpty(sqls)) {
        this.$alert(isSql ? '请检查SQL窗口内容' : '请检查脚本内容', "错误信息",
          {
            confirmButtonText: "确定",
            type: "error"
          }
        );
      } else {
        this.debugParams = []
        this.inputParams.forEach(item => {
          if (item.children && item.children.length > 0) {
            for (let it of item.children) {
              if (!it.arrayValues) {
                Vue.set(it, 'arrayValues', []);
              }
            }
          }
          this.debugParams.push(
            {
              id: item.id,
              name: item.name,
              type: item.type,
              isArray: item.isArray,
              required: item.required,
              defaultValue: item.defaultValue,
              remark: item.remark,
              value: null,
              arrayValues: [],
              children: item.children
            },
          )
        })
        this.showDebugDrawer = true
      }
    },
    deleteDebugParamsItem: function (index) {
      this.debugParams.splice(index, 1);
    },
    addArrayValuesItem: function (row) {
      row.arrayValues.push('');
    },
    delArrayValuesItem: function (array, index) {
      array.splice(index, 1);
    },
    handleExecuteDebug: function () {
      var sqls = []
      if (this.createParam.engine === 'SQL') {
        sqls = this.$refs.sqlEditors.queryContent()
      } else {
        sqls = this.$refs.scriptEditer.queryContent()
      }

      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/sqlrest/manager/api/v1/assignment/debug",
        data: JSON.stringify({
          dataSourceId: this.createParam.dataSourceId,
          engine: this.createParam.engine,
          namingStrategy: this.createParam.namingStrategy,
          formatMap: this.createParam.formatMap,
          contextList: sqls,
          paramValues: this.debugParams
        })
      }).then(
        res => {
          if (0 === res.data.code) {
            this.debugResponse = res.data.data.answer;
            this.debugConsoleLog = res.data.data.logs;
            this.outputParams = [];
            let arr = res.data.data.types;
            if (Array.isArray(arr) && arr.length === 0) {
              this.$alert("结果集内容为空", "提示信息",
                {
                  confirmButtonText: "确定",
                  type: "info"
                }
              );
            } else {
              for (let item of arr) {
                this.outputParams.push(
                  {
                    id: item.id,
                    name: item.name,
                    type: item.type,
                    isArray: item.isArray,
                    remark: item.remark,
                    children: item.children,
                  }
                )
              }
            }
          } else {
            if (res.data.message) {
              this.$alert(res.data.message, "错误信息",
                {
                  confirmButtonText: "确定",
                  type: "error"
                }
              );
            }
          }
        }
      );
    },
    handleAddOutputParams: function () {
      this.outputParams.push(
        {
          name: '',
          type: 'STRING',
          remark: null
        },
      )
    },
    shouldOutputShowOption: function (item, row) {
      if (this.getOutputParamsParentRow(row)) {
        return item.value != 'OBJECT';
      }
      return true;
    },
    getOutputParamsParentRow: function (childRow) {
      for (const row of this.outputParams) {
        if (row.children && row.children.includes(childRow)) {
          return row;
        }
      }
      return null;
    },
    addOutputSubParamsItem: function (row) {
      const index = this.outputParams.findIndex(item => row == item)
      if (index !== -1) {
        if (!this.outputParams[index].children) {
          // 如果还没有 children 数组，则创建它
          // 使用 Vue.set 来确保响应性
          Vue.set(this.outputParams[index], 'children', []);
        }
        this.outputParams[index].location = 'REQUEST_BODY';
        this.outputParams[index].type = 'OBJECT';
        this.outputParams[index].children.push(
          {
            id: this.uuid(),
            name: "",
            type: "STRING",
            location: 'REQUEST_BODY',
            isArray: false,
            required: true,
            defaultValue: "",
            remark: ""
          },
        );
      } else {
        row.type = 'STRING';
        this.$alert('只允许嵌套一层，类型被还原为字符串类型', "操作提示",
          {
            confirmButtonText: "确定",
            type: "info"
          }
        );
      }
    },
    deleteOutputParamsItem: function (idx, row) {
      const index = this.outputParams.indexOf(row);
      if (index !== -1) {
        this.outputParams.splice(index, 1);
      } else {
        this.deleteOutputSubParamsItem(idx, row);
      }
    },
    deleteOutputSubParamsItem: function (index, childRow) {
      // 通过 childRow 访问父级行数据
      const parentRow = this.getOutputParamsParentRow(childRow);
      if (parentRow) {
        const childIndex = parentRow.children.indexOf(childRow);
        if (childIndex !== -1) {
          parentRow.children.splice(childIndex, 1);
        } else {
          console.warn('Child not found');
        }
      }
    },
  },
  created () {
    this.loadAssignmentDetail();
    this.loadConnections();
    this.loadGroups();
    this.loadModules();
    this.loadGateway();
    this.loadKeywordHints();
    this.loadTreeData();
    this.loadResponseNamingStrategy();
    this.loadResponseTypeFormat();
  },
}
</script>

<style scoped>
.el-card {
  width: 100%;
  height: 100%;
  overflow: auto;
}

.tip-content {
  font-size: 12px;
}

.name-mapper-table,
.name-mapper-table table tr th,
.name-mapper-table table tr td {
  border-collapse: collapse;
  border: 1px solid #e0dddd;
  width: 100%;
}

.el-descriptions__body
  .el-descriptions__table
  .el-descriptions-row
  .el-descriptions-item__label {
  min-width: 20px;
  max-width: 60px;
}

.custom-tree-node span {
  font-size: 12px;
}

.el-col .el-select {
  width: 98%;
}
.el-tree {
  overflow: auto;
}
.el-tree-node__content {
  height: 20px;
}
.el-drawer__wrapper {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  overflow: hidden;
  margin: 0;
}
/deep/ .el-input.is-disabled .el-input__inner {
  color: #5f5e5e !important;
}
/deep/.el-table .cell {
  box-sizing: border-box;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal;
  word-break: break-all;
  line-height: 23px;
  padding-right: 10px;
  display: flex;
  flex-direction: row;
}
/deep/.el-table .cell .el-checkbox__input.is-disabled.is-checked .el-checkbox__inner {
  background-color: #1464dd;
  border-color: #f4f5f8;
}
.debug-console-log-text {
  white-space: pre-line;
}
</style>
