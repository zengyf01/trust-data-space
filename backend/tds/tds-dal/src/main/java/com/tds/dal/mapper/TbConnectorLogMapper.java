package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbConnectorLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 连接器操作日志Mapper
 */
@Mapper
public interface TbConnectorLogMapper extends BaseMapper<TbConnectorLog> {
}