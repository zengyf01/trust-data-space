# 节点Agent服务

节点Agent是运行在每个计算节点上的HTTP服务，用于管理本地Ray进程和任务执行。

## 功能特性

- Ray集群管理：启动Head节点、启动Worker节点加入集群、停止Ray
- 任务执行：提交Python脚本任务、查询任务状态、停止任务
- 节点注册：向DOS平台注册节点、上报心跳

## API接口

### Ray管理

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| POST | `/agent/ray/start-head` | 启动Ray Head | `{"rayPort": 10001, "dashboardPort": 8265}` | `{"code": 200, "data": {"rayAddress": "ray://ip:10001"}}` |
| POST | `/agent/ray/start-worker` | 启动Worker加入集群 | `{"headAddress": "ray://ip:10001", "rayPort": 10001}` | `{"code": 200, "data": {"status": "joined"}}` |
| POST | `/agent/ray/stop` | 停止Ray | `{}` | `{"code": 200, "data": {"status": "stopped"}}` |
| GET | `/agent/ray/status` | 查询Ray状态 | - | `{"code": 200, "data": {"running": true, "clusterId": "xxx"}}` |

### 任务执行

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| POST | `/agent/task/run` | 执行Python脚本 | `{"script": "print('hello')", "taskId": "xxx"}` | `{"code": 200, "data": {"jobId": "ray-job-xxx"}}` |
| GET | `/agent/task/status/{jobId}` | 查询任务状态 | - | `{"code": 200, "data": {"status": "RUNNING", "result": "..."}}` |
| POST | `/agent/task/stop/{jobId}` | 停止任务 | - | `{"code": 200, "data": {"status": "stopped"}}` |

### 节点上报

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| POST | `/agent/register` | 向DOS注册节点 | `{"nodeId": "xxx", "nodeName": "Node-A", "machineIp": "192.168.1.100", "rayPort": 10001}` | `{"code": 200, "data": {"registered": true}}` |
| POST | `/agent/heartbeat` | 心跳上报 | `{"nodeId": "xxx", "status": "IDLE"}` | `{"code": 200, "data": {"clusterId": null}}` |

## 部署方式

### 方式1：Docker部署（推荐）

```bash
# 构建镜像
docker build -t tds/node-agent:latest .

# 启动服务
docker-compose up -d

# 或直接运行
docker run -d --name node-agent \
  -p 8081:8081 \
  -e AGENT_PORT=8081 \
  tds/node-agent:latest
```

### 方式2：直接运行

```bash
# 安装依赖
pip install flask requests

# 启动服务
python agent.py
```

## 前置要求

- Python 3.10+
- Ray已安装 (`pip install ray`)
- 开放端口8081（Agent服务端口）和10001（Ray服务端口）

## 配置说明

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| AGENT_PORT | 8081 | Agent服务监听端口 |

## 注意事项

1. 每个节点只需运行一个Agent实例
2. Agent需要能访问DOS平台的API
3. Ray进程由Agent管理，不要手动启动/停止Ray
4. 任务执行超时时间为5分钟

## 目录结构

```
node-agent/
├── agent.py              # 主程序
├── Dockerfile            # Docker镜像
├── docker-compose.yml    # Docker Compose配置
├── requirements.txt      # Python依赖
└── README.md            # 本文件
```
