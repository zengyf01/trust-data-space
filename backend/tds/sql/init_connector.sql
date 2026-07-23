-- 可信数据空间 连接器管理模块 数据库初始化脚本
-- 数据库: tds

USE tds;

-- 连接器管理表
CREATE TABLE IF NOT EXISTS `tb_connector` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `sn` VARCHAR(64) NOT NULL COMMENT '序列号(设备唯一标识)',
  `name` VARCHAR(128) NOT NULL COMMENT '连接器名称',
  `type` INT(2) NOT NULL COMMENT '连接器类型(1数据连接器,2沙盒连接器,3隐私计算连接器)',
  `status` INT(2) NOT NULL COMMENT '状态(1在线,2离线,3离线待注册)',
  `version` VARCHAR(32) DEFAULT NULL COMMENT '当前版本',
  `ip_address` VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  `ssh_port` INT(5) DEFAULT NULL COMMENT 'SSH端口',
  `ssh_username` VARCHAR(64) DEFAULT NULL COMMENT 'SSH用户名',
  `ssh_password` VARCHAR(256) DEFAULT NULL COMMENT 'SSH密码(加密存储)',
  `ssh_private_key` TEXT DEFAULT NULL COMMENT 'SSH私钥(加密存储)',
  `mac_address` VARCHAR(64) DEFAULT NULL COMMENT 'MAC地址',
  `last_heartbeat` DATETIME DEFAULT NULL COMMENT '最后心跳时间',
  `registered_time` DATETIME DEFAULT NULL COMMENT '注册时间',
  `institution_id` VARCHAR(32) DEFAULT NULL COMMENT '所属机构ID',
  `institution_name` VARCHAR(128) DEFAULT NULL COMMENT '所属机构名称',
  `region` VARCHAR(64) DEFAULT NULL COMMENT '所属区域',
  `description` TEXT DEFAULT NULL COMMENT '描述',
  `f_tenant_id` VARCHAR(32) DEFAULT NULL COMMENT '租户ID',
  `f_create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sn` (`sn`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_institution_id` (`institution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='连接器管理';

-- 连接器版本表
CREATE TABLE IF NOT EXISTS `tb_connector_version` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `connector_id` VARCHAR(32) NOT NULL COMMENT '连接器ID',
  `version` VARCHAR(32) NOT NULL COMMENT '版本号',
  `file_path` VARCHAR(256) DEFAULT NULL COMMENT '文件路径',
  `file_size` BIGINT(20) DEFAULT NULL COMMENT '文件大小',
  `file_md5` VARCHAR(32) DEFAULT NULL COMMENT '文件MD5',
  `change_log` TEXT DEFAULT NULL COMMENT '变更日志',
  `status` INT(1) DEFAULT 0 COMMENT '状态(0未激活,1激活中,2已激活)',
  `f_tenant_id` VARCHAR(32) DEFAULT NULL COMMENT '租户ID',
  `f_create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `f_update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
  PRIMARY KEY (`id`),
  KEY `idx_connector_id` (`connector_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='连接器版本管理';

-- 连接器操作日志表
CREATE TABLE IF NOT EXISTS `tb_connector_log` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `connector_id` VARCHAR(32) NOT NULL COMMENT '连接器ID',
  `operate_type` VARCHAR(32) NOT NULL COMMENT '操作类型(DEPLOY部署,UPGRADE升级,UNINSTALL卸载,RESTART重启,STOP停止)',
  `operate_content` TEXT DEFAULT NULL COMMENT '操作内容',
  `operate_result` VARCHAR(32) DEFAULT NULL COMMENT '操作结果(成功SUCCESS,失败FAIL)',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `duration` INT(11) DEFAULT NULL COMMENT '耗时(秒)',
  `f_tenant_id` VARCHAR(32) DEFAULT NULL COMMENT '租户ID',
  `f_create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `f_delete_mark` INT(1) DEFAULT 0 COMMENT '删除标志(0未删,1已删)',
  PRIMARY KEY (`id`),
  KEY `idx_connector_id` (`connector_id`),
  KEY `idx_operate_type` (`operate_type`),
  KEY `idx_operate_time` (`f_create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='连接器操作日志';