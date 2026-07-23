package com.tds.dos.msp.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.msp.dal.entity.TbMspUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP User Mapper
 */
@Mapper
@DS("msp")
public interface TbMspUserMapper extends BaseMapper<TbMspUser> {
}