package com.tds.dos.msp.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.msp.dal.entity.TbMspRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP Role Mapper
 */
@Mapper
@DS("msp")
public interface TbMspRoleMapper extends BaseMapper<TbMspRole> {
}