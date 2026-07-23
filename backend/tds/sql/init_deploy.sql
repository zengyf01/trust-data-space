-- 分布式部署模块数据库初始化脚本
USE tds;

-- 部署节点表
CREATE TABLE IF NOT EXISTS tb_deploy_node (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_node_code VARCHAR(64) NOT NULL COMMENT '节点编码',
    f_node_name VARCHAR(128) NOT NULL COMMENT '节点名称',
    f_node_type VARCHAR(32) COMMENT '节点类型：CENTER/EDGE',
    f_deploy_mode VARCHAR(32) COMMENT '部署模式：CENTRALIZED/DISTRIBUTED/HYBRID',
    f_ip_address VARCHAR(64) COMMENT 'IP地址',
    f_port INT COMMENT '端口',
    f_region VARCHAR(64) COMMENT '区域',
    f_cpu_cores INT COMMENT 'CPU核心数',
    f_memory_size BIGINT COMMENT '内存大小(MB)',
    f_disk_size BIGINT COMMENT '磁盘大小(GB)',
    f_status INT DEFAULT 0 COMMENT '状态：0-离线 1-在线 2-故障 3-维护中',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_last_heartbeat DATETIME COMMENT '最后心跳时间',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_node_code (f_node_code),
    KEY idx_node_type (f_node_type),
    KEY idx_status (f_status),
    KEY idx_last_heartbeat (f_last_heartbeat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部署节点表';

-- 部署实例表
CREATE TABLE IF NOT EXISTS tb_deploy_instance (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_instance_code VARCHAR(64) NOT NULL COMMENT '实例编码',
    f_instance_name VARCHAR(128) NOT NULL COMMENT '实例名称',
    f_node_id VARCHAR(32) COMMENT '节点ID',
    f_service_type VARCHAR(32) COMMENT '服务类型：TDS/DOS/DATAR',
    f_service_version VARCHAR(32) COMMENT '服务版本',
    f_status INT DEFAULT 0 COMMENT '状态：0-已停止 1-运行中 2-启动中 3-停止中 4-故障',
    f_access_url VARCHAR(256) COMMENT '访问地址',
    f_replica_count INT DEFAULT 1 COMMENT '副本数',
    f_current_replicas INT DEFAULT 0 COMMENT '当前副本数',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_start_time DATETIME COMMENT '启动时间',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_instance_code (f_instance_code),
    KEY idx_node_id (f_node_id),
    KEY idx_service_type (f_service_type),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部署实例表';

-- 本地账户表
CREATE TABLE IF NOT EXISTS tb_local_account (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_account_code VARCHAR(64) NOT NULL COMMENT '账户编码',
    f_account_name VARCHAR(128) NOT NULL COMMENT '账户名称',
    f_account_type VARCHAR(32) DEFAULT 'LOCAL' COMMENT '账户类型：LOCAL/FEDERATED',
    f_org_id VARCHAR(32) COMMENT '所属机构ID',
    f_user_id VARCHAR(32) COMMENT '关联用户ID',
    f_id_card VARCHAR(32) COMMENT '身份证号',
    f_phone VARCHAR(32) COMMENT '手机号',
    f_email VARCHAR(128) COMMENT '邮箱',
    f_auth_mode VARCHAR(32) COMMENT '认证模式：LOCAL/PASSWORD/CERTIFICATE',
    f_credential VARCHAR(256) COMMENT '凭证（加密存储）',
    f_is_verified INT DEFAULT 0 COMMENT '是否已认证：0-否 1-是',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_account_code (f_account_code),
    KEY idx_org_id (f_org_id),
    KEY idx_user_id (f_user_id),
    KEY idx_account_type (f_account_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地账户表';

-- 初始化中心节点
INSERT INTO tb_deploy_node (f_id, f_node_code, f_node_name, f_node_type, f_deploy_mode, f_ip_address, f_port, f_region, f_cpu_cores, f_memory_size, f_disk_size, f_status, f_create_time, f_update_time) VALUES
('1', 'CENTER_001', '中心节点', 'CENTER', 'CENTRALIZED', '192.168.1.100', 8080, '主中心', 16, 65536, 500, 1, NOW(), NOW());