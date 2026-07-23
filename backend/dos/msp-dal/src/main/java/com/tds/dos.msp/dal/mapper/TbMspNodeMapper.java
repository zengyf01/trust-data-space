package com.tds.dos.msp.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.msp.dal.entity.TbMspNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP Node Mapper
 */
@Mapper
@DS("msp")
public interface TbMspNodeMapper extends BaseMapper<TbMspNode> {
}