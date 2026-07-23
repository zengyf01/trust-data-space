package com.tds.service.organization;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.common.core.PageResult;
import com.tds.dal.entity.TbDepartment;
import com.tds.dal.entity.TbOrganization;
import com.tds.dal.entity.TbRole;
import com.tds.dal.entity.TbUser;

import java.util.List;
import java.util.Map;

/**
 * 机构管理服务接口
 */
public interface IOrganizationService {

    // ==================== 机构管理 ====================

    /**
     * 分页查询机构
     */
    PageResult<TbOrganization> getOrgPage(int currentPage, int pageSize, String orgName, Integer status);

    /**
     * 获取机构详情
     */
    TbOrganization getOrgById(String id);

    /**
     * 根据编码获取机构
     */
    TbOrganization getOrgByCode(String orgCode);

    /**
     * 创建机构
     */
    TbOrganization createOrg(OrganizationDTO dto);

    /**
     * 更新机构
     */
    TbOrganization updateOrg(String id, OrganizationDTO dto);

    /**
     * 删除机构
     */
    void deleteOrg(String id);

    /**
     * 批量删除机构
     */
    void batchDeleteOrg(List<String> ids);

    /**
     * 审核机构
     */
    TbOrganization approveOrg(String id, Integer status);

    /**
     * 冻结/解冻机构
     */
    TbOrganization freezeOrg(String id, Integer status);

    // ==================== 部门管理 ====================

    /**
     * 分页查询部门
     */
    PageResult<TbDepartment> getDeptPage(int currentPage, int pageSize, String orgId, String parentId);

    /**
     * 获取部门详情
     */
    TbDepartment getDeptById(String id);

    /**
     * 获取机构下所有部门
     */
    List<TbDepartment> getDeptsByOrg(String orgId);

    /**
     * 创建部门
     */
    TbDepartment createDept(DepartmentDTO dto);

    /**
     * 更新部门
     */
    TbDepartment updateDept(String id, DepartmentDTO dto);

    /**
     * 删除部门
     */
    void deleteDept(String id);

    // ==================== 角色管理 ====================

    /**
     * 分页查询角色
     */
    PageResult<TbRole> getRolePage(int currentPage, int pageSize, String orgId, String roleType);

    /**
     * 获取角色详情
     */
    TbRole getRoleById(String id);

    /**
     * 创建角色
     */
    TbRole createRole(RoleDTO dto);

    /**
     * 更新角色
     */
    TbRole updateRole(String id, RoleDTO dto);

    /**
     * 删除角色
     */
    void deleteRole(String id);

    // ==================== 用户管理 ====================

    /**
     * 分页查询用户
     */
    PageResult<TbUser> getUserPage(int currentPage, int pageSize, String orgId, String deptId, String keyword, Integer status);

    /**
     * 获取用户详情
     */
    TbUser getUserById(String id);

    /**
     * 根据用户名获取用户
     */
    TbUser getUserByUsername(String username);

    /**
     * 创建用户
     */
    TbUser createUser(UserDTO dto);

    /**
     * 更新用户
     */
    TbUser updateUser(String id, UserDTO dto);

    /**
     * 删除用户
     */
    void deleteUser(String id);

    /**
     * 分配用户角色
     */
    void assignUserRole(String userId, List<String> roleIds);

    /**
     * 获取用户角色列表
     */
    List<TbRole> getUserRoles(String userId);

    // ==================== 认证相关 ====================

    /**
     * 用户登录
     */
    Map<String, Object> login(String username, String password);

    /**
     * 统一用户中心登录 - 支持多租户
     * @param username 用户名
     * @param password 密码
     * @param appId 应用ID (TDS/DOS/DATAR)
     */
    Map<String, Object> login(String username, String password, String appId);

    /**
     * MaxKey SSO登录 - 验证token
     */
    Map<String, Object> ssoLogin(String token);

    /**
     * MaxKey SSO登录 - 使用授权码
     */
    Map<String, Object> ssoLoginByCode(String code, String state);

    /**
     * 用户登出
     */
    void logout(String userId);

    /**
     * 刷新Token
     */
    Map<String, Object> refreshToken(String refreshToken);
}