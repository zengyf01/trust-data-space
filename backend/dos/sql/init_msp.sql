-- 可信数据空间 密算平台(MSP) 数据库初始化脚本
-- 数据库: msp

CREATE DATABASE IF NOT EXISTS msp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE msp;

-- MSP任务表
CREATE TABLE IF NOT EXISTS `tb_msp_task` (
  `f_id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `f_task_code` VARCHAR(64) DEFAULT NULL COMMENT '任务编号',
  `f_name` VARCHAR(128) NOT NULL COMMENT '任务名称',
  `f_type` INT(2) DEFAULT NULL COMMENT '任务类型(1PSI,2MPC,3联邦学习,4自定义代码,5纵向联邦,6复合任务)',
  `f_status` INT(2) DEFAULT 1 COMMENT '任务状态(1创建,2待执行,3执行中,4完成,5失败,6取消)',
  `f_algorithm` VARCHAR(128) DEFAULT NULL COMMENT '算法名称',
  `f_participants` TEXT DEFAULT NULL COMMENT '参与方列表(JSON)',
  `f_inputs` TEXT DEFAULT NULL COMMENT '输入配置(JSON)',
  `f_parameters` TEXT DEFAULT NULL COMMENT '任务参数(JSON)',
  `f_description` TEXT DEFAULT NULL COMMENT '任务描述',
  `f_code` TEXT DEFAULT NULL COMMENT '自定义代码',
  `f_result` TEXT DEFAULT NULL COMMENT '执行结果',
  `f_execution_log` TEXT DEFAULT NULL COMMENT '执行日志',
  `f_node_mode` VARCHAR(32) DEFAULT 'RAY' COMMENT '节点模式(RAY/TEE)',
  `f_creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `f_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标记(0未删,1已删)',
  PRIMARY KEY (`f_id`),
  UNIQUE KEY `uk_task_code` (`f_task_code`),
  KEY `idx_status` (`f_status`),
  KEY `idx_type` (`f_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MSP任务表';

-- MSP节点表
CREATE TABLE IF NOT EXISTS `tb_msp_node` (
  `f_id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `f_node_id` VARCHAR(64) NOT NULL COMMENT '节点ID',
  `f_node_name` VARCHAR(128) NOT NULL COMMENT '节点名称',
  `f_status` INT(2) DEFAULT 1 COMMENT '节点状态(1在线,2离线,3故障)',
  `f_node_mode` VARCHAR(32) DEFAULT 'RAY' COMMENT '节点模式(RAY/TEE)',
  `f_endpoint` VARCHAR(256) DEFAULT NULL COMMENT '内部端点',
  `f_external_endpoint` VARCHAR(256) DEFAULT NULL COMMENT '外部端点',
  `f_capabilities` TEXT DEFAULT NULL COMMENT '节点能力(JSON)',
  `f_tags` VARCHAR(512) DEFAULT NULL COMMENT '节点标签',
  `f_last_heartbeat` DATETIME DEFAULT NULL COMMENT '最后心跳时间',
  `f_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标记(0未删,1已删)',
  PRIMARY KEY (`f_id`),
  UNIQUE KEY `uk_node_id` (`f_node_id`),
  KEY `idx_status` (`f_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MSP节点表';

-- MSP数据源表
CREATE TABLE IF NOT EXISTS `tb_msp_datasource` (
  `f_id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `f_datasource_id` VARCHAR(64) NOT NULL COMMENT '数据源ID',
  `f_node_id` VARCHAR(64) DEFAULT NULL COMMENT '所属节点ID',
  `f_name` VARCHAR(128) NOT NULL COMMENT '数据源名称',
  `f_type` INT(2) DEFAULT NULL COMMENT '数据源类型(1MySQL,2PostgreSQL,3Oracle,4SQLServer,5CSV,6HTTP)',
  `f_host` VARCHAR(256) DEFAULT NULL COMMENT '主机地址',
  `f_port` INT(6) DEFAULT NULL COMMENT '端口',
  `f_database_name` VARCHAR(64) DEFAULT NULL COMMENT '数据库名',
  `f_username` VARCHAR(64) DEFAULT NULL COMMENT '用户名',
  `f_password` VARCHAR(256) DEFAULT NULL COMMENT '密码(加密)',
  `f_table_name` VARCHAR(128) DEFAULT NULL COMMENT '表名',
  `f_columns` TEXT DEFAULT NULL COMMENT '字段列表(JSON)',
  `f_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标记(0未删,1已删)',
  PRIMARY KEY (`f_id`),
  UNIQUE KEY `uk_datasource_id` (`f_datasource_id`),
  KEY `idx_node_id` (`f_node_id`),
  KEY `idx_type` (`f_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MSP数据源表';

-- MSP用户表
CREATE TABLE IF NOT EXISTS `tb_msp_user` (
  `f_id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `f_user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `f_username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `f_password` VARCHAR(256) NOT NULL COMMENT '密码',
  `f_email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `f_phone` VARCHAR(32) DEFAULT NULL COMMENT '手机号',
  `f_role` VARCHAR(32) DEFAULT NULL COMMENT '角色',
  `f_status` VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/NORMAL/LOCKED)',
  `f_enabled` INT(1) DEFAULT 1 COMMENT '是否启用(0否,1是)',
  `f_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标记(0未删,1已删)',
  PRIMARY KEY (`f_id`),
  UNIQUE KEY `uk_user_id` (`f_user_id`),
  UNIQUE KEY `uk_username` (`f_username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MSP用户表';

-- MSP角色表
CREATE TABLE IF NOT EXISTS `tb_msp_role` (
  `f_id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `f_role_id` VARCHAR(64) NOT NULL COMMENT '角色ID',
  `f_role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `f_role_code` VARCHAR(64) NOT NULL COMMENT '角色代码',
  `f_description` VARCHAR(256) DEFAULT NULL COMMENT '角色描述',
  `f_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标记(0未删,1已删)',
  PRIMARY KEY (`f_id`),
  UNIQUE KEY `uk_role_id` (`f_role_id`),
  UNIQUE KEY `uk_role_code` (`f_role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MSP角色表';

-- MSP权限表
CREATE TABLE IF NOT EXISTS `tb_msp_permission` (
  `f_id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `f_permission_id` VARCHAR(64) NOT NULL COMMENT '权限ID',
  `f_permission_name` VARCHAR(64) NOT NULL COMMENT '权限名称',
  `f_permission_code` VARCHAR(128) NOT NULL COMMENT '权限代码',
  `f_parent_id` VARCHAR(64) DEFAULT NULL COMMENT '父权限ID',
  `f_resource_type` VARCHAR(32) DEFAULT NULL COMMENT '资源类型(MENU/BUTTON/API)',
  `f_path` VARCHAR(256) DEFAULT NULL COMMENT '路由路径',
  `f_icon` VARCHAR(64) DEFAULT NULL COMMENT '图标',
  `f_sort_order` INT(4) DEFAULT 0 COMMENT '排序',
  `f_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标记(0未删,1已删)',
  PRIMARY KEY (`f_id`),
  UNIQUE KEY `uk_permission_id` (`f_permission_id`),
  KEY `idx_parent_id` (`f_parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MSP权限表';

-- MSP审计日志表
CREATE TABLE IF NOT EXISTS `tb_msp_audit_log` (
  `f_id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `f_user_id` VARCHAR(64) DEFAULT NULL COMMENT '操作用户ID',
  `f_operation` VARCHAR(64) NOT NULL COMMENT '操作类型',
  `f_resource_type` VARCHAR(64) DEFAULT NULL COMMENT '资源类型',
  `f_resource_id` VARCHAR(64) DEFAULT NULL COMMENT '资源ID',
  `f_detail` TEXT DEFAULT NULL COMMENT '操作详情',
  `f_ip_address` VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  `f_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`f_id`),
  KEY `idx_user_id` (`f_user_id`),
  KEY `idx_operation` (`f_operation`),
  KEY `idx_create_time` (`f_create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MSP审计日志表';

-- MSP告警配置表
CREATE TABLE IF NOT EXISTS `tb_msp_alert` (
  `f_id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `f_alert_id` VARCHAR(64) NOT NULL COMMENT '告警ID',
  `f_alert_name` VARCHAR(128) NOT NULL COMMENT '告警名称',
  `f_alert_type` VARCHAR(32) DEFAULT NULL COMMENT '告警类型',
  `f_condition` VARCHAR(256) DEFAULT NULL COMMENT '触发条件',
  `f_threshold` VARCHAR(64) DEFAULT NULL COMMENT '阈值',
  `f_enabled` INT(1) DEFAULT 1 COMMENT '是否启用(0否,1是)',
  `f_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标记(0未删,1已删)',
  PRIMARY KEY (`f_id`),
  UNIQUE KEY `uk_alert_id` (`f_alert_id`),
  KEY `idx_enabled` (`f_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MSP告警配置表';

-- MSP系统配置表
CREATE TABLE IF NOT EXISTS `tb_msp_system_config` (
  `f_id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `f_config_key` VARCHAR(128) NOT NULL COMMENT '配置键',
  `f_config_value` VARCHAR(512) DEFAULT NULL COMMENT '配置值',
  `f_config_type` VARCHAR(32) DEFAULT NULL COMMENT '配置类型(STRING/INT/BOOLEAN/JSON)',
  `f_description` VARCHAR(256) DEFAULT NULL COMMENT '配置描述',
  `f_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标记(0未删,1已删)',
  PRIMARY KEY (`f_id`),
  UNIQUE KEY `uk_config_key` (`f_config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MSP系统配置表';
