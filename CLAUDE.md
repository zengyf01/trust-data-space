# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在本代码库中工作时提供指导。

## 项目概述

本仓库包含**可信数据空间**项目的设计文档和部分源代码。可信数据空间是依据数标委《可信数据空间 能力建设要求》国家标准构建的数据要素流通平台，实现数据可用不可见、可信交换。

## 源代码位置

| 模块 | 路径 | 说明 |
|------|------|------|
| **TDS（服务平台）** | `tds/` | 多模块Maven项目，包含API/Service/DAL/Frontend |
| **DOS（交付平台）** | `dos/` | 多模块Maven项目，工单管理/数据服务/安全沙盒/隐私计算 |
| **Datar（连接器）** | `datar/` | 多模块Maven项目，连接器源码 |
| **MSP（密算平台）** | `https://github.com/zengyf01/msp.git` | 密算平台参考 |
| **机密容器** | `D:\project\confidential-containers` | 机密容器源码 |
| **隐私计算** | `D:\project\secretflow` | SecretFlow参考 |

## 已开发项目结构

```
trust-data-space/
├── docker-compose.yml     # Docker编排配置
├── backend/                 # 后端项目（Spring Boot，前后端已物理分离）
│   ├── tds/                 # TDS服务平台（多模块Maven）
│   │   ├── Dockerfile
│   │   ├── tds-common/              # 公共模块
│   │   │   └── src/main/java/com/tds/common/
│   │   │       ├── core/             # ApiResponse, PageResult
│   │   │       ├── enums/            # ContractStatus, DataSourceType, ProductStatus等枚举
│   │   │       ├── exception/         # BusinessException, GlobalExceptionHandler
│   │   │       └── util/             # SM2Util国密工具
│   │   ├── tds-dal/                  # 数据访问层
│   │   │   └── src/main/java/com/tds/dal/
│   │   │       ├── entity/          # TbDigitalContract, TbDataSource, TbCatalog, TbDataProduct, TbConnector等
│   │   │       └── mapper/           # Mapper接口
│   │   ├── tds-service/              # 业务逻辑层
│   │   │   └── src/main/java/com/tds/service/
│   │   │       ├── contract/         # 合约服务、ContractCreateDTO
│   │   │       ├── datasource/       # 数据源服务（IDataSourceService + DataSourceServiceImpl）
│   │   │       ├── catalog/          # 目录服务（ICatalogService + CatalogServiceImpl）
│   │   │       ├── product/          # 产品服务（IProductService + ProductServiceImpl）
│   │   │       ├── connector/        # 连接器服务（IConnectorService + ConnectorServiceImpl）
│   │   │       ├── order/            # 订单服务（IOrderService + OrderServiceImpl）
│   │   │       └── evidence/         # 存证服务（模拟区块链）
│   │   ├── tds-api/                  # API层
│   │   │   └── src/main/java/com/tds/api/
│   │   │       ├── contract/         # ContractController
│   │   │       ├── datasource/       # DataSourceController
│   │   │       ├── catalog/          # CatalogController
│   │   │       ├── product/          # ProductController
│   │   │       ├── connector/        # ConnectorController
│   │   │       └── order/            # OrderController
│   │   └── sql/
│   │       ├── init.sql              # 数字合约数据库初始化脚本
│   │       ├── init_data_resource.sql # 数据资源数据库初始化脚本
│   │       ├── init_connector.sql     # 连接器管理数据库初始化脚本
│   │       ├── init_order.sql         # 交易订单数据库初始化脚本
│   │       ├── init_billing.sql       # 计量计费数据库初始化脚本
│   │       ├── init_system.sql        # 系统管理数据库初始化脚本
│   │       ├── init_dataspace.sql     # 数据空间数据库初始化脚本
│   │       ├── init_organization.sql  # 机构管理数据库初始化脚本
│   │       └── init_deploy.sql        # 分布式部署数据库初始化脚本
│   ├── dos/                 # DOS交付平台（多模块Maven）
│   │   ├── dos-common/             # 公共模块
│   │   │   └── src/main/java/com/tds/dos/common/
│   │   │       ├── core/             # ApiResponse, PageResult
│   │   │       ├── enums/            # WorkOrderStatus, WorkOrderType
│   │   │       └── exception/         # BusinessException, GlobalExceptionHandler
│   │   ├── dos-dal/                  # 数据访问层
│   │   │   └── src/main/java/com/tds/dos/dal/
│   │   │       ├── entity/          # TbWorkOrder工单实体
│   │   │       └── mapper/           # Mapper接口
│   │   ├── dos-service/              # 业务逻辑层
│   │   │   └── src/main/java/com/tds/dos/service/
│   │   │       └── workorder/       # 工单服务（IWorkOrderService + WorkOrderServiceImpl）
│   │   ├── dos-api/                  # API层
│   │   │   └── src/main/java/com/tds/dos/api/
│   │   │       └── workorder/        # WorkOrderController
│   │   └── sql/
│   │       └── init_dos.sql          # 交付平台数据库初始化脚本
│   ├── datar/               # Datar连接器（多模块Maven）
│   │   ├── datar-common/            # 公共模块
│   │   │   └── src/main/java/com/tds/datar/common/
│   │   │       ├── core/             # ApiResponse, PageResult
│   │   │       ├── enums/            # DataSourceStatus, CatalogStatus, ProductStatus
│   │   │       └── exception/         # BusinessException, GlobalExceptionHandler
│   │   ├── datar-dal/                # 数据访问层
│   │   │   └── src/main/java/com/tds/datar/dal/
│   │   │       ├── entity/          # TbDataSource, TbCatalog, TbDataProduct, TbOrder, TbWorkOrder
│   │   │       └── mapper/           # Mapper接口
│   │   ├── datar-service/            # 业务逻辑层
│   │   │   └── src/main/java/com/tds/datar/service/
│   │   │       ├── datasource/       # 数据源服务（DataSourceService + DataSourceServiceImpl + DataSourceDTO）
│   │   │       ├── catalog/          # 目录服务（CatalogService + CatalogServiceImpl + CatalogDTO）
│   │   │       └── product/          # 产品服务（ProductService + ProductServiceImpl + ProductDTO）
│   │   ├── datar-api/                 # API层
│   │   │   └── src/main/java/com/tds/datar/
│   │   │       ├── DatarApiApplication.java # 启动类
│   │   │       └── controller/
│   │   │           ├── datasource/   # DataSourceController
│   │   │           ├── catalog/      # CatalogController
│   │   │           └── product/      # ProductController
│   │   └── sql/
│   │       └── init_datar.sql        # 连接器数据库初始化脚本
│   └── msp/                 # MSP密算平台（多模块Maven，参考）
│       ├── msp-common/
│       ├── msp-dal/
│       ├── msp-service/
│       ├── msp-api/
│       │   └── Dockerfile
│       └── sql/
├── frontend/                # 前端项目（Vue 3）
│   ├── tds-frontend/        # TDS前端（合约管理/数据空间/产品/订单等页面）
│   ├── dos-frontend/        # DOS前端（工单/沙盒/隐私计算等页面）
│   ├── datar-frontend/      # Datar前端（数据源/目录/产品等页面）
│   └── msp-frontend/        # MSP前端（密算平台页面）
└── CLAUDE.md                     # 本文件
```

