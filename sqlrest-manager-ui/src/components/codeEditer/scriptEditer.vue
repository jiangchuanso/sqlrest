<template>
  <div>
    <codemirror class="custom-code-script"
                :options="cmOptions"
                @ready="onCmReady"
                @focus="onCmFocus"
                @inputRead="onCmCodeChange">
    </codemirror>
  </div>
</template>

<script>
import { codemirror } from 'vue-codemirror'

import 'codemirror/theme/solarized.css'
import 'codemirror/theme/idea.css'
import 'codemirror/theme/darcula.css'
import 'codemirror/theme/base16-light.css'
import "codemirror/addon/hint/show-hint.css";

require('codemirror/mode/groovy/groovy.js');
require("codemirror/mode/clike/clike.js");
require("codemirror/addon/edit/closebrackets.js");
require("codemirror/lib/codemirror");
require("codemirror/addon/hint/show-hint");

export default {
  name: "scriptEditer",
  components: {
    codemirror
  },
  data () {
    return {
      cmInstance: null, // 当前codemirror实例
      cmOptions: {
        code: "",
        styleActiveLine: true,
        lineNumbers: true,
        mode: 'text/x-groovy',
        theme: 'darcula',
        lint: true,                     // 代码出错提醒
        indentUnit: 4,
        lineWrapping: true, // 是否应滚动或换行以显示长行
        fontSize: 10,
        autofocus: true,
        autoCloseBrackets: true,
        matchBrackets: true, //括号匹配
        lineWrapping: true, //代码折叠
        foldGutter: true,
        gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
        extraKeys: { "Tab": "autocomplete" },  //自动补全
        hintOptions: { // 自定义提示选项
          completeSingle: false, // 当匹配只有一项的时候是否自动补全
          tables: {
            "db": ["selectAll", "selectCount", "selectOne", "page", "insert", "update", "batchUpdate", "delete"]
          }
        }
      },
    }
  },
  props: {
    content: {
      type: String,
      default: ''
    },
    tableHints: {
      type: Object,
    }
  },
  methods: {
    onCmReady (cm) {
      this.cmInstance = cm
      this.cmInstance.setSize('100%', '200px')
      this.cmInstance.setValue(this.content)
    },
    onCmFocus (cm) {
    },
    onCmCodeChange (cm, changeObj) {
      if (/^[a-zA-Z.]/.test(changeObj.text[0])) {
        //如果输入的是字母才提示，空格不提示
        cm.showHint()
      }
    },
    queryContent: function () {
      var sqls = []
      sqls.push(this.cmInstance.getValue())
      return sqls
    }
  },
  watch: {
    content (newVal, OldVal) {
      this.cmOptions.hintOptions.value = newVal
    },
    tableHints (newVal, OldVal) {
      this.cmOptions.hintOptions.tables = newVal
    }
  },
  mounted () {
    //console.log("content:"+this.content)
  },
  created () {
    this.cmOptions.hintOptions.tables = this.tableHints
  }
}
</script>

<style scoped>
.custom-code-script {
  font-size: 13px;
  line-height: 150%;
}
</style>
