package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbOrderHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单历史Mapper
 */
@Mapper
public interface TbOrderHistoryMapper extends BaseMapper<TbOrderHistory> {
}