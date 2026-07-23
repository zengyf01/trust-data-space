package com.tds.dos.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dos.dal.entity.TbWorkOrderHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单历史Mapper
 */
@Mapper
public interface TbWorkOrderHistoryMapper extends BaseMapper<TbWorkOrderHistory> {
}