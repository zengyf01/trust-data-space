package com.tds.dos.dal.msp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.dal.msp.entity.TbTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP Task Mapper
 */
@Mapper
@DS("dos")
public interface TbTaskMapper extends BaseMapper<TbTask> {
}
