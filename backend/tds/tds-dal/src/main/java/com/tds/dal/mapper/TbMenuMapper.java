package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单Mapper
 */
@Mapper
public interface TbMenuMapper extends BaseMapper<TbMenu> {
}
