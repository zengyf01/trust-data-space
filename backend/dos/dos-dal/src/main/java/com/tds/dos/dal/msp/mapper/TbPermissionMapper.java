package com.tds.dos.dal.msp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.dal.msp.entity.TbPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP Permission Mapper
 */
@Mapper
@DS("dos")
public interface TbPermissionMapper extends BaseMapper<TbPermission> {
}
