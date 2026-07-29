package com.tds.dos.dal.msp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.dal.msp.entity.TbRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP Role Mapper
 */
@Mapper
@DS("dos")
public interface TbRoleMapper extends BaseMapper<TbRole> {
}
