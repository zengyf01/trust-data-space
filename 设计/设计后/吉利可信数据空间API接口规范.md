# 吉利可信数据空间 API 接口规范

> 版本：V1.0
> 日期：2026-06-12
> 状态：草稿

---

## 一、概述

### 1.1 文档目的

本文档定义吉利可信数据空间各模块的REST API接口规范，供前端开发和第三方系统集成使用。

### 1.2 技术框架

| 项目 | 技术选型 |
|------|----------|
| 接口协议 | RESTful API |
| 数据格式 | JSON |
| 认证方式 | 国密SM2签名 |
| 文档工具 | Swagger/OpenAPI 3.0 |

### 1.3 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": { }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码 (200=成功, 4xx=客户端错误, 5xx=服务端错误) |
| msg | String | 消息 |
| data | Object | 返回数据 |

**分页响应:**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [],
    "pagination": {
      "currentPage": 1,
      "pageSize": 20,
      "total": 100
    }
  }
}
```

---

## 二、服务平台 (TDS) API

### 2.1 基础路径

```
/api/tds
```

### 2.2 数据空间管理

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/dataSpace/getAllDataSpace` | 获取所有数据空间 | 无 |
| POST | `/dataSpace/getList` | 获取数据空间列表 | `TbDataSpacePagination` |
| POST | `/dataSpace/` | 创建数据空间 | `TbDataSpaceForm` |
| PUT | `/dataSpace/{id}` | 更新数据空间 | `id` + `TbDataSpaceForm` |
| DELETE | `/dataSpace/{id}` | 删除数据空间 | `id` |
| GET | `/dataSpace/{id}` | 获取数据空间详情 | `id` |

**TbDataSpacePagination:**
```json
{
  "currentPage": 1,
  "pageSize": 20,
  "spaceName": "string"
}
```

**TbDataSpaceForm:**
```json
{
  "spaceNumber": "string",
  "spaceName": "string",
  "spaceType": "string",
  "memberId": "string",
  "orgId": "string",
  "address": "string"
}
```

### 2.3 数据资源目录

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/dataSourceCatalogV2/getList` | 获取资源目录列表 | `DataSourceCatalogV2Pagination` |
| POST | `/dataSourceCatalogV2/` | 创建资源目录 | `DataSourceCatalogV2Form` |
| PUT | `/dataSourceCatalogV2/{id}` | 更新资源目录 | `id` + `DataSourceCatalogV2Form` |
| DELETE | `/dataSourceCatalogV2/{id}` | 删除资源目录 | `id` |
| GET | `/dataSourceCatalogV2/{id}` | 获取资源目录详情 | `id` |
| GET | `/dataSourceCatalogV2/disenable/{id}` | 启用/禁用 | `id` |

### 2.4 数据产品管理

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/dataSourceUsageV2/getList` | 获取产品列表 | `DataSourceUsageV2Pagination` |
| POST | `/dataSourceUsageV2/` | 创建产品 | `DataSourceUsageV2Form` |
| PUT | `/dataSourceUsageV2/{id}` | 更新产品 | `id` + `DataSourceUsageV2Form` |
| DELETE | `/dataSourceUsageV2/{id}` | 删除产品 | `id` |
| GET | `/dataSourceUsageV2/disenable/{id}` | 启用/禁用产品 | `id` |
| POST | `/dataSourceUsageV2/push` | 发布产品 | `DataSourceUsageV2Entity` |