## 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      可信数据空间                                  │
├────────────────┬────────────────┬────────────────┬──────────────┤
│  服务平台(TDS) │  交付平台(DOS) │   连接器(Datar) │ 区块链(FISCO) │
│  - 数字合约    │  - 数据服务    │  - 数据连接器   │  - EvidenceLog│
│  - 数据目录    │  - 安全沙盒    │  - 沙盒连接器   │  - Policy     │
│  - 认证鉴权    │  - 隐私计算    │  - 隐私计算连接器│  - ApiInvokeLog│
│  - 运营管理    │  - 工单管理    │  - API网关      │              │
│  - 审计存证    │               │                │              │
└────────────────┴────────────────┴────────────────┴──────────────┘
```

### 三种交付类型
- **数据服务**：SFTP / HTTP / 数据库同步
- **安全沙盒**：Kata容器 + TEE运行时 + JupyterLab
- **隐私计算**：SecretFlow PSI求交 / 联邦学习

### 认证方式
- **MaxKey SSO**：统一身份认证
- **SM2签名**：国密API认证（时间戳5分钟有效期）

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.x + MyBatis-Plus 3.5.x |
| 前端 | Vue 3 + Ant Design Vue 5.x + Vite |
| 数据库 | MySQL 8.0（4库：tds/dos/datar/public）|
| 缓存 | Redis 7.x Cluster + Caffeine（本地缓存）|
| 消息队列 | Kafka 3.x |
| 容器编排 | Kubernetes 1.28+ / Docker |
| 区块链 | FISCO-BCOS 3.0+（国密算法）|
| 安全容器 | Kata Containers 2.0+（TEE运行时）|
| 隐私计算 | SecretFlow 1.15+（SPU/HEU/TEEU/PSI）|

## API基础路径

| 平台 | 路径 |
|------|------|
| TDS | `/api/tds` |
| DOS | `/api/dos` |
| Datar | `/api/datar` |
| 开放接口 | `/api/open/tds/{address}` 或 `/api/open/datar/{address}` |

## 性能指标

- **并发能力**：3000并发
- **API响应**：< 200ms（P99）
- **区块链TPS**：> 1000 TPS
- **沙盒启动**：< 30秒
- **PSI规模**：100万级数据
- **系统可用性**：99.9%

## 数据库规范

- 表名前缀：`tb_`（业务表）、`data_`（V2表）
- 通用字段：`f_id`、`f_tenant_id`、`f_create_time`、`f_update_time`、`f_delete_mark`
- 索引命名：`idx_{表名}_{字段名}`，唯一索引：`uk_{表名}_{字段名}`
- 软删除：`f_delete_mark`（0=未删，1=已删）

## 重要设计模式

- **工单策略模式**：`WorkOrderStrategy`（DataServiceStrategy / SandboxStrategy / PrivacyComputStrategy）
- **合约双签**：供方签名 → 需方签名 → 执行
- **多级缓存**：Caffeine（L1）→ Redis Cluster（L2）→ MySQL
- **异步区块链**：Kafka批量 → EvidenceLog合约上链

## 核心业务流程

1. **机构入驻**：创建账号 → MaxKey SSO认证 → 创建机构 → 注册连接器 → 下发凭证（AppId/AppKey）
2. **数据产品发布**：注册数据源 → 创建目录 → 配置脱敏规则 → 创建产品 → 发布到平台
3. **合约签署**：创建订单 → 供方SM2签名 → 需方SM2签名 → EvidenceLog上链存证
4. **交付流程**：工单创建 → 选择交付类型（数据/沙盒/隐私计算）→ 执行 → 记录存证

## 连接器心跳机制

- 发送间隔：30秒
- 超时下线：3分钟无心跳标记离线
- 告警通知：10分钟无心跳发送通知

## Redis缓存Key规范

```
tds:session:{userId}          # 会话，TTL 2小时
tds:token:{appId}             # API Token，TTL 5分钟
tds:user:{userId}             # 用户信息，TTL 30分钟
datar:connector:{sn}:heartbeat # 连接器心跳，TTL 3分钟
public:dict:{type}            # 字典缓存，TTL 24小时
```

## 高可用设计

- **负载均衡**：Nginx + Keepalived VIP漂移
- **应用层**：Kubernetes HPA自动扩缩（3-25 Pods），Pod反亲和部署
- **数据库**：MySQL主从半同步，Redis Cluster 3主3从
- **消息队列**：Kafka多副本ISR
- **区块链**：FISCO-BCOS 4节点Raft共识，容忍1节点故障

## 开发规范

- **代码风格**：参考 [msp.git](https://github.com/zengyf01/msp.git)，Java/Spring Boot技术栈
- **后端框架**：Spring Boot 3.x + MyBatis-Plus 3.5.x
- **分层结构**：Controller → Service → Mapper/Dao → Entity
- **统一响应**：`{ code: 200, msg: "success", data: {} }`
- **分页响应**：`{ list: [], pagination: { currentPage, pageSize, total } }`

## 部署模式

### 1. Docker单机部署（开发/测试）

适用场景：开发调试、功能演示、小规模测试（<100并发）

```yaml
# docker-compose.yml
services:
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
  minio:
    image: minio/minio:latest
    ports:
      - "9000:9000"
      - "9001:9001"
  tds-api:
    image: ${REGISTRY}/tds-api:${VERSION}
    ports:
      - "8081:8080"
  dos-api:
    image: ${REGISTRY}/dos-api:${VERSION}
    ports:
      - "8082:8080"
  datar-api:
    image: ${REGISTRY}/datar-api:${VERSION}
    ports:
      - "8083:8080"
  frontend:
    image: ${REGISTRY}/frontend:${VERSION}
    ports:
      - "80:80"
