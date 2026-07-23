package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联Mapper
 */
@Mapper
public interface TbUserRoleMapper extends BaseMapper<TbUserRole> {
}