### 2.5 交易订单管理

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/DataResourceOrderV2/getList` | 获取订单列表 | `DataResourceOrderV2Pagination` |
| POST | `/DataResourceOrderV2/` | 创建订单 | `DataResourceOrderV2Form` |
| POST | `/DataResourceOrderV2/buy` | 购买数据服务 | `DatarOrderDTO` |
| PUT | `/DataResourceOrderV2/{id}` | 更新订单 | `id` + `DataResourceOrderV2Form` |
| DELETE | `/DataResourceOrderV2/{id}` | 删除订单 | `id` |
| GET | `/DataResourceOrderV2/{id}` | 获取订单详情 | `id` |
| POST | `/DataResourceOrderV2/approve` | 审核订单 | `DataResouceOrderV2Approve` |
| POST | `/DataResourceOrderV2/sign` | 签署合约 | `DataResouceOrderV2Sign` |
| GET | `/DataResourceOrderV2/getContract/{contractCode}` | 获取合约 | `contractCode` |
| GET | `/DataResourceOrderV2/getDeliveryApiInfo/{orderCode}` | 获取交付API信息 | `orderCode` |

**DataResouceOrderV2Sign:**
```json
{
  "orderCode": "string",
  "signature": "string",
  "publicKey": "string"
}
```

### 2.6 数字合约管理

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/digitalContract/contractList` | 获取合约列表 | `pageNumber`, `pageSize` |
| POST | `/digitalContract/getAllDigitalContract` | 获取所有合约 | `connectorNumber` |

### 2.7 开放接口 (Open API)

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/open/tds/{address}` | 接口转发 | `address` + `Map<String,String>` |
| GET | `/open/tds/approvedMember` | 创建机构 | `member` |
| GET | `/open/tds/approvedBaseUser` | 创建账号 | `userAcc` |
| POST | `/open/tds/changePassword` | 修改密码 | `{id, oldPassword, newPassword}` |
| POST | `/open/tds/addMember` | 新增机构 | `{baseUserId, userId, code, name, license}` |
| GET | `/open/tds/getMemberInfo/{id}` | 获取机构信息 | `id` |
| POST | `/open/tds/getProduct` | 接受产品发布 | `Products` |

---

## 三、交付平台 (DOS) API

### 3.1 基础路径

```
/api/dos
```

### 3.2 工单管理

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/workOrder/createWorkOrder` | 创建工单 | `String workOrder` (JSON) |
| POST | `/workOrder/getWorkOrderPage` | 获取工单分页 | `WorkOrderPagination` |
| POST | `/workOrder/uploadWorkOrderFile` | 上传工单文件 | `workOrderCode` + `file` |
| GET | `/workOrder/downloadResultFile` | 下载结果文件 | `resultId` |

**WorkOrderPagination:**
```json
{
  "currentPage": 1,
  "pageSize": 20,
  "deliveryType": 1,
  "deliveryStatus": 1,
  "workOrderCode": "string"
}
```

### 3.3 安全沙盒管理

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| GET | `/sandbox/createDev/{workOrderId}` | 创建开发环境 | `workOrderId` |
| GET | `/sandbox/destroyDev/{workOrderId}` | 销毁开发环境 | `workOrderId` |
| POST | `/sandbox/deploy` | 部署沙盒 | `JSONObject data` |
| GET | `/sandbox/destroy/{workOrderId}` | 销毁沙盒 | `workOrderId` |
| GET | `/sandbox/getPodStatus/{workOrderId}` | 获取Pod状态 | `workOrderId` |
| GET | `/sandbox/getPodLogs/{workOrderId}/{tailLines}` | 获取Pod日志 | `workOrderId`, `tailLines` |
| GET | `/sandbox/getPodEvents/{workOrderId}/{tailLines}` | 获取Pod事件 | `workOrderId`, `tailLines` |
| GET | `/sandbox/stopPod/{workOrderId}` | 停止Pod | `workOrderId` |

### 3.4 隐私计算管理

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| GET | `/privacy-comput/createDev/{workOrderId}` | 创建隐私计算开发环境 | `workOrderId` |
| POST | `/privacy-comput/deploy/{workOrderId}` | 部署隐私计算作业 | `workOrderId` |
| GET | `/privacy-comput/destroy/{workOrderId}` | 销毁隐私计算 | `workOrderId` |
| POST | `/privacy-comput/uploadProgramFile` | 上传程序文件 | `JSONObject data` |
| GET | `/privacy-comput/getPodStatus/{workOrderId}` | 获取作业状态 | `workOrderId` |
| GET | `/privacy-comput/getPodLogs/{workOrderId}/{tailLines}` | 获取作业日志 | `workOrderId`, `tailLines` |

