-- TDS审计存证数据库初始化脚本
CREATE DATABASE IF NOT EXISTS tds DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tds;

-- 操作日志表
CREATE TABLE IF NOT EXISTS tb_operation_log (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_user_id VARCHAR(32) COMMENT '用户ID',
    f_user_name VARCHAR(64) COMMENT '用户名称',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_module VARCHAR(64) COMMENT '模块',
    f_operation VARCHAR(128) COMMENT '操作描述',
    f_method VARCHAR(64) COMMENT '请求方法',
    f_url VARCHAR(255) COMMENT '请求URL',
    f_request_params TEXT COMMENT '请求参数',
    f_response_result TEXT COMMENT '响应结果',
    f_status INT COMMENT '状态：1-成功 2-失败',
    f_error_message TEXT COMMENT '错误信息',
    f_ip_address VARCHAR(64) COMMENT 'IP地址',
    f_user_agent VARCHAR(255) COMMENT '用户代理',
    f_duration BIGINT COMMENT '耗时(ms)',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (f_user_id),
    KEY idx_module (f_module),
    KEY idx_create_time (f_create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 区块链存证表
CREATE TABLE IF NOT EXISTS tb_evidence_log (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_tx_hash VARCHAR(128) NOT NULL COMMENT '交易哈希',
    f_block_hash VARCHAR(128) COMMENT '区块哈希',
    f_block_number BIGINT COMMENT '区块高度',
    f_evidence_type VARCHAR(32) NOT NULL COMMENT '存证类型',
    f_evidence_data TEXT NOT NULL COMMENT '存证数据(JSON)',
    f_contract_id VARCHAR(32) COMMENT '合约ID',
    f_order_id VARCHAR(32) COMMENT '订单ID',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_user VARCHAR(64) COMMENT '创建人',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_chain_time DATETIME COMMENT '上链时间',
    UNIQUE KEY uk_tx_hash (f_tx_hash),
    KEY idx_contract_id (f_contract_id),
    KEY idx_evidence_type (f_evidence_type),
    KEY idx_create_time (f_create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链存证表';

-- 数据消费日志表
CREATE TABLE IF NOT EXISTS tb_data_consume_log (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_contract_id VARCHAR(32) COMMENT '合约ID',
    f_order_id VARCHAR(32) COMMENT '订单ID',
    f_product_id VARCHAR(32) COMMENT '产品ID',
    f_consumer_tenant_id VARCHAR(32) NOT NULL COMMENT '消费方租户ID',
    f_provider_tenant_id VARCHAR(32) NOT NULL COMMENT '提供方租户ID',
    f_consume_type VARCHAR(32) NOT NULL COMMENT '消费类型：API_CALL/SFTP_DOWNLOAD/DATA_SERVICE/SANDBOX_ACCESS/PRIVACY_COMPUTE',
    f_api_endpoint VARCHAR(255) COMMENT 'API端点',
    f_api_count BIGINT DEFAULT 0 COMMENT 'API调用次数',
    f_data_volume BIGINT DEFAULT 0 COMMENT '数据量(Byte)',
    f_tx_hash VARCHAR(128) COMMENT '交易哈希',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_consume_time DATETIME COMMENT '消费时间',
    KEY idx_contract_id (f_contract_id),
    KEY idx_consumer_tenant_id (f_consumer_tenant_id),
    KEY idx_provider_tenant_id (f_provider_tenant_id),
    KEY idx_consume_type (f_consume_type),
    KEY idx_create_time (f_create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据消费日志表';