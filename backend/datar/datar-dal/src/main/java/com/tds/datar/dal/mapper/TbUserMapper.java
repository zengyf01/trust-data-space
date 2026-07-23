package com.tds.datar.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.datar.dal.entity.TbUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 */
@Mapper
public interface TbUserMapper extends BaseMapper<TbUser> {
}
