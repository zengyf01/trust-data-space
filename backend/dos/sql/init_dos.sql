-- 可信数据空间 交付平台(DOS) 数据库初始化脚本
-- 数据库: dos

CREATE DATABASE IF NOT EXISTS dos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE dos;

-- 工单表
CREATE TABLE IF NOT EXISTS `tb_work_order` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `work_order_code` VARCHAR(64) NOT NULL COMMENT '工单编号',
  `order_code` VARCHAR(64) NOT NULL COMMENT '关联订单编号',
  `work_order_type` INT(2) NOT NULL COMMENT '工单类型(1数据服务,2安全沙盒,3隐私计算)',
  `work_order_status` INT(2) DEFAULT 1 COMMENT '工单状态(1待处理,2处理中,3已完成,4失败,5已取消)',
  `result_message` TEXT DEFAULT NULL COMMENT '处理结果',
  `config_json` TEXT DEFAULT NULL COMMENT '处理配置JSON',
  `output_file_path` VARCHAR(256) DEFAULT NULL COMMENT '产出文件路径',
  `output_file_url` VARCHAR(256) DEFAULT NULL COMMENT '产出文件URL',
  `start_time` DATETIME DEFAULT NULL COMMENT '执行开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '执行结束时间',
  `duration` INT(11) DEFAULT NULL COMMENT '耗时(秒)',
  `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `creator_id` VARCHAR(32) DEFAULT NULL COMMENT '创建人ID',
  `f_tenant_id` VARCHAR(32) DEFAULT NULL COMMENT '租户ID',
  `f_space_id` VARCHAR(32) DEFAULT NULL COMMENT '数据空间ID',
  `f_create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_work_order_code` (`work_order_code`),
  KEY `idx_order_code` (`order_code`),
  KEY `idx_work_order_type` (`work_order_type`),
  KEY `idx_work_order_status` (`work_order_status`),
  KEY `idx_dos_work_order_space_id` (`f_space_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单管理';