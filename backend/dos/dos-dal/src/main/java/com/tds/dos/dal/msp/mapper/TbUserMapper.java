package com.tds.dos.dal.msp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.dal.msp.entity.TbUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * MSP User Mapper
 */
@Mapper
@DS("dos")
public interface TbUserMapper extends BaseMapper<TbUser> {
}
