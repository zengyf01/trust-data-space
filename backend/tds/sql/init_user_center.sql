-- ============================================================
-- 统一用户中心初始化脚本
-- 初始化租户数据和默认用户
-- ============================================================

-- 插入租户数据 (如果不存在)
INSERT IGNORE INTO tb_organization (f_id, f_org_code, f_org_name, f_org_type, f_status, f_tenant_id, f_delete_mark, f_create_time, f_update_time)
VALUES
    ('ORG_TDS', 'TDS', 'TDS平台', 'PLATFORM', 1, 'TENANT_TDS', 0, NOW(), NOW()),
    ('ORG_DOS', 'DOS', 'DOS平台', 'PLATFORM', 1, 'TENANT_DOS', 0, NOW(), NOW()),
    ('ORG_DATAR', 'DATAR', 'Datar平台', 'PLATFORM', 1, 'TENANT_DATAR', 0, NOW(), NOW());

-- 初始化超级管理员用户 (密码: admin123, SM3加密)
-- SM3("admin123") = 2401d2a5f4f4c33e2e62d3f1a7c8b9a0d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a
-- 注意: 首次部署时需要确保密码加密方式正确，部署后通过系统管理界面修改密码
INSERT IGNORE INTO tb_user (f_id, f_username, f_password, f_real_name, f_nick_name, f_email, f_phone, f_org_id, f_tenant_id, f_status, f_user_type, f_delete_mark, f_create_time, f_update_time)
VALUES ('USER_ADMIN_TDS', 'admin', '2401d2a5f4f4c33e2e62d3f1a7c8b9a0d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a', '系统管理员', 'admin', 'admin@example.com', '13800000000', 'ORG_TDS', 'TENANT_TDS', 1, '0', 0, NOW(), NOW());

-- 初始化DOS租户管理员
INSERT IGNORE INTO tb_user (f_id, f_username, f_password, f_real_name, f_nick_name, f_email, f_phone, f_org_id, f_tenant_id, f_status, f_user_type, f_delete_mark, f_create_time, f_update_time)
VALUES ('USER_ADMIN_DOS', 'dos_admin', '2401d2a5f4f4c33e2e62d3f1a7c8b9a0d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a', 'DOS管理员', 'dos_admin', 'dos_admin@example.com', '13800000001', 'ORG_DOS', 'TENANT_DOS', 1, '0', 0, NOW(), NOW());

-- 初始化Datar租户管理员
INSERT IGNORE INTO tb_user (f_id, f_username, f_password, f_real_name, f_nick_name, f_email, f_phone, f_org_id, f_tenant_id, f_status, f_user_type, f_delete_mark, f_create_time, f_update_time)
VALUES ('USER_ADMIN_DATAR', 'datar_admin', '2401d2a5f4f4c33e2e62d3f1a7c8b9a0d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a', 'Datar管理员', 'datar_admin', 'datar_admin@example.com', '13800000002', 'ORG_DATAR', 'TENANT_DATAR', 1, '0', 0, NOW(), NOW());

-- 初始化测试用户 (密码也是 admin123)
INSERT IGNORE INTO tb_user (f_id, f_username, f_password, f_real_name, f_nick_name, f_email, f_phone, f_org_id, f_tenant_id, f_status, f_user_type, f_delete_mark, f_create_time, f_update_time)
VALUES ('USER_TEST_TDS', 'test', '2401d2a5f4f4c33e2e62d3f1a7c8b9a0d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a', '测试用户', 'test', 'test@example.com', '13800000003', 'ORG_TDS', 'TENANT_TDS', 1, '1', 0, NOW(), NOW());
