package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbDepartment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门Mapper
 */
@Mapper
public interface TbDepartmentMapper extends BaseMapper<TbDepartment> {
}