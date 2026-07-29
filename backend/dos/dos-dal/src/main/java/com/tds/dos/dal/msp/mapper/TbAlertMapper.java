package com.tds.dos.dal.msp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.dal.msp.entity.TbAlert;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP Alert Mapper
 */
@Mapper
@DS("dos")
public interface TbAlertMapper extends BaseMapper<TbAlert> {
}