### 3.5 数据服务代理

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| GET | `/data-service/proxy/{workOrderCode}` | GET代理 | `workOrderCode` |
| POST | `/data-service/proxy/{workOrderCode}` | POST代理 | `workOrderCode` |

### 3.6 外部开放接口

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/open/dos/createWorkOrder` | 创建工单 | `String workOrder` |
| GET | `/open/dos/getSandboxInfo/{workOrderId}` | 获取沙盒信息 | `workOrderId` |
| POST | `/open/dos/updateImageStatus` | 更新镜像状态 | `{workOrderCode, status, error}` |
| POST | `/open/dos/updateAuditResult` | 更新审核结果 | `{workOrderCode, approved, reason}` |

### 3.7 认证接口

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/maxkey/login` | 登录 | `account`, `password` |
| GET | `/maxkey/sso` | SSO认证 | `Authorization` header |
| POST | `/maxkey/logout` | 登出 | `Authorization` header |
| GET | `/maxkey/config` | 获取认证配置 | 无 |

**登录响应:**
```json
{
  "code": 200,
  "data": {
    "token": "string",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "username": "string",
    "userId": "string",
    "maxKeyToken": "string",
    "systemToken": "string"
  }
}
```

---

## 四、连接器 (Datar) API

### 4.1 基础路径

```
/api/datar
```

### 4.2 数据源管理

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/dataSource/getList` | 获取数据源列表 | `DataSourcePagination` |
| POST | `/dataSource/` | 创建数据源 | `DataSourceForm` |
| PUT | `/dataSource/{id}` | 更新数据源 | `id` + `DataSourceForm` |
| DELETE | `/dataSource/{id}` | 删除数据源 | `id` |
| GET | `/dataSource/{id}` | 获取数据源详情 | `id` |
| GET | `/dataSource/disenable/{id}` | 启用/禁用 | `id` |
| POST | `/dataSource/Actions/Test` | 测试连接 | `DbLinkBaseForm` |
| GET | `/dataSource/oss/token` | 获取OSS上传令牌 | 无 |

### 4.3 数据源目录

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/dataSourceCatalogV2/getList` | 获取目录列表 | `DataSourceCatalogV2Pagination` |
| POST | `/dataSourceCatalogV2/` | 创建目录 | `DataSourceCatalogV2Form` |
| PUT | `/dataSourceCatalogV2/{id}` | 更新目录 | `id` + `DataSourceCatalogV2Form` |
| DELETE | `/dataSourceCatalogV2/{id}` | 删除目录 | `id` |
| GET | `/dataSourceCatalogV2/{id}` | 获取目录详情 | `id` |
| GET | `/dataSourceCatalogV2/disenable/{id}` | 启用/禁用 | `id` |
| GET | `/dataSourceCatalogV2/ver/{code}` | 获取版本信息 | `code` |
| GET | `/dataSourceCatalogV2/history/{code}` | 获取历史版本 | `code` |
| POST | `/dataSourceCatalogV2/testSQL/{sourceID}` | 测试SQL | `sourceID` + `{sql}` |

