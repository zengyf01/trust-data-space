package com.tds.datar.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.datar.dal.entity.TbWorkOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单Mapper
 */
@Mapper
public interface TbWorkOrderMapper extends BaseMapper<TbWorkOrder> {
}