```

启动命令：
```bash
docker-compose up -d mysql redis minio
docker-compose up -d
```

### 2. DockerCompose集群部署（客户机房/边缘节点）

适用场景：小规模生产、边缘节点、客户机房（100-500并发）

```
┌─────────────────────────────────────────────────────────────────┐
│                    HAProxy 负载均衡 (VIP)                       │
└───────────────────────┬─────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│  Connector 1  │ │  Connector 2  │ │  Connector 3  │
│   (主)        │ │   (备)        │ │   (备)        │
└──────────────┘ └──────────────┘ └──────────────┘
        │
        ▼
  本地MySQL缓存
```

- 连接器主备部署，HAProxy自动切换（<15秒）
- 支持连接器独立运行，离线工作
- 部署在客户机房，数据不出机房

### 3. Kubernetes生产部署（大规横/高并发）

适用场景：大规模生产、高可用、跨AZ部署（500-10000+并发）

```
┌────────────────────────────────────────────────────────────────┐
│                     控制面 (Master高可用)                       │
│         ┌─────────┐  ┌─────────┐  ┌─────────┐               │
│         │Master1  │  │Master2  │  │Master3  │  ETCD Raft    │
│         │  AZ1    │  │  AZ2    │  │  AZ3    │               │
│         └─────────┘  └─────────┘  └─────────┘               │
└──────────────────────────────────────────────────────────────┘
                        │
                        ▼
