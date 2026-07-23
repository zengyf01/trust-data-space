-- 策略管理模块数据库初始化脚本
USE tds;

-- 策略规则表
CREATE TABLE IF NOT EXISTS tb_policy_rule (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_policy_code VARCHAR(64) NOT NULL COMMENT '策略编码',
    f_policy_name VARCHAR(128) NOT NULL COMMENT '策略名称',
    f_policy_type VARCHAR(32) NOT NULL COMMENT '策略类型：ACCESS-访问控制/RATE_LIMIT-限流/DATA_MASK-数据脱敏',
    f_policy_content TEXT COMMENT '策略内容（JSON格式）',
    f_priority INT DEFAULT 0 COMMENT '优先级',
    f_status VARCHAR(32) DEFAULT 'ENABLED' COMMENT '状态：ENABLED-启用/DISABLED-禁用',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_policy_code (f_policy_code),
    KEY idx_policy_type (f_policy_type),
    KEY idx_status (f_status),
    KEY idx_tenant_id (f_tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='策略规则表';

-- 策略绑定表
CREATE TABLE IF NOT EXISTS tb_policy_binding (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_policy_id VARCHAR(32) NOT NULL COMMENT '策略ID',
    f_resource_type VARCHAR(32) NOT NULL COMMENT '资源类型：DATA_SOURCE/CATALOG/PRODUCT/DATASPACE',
    f_resource_id VARCHAR(32) NOT NULL COMMENT '资源ID',
    f_binding_type VARCHAR(32) DEFAULT 'GRANT' COMMENT '绑定类型：GRANT-授权/DENY-拒绝',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_policy_id (f_policy_id),
    KEY idx_resource (f_resource_type, f_resource_id),
    KEY idx_tenant_id (f_tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='策略绑定表';

-- 策略访问日志表
CREATE TABLE IF NOT EXISTS tb_policy_access_log (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_policy_id VARCHAR(32) COMMENT '策略ID',
    f_policy_name VARCHAR(128) COMMENT '策略名称',
    f_visitor_id VARCHAR(32) COMMENT '访问者ID',
    f_visitor_name VARCHAR(128) COMMENT '访问者名称',
    f_resource_type VARCHAR(32) COMMENT '资源类型',
    f_resource_id VARCHAR(32) COMMENT '资源ID',
    f_resource_name VARCHAR(128) COMMENT '资源名称',
    f_access_result VARCHAR(32) COMMENT '访问结果：ALLOW-允许/DENY-拒绝',
    f_access_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    f_ip_address VARCHAR(64) COMMENT 'IP地址',
    f_user_agent VARCHAR(512) COMMENT 'User Agent',
    f_request_detail TEXT COMMENT '请求详情（JSON）',
    f_response_time_ms INT COMMENT '响应时间（毫秒）',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    KEY idx_policy_id (f_policy_id),
    KEY idx_visitor_id (f_visitor_id),
    KEY idx_access_time (f_access_time),
    KEY idx_resource (f_resource_type, f_resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='策略访问日志表';

-- 策略执行日志表
CREATE TABLE IF NOT EXISTS tb_policy_exec_log (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_policy_id VARCHAR(32) COMMENT '策略ID',
    f_policy_name VARCHAR(128) COMMENT '策略名称',
    f_exec_type VARCHAR(32) COMMENT '执行类型：CREATE/UPDATE/DELETE/QUERY',
    f_exec_result VARCHAR(32) COMMENT '执行结果：SUCCESS/FAIL',
    f_error_message TEXT COMMENT '错误信息',
    f_exec_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    f_operator_id VARCHAR(32) COMMENT '操作人ID',
    f_operator_name VARCHAR(128) COMMENT '操作人名称',
    f_exec_detail TEXT COMMENT '执行详情（JSON）',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    KEY idx_policy_id (f_policy_id),
    KEY idx_exec_time (f_exec_time),
    KEY idx_operator_id (f_operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='策略执行日志表';

-- 初始化默认策略
INSERT INTO tb_policy_rule (f_id, f_policy_code, f_policy_name, f_policy_type, f_policy_content, f_priority, f_status) VALUES
('1', 'DEFAULT_ACCESS', '默认访问策略', 'ACCESS', '{"rule":"allow all"}', 0, 'ENABLED'),
('2', 'RATE_LIMIT_100', '默认限流策略', 'RATE_LIMIT', '{"maxRequests":100,"windowSeconds":60}', 1, 'ENABLED');