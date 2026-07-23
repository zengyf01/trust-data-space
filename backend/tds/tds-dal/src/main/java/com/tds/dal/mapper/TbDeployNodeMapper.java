package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbDeployNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部署节点Mapper
 */
@Mapper
public interface TbDeployNodeMapper extends BaseMapper<TbDeployNode> {
}