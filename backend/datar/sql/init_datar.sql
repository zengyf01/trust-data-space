-- Datar连接器数据库初始化脚本
CREATE DATABASE IF NOT EXISTS datar DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE datar;

-- 数据源表
CREATE TABLE IF NOT EXISTS tb_data_source (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_data_source_code VARCHAR(64) NOT NULL COMMENT '数据源编码',
    f_data_source_name VARCHAR(128) NOT NULL COMMENT '数据源名称',
    f_data_source_type VARCHAR(32) NOT NULL COMMENT '数据源类型：MYSQL/POSTGRESQL/SFTP/HTTP',
    f_host VARCHAR(255) COMMENT '主机地址',
    f_port INT COMMENT '端口',
    f_database_name VARCHAR(128) COMMENT '数据库名',
    f_username VARCHAR(128) COMMENT '用户名',
    f_password VARCHAR(255) COMMENT '密码(加密)',
    f_private_key VARCHAR(512) COMMENT '私钥(SFTP)',
    f_base_path VARCHAR(512) COMMENT '基础路径',
    f_conn_params VARCHAR(1024) COMMENT '连接参数',
    f_status INT DEFAULT 1 COMMENT '状态：1-启用 2-禁用',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_data_source_code (f_data_source_code),
    KEY idx_data_source_name (f_data_source_name),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源表';

-- 资源目录表
CREATE TABLE IF NOT EXISTS tb_catalog (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_catalog_code VARCHAR(64) NOT NULL COMMENT '目录编码',
    f_catalog_name VARCHAR(128) NOT NULL COMMENT '目录名称',
    f_data_source_id VARCHAR(32) NOT NULL COMMENT '数据源ID',
    f_schema_name VARCHAR(128) COMMENT 'Schema名',
    f_table_name VARCHAR(128) COMMENT '表名',
    f_description TEXT COMMENT '描述',
    f_version INT DEFAULT 1 COMMENT '版本号',
    f_status INT DEFAULT 1 COMMENT '状态：1-草稿 2-已发布 3-已下线',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_catalog_code (f_catalog_code),
    KEY idx_catalog_name (f_catalog_name),
    KEY idx_data_source_id (f_data_source_id),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源目录表';

-- 数据产品表
CREATE TABLE IF NOT EXISTS tb_data_product (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_product_code VARCHAR(64) NOT NULL COMMENT '产品编码',
    f_product_name VARCHAR(128) NOT NULL COMMENT '产品名称',
    f_catalog_id VARCHAR(32) COMMENT '目录ID',
    f_product_desc TEXT COMMENT '产品描述',
    f_pricing_model VARCHAR(32) COMMENT '定价模型：FIXED/API_CALL/VOLUME',
    f_price DECIMAL(12,2) DEFAULT 0.00 COMMENT '价格',
    f_status INT DEFAULT 1 COMMENT '状态：1-草稿 2-已发布 3-已下线',
    f_publish_time DATETIME COMMENT '发布时间',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_product_code (f_product_code),
    KEY idx_product_name (f_product_name),
    KEY idx_catalog_id (f_catalog_id),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据产品表';

-- 订单表
CREATE TABLE IF NOT EXISTS tb_order (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_order_code VARCHAR(64) NOT NULL COMMENT '订单编码',
    f_product_id VARCHAR(32) NOT NULL COMMENT '产品ID',
    f_buyer_tenant_id VARCHAR(32) NOT NULL COMMENT '买方租户ID',
    f_seller_tenant_id VARCHAR(32) NOT NULL COMMENT '卖方租户ID',
    f_contract_id VARCHAR(32) COMMENT '合约ID',
    f_status INT DEFAULT 1 COMMENT '状态：1-待审核 2-已通过 3-签署中 4-执行中 5-已完成 6-已拒绝 7-已取消',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_order_code (f_order_code),
    KEY idx_product_id (f_product_id),
    KEY idx_buyer_tenant_id (f_buyer_tenant_id),
    KEY idx_seller_tenant_id (f_seller_tenant_id),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 工单表
CREATE TABLE IF NOT EXISTS tb_work_order (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_order_id VARCHAR(32) NOT NULL COMMENT '订单ID',
    f_work_order_type VARCHAR(32) NOT NULL COMMENT '工单类型：DATA_SERVICE/SANDBOX/PRIVACY_COMPUTE',
    f_work_order_code VARCHAR(64) NOT NULL COMMENT '工单编码',
    f_status INT DEFAULT 1 COMMENT '状态：1-待处理 2-处理中 3-已完成 4-失败 5-已取消',
    f_input_params TEXT COMMENT '输入参数JSON',
    f_output_result TEXT COMMENT '输出结果JSON',
    f_error_message TEXT COMMENT '错误信息',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_work_order_code (f_work_order_code),
    KEY idx_order_id (f_order_id),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- 交付任务表
CREATE TABLE IF NOT EXISTS tb_delivery_task (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_work_order_id VARCHAR(32) NOT NULL COMMENT '工单ID',
    f_task_type VARCHAR(32) NOT NULL COMMENT '任务类型：SANDBOX_INIT-沙盒初始化, IMAGE_BUILD-镜像构建, SOURCE_DOWNLOAD-源码下载',
    f_status INT DEFAULT 0 COMMENT '状态：0-待执行 1-执行中 2-成功 3-失败',
    f_sandbox_id VARCHAR(64) COMMENT '沙盒ID',
    f_work_directory VARCHAR(512) COMMENT '工作目录',
    f_image_name VARCHAR(128) COMMENT '镜像名称',
    f_image_tag VARCHAR(64) COMMENT '镜像标签',
    f_source_url VARCHAR(512) COMMENT '源码URL',
    f_source_path VARCHAR(512) COMMENT '源码本地路径',
    f_build_log TEXT COMMENT '构建日志',
    f_error_message TEXT COMMENT '错误信息',
    f_duration BIGINT COMMENT '执行时长(毫秒)',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    KEY idx_work_order_id (f_work_order_id),
    KEY idx_task_type (f_task_type),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交付任务表';

-- 用户表
CREATE TABLE IF NOT EXISTS tb_user (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_username VARCHAR(64) NOT NULL COMMENT '用户名',
    f_password VARCHAR(255) NOT NULL COMMENT '密码',
    f_real_name VARCHAR(128) COMMENT '真实姓名',
    f_phone VARCHAR(32) COMMENT '手机号',
    f_email VARCHAR(128) COMMENT '邮箱',
    f_user_type VARCHAR(32) COMMENT '用户类型',
    f_status INT DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_username (f_username),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS tb_role (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    f_role_name VARCHAR(128) NOT NULL COMMENT '角色名称',
    f_role_type VARCHAR(32) COMMENT '角色类型：SYSTEM-系统角色 BUSINESS-业务角色',
    f_role_desc VARCHAR(512) COMMENT '角色描述',
    f_is_system INT DEFAULT 0 COMMENT '是否系统角色：1-是 0-否',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_role_code (f_role_code),
    KEY idx_role_name (f_role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS tb_user_role (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    f_role_id VARCHAR(32) NOT NULL COMMENT '角色ID',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (f_user_id, f_role_id),
    KEY idx_user_id (f_user_id),
    KEY idx_role_id (f_role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 初始化默认管理员账号 (密码: admin123)
INSERT INTO tb_user (f_id, f_username, f_password, f_real_name, f_status, f_create_time) VALUES
('1', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '管理员', 1, NOW());

-- 连接器管理表
CREATE TABLE IF NOT EXISTS tb_connector (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_sn VARCHAR(64) NOT NULL COMMENT '序列号(设备唯一标识)',
    f_name VARCHAR(128) NOT NULL COMMENT '连接器名称',
    f_type INT(2) NOT NULL COMMENT '连接器类型(1数据连接器,2沙盒连接器,3隐私计算连接器)',
    f_status INT(2) DEFAULT 3 COMMENT '状态(1在线,2离线,3离线待注册)',
    f_version VARCHAR(32) COMMENT '当前版本',
    f_ip_address VARCHAR(64) COMMENT 'IP地址',
    f_ssh_port INT(5) COMMENT 'SSH端口',
    f_ssh_username VARCHAR(64) COMMENT 'SSH用户名',
    f_ssh_password VARCHAR(256) COMMENT 'SSH密码(加密存储)',
    f_ssh_private_key TEXT COMMENT 'SSH私钥(加密存储)',
    f_mac_address VARCHAR(64) COMMENT 'MAC地址',
    f_last_heartbeat DATETIME COMMENT '最后心跳时间',
    f_registered_time DATETIME COMMENT '注册时间',
    f_institution_id VARCHAR(32) COMMENT '所属机构ID',
    f_institution_name VARCHAR(128) COMMENT '所属机构名称',
    f_region VARCHAR(64) COMMENT '所属区域',
    f_description TEXT COMMENT '描述',
    f_is_system INT DEFAULT 0 COMMENT '是否系统连接器：1-是 0-否',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
    UNIQUE KEY uk_sn (f_sn),
    KEY idx_type (f_type),
    KEY idx_status (f_status),
    KEY idx_institution_id (f_institution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='连接器管理';

-- 连接器版本表
CREATE TABLE IF NOT EXISTS tb_connector_version (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_connector_id VARCHAR(32) NOT NULL COMMENT '连接器ID',
    f_version VARCHAR(32) NOT NULL COMMENT '版本号',
    f_file_path VARCHAR(256) COMMENT '文件路径',
    f_file_size BIGINT(20) COMMENT '文件大小',
    f_file_md5 VARCHAR(32) COMMENT '文件MD5',
    f_change_log TEXT COMMENT '变更日志',
    f_status INT(1) DEFAULT 0 COMMENT '状态(0未激活,1激活中,2已激活)',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
    KEY idx_connector_id (f_connector_id),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='连接器版本管理';

-- 连接器操作日志表
CREATE TABLE IF NOT EXISTS tb_connector_log (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_connector_id VARCHAR(32) NOT NULL COMMENT '连接器ID',
    f_operate_type VARCHAR(32) NOT NULL COMMENT '操作类型(DEPLOY部署,UPGRADE升级,UNINSTALL卸载,RESTART重启,STOP停止)',
    f_operate_content TEXT COMMENT '操作内容',
    f_operate_result VARCHAR(32) COMMENT '操作结果(成功SUCCESS,失败FAIL)',
    f_error_message TEXT COMMENT '错误信息',
    f_start_time DATETIME COMMENT '开始时间',
    f_end_time DATETIME COMMENT '结束时间',
    f_duration INT(11) COMMENT '耗时(秒)',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
    KEY idx_connector_id (f_connector_id),
    KEY idx_operate_type (f_operate_type),
    KEY idx_operate_time (f_create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='连接器操作日志';

-- =====================================================
-- 后续增量修改：添加数据空间ID字段 (spaceId)
-- =====================================================

-- 数据源表添加 spaceId
ALTER TABLE tb_data_source ADD COLUMN f_space_id VARCHAR(32) DEFAULT NULL COMMENT '数据空间ID' AFTER f_tenant_id;
CREATE INDEX idx_data_source_space_id ON tb_data_source(f_space_id);

-- 资源目录表添加 spaceId
ALTER TABLE tb_catalog ADD COLUMN f_space_id VARCHAR(32) DEFAULT NULL COMMENT '数据空间ID' AFTER f_tenant_id;
CREATE INDEX idx_catalog_space_id ON tb_catalog(f_space_id);

-- 数据产品表添加 spaceId
ALTER TABLE tb_data_product ADD COLUMN f_space_id VARCHAR(32) DEFAULT NULL COMMENT '数据空间ID' AFTER f_tenant_id;
CREATE INDEX idx_data_product_space_id ON tb_data_product(f_space_id);

-- 连接器表添加 spaceId
ALTER TABLE tb_connector ADD COLUMN f_space_id VARCHAR(32) DEFAULT NULL COMMENT '数据空间ID' AFTER f_institution_name;
CREATE INDEX idx_connector_space_id ON tb_connector(f_space_id);

-- 订单表添加买方/卖方空间ID
ALTER TABLE tb_order ADD COLUMN f_buyer_space_id VARCHAR(32) DEFAULT NULL COMMENT '买方空间ID' AFTER f_seller_tenant_id;
ALTER TABLE tb_order ADD COLUMN f_seller_space_id VARCHAR(32) DEFAULT NULL COMMENT '卖方空间ID' AFTER f_buyer_space_id;
CREATE INDEX idx_order_buyer_space_id ON tb_order(f_buyer_space_id);
CREATE INDEX idx_order_seller_space_id ON tb_order(f_seller_space_id);

-- 工单表添加 spaceId
ALTER TABLE tb_work_order ADD COLUMN f_space_id VARCHAR(32) DEFAULT NULL COMMENT '数据空间ID' AFTER f_tenant_id;
CREATE INDEX idx_work_order_space_id ON tb_work_order(f_space_id);