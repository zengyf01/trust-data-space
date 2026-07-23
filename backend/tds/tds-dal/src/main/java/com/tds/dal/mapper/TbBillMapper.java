package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbBill;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单Mapper
 */
@Mapper
public interface TbBillMapper extends BaseMapper<TbBill> {
}