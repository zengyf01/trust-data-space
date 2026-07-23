package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbNotificationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知发送记录Mapper
 */
@Mapper
public interface TbNotificationLogMapper extends BaseMapper<TbNotificationLog> {
}