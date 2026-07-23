package com.tds.dos.msp.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.msp.dal.entity.TbMspSystemConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP System Config Mapper
 */
@Mapper
@DS("msp")
public interface TbMspSystemConfigMapper extends BaseMapper<TbMspSystemConfig> {
}