package com.tds.service.policy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.common.exception.BusinessException;
import com.tds.dal.entity.TbPolicyRule;
import com.tds.dal.entity.TbPolicyBinding;
import com.tds.dal.entity.TbPolicyAccessLog;
import com.tds.dal.entity.TbPolicyExecLog;
import com.tds.dal.mapper.TbPolicyRuleMapper;
import com.tds.dal.mapper.TbPolicyBindingMapper;
import com.tds.dal.mapper.TbPolicyAccessLogMapper;
import com.tds.dal.mapper.TbPolicyExecLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 策略服务实现
 */
@Service
public class PolicyServiceImpl implements IPolicyService {

    @Autowired
    private TbPolicyRuleMapper policyRuleMapper;

    @Autowired
    private TbPolicyBindingMapper policyBindingMapper;

    @Autowired
    private TbPolicyAccessLogMapper accessLogMapper;

    @Autowired
    private TbPolicyExecLogMapper execLogMapper;

    // ==================== 策略规则管理 ====================

    @Override
    public IPage<TbPolicyRule> getPolicyPage(int currentPage, int pageSize, String policyName, String policyType) {
        Page<TbPolicyRule> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbPolicyRule> wrapper = new LambdaQueryWrapper<>();
        if (policyName != null && !policyName.isEmpty()) {
            wrapper.like(TbPolicyRule::getfPolicyName, policyName);
        }
        if (policyType != null && !policyType.isEmpty()) {
            wrapper.eq(TbPolicyRule::getfPolicyType, policyType);
        }
        wrapper.orderByDesc(TbPolicyRule::getfCreateTime);
        return policyRuleMapper.selectPage(page, wrapper);
    }

    @Override
    public TbPolicyRule getPolicyById(String id) {
        return policyRuleMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbPolicyRule createPolicy(PolicyDTO dto) {
        TbPolicyRule policy = new TbPolicyRule();
        policy.setfId(UUID.randomUUID().toString().replace("-", ""));
        policy.setfPolicyCode("POL" + System.currentTimeMillis());
        policy.setfPolicyName(dto.getPolicyName());
        policy.setfPolicyType(dto.getPolicyType());
        policy.setfPolicyContent(dto.getPolicyContent());
        policy.setfPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        policy.setfStatus("ENABLED");
        policy.setfTenantId(dto.getTenantId());
        policy.setfCreateTime(LocalDateTime.now());
        policy.setfUpdateTime(LocalDateTime.now());
        policy.setfDeleteMark(0);

        policyRuleMapper.insert(policy);
        return policy;
    }

    @Override
    @Transactional
    public TbPolicyRule updatePolicy(String id, PolicyDTO dto) {
        TbPolicyRule policy = policyRuleMapper.selectById(id);
        if (policy == null) {
            throw new BusinessException("策略不存在");
        }
        policy.setfPolicyName(dto.getPolicyName());
        policy.setfPolicyType(dto.getPolicyType());
        policy.setfPolicyContent(dto.getPolicyContent());
        if (dto.getPriority() != null) {
            policy.setfPriority(dto.getPriority());
        }
        policy.setfUpdateTime(LocalDateTime.now());
        policyRuleMapper.updateById(policy);
        return policy;
    }

    @Override
    @Transactional
    public void deletePolicy(String id) {
        TbPolicyRule policy = policyRuleMapper.selectById(id);
        if (policy == null) {
            throw new BusinessException("策略不存在");
        }
        policy.setfDeleteMark(1);
        policy.setfUpdateTime(LocalDateTime.now());
        policyRuleMapper.updateById(policy);
    }

    @Override
    @Transactional
    public TbPolicyRule togglePolicyStatus(String id, String status) {
        TbPolicyRule policy = policyRuleMapper.selectById(id);
        if (policy == null) {
            throw new BusinessException("策略不存在");
        }
        policy.setfStatus(status);
        policy.setfUpdateTime(LocalDateTime.now());
        policyRuleMapper.updateById(policy);
        return policy;
    }

    // ==================== 策略绑定管理 ====================

    @Override
    @Transactional
    public TbPolicyBinding bindPolicy(PolicyDTO dto) {
        TbPolicyBinding binding = new TbPolicyBinding();
        binding.setfId(UUID.randomUUID().toString().replace("-", ""));
        binding.setfPolicyId(dto.getPolicyId());
        binding.setfResourceType(dto.getResourceType());
        binding.setfResourceId(dto.getResourceId());
        binding.setfTenantId(dto.getTenantId());
        binding.setfCreateTime(LocalDateTime.now());
        binding.setfDeleteMark(0);

        policyBindingMapper.insert(binding);
        return binding;
    }

    @Override
    @Transactional
    public void unbindPolicy(String bindingId) {
        TbPolicyBinding binding = policyBindingMapper.selectById(bindingId);
        if (binding == null) {
            throw new BusinessException("绑定记录不存在");
        }
        binding.setfDeleteMark(1);
        policyBindingMapper.updateById(binding);
    }

    @Override
    public IPage<TbPolicyBinding> getResourceBindings(String resourceType, String resourceId) {
        Page<TbPolicyBinding> page = new Page<>(1, 100);
        LambdaQueryWrapper<TbPolicyBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbPolicyBinding::getfResourceType, resourceType);
        wrapper.eq(TbPolicyBinding::getfResourceId, resourceId);
        return policyBindingMapper.selectPage(page, wrapper);
    }

