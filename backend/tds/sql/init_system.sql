-- 系统管理模块数据库初始化脚本
USE tds;

-- 系统参数配置表
CREATE TABLE IF NOT EXISTS tb_system_config (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_config_key VARCHAR(128) NOT NULL COMMENT '配置键',
    f_config_value TEXT COMMENT '配置值',
    f_value_type VARCHAR(32) DEFAULT 'STRING' COMMENT '值类型：STRING/NUMBER/BOOLEAN/JSON/TEXT',
    f_config_name VARCHAR(128) COMMENT '配置名称',
    f_config_group VARCHAR(64) COMMENT '配置分组',
    f_description VARCHAR(512) COMMENT '描述',
    f_sort_order INT DEFAULT 0 COMMENT '排序',
    f_is_visible INT DEFAULT 1 COMMENT '是否可见：0-隐藏 1-可见',
    f_is_editable INT DEFAULT 1 COMMENT '是否可编辑：0-不可编辑 1-可编辑',
    f_tenant_id VARCHAR(32) COMMENT '租户ID（空表示全局配置）',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key_tenant (f_config_key, f_tenant_id),
    KEY idx_config_group (f_config_group),
    KEY idx_config_name (f_config_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数配置表';

-- 通知配置表
CREATE TABLE IF NOT EXISTS tb_notification_config (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_config_code VARCHAR(64) NOT NULL COMMENT '配置编码',
    f_config_name VARCHAR(128) NOT NULL COMMENT '配置名称',
    f_notification_type VARCHAR(32) NOT NULL COMMENT '通知类型：EMAIL/SMS/WECHAT/WEBHOOK',
    f_is_enabled INT DEFAULT 0 COMMENT '是否启用：0-禁用 1-启用',
    f_host VARCHAR(255) COMMENT '主机地址',
    f_port INT COMMENT '端口',
    f_username VARCHAR(128) COMMENT '用户名',
    f_password VARCHAR(255) COMMENT '密码',
    f_api_key VARCHAR(255) COMMENT 'API Key',
    f_api_secret VARCHAR(255) COMMENT 'API Secret',
    f_signature VARCHAR(128) COMMENT '签名',
    f_template_code VARCHAR(64) COMMENT '模板编码',
    f_webhook_url VARCHAR(512) COMMENT 'Webhook URL',
    f_description VARCHAR(512) COMMENT '描述',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_config_code (f_config_code),
    KEY idx_notification_type (f_notification_type),
    KEY idx_is_enabled (f_is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知配置表';

-- 通知发送记录表
CREATE TABLE IF NOT EXISTS tb_notification_log (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_notification_type VARCHAR(32) NOT NULL COMMENT '通知类型',
    f_recipient VARCHAR(256) NOT NULL COMMENT '接收人',
    f_recipient_name VARCHAR(128) COMMENT '接收人名称',
    f_subject VARCHAR(256) COMMENT '主题',
    f_content TEXT COMMENT '内容',
    f_status INT DEFAULT 0 COMMENT '状态：0-待发送 1-发送中 2-成功 3-失败',
    f_error_message TEXT COMMENT '错误信息',
    f_retry_count INT DEFAULT 0 COMMENT '重试次数',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_send_time DATETIME COMMENT '发送时间',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_notification_type (f_notification_type),
    KEY idx_status (f_status),
    KEY idx_recipient (f_recipient),
    KEY idx_create_time (f_create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知发送记录表';

-- 初始化默认系统配置
INSERT INTO tb_system_config (f_id, f_config_key, f_config_value, f_value_type, f_config_name, f_config_group, f_description, f_sort_order, f_is_visible, f_is_editable, f_tenant_id, f_create_time, f_update_time) VALUES
('1', 'system.name', '可信数据空间', 'STRING', '系统名称', 'basic', '系统名称', 1, 1, 1, NULL, NOW(), NOW()),
('2', 'system.version', '1.0.0', 'STRING', '系统版本', 'basic', '系统版本号', 2, 1, 0, NULL, NOW(), NOW()),
('3', 'system.logo', '/static/logo.png', 'STRING', '系统Logo', 'basic', '系统Logo路径', 3, 1, 1, NULL, NOW(), NOW()),
('4', 'session.timeout', '7200', 'NUMBER', '会话超时', 'security', '会话超时时间（秒）', 10, 1, 1, NULL, NOW(), NOW()),
('5', 'password.min_length', '8', 'NUMBER', '密码最小长度', 'security', '密码最小长度', 11, 1, 1, NULL, NOW(), NOW()),
('6', 'upload.max_size', '10485760', 'NUMBER', '文件上传大小', 'upload', '文件上传大小限制（字节）', 20, 1, 1, NULL, NOW(), NOW()),
('7', 'upload.allowed_types', 'jpg,png,pdf,doc,docx,xls,xlsx', 'STRING', '允许上传类型', 'upload', '允许上传的文件类型', 21, 1, 1, NULL, NOW(), NOW()),
('8', 'email.smtp_host', 'smtp.example.com', 'STRING', 'SMTP主机', 'notification', '邮件SMTP服务器地址', 30, 1, 1, NULL, NOW(), NOW()),
('9', 'email.smtp_port', '587', 'NUMBER', 'SMTP端口', 'notification', '邮件SMTP服务器端口', 31, 1, 1, NULL, NOW(), NOW()),
('10', 'sms.provider', 'aliyun', 'STRING', '短信提供商', 'notification', '短信服务提供商', 40, 1, 1, NULL, NOW(), NOW());

-- 菜单表
CREATE TABLE IF NOT EXISTS tb_menu (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_parent_id VARCHAR(32) COMMENT '父菜单ID',
    f_menu_name VARCHAR(64) NOT NULL COMMENT '菜单名称',
    f_menu_code VARCHAR(64) COMMENT '菜单编码',
    f_menu_type INT DEFAULT 1 COMMENT '类型：1-分组 2-菜单',
    f_path VARCHAR(256) COMMENT '路由路径',
    f_icon VARCHAR(64) COMMENT '图标',
    f_sort_order INT DEFAULT 0 COMMENT '排序',
    f_status INT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记',
    KEY idx_parent_id (f_parent_id),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 初始化菜单数据
INSERT INTO tb_menu (f_id, f_parent_id, f_menu_name, f_menu_code, f_menu_type, f_path, f_icon, f_sort_order, f_status) VALUES
('g1', NULL, '数据空间', 'dataspace', 1, NULL, 'ClusterOutlined', 1, 1),
('m1', 'g1', '数据空间', 'dataspace_list', 2, '/dataspace', 'DatabaseOutlined', 1, 1),
('m2', 'g1', '机构管理', 'organization', 2, '/organization', 'BankOutlined', 2, 1),
('g2', NULL, '数据资源', 'dataresource', 1, NULL, 'CloudServerOutlined', 2, 1),
('m3', 'g2', '数据源', 'datasource', 2, '/datasource', 'CloudServerOutlined', 1, 1),
('m4', 'g2', '资源目录', 'catalog', 2, '/catalog', 'FolderOutlined', 2, 1),
('m5', 'g2', '数据产品', 'product', 2, '/product', 'AppstoreOutlined', 3, 1),
('g3', NULL, '交易管理', 'transaction', 1, NULL, 'ShoppingCartOutlined', 3, 1),
('m6', 'g3', '数字合约', 'contract', 2, '/contract', 'FileTextOutlined', 1, 1),
('m7', 'g3', '交易订单', 'order', 2, '/order', 'ShoppingCartOutlined', 2, 1),
('g4', NULL, '运维管理', 'operation', 1, NULL, 'MonitorOutlined', 4, 1),
('m8', 'g4', '连接器', 'connector', 2, '/connector', 'ApiOutlined', 1, 1),
('m9', 'g4', '策略管理', 'policy', 2, '/policy', 'SafetyOutlined', 2, 1),
('m10', 'g4', '审计存证', 'evidence', 2, '/evidence', 'AuditOutlined', 3, 1),
('g5', NULL, '系统管理', 'system', 1, NULL, 'SettingOutlined', 5, 1),
('m11', 'g5', '计量计费', 'billing', 2, '/billing', 'DollarOutlined', 1, 1),
('m12', 'g5', '系统配置', 'system_config', 2, '/system', 'SettingOutlined', 2, 1),
('g6', NULL, '用户中心', 'usercenter', 1, NULL, 'UserOutlined', 0, 1),
('m13', 'g6', '用户管理', 'user', 2, '/user', 'UserOutlined', 1, 1),
('m14', 'g6', '角色管理', 'role', 2, '/role', 'SafetyOutlined', 2, 1);