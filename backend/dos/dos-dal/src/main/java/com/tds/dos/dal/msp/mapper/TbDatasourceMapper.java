package com.tds.dos.dal.msp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.dal.msp.entity.TbDatasource;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP DataSource Mapper
 */
@Mapper
@DS("dos")
public interface TbDatasourceMapper extends BaseMapper<TbDatasource> {
}
