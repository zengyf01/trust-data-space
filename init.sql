-- ============================================================
-- 可信数据空间 统一数据库初始化入口
-- 首次启动时由 mysql 容器 /docker-entrypoint-initdb.d 自动执行
-- ============================================================

-- 创建所有数据库
CREATE DATABASE IF NOT EXISTS tds    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS dos    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS datar  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS msp    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- TDS 平台 - 加载所有模块的 SQL（数字合约、机构、连接器、订单、计费、存证、系统等）
-- ============================================================
USE tds;
SOURCE /docker-entrypoint-initdb.d/tds/init_organization.sql;
SOURCE /docker-entrypoint-initdb.d/tds/init_dataspace.sql;
SOURCE /docker-entrypoint-initdb.d/tds/init_data_resource.sql;
SOURCE /docker-entrypoint-initdb.d/tds/init_connector.sql;
SOURCE /docker-entrypoint-initdb.d/tds/init_order.sql;
SOURCE /docker-entrypoint-initdb.d/tds/init_billing.sql;
SOURCE /docker-entrypoint-initdb.d/tds/init_evidence.sql;
SOURCE /docker-entrypoint-initdb.d/tds/init_system.sql;
SOURCE /docker-entrypoint-initdb.d/tds/init_deploy.sql;
SOURCE /docker-entrypoint-initdb.d/tds/init.sql;

-- ============================================================
-- DOS 交付平台
-- ============================================================
USE dos;
SOURCE /docker-entrypoint-initdb.d/dos/init_dos.sql;

-- ============================================================
-- Datar 连接器
-- ============================================================
USE datar;
SOURCE /docker-entrypoint-initdb.d/datar/init_datar.sql;

-- ============================================================
-- MSP 密算平台
-- ============================================================
USE msp;
SOURCE /docker-entrypoint-initdb.d/msp/init_msp.sql;
