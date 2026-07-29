package com.tds.dos.dal.msp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.dal.msp.entity.TbAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP Audit Log Mapper
 */
@Mapper
@DS("dos")
public interface TbAuditLogMapper extends BaseMapper<TbAuditLog> {
}
