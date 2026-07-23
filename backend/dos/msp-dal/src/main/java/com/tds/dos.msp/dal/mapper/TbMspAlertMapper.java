package com.tds.dos.msp.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.msp.dal.entity.TbMspAlert;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP Alert Mapper
 */
@Mapper
@DS("msp")
public interface TbMspAlertMapper extends BaseMapper<TbMspAlert> {
}