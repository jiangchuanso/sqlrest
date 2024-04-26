# SQLREST

> 将数据库的SQL生成RESTful风格的http接口的工具

## 一、工具介绍

### 1、功能介绍

一句话, sqlrest工具提供快速构建RESTful的API接口工具，包括SQl方式和脚本方式。功能包括：

- SQL方式构建RESTful接口
> 提供类似mybatis的动态SQL语法方式构建接口。

- 脚本方式构建RESTful接口
> 基于magic-script脚本的语法方式构建复杂场景下的接口。

- 支持接口的token安全认证功能
> 执行器支持生成token及token认证。

- 支持生成在线接口文档功能
> 基于swagger-ui提供生成在线接口文档功能。

### 2、支持的数据库

- 甲骨文的Oracle
- 微软的Microsoft SQLServer
- MySQL
- MariaDB
- PostgreSQL
- Greenplum(需使用PostgreSQL类型)
- IBM的DB2
- Sybase数据库
- 国产达梦数据库DMDB
- 国产人大金仓数据库Kingbase8
- 国产翰高数据库HighGo
- 国产神通数据库Oscar
- 国产南大通用数据库GBase8a
- Apache Hive
- SQLite3
- OpenGuass
- ClickHouse

### 3、模块结构功能

```
└── sqlrest
    ├── sqlrest-common           // sqlrest通用定义模块
    ├── sqlrest-template         // sqlrest的SQL内容模板模块
    ├── sqlrest-script           // sqlrest的接口脚本解析模块
    ├── sqlrest-cache            // sqlrest执行器缓存模块
    ├── sqlrest-persistence      // sqlrest的数据库持久化模块
    ├── sqlrest-core             // sqlrest-core的接口实现模块
    ├── sqlrest-gateway          // Gateway网关节点
    ├── sqlrest-executor         // Executor接口执行节点
    ├── sqlrest-manager          // Manager管理节点
    ├── sqlrest-manager-ui       // 基于Vue2的Manager前段WEB交互页面
    ├── sqlrest-dist             // 基于maven-assembly-plugin插件的项目打包模块
```

## 二、编译打包

本工具纯Java语言开发，代码中的依赖全部来自于开源项目。

### 1、编译打包

- 环境要求:

  **JDK**:>=1.8 （建议用JDK 1.8）

  **maven**:>=3.6
> Maven 仓库默认在国外， 国内使用难免很慢，可以更换为阿里云的仓库。 参考教程： [配置阿里云的仓库教程](https://www.runoob.com/maven/maven-repositories.html)

- 编译命令:

**(1) windows下：**

```
 双击build.cmd脚本文件即可编译打包
```

**(2) Linux下：**

```
git clone https://gitee.com/inrgihc/sqlrest.git
cd sqlrest/
sh ./build.sh
```

**(3) Docker下:**

```
git clone https://gitee.com/inrgihc/sqlrest.git
cd sqlrest/
sh ./docker-maven-build.sh
```

### 2、安装部署

(1) 当编译打包完成后，会在sqlrest/target/目录下生成sqlrest-relase-x.x.x.tar.gz的打包文件，将文件拷贝到已安装JRE的部署机器上解压即可。

(2) 基于docker-compose提供linux联网环境下的**一键安装**，安装命令见 [发行版链接地址](https://gitee.com/inrgihc/sqlrest/releases)

(3) 物理机方式部署

- 步骤1：准备好一个MySQL5.7+的数据库，假设连接地址如下：

| mysql的host地址 | mysql的端口号 | mysql的账号 | mysql的密码 |
| :------| :------ | :------ | :------ |
| 127.0.0.1 | 3306 | root | 123456 |

- 步骤2：修改sqlrest-relase-x.x.x/conf/config.ini配置文件

```
# manager节点的host地址，如果gateway与executor节点
# 与manager不在同一台机器时需要配置manger节点的IP地址
MANAGER_HOST=localhost


# manager的端口号
MANAGER_PORT=8090

# executor的端口号
EXECUTOR_PORT=8092

# gateway的端口号
GATEWAY_PORT=8091


# mysql的host地址
MYSQLDB_HOST=127.0.0.1

# mysql的端口号
MYSQLDB_PORT=3306

# mysql的库名
MYSQLDB_NAME=sqlrest

# mysql的账号
MYSQLDB_USERNAME=root

# mysql的密码
MYSQLDB_PASSWORD=123456
```

- 步骤3：如果为多主机节点部署，需要将sqlrest-relase-x.x.x分发到其他主机节点上；如果为单机（单节点）部署可直接忽略本步骤。

- 步骤4：启动服务

> windows下，需按照如下顺序双击脚本启动对应的服务

启动manager服务：bin/manager_startup.cmd

启动executor服务：bin/executor_startup.cmd

启动gateway服务：bin/gateway_startup.cmd

> linux下，需按照如下顺序执行脚本启动对应的服务

启动manager服务：sh bin/sqlrestctl.sh start manager

启动executor服务：sh bin/sqlrestctl.sh start executor

启动gateway服务：sh bin/sqlrestctl.sh start gateway

## 三、使用教程

### 1、部分系统截图

![001.png](docs/images/001.PNG)

![000.png](docs/images/000.PNG)

![002.png](docs/images/002.PNG)

![003.png](docs/images/003.PNG)

![004.png](docs/images/004.PNG)

## 四、问题反馈

如果您看到并使用了本工具，或您觉得本工具对您有价值，请为此项目**点个赞**，以表示对本项目的支持，多谢！如果您在使用时遇到了bug，欢迎在issue中反馈。
