package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbDeployInstance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部署实例Mapper
 */
@Mapper
public interface TbDeployInstanceMapper extends BaseMapper<TbDeployInstance> {
}