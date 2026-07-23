package com.tds.dos.msp.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.msp.dal.entity.TbMspDatasource;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP DataSource Mapper
 */
@Mapper
@DS("msp")
public interface TbMspDatasourceMapper extends BaseMapper<TbMspDatasource> {
}