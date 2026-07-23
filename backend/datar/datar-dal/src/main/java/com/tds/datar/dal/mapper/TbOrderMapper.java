package com.tds.datar.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.datar.dal.entity.TbOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper
 */
@Mapper
public interface TbOrderMapper extends BaseMapper<TbOrder> {
}