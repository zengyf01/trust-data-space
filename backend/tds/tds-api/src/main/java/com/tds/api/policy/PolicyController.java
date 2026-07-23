package com.tds.api.policy;

import com.tds.common.core.ApiResponse;
import com.tds.dal.entity.TbPolicyRule;
import com.tds.dal.entity.TbPolicyBinding;
import com.tds.dal.entity.TbPolicyAccessLog;
import com.tds.dal.entity.TbPolicyExecLog;
import com.tds.service.policy.IPolicyService;
import com.tds.service.policy.PolicyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 策略管理
 */
@RestController
@RequestMapping("/policy")
public class PolicyController {

    @Autowired
    private IPolicyService policyService;

    // ==================== 策略规则管理 ====================

    @GetMapping("/page")
    public ApiResponse<?> getPolicyPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String policyName,
            @RequestParam(required = false) String policyType) {
        return ApiResponse.success(policyService.getPolicyPage(currentPage, pageSize, policyName, policyType));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getPolicyById(@PathVariable String id) {
        return ApiResponse.success(policyService.getPolicyById(id));
    }

    @PostMapping
    public ApiResponse<?> createPolicy(@RequestBody PolicyDTO dto) {
        return ApiResponse.success(policyService.createPolicy(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updatePolicy(@PathVariable String id, @RequestBody PolicyDTO dto) {
        return ApiResponse.success(policyService.updatePolicy(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deletePolicy(@PathVariable String id) {
        policyService.deletePolicy(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/toggle")
    public ApiResponse<?> togglePolicyStatus(
            @PathVariable String id,
            @RequestParam String status) {
        return ApiResponse.success(policyService.togglePolicyStatus(id, status));
    }

    // ==================== 策略绑定管理 ====================

    @PostMapping("/bind")
    public ApiResponse<?> bindPolicy(@RequestBody PolicyDTO dto) {
        return ApiResponse.success(policyService.bindPolicy(dto));
    }

    @DeleteMapping("/bind/{bindingId}")
    public ApiResponse<?> unbindPolicy(@PathVariable String bindingId) {
        policyService.unbindPolicy(bindingId);
        return ApiResponse.success(null);
    }

    @GetMapping("/bind/resource")
    public ApiResponse<?> getResourceBindings(
            @RequestParam String resourceType,
            @RequestParam String resourceId) {
        return ApiResponse.success(policyService.getResourceBindings(resourceType, resourceId));
    }

    // ==================== 策略访问控制 ====================

    @PostMapping("/check")
    public ApiResponse<?> checkAccess(@RequestBody PolicyDTO dto) {
        return ApiResponse.success(policyService.checkAccess(dto));
    }

    @GetMapping("/accessLog/page")
    public ApiResponse<?> getAccessLogPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String policyId,
            @RequestParam(required = false) String visitorId) {
        return ApiResponse.success(policyService.getAccessLogPage(currentPage, pageSize, policyId, visitorId));
    }

    @GetMapping("/execLog/page")
    public ApiResponse<?> getExecLogPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String policyId) {
        return ApiResponse.success(policyService.getExecLogPage(currentPage, pageSize, policyId));
    }
}