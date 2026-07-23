package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbPolicyExecLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 策略执行日志Mapper
 */
@Mapper
public interface TbPolicyExecLogMapper extends BaseMapper<TbPolicyExecLog> {
}