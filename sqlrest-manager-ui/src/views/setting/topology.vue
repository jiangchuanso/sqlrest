```vue
<template>
  <div class="topology-container"
       :style="{ backgroundColor: backgroundColor }">
    <div class="header">
      <div class="legend-item">
        <div class="legend-color manager-color"></div>
        <div class="legend-info">
          <span class="legend-text">Manager</span>
          <span class="legend-desc">接口管理节点</span>
        </div>
      </div>
      <div class="legend-item">
        <div class="legend-color gateway-color"></div>
        <div class="legend-info">
          <span class="legend-text">Gateway</span>
          <span class="legend-desc">接口网关节点</span>
        </div>
      </div>
      <div class="legend-item">
        <div class="legend-color executor-color"></div>
        <div class="legend-info">
          <span class="legend-text">Executor</span>
          <span class="legend-desc">接口执行节点</span>
        </div>
      </div>
      <button class="refresh-btn"
              @click="refreshData">点击刷新</button>
    </div>
    <div class="topology-content">
      <div id="topology-chart"
           ref="topologyChart"></div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'TopologyView',
  data () {
    return {
      backgroundColor: '#b2c8e2',
      chart: null,
      nodes: [],
      links: [],
      option: {}
    };
  },
  mounted () {
    this.initTopology();
    this.renderChart();
  },
  created () {
    this.loadNodeData();
  },
  methods: {
    initTopology () {
      if (this.chart) {
        this.chart.dispose();
      }
      this.chart = echarts.init(this.$refs.topologyChart);
    },
    loadNodeData () {
      this.$http.get("/sqlrest/manager/api/v1/node/topology").then(res => {
        if (0 === res.data.code) {
          var managerNodes = []
          var gatewayNodes = []
          var executorNodes = []
          for (let node of res.data.data) {
            if (node.serviceId.includes('MANAGER')) {
              managerNodes.push({
                id: node.instanceId,
                name: node.host + ":" + node.port,
                role: 'Manager',
                address: node.host,
                port: node.port,
                memory: node.memory | 65,
                cpu: node.cpu | 33,
                disk: node.disk | 20,
                status: node.status | 'normal'
              })
            } else if (node.serviceId.includes('GATEWAY')) {
              gatewayNodes.push(
                {
                  id: node.instanceId,
                  name: node.host + ":" + node.port,
                  role: 'Gateway',
                  address: node.host,
                  port: node.port,
                  memory: node.memory | 35,
                  cpu: node.cpu | 53,
                  disk: node.disk | 30,
                  status: node.status | 'normal'
                }
              )
            } else if (node.serviceId.includes('EXECUTOR')) {
              executorNodes.push(
                {
                  id: node.instanceId,
                  name: node.host + ":" + node.port,
                  role: 'Executor',
                  address: node.host,
                  port: node.port,
                  memory: node.memory | 25,
                  cpu: node.cpu | 13,
                  disk: node.disk | 20,
                  status: node.status | 'normal'
                }
              )
            }
          }

          // 构建节点数组
          this.nodes = [...managerNodes, ...gatewayNodes, ...executorNodes];
          // 构建连接关系
          this.links = [];
          // Gateway连接到所有Executor
          gatewayNodes.forEach(gateway => {
            executorNodes.forEach(executor => {
              this.links.push({
                source: gateway.id,
                target: executor.id
              });
            });
          });
        } else {
          alert("加载数据失败:" + res.data.message);
          return
        }
      });
    },
    renderChart () {
      // 计算节点位置
      const positions = this.calculatePositions();

      // 为每个节点创建配置
      const seriesData = this.nodes.map(node => {
        let nodeColor = '#409EFF';
        if (node.role === 'Gateway') nodeColor = '#10b981';
        if (node.role === 'Executor') nodeColor = '#f59e0b';
        if (node.role === 'Manager') nodeColor = '#3b82f6';

        return {
          id: node.id,
          name: `${node.name}\n${node.address}\nMem: ${node.memory}% | CPU: ${node.cpu}% | Disk: ${node.disk}%`,
          x: positions[node.id].x,
          y: positions[node.id].y,
          value: `${node.address}`,
          port: `${node.port}`,
          symbolSize: node.role === 'Manager' ? 100 : 90,
          itemStyle: {
            color: nodeColor,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'inside',
            formatter: node.name,
            fontSize: 12,
            fontWeight: 'bold',
            color: '#fff'
          },
          emphasis: {
            itemStyle: {
              borderWidth: 4,
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowOffsetY: 0,
              shadowColor: 'rgba(0, 0, 0, 0.3)'
            }
          }
        };
      });

      this.option = {
        backgroundColor: this.backgroundColor,
        title: {
          text: '',
          subtext: '',
          top: '10',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: function (params) {
            if (params.dataType === 'edge') {
              return `${params.data.source} → ${params.data.target}`;
            }
            const node = params.data;
            return `
              <div style="padding: 10px;">
                <div><strong>${node.name.split('\n')[0]}</strong></div>
                <div>地址: ${node.value}</div>
                <div>端口: ${node.port}</div>
                <div>内存使用率: ${node.name.split('Mem: ')[1].split(' | ')[0]}</div>
                <div>CPU使用率: ${node.name.split('CPU: ')[1].split(' | ')[0]}</div>
                <div>磁盘使用率: ${node.name.split('Disk: ')[1].split('%')[0]}%</div>
              </div>
            `;
          }
        },
        series: [
          {
            type: 'graph',
            layout: 'none',
            symbol: 'roundRect',
            roam: true,
            edgeSymbol: ['none', 'arrow'],
            edgeSymbolSize: [80, 45],
            edgeLabel: {
              show: true
            },
            data: seriesData,
            links: this.links,
            lineStyle: {
              color: '#999',
              opacity: 0.6,
              width: 3,
              curveness: 0
            },
            emphasis: {
              focus: 'adjacency',
              lineStyle: {
                width: 5,
                color: '#333'
              }
            }
          }
        ]
      };

      this.chart.setOption(this.option);
      window.addEventListener('resize', () => {
        this.chart.resize();
      });
    },
    calculatePositions () {
      const positions = {};
      const width = this.$refs.topologyChart.clientWidth || 800;
      const height = this.$refs.topologyChart.clientHeight || 300;

      // 找到各角色节点
      const managerNode = this.nodes.find(node => node.role === 'Manager');
      const gatewayNodes = this.nodes.filter(node => node.role === 'Gateway');
      const executorNodes = this.nodes.filter(node => node.role === 'Executor');

      // 设置Manager位置在顶部中央
      if (managerNode) {
        positions[managerNode.id] = {
          x: (width / 2),
          y: height * 0.01
        };
      }

      // 设置Gateway位置在中间层，平均分布
      if (gatewayNodes.length > 0) {
        const gatewaySpacing = width / (gatewayNodes.length + 1);
        gatewayNodes.forEach((node, index) => {
          positions[node.id] = {
            x: gatewaySpacing * (index + 1),
            y: height * 0.10
          };
        });
      }

      // 设置Executor位置在底部层，平均分布
      if (executorNodes.length > 0) {
        const executorSpacing = width / (executorNodes.length + 1);
        executorNodes.forEach((node, index) => {
          positions[node.id] = {
            x: executorSpacing * (index + 1),
            y: height * 0.25
          };
        });
      }

      return positions;
    },
    updateNodeData () {
      this.loadNodeData();
      // 模拟实时数据更新
      this.nodes.forEach(node => {
        // 随机更新资源使用率
        node.memory = Math.min(100, Math.max(0, node.memory + (Math.random() - 0.5) * 10));
        node.cpu = Math.min(100, Math.max(0, node.cpu + (Math.random() - 0.5) * 10));
        node.disk = Math.min(100, Math.max(0, node.disk + (Math.random() - 0.5) * 2));

        // 随机改变状态
        if (Math.random() > 0.8) {
          node.status = Math.random() > 0.2 ? 'normal' : 'warning';
        }
      });

      // 更新图表
      this.renderChart();
    },
    refreshData () {
      // 模拟数据刷新
      this.updateNodeData();

      // 显示刷新提示
      this.$message({
        message: '数据已刷新',
        type: 'success',
        duration: 1000
      });
    }
  },
  beforeDestroy () {
    if (this.chart) {
      this.chart.dispose();
    }
  }
};
</script>

<style scoped>
.topology-container {
  width: 100%;
  height: 88vh;
  display: flex;
  flex-direction: column;
}

.header {
  padding: 20px;
  background-color: #b2c8e2;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 10;
}

.refresh-btn {
  padding: 10px 20px;
  background-color: #409eff;
  color: rgb(23, 14, 109);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.refresh-btn:hover {
  background-color: #66b1ff;
}

.topology-content {
  flex: 1;
  padding: 5px;
  display: flex;
  justify-content: center;
  align-items: center;
}

#topology-chart {
  width: 100%;
  height: 100%;
  min-height: 400px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.legend {
  display: flex;
  gap: 6px;
  padding: 5px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(226, 232, 240, 0.5);
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  flex-shrink: 0;
}

.manager-color {
  background-color: #3b82f6;
}

.gateway-color {
  background-color: #10b981;
}

.executor-color {
  background-color: #f59e0b;
}

.legend-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.legend-text {
  color: #1e293b;
  font-size: 14px;
  font-weight: 600;
}

.legend-desc {
  color: #64748b;
  font-size: 12px;
  font-weight: 400;
}
</style>