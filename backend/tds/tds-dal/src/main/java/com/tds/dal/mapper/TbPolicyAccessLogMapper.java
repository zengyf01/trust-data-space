package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbPolicyAccessLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 策略访问记录Mapper
 */
@Mapper
public interface TbPolicyAccessLogMapper extends BaseMapper<TbPolicyAccessLog> {
}