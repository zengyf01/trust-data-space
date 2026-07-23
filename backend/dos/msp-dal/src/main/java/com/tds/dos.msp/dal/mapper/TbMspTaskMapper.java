package com.tds.dos.msp.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.msp.dal.entity.TbMspTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP Task Mapper
 */
@Mapper
@DS("msp")
public interface TbMspTaskMapper extends BaseMapper<TbMspTask> {
}