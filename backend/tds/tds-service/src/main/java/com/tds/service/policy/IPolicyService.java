package com.tds.service.policy;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dal.entity.TbPolicyRule;
import com.tds.dal.entity.TbPolicyBinding;
import com.tds.dal.entity.TbPolicyAccessLog;
import com.tds.dal.entity.TbPolicyExecLog;
import java.util.Map;

/**
 * 策略服务接口
 */
public interface IPolicyService {

    // ==================== 策略规则管理 ====================

    /**
     * 分页查询策略规则
     */
    IPage<TbPolicyRule> getPolicyPage(int currentPage, int pageSize, String policyName, String policyType);

    /**
     * 获取策略详情
     */
    TbPolicyRule getPolicyById(String id);

    /**
     * 创建策略规则
     */
    TbPolicyRule createPolicy(PolicyDTO dto);

    /**
     * 更新策略规则
     */
    TbPolicyRule updatePolicy(String id, PolicyDTO dto);

    /**
     * 删除策略规则
     */
    void deletePolicy(String id);

    /**
     * 启用/禁用策略
     */
    TbPolicyRule togglePolicyStatus(String id, String status);

    // ==================== 策略绑定管理 ====================

    /**
     * 绑定策略到资源
     */
    TbPolicyBinding bindPolicy(PolicyDTO dto);

    /**
     * 解绑策略
     */
    void unbindPolicy(String bindingId);

    /**
     * 查询资源绑定的策略
     */
    IPage<TbPolicyBinding> getResourceBindings(String resourceType, String resourceId);

    // ==================== 策略访问控制 ====================

    /**
     * 检查访问权限
     */
    Map<String, Object> checkAccess(PolicyDTO dto);

    /**
     * 分页查询访问记录
     */
    IPage<TbPolicyAccessLog> getAccessLogPage(int currentPage, int pageSize, String policyId, String visitorId);

    /**
     * 分页查询执行日志
     */
    IPage<TbPolicyExecLog> getExecLogPage(int currentPage, int pageSize, String policyId);
}