┌────────────────────────────────────────────────────────────────┐
│                     计算节点 (多AZ分布)                          │
│  ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐             │
│  │Node1  │ │Node2  │ │Node3  │ │Node4  │ │Node5  │  ...       │
│  │ AZ1   │ │ AZ2   │ │ AZ3   │ │ AZ1   │ │ AZ2   │           │
│  │ TDS   │ │ TDS   │ │ DOS   │ │ DOS   │ │Datar  │           │
│  │ DOS   │ │ DOS   │ │Datar  │ │Datar  │ │       │           │
│  └───────┘ └───────┘ └───────┘ └───────┘ └───────┘             │
└────────────────────────────────────────────────────────────────┘
                        │
                        ▼
┌────────────────────────────────────────────────────────────────┐
│                       数据层                                    │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐           │
│  │  MySQL  │  │  Redis  │  │  Kafka  │  │  MinIO  │           │
│  │ 主从     │  │ Cluster │  │ 集群    │  │ 分布式   │           │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘           │
└────────────────────────────────────────────────────────────────┘
                        │
                        ▼
┌────────────────────────────────────────────────────────────────┐
│                    FISCO-BCOS 区块链 (4节点)                     │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐            │
│  │ FISCO1  │  │ FISCO2  │  │ FISCO3  │  │ FISCO4  │            │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘            │
└────────────────────────────────────────────────────────────────┘
```

**HPA自动扩缩配置：**
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  minReplicas: 3
  maxReplicas: 25
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

**资源配置：**
| 组件 | 副本数 | CPU | 内存 |
|------|--------|-----|------|
| TDS API | 3 | 4核 | 8GB |
| DOS API | 3 | 4核 | 8GB |
| Datar API | 3 | 4核 | 8GB |
| MySQL | 2 | 8核 | 32GB |
| Redis | 6 | 4核 | 16GB |
| FISCO Node | 4 | 8核 | 16GB |

### 部署模式对比

| 特性 | Docker单机 | DockerCompose | K8s生产 |
|------|------------|---------------|---------|
| 适用场景 | 开发/测试 | 小规模生产 | 大规模生产 |
| 并发支持 | 1-100 | 100-500 | 3000+ |
| 高可用 | 单机 | 主备 | 多AZ |
| 自动扩缩 | 手动 | 手动 | HPA自动 |
| 规模用户 | 基础 | 中等 | 10000+ |

## 已完成功能需求 (P0 核心功能)

以下为核心功能模块，已完成设计并可进入开发阶段：

### 服务平台 (TDS)

| 功能模块 | 功能项 | 说明 | 状态 |
|----------|--------|------|------|
| **数据空间管理** | 数据空间CRUD、成员管理、资源目录、数据产品 | 基础管理功能 | ✅已完成 |
| **机构管理** | 机构CRUD、角色权限、MaxKey SSO | 统一身份认证 | ✅已完成 |
| **连接器管理** | 连接器CRUD、心跳监控(30秒/Redis TTL)、远程SSH操作、版本管理 | 连接器生命周期 | ✅已完成 |
| **数字合约** | 合约创建、双签签署(SM2)、合约详情 | 双签流程 | ✅已完成 |
| **交易订单** | 订单CRUD、审核通过触发合约创建、订单历史、交付API信息 | 订单全流程 | ✅已完成 |
| **数据资源** | 数据源CRUD、目录CRUD(含字段/脱敏)、产品CRUD(含审核流程) | 数据资产管理 | ✅已完成 |
| **审计存证** | 操作日志、区块链存证(EvidenceLog)、数据消费日志 | 审计追溯 | ✅已完成 |
| **开放接口** | 接口转发、机构创建、账号创建、凭证下发 | Open API | ✅已完成 |
| **计量计费** | 计费模板、产品定价、用量记录、账单管理 | 计费系统 | ✅已完成 |
| **系统管理** | 系统参数配置、通知配置、发送记录 | 系统配置 | ✅已完成 |

### 交付平台 (DOS)

| 功能模块 | 功能项 | 说明 | 状态 |
|----------|--------|------|------|
| **工单管理** | 工单创建、工单分页查询、文件上传、结果下载 | 工单生命周期 | ✅已完成 |
| **工单策略** | DataServiceStrategy/SandboxStrategy/PrivacyComputeStrategy | 策略模式执行 | ✅已完成 |
| **数据服务** | SFTP交付、HTTP推送、交付API代理 | 数据交付 | ✅已完成 |
| **安全沙盒** | 沙盒创建/销毁/停止、Pod状态/日志/事件、JupyterLab | Kata+TEE | ✅已完成 |
| **隐私计算** | 开发环境创建、作业部署/销毁、PSI求交、联邦学习 | SecretFlow | ✅已完成 |
| **认证接口** | MaxKey登录、SSO认证、登出 | 用户认证 | ✅已完成 |

### 连接器 (Datar)

| 功能模块 | 功能项 | 说明 | 状态 |
|----------|--------|------|------|
| **数据源管理** | 数据源CRUD、连接测试(MySQL/PostgreSQL/SFTP/HTTP)、表/字段信息 | 数据源接入 | ✅已完成 |
| **数据源目录** | 目录CRUD、启禁用、版本管理、SQL测试 | 目录管理 | ✅已完成 |
| **数据服务** | 服务CRUD、产品上下架 | 服务配置 | ✅已完成 |
| **数据产品发布** | 发布CRUD、发布到平台、取消发布、资源购买 | 产品发布 | ✅已完成 |
| **订单管理** | 订单CRUD、签约、合约查询、工单处理 | 订单处理 | ✅已完成 |
| **交付操作** | 沙盒工作目录初始化、镜像构建、源代码下载 | 交付准备 | ✅已完成 |
| **开放接口** | 接口转发、默认账号注册、连接器注册、统一数据接口 | 外部集成 | ✅已完成 |

### 区块链平台

| 功能项 | 说明 |
|--------|------|
| **EvidenceLog存证** | 数据交付凭证上链 |
| **Policy策略管理** | 访问策略上链验证 |
| **ApiInvokeLog** | API调用日志上链 |
| **SM2国密签名** | 国密SM2签名验签 |

---

## 未完成功能需求 (P1 重要功能)

以下功能需要后续迭代开发：

| 功能模块 | 功能项 | 说明 | 优先级 |
|----------|--------|------|--------|
| **数据服务** | 数据库同步、数据转换 | 数据直接入库、格式转换 | ✅已完成 |
| **工单管理** | 工单历史 | 操作历史记录 | ✅已完成 |
| **系统管理** | 系统参数配置、通知配置 | 邮件/短信通知 | ✅已完成 |
| **数据源管理** | 数据预览、SFTP文件列表/下载、CSV读取、HTTP测试 | 数据源调试 | ✅已完成 |
| **数据产品** | 样例数据导出(Excel/JSON) | 数据导出功能 | ✅已完成 |
| **安全特性** | 远程认证(TEE)、KBS密钥服务、数据加密 | TEE安全环境 | P1 |
| **策略管控** | 策略规则CRUD、策略绑定、访问记录、策略执行日志 | 策略引擎 | ✅已完成 |
| **计量计费** | 计费模板、产品定价、用量记录、账单管理、成本分摊 | 计费系统 | ✅已完成 |
| **区块链** | SM4加密、证据验证、合规报告生成 | 国密加密 | P1 |
| **分布式部署** | 本地用户认证、混合模式切换、离线独立运行 | 分布式特性 | ✅已完成 |

---

## 功能优先级说明

| 优先级 | 说明 |
|--------|------|
| **P0** | 核心功能，必须实现，系统可用 |
| **P1** | 重要功能，计划实现，增强体验 |
| **P2** | 扩展功能，适时实现 |