package com.tds.dos.dal.msp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.dal.msp.entity.TbSystemConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP System Config Mapper
 */
@Mapper
@DS("dos")
public interface TbSystemConfigMapper extends BaseMapper<TbSystemConfig> {
}
