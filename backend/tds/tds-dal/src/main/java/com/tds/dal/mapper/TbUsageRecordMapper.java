package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbUsageRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用量记录Mapper
 */
@Mapper
public interface TbUsageRecordMapper extends BaseMapper<TbUsageRecord> {
}