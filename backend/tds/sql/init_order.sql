-- 可信数据空间 交易订单模块 数据库初始化脚本
-- 数据库: tds

USE tds;

-- 交易订单表
CREATE TABLE IF NOT EXISTS `tb_trading_order` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `order_code` VARCHAR(64) NOT NULL COMMENT '订单编号',
  `product_id` VARCHAR(32) NOT NULL COMMENT '关联产品ID',
  `product_code` VARCHAR(64) NOT NULL COMMENT '产品编号',
  `product_name` VARCHAR(128) NOT NULL COMMENT '产品名称',
  `product_snapshot` TEXT DEFAULT NULL COMMENT '产品快照JSON',
  `provider_institution_id` VARCHAR(64) NOT NULL COMMENT '供方机构ID',
  `provider_institution_name` VARCHAR(128) NOT NULL COMMENT '供方机构名称',
  `use_institution_id` VARCHAR(64) NOT NULL COMMENT '需方机构ID',
  `use_institution_name` VARCHAR(128) NOT NULL COMMENT '需方机构名称',
  `provider_connector_sn` VARCHAR(64) DEFAULT NULL COMMENT '供方连接器编号',
  `use_connector_sn` VARCHAR(64) DEFAULT NULL COMMENT '需方连接器编号',
  `pricing_model` VARCHAR(32) DEFAULT NULL COMMENT '计费模式(FREE/PER_USE/SUBSCRIPTION)',
  `price` DECIMAL(10,2) DEFAULT 0 COMMENT '价格',
  `order_status` INT(2) DEFAULT 1 COMMENT '订单状态(1待审批,2已审批,3签署中,4执行中,5已完成,6已驳回,7已取消)',
  `pay_status` INT(2) DEFAULT 1 COMMENT '支付状态(1未支付,2已支付,3已退款)',
  `delivery_type` INT(2) DEFAULT 1 COMMENT '交付类型(1数据服务,2安全沙盒,3隐私计算)',
  `delivery_api_info` TEXT DEFAULT NULL COMMENT '交付API信息',
  `valid_start_time` DATETIME DEFAULT NULL COMMENT '订单有效期开始',
  `valid_end_time` DATETIME DEFAULT NULL COMMENT '订单有效期结束',
  `reject_reason` VARCHAR(256) DEFAULT NULL COMMENT '驳回原因',
  `approver` VARCHAR(64) DEFAULT NULL COMMENT '审批人',
  `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `approve_remark` VARCHAR(256) DEFAULT NULL COMMENT '审批备注',
  `f_tenant_id` VARCHAR(32) DEFAULT NULL COMMENT '租户ID',
  `f_create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_code` (`order_code`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_provider_institution_id` (`provider_institution_id`),
  KEY `idx_use_institution_id` (`use_institution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单管理';

-- 订单历史记录表
CREATE TABLE IF NOT EXISTS `tb_order_history` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `order_id` VARCHAR(32) NOT NULL COMMENT '订单ID',
  `order_code` VARCHAR(64) NOT NULL COMMENT '订单编号',
  `operate_type` VARCHAR(32) NOT NULL COMMENT '操作类型',
  `operate_desc` VARCHAR(256) DEFAULT NULL COMMENT '操作描述',
  `operator` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  `operator_id` VARCHAR(32) DEFAULT NULL COMMENT '操作人ID',
  `operate_time` DATETIME DEFAULT NULL COMMENT '操作时间',
  `from_status` INT(2) DEFAULT NULL COMMENT '变更前状态',
  `to_status` INT(2) DEFAULT NULL COMMENT '变更后状态',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `f_tenant_id` VARCHAR(32) DEFAULT NULL COMMENT '租户ID',
  `f_create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_code` (`order_code`),
  KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单历史记录';