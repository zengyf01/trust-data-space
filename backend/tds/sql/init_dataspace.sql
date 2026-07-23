-- 数据空间模块数据库初始化脚本
USE tds;

-- 数据空间表
CREATE TABLE IF NOT EXISTS tb_data_space (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_space_code VARCHAR(64) NOT NULL COMMENT '空间编码',
    f_space_name VARCHAR(128) NOT NULL COMMENT '空间名称',
    f_space_desc TEXT COMMENT '空间描述',
    f_owner_id VARCHAR(32) NOT NULL COMMENT '所有者ID',
    f_owner_name VARCHAR(128) COMMENT '所有者名称',
    f_organization_id VARCHAR(32) COMMENT '所属机构ID',
    f_organization_name VARCHAR(128) COMMENT '所属机构名称',
    f_status INT DEFAULT 1 COMMENT '状态：0-待审核 1-正常 2-冻结 3-已注销',
    f_space_type VARCHAR(32) DEFAULT 'PRIVATE' COMMENT '空间类型：PUBLIC/PRIVATE',
    f_member_count INT DEFAULT 0 COMMENT '成员数量',
    f_resource_count INT DEFAULT 0 COMMENT '资源数量',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_space_code (f_space_code),
    KEY idx_space_name (f_space_name),
    KEY idx_owner_id (f_owner_id),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据空间表';

-- 数据空间成员表
CREATE TABLE IF NOT EXISTS tb_data_space_member (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_space_id VARCHAR(32) NOT NULL COMMENT '数据空间ID',
    f_organization_id VARCHAR(32) COMMENT '机构ID',
    f_organization_name VARCHAR(128) COMMENT '机构名称',
    f_role INT NOT NULL COMMENT '角色：1-所有者 2-管理员 3-成员 4-访客',
    f_status INT DEFAULT 0 COMMENT '状态：0-待审核 1-已加入 2-已拒绝',
    f_apply_reason VARCHAR(512) COMMENT '申请原因',
    f_join_time DATETIME COMMENT '加入时间',
    f_expire_time DATETIME COMMENT '过期时间',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_space_org (f_space_id, f_organization_id),
    KEY idx_space_id (f_space_id),
    KEY idx_organization_id (f_organization_id),
    KEY idx_role (f_role),
    KEY idx_status (f_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据空间成员表';

-- 数据空间资源表
CREATE TABLE IF NOT EXISTS tb_data_space_resource (
    f_id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    f_space_id VARCHAR(32) NOT NULL COMMENT '数据空间ID',
    f_resource_type VARCHAR(32) NOT NULL COMMENT '资源类型：CATALOG/PRODUCT/DATASOURCE',
    f_resource_id VARCHAR(32) NOT NULL COMMENT '资源ID',
    f_resource_name VARCHAR(128) COMMENT '资源名称',
    f_resource_desc VARCHAR(512) COMMENT '资源描述',
    f_access_level INT DEFAULT 1 COMMENT '访问级别：1-只读 2-可写 3-可管理',
    f_tenant_id VARCHAR(32) COMMENT '租户ID',
    f_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    f_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    f_delete_mark INT DEFAULT 0 COMMENT '删除标记：0-未删 1-已删',
    UNIQUE KEY uk_space_resource (f_space_id, f_resource_id),
    KEY idx_space_id (f_space_id),
    KEY idx_resource_type (f_resource_type),
    KEY idx_resource_id (f_resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据空间资源表';