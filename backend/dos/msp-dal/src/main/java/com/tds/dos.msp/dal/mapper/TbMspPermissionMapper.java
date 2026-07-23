package com.tds.dos.msp.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.msp.dal.entity.TbMspPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP Permission Mapper
 */
@Mapper
@DS("msp")
public interface TbMspPermissionMapper extends BaseMapper<TbMspPermission> {
}