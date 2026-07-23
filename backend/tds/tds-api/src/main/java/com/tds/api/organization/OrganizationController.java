package com.tds.api.organization;

import com.tds.common.core.ApiResponse;
import com.tds.service.organization.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * SSO认证成功后的用户信息
 */
class SsoUserInfo {
    public String userId;
    public String username;
    public String realName;
    public String orgId;
    public String token;
    public String refreshToken;
    public String ssoType;
    public String openid;
}

/**
 * 机构管理
 */
@RestController
@RequestMapping("/organization")
public class OrganizationController {

    @Autowired
    private IOrganizationService organizationService;

    // ==================== 机构管理 ====================

    @GetMapping("/page")
    public ApiResponse<?> getOrgPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String orgName,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(organizationService.getOrgPage(currentPage, pageSize, orgName, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getOrgById(@PathVariable String id) {
        return ApiResponse.success(organizationService.getOrgById(id));
    }

    @GetMapping("/code/{orgCode}")
    public ApiResponse<?> getOrgByCode(@PathVariable String orgCode) {
        return ApiResponse.success(organizationService.getOrgByCode(orgCode));
    }

    @PostMapping
    public ApiResponse<?> createOrg(@RequestBody OrganizationDTO dto) {
        return ApiResponse.success(organizationService.createOrg(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateOrg(@PathVariable String id, @RequestBody OrganizationDTO dto) {
        return ApiResponse.success(organizationService.updateOrg(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteOrg(@PathVariable String id) {
        organizationService.deleteOrg(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/batch")
    public ApiResponse<?> batchDeleteOrg(@RequestBody List<String> ids) {
        organizationService.batchDeleteOrg(ids);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<?> approveOrg(@PathVariable String id, @RequestParam Integer status) {
        return ApiResponse.success(organizationService.approveOrg(id, status));
    }

    @PostMapping("/{id}/freeze")
    public ApiResponse<?> freezeOrg(@PathVariable String id, @RequestParam Integer status) {
        return ApiResponse.success(organizationService.freezeOrg(id, status));
    }

    // ==================== 部门管理 ====================

    @GetMapping("/dept/page")
    public ApiResponse<?> getDeptPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String orgId,
            @RequestParam(required = false) String parentId) {
        return ApiResponse.success(organizationService.getDeptPage(currentPage, pageSize, orgId, parentId));
    }

    @GetMapping("/dept/{id}")
    public ApiResponse<?> getDeptById(@PathVariable String id) {
        return ApiResponse.success(organizationService.getDeptById(id));
    }

    @GetMapping("/dept/org/{orgId}")
    public ApiResponse<?> getDeptsByOrg(@PathVariable String orgId) {
        return ApiResponse.success(organizationService.getDeptsByOrg(orgId));
    }

    @PostMapping("/dept")
    public ApiResponse<?> createDept(@RequestBody DepartmentDTO dto) {
        return ApiResponse.success(organizationService.createDept(dto));
    }

    @PutMapping("/dept/{id}")
    public ApiResponse<?> updateDept(@PathVariable String id, @RequestBody DepartmentDTO dto) {
        return ApiResponse.success(organizationService.updateDept(id, dto));
    }

    @DeleteMapping("/dept/{id}")
    public ApiResponse<?> deleteDept(@PathVariable String id) {
        organizationService.deleteDept(id);
        return ApiResponse.success(null);
    }

    // ==================== 角色管理 ====================

    @GetMapping("/role/page")
    public ApiResponse<?> getRolePage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String orgId,
            @RequestParam(required = false) String roleType) {
        return ApiResponse.success(organizationService.getRolePage(currentPage, pageSize, orgId, roleType));
    }

    @GetMapping("/role/{id}")
    public ApiResponse<?> getRoleById(@PathVariable String id) {
        return ApiResponse.success(organizationService.getRoleById(id));
    }

    @PostMapping("/role")
    public ApiResponse<?> createRole(@RequestBody RoleDTO dto) {
        return ApiResponse.success(organizationService.createRole(dto));
    }

    @PutMapping("/role/{id}")
    public ApiResponse<?> updateRole(@PathVariable String id, @RequestBody RoleDTO dto) {
        return ApiResponse.success(organizationService.updateRole(id, dto));
    }

    @DeleteMapping("/role/{id}")
    public ApiResponse<?> deleteRole(@PathVariable String id) {
        organizationService.deleteRole(id);
        return ApiResponse.success(null);
    }

    // ==================== 用户管理 ====================

    @GetMapping("/user/page")
    public ApiResponse<?> getUserPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String orgId,
            @RequestParam(required = false) String deptId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(organizationService.getUserPage(currentPage, pageSize, orgId, deptId, keyword, status));
    }

    @GetMapping("/user/{id}")
    public ApiResponse<?> getUserById(@PathVariable String id) {
        return ApiResponse.success(organizationService.getUserById(id));
    }

    @GetMapping("/user/username/{username}")
    public ApiResponse<?> getUserByUsername(@PathVariable String username) {
        return ApiResponse.success(organizationService.getUserByUsername(username));
    }

    @PostMapping("/user")
    public ApiResponse<?> createUser(@RequestBody UserDTO dto) {
        return ApiResponse.success(organizationService.createUser(dto));
    }

    @PutMapping("/user/{id}")
    public ApiResponse<?> updateUser(@PathVariable String id, @RequestBody UserDTO dto) {
        return ApiResponse.success(organizationService.updateUser(id, dto));
    }

    @DeleteMapping("/user/{id}")
    public ApiResponse<?> deleteUser(@PathVariable String id) {
        organizationService.deleteUser(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/user/{userId}/role")
    public ApiResponse<?> assignUserRole(@PathVariable String userId, @RequestBody List<String> roleIds) {
        organizationService.assignUserRole(userId, roleIds);
        return ApiResponse.success(null);
    }

    @GetMapping("/user/{userId}/roles")
    public ApiResponse<?> getUserRoles(@PathVariable String userId) {
        return ApiResponse.success(organizationService.getUserRoles(userId));
    }

    // ==================== 认证相关 ====================

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestParam String username, @RequestParam String password) {
        return ApiResponse.success(organizationService.login(username, password));
    }

    /**
     * 统一用户中心登录接口 - 支持多租户
     */
    @PostMapping("/uc/login")
    public ApiResponse<?> ucLogin(@RequestParam String username, @RequestParam String password, @RequestParam String appId) {
        return ApiResponse.success(organizationService.login(username, password, appId));
    }

    @PostMapping("/sso/login")
    public ApiResponse<?> ssoLogin(@RequestParam String token) {
        return ApiResponse.success(organizationService.ssoLogin(token));
    }

    /**
     * SSO授权回调 - 用授权码换取令牌
     * 前端从MaxKey授权页面重定向回来时携带code参数
     */
    @GetMapping("/sso/callback")
    public ApiResponse<?> ssoCallback(@RequestParam String code, @RequestParam(required = false) String state) {
        // 调用SSO登录逻辑，code会被用于换取access_token
        // 这里简化处理，实际需要先用code换token再验证
        Map<String, Object> result = organizationService.ssoLoginByCode(code, state);
        return ApiResponse.success(result);
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestParam String userId) {
        organizationService.logout(userId);
        return ApiResponse.success(null);
    }

    @PostMapping("/refresh")
    public ApiResponse<?> refreshToken(@RequestParam String refreshToken) {
        return ApiResponse.success(organizationService.refreshToken(refreshToken));
    }
}