    // ==================== 策略访问控制 ====================

    @Override
    @Transactional
    public Map<String, Object> checkAccess(PolicyDTO dto) {
        long startTime = System.currentTimeMillis();

        Map<String, Object> result = new HashMap<>();
        result.put("allowed", true);
        result.put("policyId", dto.getPolicyId());
        result.put("visitorId", dto.getVisitorId());

        // 模拟策略检查
        TbPolicyRule policy = policyRuleMapper.selectById(dto.getPolicyId());
        if (policy != null && "DISABLED".equals(policy.getfStatus())) {
            result.put("allowed", false);
            result.put("denyReason", "策略已禁用");
        }

        // 记录访问日志
        TbPolicyAccessLog accessLog = new TbPolicyAccessLog();
        accessLog.setfId(UUID.randomUUID().toString().replace("-", ""));
        accessLog.setfPolicyId(dto.getPolicyId());
        accessLog.setfResourceType(dto.getResourceType());
        accessLog.setfResourceId(dto.getResourceId());
        accessLog.setfVisitorId(dto.getVisitorId());
        accessLog.setfVisitorTenantId(dto.getVisitorTenantId());
        accessLog.setfAccessResult((Boolean) result.get("allowed") ? "ALLOW" : "DENY");
        accessLog.setfRequestParams(dto.getRequestParams());
        accessLog.setfTenantId(dto.getTenantId());
        accessLog.setfAccessTime(LocalDateTime.now());
        accessLogMapper.insert(accessLog);

        // 记录执行日志
        long duration = System.currentTimeMillis() - startTime;
        TbPolicyExecLog execLog = new TbPolicyExecLog();
        execLog.setfId(UUID.randomUUID().toString().replace("-", ""));
        execLog.setfPolicyId(dto.getPolicyId());
        execLog.setfExecType("CHECK");
        execLog.setfExecResult("SUCCESS");
        execLog.setfDuration(duration);
        execLog.setfTenantId(dto.getTenantId());
        execLog.setfCreateTime(LocalDateTime.now());
        execLogMapper.insert(execLog);

        return result;
    }

    @Override
    public IPage<TbPolicyAccessLog> getAccessLogPage(int currentPage, int pageSize, String policyId, String visitorId) {
        Page<TbPolicyAccessLog> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbPolicyAccessLog> wrapper = new LambdaQueryWrapper<>();
        if (policyId != null && !policyId.isEmpty()) {
            wrapper.eq(TbPolicyAccessLog::getfPolicyId, policyId);
        }
        if (visitorId != null && !visitorId.isEmpty()) {
            wrapper.eq(TbPolicyAccessLog::getfVisitorId, visitorId);
        }
        wrapper.orderByDesc(TbPolicyAccessLog::getfAccessTime);
        return accessLogMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<TbPolicyExecLog> getExecLogPage(int currentPage, int pageSize, String policyId) {
        Page<TbPolicyExecLog> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbPolicyExecLog> wrapper = new LambdaQueryWrapper<>();
        if (policyId != null && !policyId.isEmpty()) {
            wrapper.eq(TbPolicyExecLog::getfPolicyId, policyId);
        }
        wrapper.orderByDesc(TbPolicyExecLog::getfCreateTime);
        return execLogMapper.selectPage(page, wrapper);
    }
}