### 4.4 数据产品发布

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/dataResourceReleaseV2/getList` | 获取发布列表 | `DataResourceReleaseV2Pagination` |
| POST | `/dataResourceReleaseV2/` | 创建发布 | `DataResourceReleaseV2Form` |
| PUT | `/dataResourceReleaseV2/{id}` | 更新发布 | `id` + `DataResourceReleaseV2Form` |
| DELETE | `/dataResourceReleaseV2/{id}` | 删除发布 | `id` |
| GET | `/dataResourceReleaseV2/release/{id}` | 发布到平台 | `id` |
| GET | `/dataResourceReleaseV2/unRelease/{id}` | 取消发布 | `id` |
| POST | `/dataResourceReleaseV2/order` | 购买资源 | `DatarOrderDTO` |

### 4.5 订单管理

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/DataResourceOrderV2/getList` | 获取订单列表 | `DataResourceOrderV2Pagination` |
| POST | `/DataResourceOrderV2/` | 创建订单 | `DataResourceOrderV2Form` |
| PUT | `/DataResourceOrderV2/{id}` | 更新订单 | `id` + `DataResourceOrderV2Form` |
| DELETE | `/DataResourceOrderV2/{id}` | 删除订单 | `id` |
| GET | `/DataResourceOrderV2/{id}` | 获取订单详情 | `id` |
| POST | `/DataResourceOrderV2/sign` | 签署合约 | `DataResouceOrderV2Sign` |
| GET | `/DataResourceOrderV2/getContract/{contractCode}` | 获取合约 | `contractCode` |
| GET | `/DataResourceOrderV2/getSandboxInfo/{id}` | 获取沙盒信息 | `id` |

### 4.6 数据服务

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/dataService/getList` | 获取服务列表 | `DataServicePagination` |
| POST | `/dataService/` | 创建服务 | `DataServiceForm` |
| PUT | `/dataService/{id}` | 更新服务 | `id` + `DataServiceForm` |
| PUT | `/dataService/updateSpace/{id}` | 更新目录/状态 | `id` + `DataServiceForm` |
| DELETE | `/dataService/{id}` | 删除服务 | `id` |
| GET | `/dataService/{id}` | 获取服务详情 | `id` |

### 4.7 外部开放接口

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/open/datar/{address}` | 接口转发 | `address` + `Map` |
| POST | `/open/datar/register` | 注册默认账号 | `{account, name, pwd}` |
| POST | `/open/datar/registerConnector` | 注册连接器 | `ConnectorRegistryRequest` |
| POST | `/open/datar/registerEcoRole` | 注册生态角色 | `UserEntity` |
| POST | `/open/datar/serviceNodeRegistry` | 服务节点注册 | `ServiceNodeRegistryRequest` |
| POST | `/open/datar/test/get` | 统一数据接口 | Multipart/JSON |
| POST | `/open/datar/downloadDataset` | 下载数据集 | Dataset参数 |
| POST | `/open/datar/uploadfile` | 上传文件 | Multipart files |
| GET | `/{id}/Actions/Response` | GET访问接口 | `id` + `map` |
| POST | `/{id}/Actions/Response` | POST访问接口 | `id` + `map` |

### 4.8 数据服务接口

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/order/service/get` | 统一数据接口 | Multipart/JSON |

**请求体:**
```json
{
  "orderCode": "string",
  "pageNumber": 1,
  "pageSize": 10,
  "searchKeyword": "string",
  "searchColumns": ["col1", "col2"],
  "path": "string",
  "isDownload": true,
  "map": {}
}
```

### 4.9 交付操作接口

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/delivery/initSboxDevWorkDir` | 初始化沙盒工作目录 | `InitDevWorkDirDTO` |
| POST | `/delivery/createSboxImage` | 构建推送Docker镜像 | `ImageBuildRequest` |
| GET | `/delivery/download-sourcecode` | 下载源代码 | `workOrder`, `deliveryType` |

---

## 五、API认证规范

### 5.1 签名认证流程

```
1. 拼接签名原文: appId + timestamp + requestBody
2. 使用SM2私钥签名
3. 添加HTTP Header:
   - Authorization: appId:timestamp:signature
   - Content-Type: application/json
4. Timestamp有效期: 5分钟
```

### 5.2 响应类型

| Header值 | 说明 |
|----------|------|
| `file` | 文件下载 |
| `data` | JSON数据 |
| `stream` | 流式响应 |

---

## 六、错误码定义

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/签名验证失败 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

*文档结束*