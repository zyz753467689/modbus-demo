# Modbus Demo

基于 Java 17 + Spring Boot 的 Modbus 协议测试演示项目，支持 Modbus TCP 和 Modbus RTU 两种协议，包含服务端（模拟从站设备）、客户端（读写操作）和前端可视化界面。

## 技术栈

| 层级 | 技术 | 说明 |
|---|---|---|
| 后端框架 | Spring Boot 3.4.1 | Java 17 |
| Modbus 库 | j2mod 3.2.1 | Apache-2.0，支持 TCP/RTU Master 与 Slave |
| 串口通信 | jSerialComm | j2mod 内置，跨平台串口访问 |
| 实时推送 | WebSocket | 轮询数据和模拟器数据实时推送 |
| 前端框架 | Vue 3 + Vite | Composition API |
| UI 组件库 | Element Plus | 表单、表格、卡片等 |
| 图表 | ECharts (vue-echarts) | 轮询数据实时折线图 |
| 状态管理 | Pinia | Vue 3 官方推荐 |
| HTTP 客户端 | Axios | REST API 调用 |

## 功能特性

- **Modbus TCP 服务端** — 模拟从站设备，支持线圈和寄存器的读写操作
- **Modbus TCP 客户端** — 连接远程 TCP 服务端，支持全部标准功能码（FC01-FC06, FC15-FC16）
- **Modbus RTU 服务端** — 基于串口的从站模拟，支持自定义波特率、数据位等参数
- **Modbus RTU 客户端** — 通过串口连接 RTU 从站设备
- **数据模拟器** — 自动生成模拟数据（计数器、正弦波、随机漂移、线圈翻转）
- **轮询监控** — 创建定时轮询任务，WebSocket 实时推送数据，ECharts 实时折线图
- **虚拟串口** — 无需硬件即可测试 RTU 协议（macOS/Linux 使用 socat，Windows 使用 com0com）
- **跨平台** — 支持 macOS、Linux 和 Windows

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.8+
- Node.js 20+（前端开发时需要）

### 启动应用

**方式一：Maven 一键启动**

```bash
mvn org.springframework.boot:spring-boot-maven-plugin:3.4.1:run -DskipTests
```

访问 http://localhost:8080

**方式二：前后端分离开发**

```bash
# 终端1：启动后端
mvn compile -DskipTests
mvn org.springframework.boot:spring-boot-maven-plugin:3.4.1:run

# 终端2：启动前端开发服务器
cd src/main/frontend
npm run dev
```

前端开发服务器运行在 http://localhost:5173，自动代理 API 和 WebSocket 到后端 8080 端口。

**方式三：打包为 JAR**

```bash
# 构建前端
cd src/main/frontend && npm run build && cd ../..

# 复制前端到 static
cp -r src/main/frontend/dist/* src/main/resources/static/

# 打包运行
mvn package -DskipTests
java -jar target/modbus-demo-0.1.0.jar
```

## 使用指南

### TCP 协议测试流程

1. 打开 **TCP 服务端** 页面，点击「启动」（默认端口 5020）
2. 服务端启动后，模拟器自动运行，数据实时更新
3. 打开 **TCP 客户端** 页面，连接 `localhost:5020`
4. 选择功能码和地址，执行读写操作
5. 在 **轮询监控** 页面创建轮询任务，查看实时图表

### RTU 协议测试流程

1. 打开 **RTU 服务端** 页面，先点击「创建虚拟串口对」
2. 从串口下拉框选择虚拟端口，配置参数后点击「启动」
3. 打开 **RTU 客户端** 页面，选择对应的虚拟端口，点击「连接」
4. 执行读写操作

