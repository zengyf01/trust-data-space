-- 机构管理模块数据库初始化脚本
USE tds;

-- 机构表
CREATE TABLE IF NOT EXISTS tb_organization (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_org_code VARCHAR(64) NOT NULL COMMENT '机构编码',
    f_org_name VARCHAR(128) NOT NULL COMMENT '机构名称',
    f_org_type VARCHAR(32) COMMENT '机构类型：ENTERPRISE/GOV/INDIVIDUAL',
    f_org_desc TEXT COMMENT '机构描述',
    f_legal_person VARCHAR(64) COMMENT '法人代表',
    f_contact VARCHAR(64) COMMENT '联系人',
    f_contact_phone VARCHAR(32) COMMENT '联系电话',
    f_contact_email VARCHAR(128) COMMENT '联系邮箱',
    f_address VARCHAR(512) COMMENT '地址',
    f_business_license VARCHAR(256) COMMENT '营业执照',
    f_status INT DEFAULT 0 COMMENT '状态：0-待审核 1-正常 2-冻结 3-已注销',
    f_user_count INT DEFAULT 0 COMMENT '用户数量',
    f_connector_count INT DEFAULT 0 COMMENT '连接器数量',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_org_code (f_org_code),
    KEY idx_org_name (f_org_name),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构表';

-- 部门表
CREATE TABLE IF NOT EXISTS tb_department (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_dept_code VARCHAR(64) NOT NULL COMMENT '部门编码',
    f_dept_name VARCHAR(128) NOT NULL COMMENT '部门名称',
    f_parent_id VARCHAR(32) COMMENT '上级部门ID',
    f_org_id VARCHAR(32) COMMENT '机构ID',
    f_dept_level INT DEFAULT 1 COMMENT '部门层级',
    f_sort_order INT DEFAULT 0 COMMENT '排序',
    f_manager_id VARCHAR(32) COMMENT '部门负责人ID',
    f_manager_name VARCHAR(64) COMMENT '部门负责人名称',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_dept_code (f_dept_code),
    KEY idx_parent_id (f_parent_id),
    KEY idx_org_id (f_org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 角色表
CREATE TABLE IF NOT EXISTS tb_role (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    f_role_name VARCHAR(128) NOT NULL COMMENT '角色名称',
    f_role_type VARCHAR(32) COMMENT '角色类型：SYSTEM/BUSINESS',
    f_org_id VARCHAR(32) COMMENT '所属机构ID',
    f_role_desc VARCHAR(512) COMMENT '角色描述',
    f_is_system INT DEFAULT 0 COMMENT '是否系统角色：0-否 1-是',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_role_code (f_role_code),
    KEY idx_org_id (f_org_id),
    KEY idx_role_type (f_role_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户表
CREATE TABLE IF NOT EXISTS tb_user (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_username VARCHAR(64) NOT NULL COMMENT '用户名',
    f_password VARCHAR(256) NOT NULL COMMENT '密码',
    f_real_name VARCHAR(64) COMMENT '真实姓名',
    f_nick_name VARCHAR(64) COMMENT '昵称',
    f_email VARCHAR(128) COMMENT '邮箱',
    f_phone VARCHAR(32) COMMENT '手机号',
    f_avatar VARCHAR(256) COMMENT '头像',
    f_org_id VARCHAR(32) COMMENT '所属机构ID',
    f_dept_id VARCHAR(32) COMMENT '所属部门ID',
    f_user_type VARCHAR(32) COMMENT '用户类型',
    f_status INT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    f_last_login_ip VARCHAR(32) COMMENT '最后登录IP',
    f_last_login_time DATETIME COMMENT '最后登录时间',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_username (f_username),
    KEY idx_org_id (f_org_id),
    KEY idx_dept_id (f_dept_id),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

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

-- 初始化系统角色
INSERT INTO tb_role (f_id, f_role_code, f_role_name, f_role_type, f_org_id, f_role_desc, f_is_system, f_create_time, f_update_time) VALUES
('1', 'SUPER_ADMIN', '超级管理员', 'SYSTEM', NULL, '系统超级管理员，拥有所有权限', 1, NOW(), NOW()),
('2', 'ORG_ADMIN', '机构管理员', 'SYSTEM', NULL, '机构管理员，管理本机构所有资源', 1, NOW(), NOW()),
('3', 'DEPT_ADMIN', '部门管理员', 'BUSINESS', NULL, '部门管理员，管理本部门用户', 1, NOW(), NOW()),
('4', 'NORMAL_USER', '普通用户', 'BUSINESS', NULL, '普通用户，基本操作权限', 1, NOW(), NOW());

-- 初始化超级管理员用户
INSERT INTO tb_user (f_id, f_username, f_password, f_real_name, f_user_type, f_status, f_create_time, f_update_time) VALUES
('1', 'admin', 'admin', '系统管理员', 'SUPER_ADMIN', 1, NOW(), NOW());

-- 关联超级管理员角色
INSERT INTO tb_user_role (f_id, f_user_id, f_role_id, f_create_time) VALUES
('1', '1', '1', NOW());