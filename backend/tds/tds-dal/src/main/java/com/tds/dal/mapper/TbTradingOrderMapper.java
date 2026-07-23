package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbTradingOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易订单Mapper
 */
@Mapper
public interface TbTradingOrderMapper extends BaseMapper<TbTradingOrder> {
}