> **虚拟串口安装：**
> - macOS: `brew install socat`
> - Linux: `sudo apt install socat` 或 `sudo yum install socat`
> - Windows: 安装 [com0com](https://sourceforge.net/projects/com0com/)

### 支持的 Modbus 功能码

| 功能码 | 名称 | 类型 |
|---|---|---|
| FC01 | 读线圈 | 读取 |
| FC02 | 读离散输入 | 读取 |
| FC03 | 读保持寄存器 | 读取 |
| FC04 | 读输入寄存器 | 读取 |
| FC05 | 写单个线圈 | 写入 |
| FC06 | 写单个寄存器 | 写入 |
| FC15 | 写多个线圈 | 写入 |
| FC16 | 写多个寄存器 | 写入 |

## 项目结构

```
modbus-demo/
├── pom.xml
├── src/main/java/com/modbus/demo/
│   ├── ModbusDemoApplication.java          # 启动类
│   ├── config/
│   │   ├── WebSocketConfig.java            # WebSocket 配置
│   │   └── SpaWebConfig.java               # Vue Router SPA 转发
│   ├── modbus/
│   │   ├── common/ModbusFunction.java      # 功能码枚举
│   │   ├── server/
│   │   │   ├── ModbusDataStore.java        # 数据模型（4种数据区）
│   │   │   ├── ModbusTcpServer.java        # TCP Slave
│   │   │   └── ModbusRtuServer.java        # RTU Slave
│   │   ├── client/
│   │   │   ├── ModbusTcpClient.java        # TCP Master
│   │   │   └── ModbusRtuClient.java        # RTU Master
│   │   └── simulator/DataSimulator.java    # 数据模拟器
│   ├── poll/
│   │   ├── PollTask.java                   # 轮询任务
│   │   └── PollManager.java                # 轮询管理器
│   ├── serial/
│   │   ├── SerialPortService.java          # 串口枚举
│   │   └── VirtualPortManager.java         # 虚拟串口（跨平台）
│   ├── websocket/
│   │   ├── DataWebSocketHandler.java       # WebSocket 处理器
│   │   └── DataPushService.java            # 数据推送服务
│   ├── controller/                         # REST API 控制器
│   └── model/request/                      # 请求 DTO
├── src/main/frontend/                      # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── App.vue                         # 主布局（侧边栏导航）
│       ├── router/index.js                 # 路由配置
│       ├── api/index.js                     # REST API 封装
│       ├── composables/useWebSocket.js      # WebSocket 组合式函数
│       └── views/
│           ├── DashboardView.vue           # 总览页
│           ├── TcpServerView.vue           # TCP 服务端页
│           ├── TcpClientView.vue           # TCP 客户端页
│           ├── RtuServerView.vue           # RTU 服务端页
│           ├── RtuClientView.vue           # RTU 客户端页
│           └── PollView.vue                # 轮询监控页
└── src/main/resources/
    └── application.yml                     # 配置文件
```

## REST API

```
TCP Server:
  POST /api/server/tcp/start      {port, unitId}
  POST /api/server/tcp/stop
  GET  /api/server/tcp/status
  GET  /api/server/tcp/data
  PUT  /api/server/tcp/data/{type}/{offset}  {value}

RTU Server:
  POST /api/server/rtu/start      {serialPort, baudRate, dataBits, stopBits, parity, unitId}
  POST /api/server/rtu/stop
  GET  /api/server/rtu/status
  GET  /api/server/rtu/data

TCP Client:
  POST /api/client/tcp/connect    {host, port}
  POST /api/client/tcp/disconnect
  GET  /api/client/tcp/status
  POST /api/client/tcp/read       {unitId, function, offset, quantity}
  POST /api/client/tcp/write      {unitId, function, offset, values}

RTU Client:
  POST /api/client/rtu/connect    {serialPort, baudRate, dataBits, stopBits, parity}
  POST /api/client/rtu/disconnect
  GET  /api/client/rtu/status
  POST /api/client/rtu/read       {unitId, function, offset, quantity}
  POST /api/client/rtu/write      {unitId, function, offset, values}

轮询:
  POST /api/poll/start            {connectionType, unitId, function, offset, quantity, intervalMs}
  POST /api/poll/stop             {id}
  GET  /api/poll/list

串口:
  GET  /api/serial/ports
  POST /api/serial/virtual/start
  POST /api/serial/virtual/stop
  GET  /api/serial/virtual/status

WebSocket:
  /ws/data  → 实时推送 {type: "simulator"|"poll", data: {...}, timestamp}
```

## 配置参数

`application.yml` 中可修改的默认值：

```yaml
modbus:
  tcp:
    server:
      default-port: 5020
      default-unit-id: 1
  rtu:
    server:
      default-baud-rate: 9600
      default-data-bits: 8
      default-stop-bits: 1
      default-parity: 0        # 0=无, 1=奇校验, 2=偶校验
      default-unit-id: 1
  simulator:
    enabled: true
    interval-ms: 2000
  data-store:
    register-count: 100
    coil-count: 100
```

## 数据模拟器

模拟器通过 `@Scheduled` 定时任务，每 2 秒修改 DataStore 中的数据：

| 地址 | 数据区 | 模拟策略 |
|---|---|---|
| HR 0 | Holding Register | 计数器（0→65535 循环递增） |
| HR 1 | Holding Register | 正弦波 sin(t) |
| IR 0 | Input Register | 随机游走 |
| IR 1 | Input Register | 随机数 |
| Coil 0 | Coil | 每 5 次翻转 |
| Coil 1 | Coil | 每 3 次随机 |

## 跨平台说明

| 平台 | 串口格式 | 虚拟串口工具 |
|---|---|---|
| macOS | `/dev/cu.*`, `/dev/tty.*` | socat (`brew install socat`) |
| Linux | `/dev/ttyS*`, `/dev/ttyUSB*` | socat (`apt install socat`) |
| Windows | `COM1`, `COM2`, ... | [com0com](https://sourceforge.net/projects/com0com/) |

串口枚举使用 jSerialComm，自动识别当前平台的所有可用串口，无需手动配置。

## License

Apache-2.0
