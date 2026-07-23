-- 修改数据空间成员表，删除旧唯一索引
USE tds;

-- 删除旧的基于user_id的唯一索引（避免唯一索引冲突）
ALTER TABLE tb_data_space_member DROP INDEX uk_space_user;