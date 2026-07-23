package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbDataConsumeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据消费日志Mapper
 */
@Mapper
public interface TbDataConsumeLogMapper extends BaseMapper<TbDataConsumeLog> {
}