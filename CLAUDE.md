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

## 项目结构

```
modbus-demo/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/modbus/demo/
│   │   │   ├── ModbusDemoApplication.java          # 启动类
│   │   │   ├── config/
│   │   │   │   ├── WebSocketConfig.java             # WebSocket 端点配置
│   │   │   │   └── SpaWebConfig.java                # Vue Router SPA 转发
│   │   │   ├── modbus/
│   │   │   │   ├── common/
│   │   │   │   │   └── ModbusFunction.java          # Modbus 功能码枚举
│   │   │   │   ├── server/
│   │   │   │   │   ├── ModbusDataStore.java         # 数据模型（4种数据区）
│   │   │   │   │   ├── ModbusTcpServer.java         # TCP Slave 服务端
│   │   │   │   │   └── ModbusRtuServer.java         # RTU Slave 服务端
│   │   │   │   ├── client/
│   │   │   │   │   ├── ModbusTcpClient.java         # TCP Master 客户端
│   │   │   │   │   └── ModbusRtuClient.java        # RTU Master 客户端
│   │   │   │   └── simulator/
│   │   │   │       └── DataSimulator.java           # 数据模拟器
│   │   │   ├── poll/
│   │   │   │   ├── PollTask.java                    # 轮询任务
│   │   │   │   └── PollManager.java                 # 轮询管理器
│   │   │   ├── serial/
│   │   │   │   ├── SerialPortService.java           # 串口枚举
│   │   │   │   └── VirtualPortManager.java          # socat 虚拟串口
│   │   │   ├── websocket/
│   │   │   │   ├── DataWebSocketHandler.java        # WebSocket 处理器
│   │   │   │   └── DataPushService.java             # 数据推送服务
│   │   │   ├── controller/
│   │   │   │   ├── TcpServerController.java         # TCP 服务端 API
│   │   │   │   ├── RtuServerController.java         # RTU 服务端 API
│   │   │   │   ├── TcpClientController.java         # TCP 客户端 API
│   │   │   │   ├── RtuClientController.java         # RTU 客户端 API
│   │   │   │   ├── PollController.java              # 轮询 API
│   │   │   │   └── SerialPortController.java        # 串口 API
│   │   │   └── model/request/                       # 请求 DTO
│   │   ├── resources/
│   │   │   ├── application.yml                      # 配置文件
│   │   │   └── static/                              # 前端构建产物
│   │   └── frontend/                                # Vue 3 项目源码
│   │       ├── package.json
│   │       ├── vite.config.js
│   │       └── src/
│   │           ├── App.vue                          # 主布局（侧边栏导航）
│   │           ├── router/index.js                  # 路由配置
│   │           ├── api/index.js                     # REST API 封装
│   │           ├── composables/useWebSocket.js      # WebSocket 组合式函数
│   │           └── views/
│   │               ├── DashboardView.vue            # 总览页
│   │               ├── TcpServerView.vue            # TCP 服务端页
│   │               ├── TcpClientView.vue            # TCP 客户端页
│   │               ├── RtuServerView.vue            # RTU 服务端页
│   │               ├── RtuClientView.vue            # RTU 客户端页
│   │               └── PollView.vue                 # 轮询监控页
│   └── test/
└── CLAUDE.md
```

## 核心设计

### Modbus 数据模型 (ModbusDataStore)

服务端模拟从站设备，内存中维护 4 种数据区（各 100 个）：

| 数据区 | 类型 | 读写 | Modbus 功能码 |
|---|---|---|---|
| Coils | boolean | 读写 | FC01 读 / FC05 写单 / FC15 写多 |
| Discrete Inputs | boolean | 只读 | FC02 读 |
| Holding Registers | 16-bit int | 读写 | FC03 读 / FC06 写单 / FC16 写多 |
| Input Registers | 16-bit int | 只读 | FC04 读 |

### 数据模拟器 (DataSimulator)

`@Scheduled` 定时任务，每 2 秒修改 DataStore：

- **计数器** — Holding Register 0 递增（0→65535 循环）
- **正弦波** — Holding Register 1 跟随 sin(t)
- **随机漂移** — Input Register 0 随机游走
- **随机数** — Input Register 1 随机值
- **翻转** — Coil 0 每 5 次翻转，Coil 1 每 3 次随机

### 轮询 + WebSocket

`PollManager` 管理多个 `ScheduledExecutorService` 轮询任务，每次读取结果通过 `DataPushService` 广播到所有 WebSocket 客户端。前端通过 `useWebSocket` 组合式函数订阅数据。

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

## 使用方法

### 前置条件

- JDK 17+
- Maven 3.8+
- Node.js 20+（前端开发时需要，Maven 构建时自动下载）

### 启动应用

**方式一：Maven 一键启动**

```bash
cd modbus-demo
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
# 先构建前端
cd src/main/frontend && npm run build && cd ../..

# 复制前端到 static
cp -r src/main/frontend/dist/* src/main/resources/static/

# 打包
mvn package -DskipTests

# 运行
java -jar target/modbus-demo-0.1.0.jar
```

### TCP 协议测试流程

1. 打开 **TCP 服务端** 页面，点击「启动」（默认端口 5020）
2. 服务端启动后，模拟器自动运行，数据实时更新
3. 打开 **TCP 客户端** 页面，连接 `localhost:5020`
4. 选择功能码和地址，执行读写操作
5. 在 **轮询监控** 页面创建轮询任务，查看实时图表

### RTU 协议测试流程

1. 打开 **RTU 服务端** 页面，先点击「创建虚拟串口对」（需要 socat）
2. 选择 `/tmp/vmodbus0`，点击「启动」
3. 打开 **RTU 客户端** 页面，连接 `/tmp/vmodbus1`
4. 执行读写操作

> 虚拟串口需要 socat：`brew install socat`

### 配置参数

`application.yml` 中可修改的默认值：

```yaml
modbus:
  tcp:
    server:
      default-port: 5020        # TCP 服务端默认端口
      default-unit-id: 1        # 默认从站地址
  rtu:
    server:
      default-baud-rate: 9600   # 默认波特率
      default-data-bits: 8      # 默认数据位
      default-stop-bits: 1      # 默认停止位
      default-parity: 0         # 默认校验（0=无）
      default-unit-id: 1        # 默认从站地址
  simulator:
    enabled: true               # 是否启用模拟器
    interval-ms: 2000           # 模拟更新间隔（毫秒）
  data-store:
    register-count: 100         # 寄存器数量
    coil-count: 100             # 线圈数量
```
