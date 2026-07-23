package com.tds.api.deploy;

import com.tds.common.core.ApiResponse;
import com.tds.service.deploy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 分布式部署管理
 */
@RestController
@RequestMapping("/deploy")
public class DeployController {

    @Autowired
    private IDeployService deployService;

    // ==================== 部署节点管理 ====================

    @GetMapping("/node/page")
    public ApiResponse<?> getNodePage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String nodeType,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(deployService.getNodePage(currentPage, pageSize, nodeType, status));
    }

    @GetMapping("/node/{id}")
    public ApiResponse<?> getNodeById(@PathVariable String id) {
        return ApiResponse.success(deployService.getNodeById(id));
    }

    @PostMapping("/node")
    public ApiResponse<?> createNode(@RequestBody DeployNodeDTO dto) {
        return ApiResponse.success(deployService.createNode(dto));
    }

    @PutMapping("/node/{id}")
    public ApiResponse<?> updateNode(@PathVariable String id, @RequestBody DeployNodeDTO dto) {
        return ApiResponse.success(deployService.updateNode(id, dto));
    }

    @DeleteMapping("/node/{id}")
    public ApiResponse<?> deleteNode(@PathVariable String id) {
        deployService.deleteNode(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/node/heartbeat")
    public ApiResponse<?> heartbeat(@RequestParam String nodeCode) {
        return ApiResponse.success(deployService.heartbeat(nodeCode));
    }

    @GetMapping("/node/online")
    public ApiResponse<?> getOnlineNodes() {
        return ApiResponse.success(deployService.getOnlineNodes());
    }

    // ==================== 部署实例管理 ====================

    @GetMapping("/instance/page")
    public ApiResponse<?> getInstancePage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String nodeId,
            @RequestParam(required = false) String serviceType) {
        return ApiResponse.success(deployService.getInstancePage(currentPage, pageSize, nodeId, serviceType));
    }

    @GetMapping("/instance/{id}")
    public ApiResponse<?> getInstanceById(@PathVariable String id) {
        return ApiResponse.success(deployService.getInstanceById(id));
    }

    @PostMapping("/instance")
    public ApiResponse<?> createInstance(@RequestBody DeployInstanceDTO dto) {
        return ApiResponse.success(deployService.createInstance(dto));
    }

    @PutMapping("/instance/{id}")
    public ApiResponse<?> updateInstance(@PathVariable String id, @RequestBody DeployInstanceDTO dto) {
        return ApiResponse.success(deployService.updateInstance(id, dto));
    }

    @DeleteMapping("/instance/{id}")
    public ApiResponse<?> deleteInstance(@PathVariable String id) {
        deployService.deleteInstance(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/instance/{id}/start")
    public ApiResponse<?> startInstance(@PathVariable String id) {
        return ApiResponse.success(deployService.startInstance(id));
    }

    @PostMapping("/instance/{id}/stop")
    public ApiResponse<?> stopInstance(@PathVariable String id) {
        return ApiResponse.success(deployService.stopInstance(id));
    }

    @PostMapping("/instance/{id}/restart")
    public ApiResponse<?> restartInstance(@PathVariable String id) {
        return ApiResponse.success(deployService.restartInstance(id));
    }

    // ==================== 本地账户管理 ====================

    @GetMapping("/account/page")
    public ApiResponse<?> getAccountPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String orgId,
            @RequestParam(required = false) String accountType) {
        return ApiResponse.success(deployService.getAccountPage(currentPage, pageSize, orgId, accountType));
    }

    @GetMapping("/account/{id}")
    public ApiResponse<?> getAccountById(@PathVariable String id) {
        return ApiResponse.success(deployService.getAccountById(id));
    }

    @PostMapping("/account")
    public ApiResponse<?> createAccount(@RequestBody LocalAccountDTO dto) {
        return ApiResponse.success(deployService.createAccount(dto));
    }

    @PutMapping("/account/{id}")
    public ApiResponse<?> updateAccount(@PathVariable String id, @RequestBody LocalAccountDTO dto) {
        return ApiResponse.success(deployService.updateAccount(id, dto));
    }

    @DeleteMapping("/account/{id}")
    public ApiResponse<?> deleteAccount(@PathVariable String id) {
        deployService.deleteAccount(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/account/auth")
    public ApiResponse<?> localAuth(@RequestParam String accountCode, @RequestParam String credential) {
        return ApiResponse.success(deployService.localAuth(accountCode, credential));
    }

    // ==================== 部署模式切换 ====================

    @PostMapping("/mode/switch")
    public ApiResponse<?> switchDeployMode(
            @RequestParam String mode,
            @RequestParam(required = false) String nodeId) {
        return ApiResponse.success(deployService.switchDeployMode(mode, nodeId));
    }

    @PostMapping("/offline/operation")
    public ApiResponse<?> offlineOperation(
            @RequestParam String operation,
            @RequestBody Map<String, Object> params) {
        return ApiResponse.success(deployService.offlineOperation(operation, params));
    }
}