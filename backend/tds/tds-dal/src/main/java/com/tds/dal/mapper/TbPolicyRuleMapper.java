package com.tds.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tds.dal.entity.TbPolicyRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 策略规则Mapper
 */
@Mapper
public interface TbPolicyRuleMapper extends BaseMapper<TbPolicyRule> {
}