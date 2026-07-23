package com.tds.datar.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.datar.dal.entity.TbDataProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据产品Mapper
 */
@Mapper
public interface TbDataProductMapper extends BaseMapper<TbDataProduct> {
}