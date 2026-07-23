-- 可信数据空间 数字合约模块 数据库初始化脚本
-- 数据库: tds

CREATE DATABASE IF NOT EXISTS tds DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tds;

-- 数字合约表
CREATE TABLE IF NOT EXISTS `tb_digital_contract` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `order_code` VARCHAR(64) DEFAULT NULL COMMENT '订单编号',
  `contract_code` VARCHAR(64) DEFAULT NULL COMMENT '合约编号',
  `contract_type` INT(2) DEFAULT NULL COMMENT '合约类型',
  `contract_status` INT(2) DEFAULT NULL COMMENT '合约状态(1待签,2签署中,3执行,4拒绝,5终止)',
  `contract_abstract` TEXT DEFAULT NULL COMMENT '合约摘要(SHA256)',
  `contract_start_time` DATETIME DEFAULT NULL COMMENT '合约开始时间',
  `contract_end_time` DATETIME DEFAULT NULL COMMENT '合约结束时间',
  `contract_json` TEXT DEFAULT NULL COMMENT '合约JSON',
  `resource_snapshot` TEXT DEFAULT NULL COMMENT '资源快照',
  `appid` VARCHAR(64) DEFAULT NULL COMMENT '应用ID',
  `appkey` VARCHAR(128) DEFAULT NULL COMMENT '应用密钥',
  `provider_institution_id` VARCHAR(64) DEFAULT NULL COMMENT '供应方机构ID',
  `provider_institution_name` VARCHAR(128) DEFAULT NULL COMMENT '供应方机构名称',
  `provider_contact_name` VARCHAR(64) DEFAULT NULL COMMENT '供应方联系人',
  `provider_phone` VARCHAR(32) DEFAULT NULL COMMENT '供应方电话',
  `provider_email` VARCHAR(64) DEFAULT NULL COMMENT '供应方邮箱',
  `provider_connector_address` VARCHAR(256) DEFAULT NULL COMMENT '供应方连接器地址',
  `provider_institution_address` VARCHAR(256) DEFAULT NULL COMMENT '供应方机构地址',
  `provider_public_key` TEXT DEFAULT NULL COMMENT '供应方公钥',
  `provider_signature` TEXT DEFAULT NULL COMMENT '供应方签名',
  `provider_sign_time` DATETIME DEFAULT NULL COMMENT '供应方签名时间',
  `use_institution_id` VARCHAR(64) DEFAULT NULL COMMENT '使用方机构ID',
  `use_institution_name` VARCHAR(128) DEFAULT NULL COMMENT '使用方机构名称',
  `use_contact_name` VARCHAR(64) DEFAULT NULL COMMENT '使用方联系人',
  `use_phone` VARCHAR(32) DEFAULT NULL COMMENT '使用方电话',
  `use_email` VARCHAR(64) DEFAULT NULL COMMENT '使用方邮箱',
  `use_connector_address` VARCHAR(256) DEFAULT NULL COMMENT '使用方连接器地址',
  `use_institution_address` VARCHAR(256) DEFAULT NULL COMMENT '使用方机构地址',
  `use_public_key` TEXT DEFAULT NULL COMMENT '使用方公钥',
  `use_signature` TEXT DEFAULT NULL COMMENT '使用方签名',
  `use_sign_time` DATETIME DEFAULT NULL COMMENT '使用方签名时间',
  `f_tenant_id` VARCHAR(32) DEFAULT NULL COMMENT '租户ID',
  `f_create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
  PRIMARY KEY (`id`),
  KEY `idx_order_code` (`order_code`),
  KEY `idx_contract_code` (`contract_code`),
  KEY `idx_contract_status` (`contract_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字合约管理';