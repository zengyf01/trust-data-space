-- 可信数据空间 数据资源模块 数据库初始化脚本
-- 数据库: tds

USE tds;

-- 数据源表
CREATE TABLE IF NOT EXISTS `tb_data_source` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `source_code` VARCHAR(64) NOT NULL COMMENT '数据源编号',
  `source_name` VARCHAR(128) NOT NULL COMMENT '数据源名称',
  `source_type` INT(2) NOT NULL COMMENT '数据源类型(1 MySQL, 2 PostgreSQL, 3 SFTP, 4 HTTP, 5 OSS)',
  `host` VARCHAR(256) DEFAULT NULL COMMENT '主机地址',
  `port` INT(6) DEFAULT NULL COMMENT '端口',
  `database_name` VARCHAR(64) DEFAULT NULL COMMENT '数据库名',
  `username` VARCHAR(64) DEFAULT NULL COMMENT '用户名',
  `password` VARCHAR(256) DEFAULT NULL COMMENT '密码(加密存储)',
  `base_path` VARCHAR(256) DEFAULT NULL COMMENT '基础路径(SFTP/OSS用)',
  `conn_params` TEXT DEFAULT NULL COMMENT '连接参数(JSON)',
  `status` INT(2) DEFAULT 1 COMMENT '状态(1启用,2禁用)',
  `last_test_time` DATETIME DEFAULT NULL COMMENT '最后测试时间',
  `last_test_result` VARCHAR(32) DEFAULT NULL COMMENT '最后测试结果',
  `f_tenant_id` VARCHAR(32) DEFAULT NULL COMMENT '租户ID',
  `f_create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source_code` (`source_code`),
  KEY `idx_source_type` (`source_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源管理';

-- 资源目录表
CREATE TABLE IF NOT EXISTS `tb_catalog` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `catalog_code` VARCHAR(64) NOT NULL COMMENT '目录编号',
  `catalog_name` VARCHAR(128) NOT NULL COMMENT '目录名称',
  `data_source_id` VARCHAR(32) NOT NULL COMMENT '关联数据源ID',
  `schema_name` VARCHAR(64) DEFAULT NULL COMMENT 'schema名',
  `table_name` VARCHAR(64) DEFAULT NULL COMMENT '表名',
  `description` VARCHAR(512) DEFAULT NULL COMMENT '目录描述',
  `version` INT(4) DEFAULT 1 COMMENT '版本号',
  `status` INT(2) DEFAULT 1 COMMENT '状态(1草稿,2已发布,3已下线)',
  `f_tenant_id` VARCHAR(32) DEFAULT NULL COMMENT '租户ID',
  `f_create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_catalog_code` (`catalog_code`),
  KEY `idx_data_source_id` (`data_source_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源目录管理';

-- 目录字段表
CREATE TABLE IF NOT EXISTS `tb_catalog_field` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `catalog_id` VARCHAR(32) NOT NULL COMMENT '目录ID',
  `field_name` VARCHAR(64) NOT NULL COMMENT '字段名',
  `field_type` VARCHAR(32) NOT NULL COMMENT '字段类型',
  `field_comment` VARCHAR(256) DEFAULT NULL COMMENT '字段注释',
  `is_primary_key` INT(1) DEFAULT 0 COMMENT '是否主键(0否,1是)',
  `is_nullable` INT(1) DEFAULT 1 COMMENT '是否可空(0否,1是)',
  `is_sensitive` INT(1) DEFAULT 0 COMMENT '是否敏感字段(0否,1是)',
  `desensitize_rule` VARCHAR(32) DEFAULT NULL COMMENT '脱敏规则',
  `sort_order` INT(4) DEFAULT 0 COMMENT '排序',
  `f_tenant_id` VARCHAR(32) DEFAULT NULL COMMENT '租户ID',
  `f_create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
  PRIMARY KEY (`id`),
  KEY `idx_catalog_id` (`catalog_id`),
  KEY `idx_is_sensitive` (`is_sensitive`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目录字段信息';

-- 数据产品表
CREATE TABLE IF NOT EXISTS `tb_data_product` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `product_code` VARCHAR(64) NOT NULL COMMENT '产品编号',
  `product_name` VARCHAR(128) NOT NULL COMMENT '产品名称',
  `catalog_id` VARCHAR(32) NOT NULL COMMENT '关联目录ID',
  `product_desc` VARCHAR(512) DEFAULT NULL COMMENT '产品描述',
  `pricing_model` VARCHAR(32) DEFAULT NULL COMMENT '计费模式(FREE/PER_USE/SUBSCRIPTION)',
  `price` DECIMAL(10,2) DEFAULT 0 COMMENT '价格',
  `status` INT(2) DEFAULT 1 COMMENT '状态(1草稿,2待审核,3审核通过,4审核拒绝,5已发布,6已下架)',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `reject_reason` VARCHAR(256) DEFAULT NULL COMMENT '审核拒绝原因',
  `f_tenant_id` VARCHAR(32) DEFAULT NULL COMMENT '租户ID',
  `f_create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`),
  KEY `idx_catalog_id` (`catalog_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据产品管理';