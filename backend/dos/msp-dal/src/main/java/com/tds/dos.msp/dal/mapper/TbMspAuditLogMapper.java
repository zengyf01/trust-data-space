package com.tds.dos.msp.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.msp.dal.entity.TbMspAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP Audit Log Mapper
 */
@Mapper
@DS("msp")
public interface TbMspAuditLogMapper extends BaseMapper<TbMspAuditLog> {
}