-- 计量计费模块数据库初始化脚本
USE tds;

-- 计费模板表
CREATE TABLE IF NOT EXISTS tb_billing_template (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_template_code VARCHAR(64) NOT NULL COMMENT '模板编码',
    f_template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
    f_billing_model VARCHAR(32) NOT NULL COMMENT '计费模型：FIXED/API_CALL/VOLUME/SUBSCRIPTION/CUSTOM',
    f_base_price DECIMAL(12,2) DEFAULT 0.00 COMMENT '基础价格',
    f_unit_price DECIMAL(12,4) DEFAULT 0.00 COMMENT '单价',
    f_unit VARCHAR(32) COMMENT '单位：次/GB/小时',
    f_free_quota INT DEFAULT 0 COMMENT '免费额度',
    f_description TEXT COMMENT '描述',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_template_code (f_template_code),
    KEY idx_template_name (f_template_name),
    KEY idx_billing_model (f_billing_model)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计费模板表';

-- 产品定价表
CREATE TABLE IF NOT EXISTS tb_product_pricing (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_product_id VARCHAR(32) NOT NULL COMMENT '产品ID',
    f_template_id VARCHAR(32) COMMENT '计费模板ID',
    f_billing_model VARCHAR(32) NOT NULL COMMENT '计费模型',
    f_price DECIMAL(12,2) DEFAULT 0.00 COMMENT '定价',
    f_unit_price DECIMAL(12,4) DEFAULT 0.00 COMMENT '单价',
    f_unit VARCHAR(32) COMMENT '单位',
    f_min_quota INT DEFAULT 1 COMMENT '最小购买量',
    f_max_quota INT COMMENT '最大购买量',
    f_start_time DATETIME COMMENT '生效时间',
    f_end_time DATETIME COMMENT '失效时间',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    KEY idx_product_id (f_product_id),
    KEY idx_template_id (f_template_id),
    KEY idx_start_time (f_start_time),
    KEY idx_end_time (f_end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品定价表';

-- 用量记录表
CREATE TABLE IF NOT EXISTS tb_usage_record (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_tenant_id VARCHAR(32) NOT NULL COMMENT '租户ID',
    f_product_id VARCHAR(32) NOT NULL COMMENT '产品ID',
    f_order_id VARCHAR(32) COMMENT '订单ID',
    f_usage_type VARCHAR(32) NOT NULL COMMENT '用量类型：API_CALL/DATA_VOLUME/STORAGE/COMPUTE',
    f_usage_count BIGINT DEFAULT 0 COMMENT '用量数量',
    f_unit_price DECIMAL(12,4) DEFAULT 0.00 COMMENT '单价',
    f_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '金额',
    f_usage_period VARCHAR(7) COMMENT '用量周期：YYYY-MM',
    f_usage_time DATETIME COMMENT '用量时间',
    f_description VARCHAR(512) COMMENT '描述',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_tenant_id (f_tenant_id),
    KEY idx_product_id (f_product_id),
    KEY idx_usage_period (f_usage_period),
    KEY idx_usage_time (f_usage_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用量记录表';

-- 账单表
CREATE TABLE IF NOT EXISTS tb_bill (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_bill_code VARCHAR(64) NOT NULL COMMENT '账单编码',
    f_tenant_id VARCHAR(32) NOT NULL COMMENT '租户ID',
    f_billing_period VARCHAR(7) NOT NULL COMMENT '账期：YYYY-MM',
    f_total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额',
    f_paid_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '已支付金额',
    f_pending_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '待支付金额',
    f_status INT DEFAULT 0 COMMENT '状态：0-待结算 1-已确认 2-已支付 3-已逾期',
    f_due_date DATETIME COMMENT '到期日',
    f_paid_time DATETIME COMMENT '支付时间',
    f_payment_method VARCHAR(32) COMMENT '支付方式',
    f_remark TEXT COMMENT '备注',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_bill_code (f_bill_code),
    KEY idx_tenant_id (f_tenant_id),
    KEY idx_billing_period (f_billing_period),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表';