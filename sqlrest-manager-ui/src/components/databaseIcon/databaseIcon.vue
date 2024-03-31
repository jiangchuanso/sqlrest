<template>
  <div style="display: inline-block">
    <i class="iconfont icon-my-SQL db" v-if="type == 'MYSQL'"></i>
    <i class="iconfont icon-postgresql db" v-if="type == 'POSTGRESQL'"></i>
    <i class="iconfont icon-hive db" v-if="type == 'HIVE'"></i>
    <i class="iconfont icon-SQLServer db" v-if="type == 'SQLSERVER'"></i>
    <i class="iconfont icon-clickhouse2 db" v-if="type == 'CLICKHOUSE'"></i>
    <i class="iconfont icon-Kylin db" v-if="type == 'kylin'"></i>
    <i class="iconfont icon-oracle db" v-if="type == 'ORACLE'"></i>
    <i class="iconfont icon-OTHER db" v-if="type == 'others'"></i>
  </div>
</template>

<script>
export default {
  name: "databaseIcon",
  props:['type']
}
</script>

<style scoped>
.db {
  color: #0698a5;
  font-size: 18px;
  font-weight: 500;
  margin-right: 5px;
}
</style>