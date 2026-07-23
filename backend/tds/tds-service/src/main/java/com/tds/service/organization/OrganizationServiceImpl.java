package com.tds.service.organization;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.common.config.MaxKeyClient;
import com.tds.common.util.SM2Util;
import com.tds.common.core.PageResult;
import com.tds.common.enums.OrganizationStatus;
import com.tds.common.exception.BusinessException;
import com.tds.dal.entity.TbDepartment;
import com.tds.dal.entity.TbOrganization;
import com.tds.dal.entity.TbRole;
import com.tds.dal.entity.TbUser;
import com.tds.dal.entity.TbUserRole;
import com.tds.dal.mapper.TbDepartmentMapper;
import com.tds.dal.mapper.TbOrganizationMapper;
import com.tds.dal.mapper.TbRoleMapper;
import com.tds.dal.mapper.TbUserMapper;
import com.tds.dal.mapper.TbUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 机构管理服务实现
 */
@Service
public class OrganizationServiceImpl implements IOrganizationService {

    @Autowired
    private TbOrganizationMapper orgMapper;

    @Autowired
    private TbDepartmentMapper deptMapper;

    @Autowired
    private TbRoleMapper roleMapper;

    @Autowired
    private TbUserMapper userMapper;

    @Autowired
    private TbUserRoleMapper userRoleMapper;

    @Autowired
    private MaxKeyClient maxKeyClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "tds:session:";
    private static final String REFRESH_PREFIX = "tds:refresh:";
    private static final long TOKEN_EXPIRE_SECONDS = 2 * 60 * 60; // 2小时

    // ==================== 机构管理 ====================

    @Override
    public PageResult<TbOrganization> getOrgPage(int currentPage, int pageSize, String orgName, Integer status) {
        Page<TbOrganization> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbOrganization> wrapper = new LambdaQueryWrapper<>();
        if (orgName != null && !orgName.isEmpty()) {
            wrapper.like(TbOrganization::getfOrgName, orgName);
        }
        if (status != null) {
            wrapper.eq(TbOrganization::getfStatus, status);
        }
        wrapper.orderByDesc(TbOrganization::getfCreateTime);
        // 先查总数
        long total = orgMapper.selectCount(wrapper);
        // 再查分页数据
        IPage<TbOrganization> result = orgMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), total, currentPage, pageSize);
    }

    @Override
    public TbOrganization getOrgById(String id) {
        return orgMapper.selectById(id);
    }

    @Override
    public TbOrganization getOrgByCode(String orgCode) {
        LambdaQueryWrapper<TbOrganization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbOrganization::getfOrgCode, orgCode);
        return orgMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public TbOrganization createOrg(OrganizationDTO dto) {
        TbOrganization org = new TbOrganization();
        org.setfId(UUID.randomUUID().toString().replace("-", ""));
        org.setfOrgCode("ORG" + System.currentTimeMillis());
        org.setfOrgName(dto.getOrgName());
        org.setfOrgType(dto.getOrgType());
        org.setfOrgDesc(dto.getOrgDesc());
        org.setfLegalPerson(dto.getLegalPerson());
        org.setfContact(dto.getContact());
        org.setfContactPhone(dto.getContactPhone());
        org.setfContactEmail(dto.getContactEmail());
        org.setfAddress(dto.getAddress());
        org.setfBusinessLicense(dto.getBusinessLicense());
        org.setfStatus(OrganizationStatus.PENDING.getCode());
        org.setfUserCount(0);
        org.setfConnectorCount(0);
        org.setfTenantId(dto.getTenantId());
        org.setfCreateTime(LocalDateTime.now());
        org.setfUpdateTime(LocalDateTime.now());
        org.setfDeleteMark(0);

        orgMapper.insert(org);
        return org;
    }

    @Override
    @Transactional
    public TbOrganization updateOrg(String id, OrganizationDTO dto) {
        TbOrganization org = orgMapper.selectById(id);
        if (org == null) {
            throw new BusinessException("机构不存在");
        }
        org.setfOrgName(dto.getOrgName());
        org.setfOrgType(dto.getOrgType());
        org.setfOrgDesc(dto.getOrgDesc());
        org.setfLegalPerson(dto.getLegalPerson());
        org.setfContact(dto.getContact());
        org.setfContactPhone(dto.getContactPhone());
        org.setfContactEmail(dto.getContactEmail());
        org.setfAddress(dto.getAddress());
        org.setfUpdateTime(LocalDateTime.now());
        orgMapper.updateById(org);
        return org;
    }

    @Override
    @Transactional
    public void deleteOrg(String id) {
        TbOrganization org = orgMapper.selectById(id);
        if (org == null) {
            throw new BusinessException("机构不存在");
        }
        orgMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDeleteOrg(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的机构");
        }
        orgMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional
    public TbOrganization approveOrg(String id, Integer status) {
        TbOrganization org = orgMapper.selectById(id);
        if (org == null) {
            throw new BusinessException("机构不存在");
        }
        org.setfStatus(status);
        org.setfUpdateTime(LocalDateTime.now());
        orgMapper.updateById(org);
        return org;
    }

    @Override
    @Transactional
    public TbOrganization freezeOrg(String id, Integer status) {
        TbOrganization org = orgMapper.selectById(id);
        if (org == null) {
            throw new BusinessException("机构不存在");
        }
        org.setfStatus(status);
        org.setfUpdateTime(LocalDateTime.now());
        orgMapper.updateById(org);
        return org;
    }

    // ==================== 部门管理 ====================

    @Override
    public PageResult<TbDepartment> getDeptPage(int currentPage, int pageSize, String orgId, String parentId) {
        Page<TbDepartment> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbDepartment> wrapper = new LambdaQueryWrapper<>();
        if (orgId != null && !orgId.isEmpty()) {
            wrapper.eq(TbDepartment::getfOrgId, orgId);
        }
        if (parentId != null && !parentId.isEmpty()) {
            wrapper.eq(TbDepartment::getfParentId, parentId);
        }
        wrapper.orderByAsc(TbDepartment::getfSortOrder);
        long total = deptMapper.selectCount(wrapper);
        IPage<TbDepartment> result = deptMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), total, currentPage, pageSize);
    }

    @Override
    public TbDepartment getDeptById(String id) {
        return deptMapper.selectById(id);
    }

    @Override
    public List<TbDepartment> getDeptsByOrg(String orgId) {
        LambdaQueryWrapper<TbDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbDepartment::getfOrgId, orgId)
                .eq(TbDepartment::getfDeleteMark, 0)
                .orderByAsc(TbDepartment::getfSortOrder);
        return deptMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public TbDepartment createDept(DepartmentDTO dto) {
        TbDepartment dept = new TbDepartment();
        dept.setfId(UUID.randomUUID().toString().replace("-", ""));
        dept.setfDeptCode("DEPT" + System.currentTimeMillis());
        dept.setfDeptName(dto.getDeptName());
        dept.setfParentId(dto.getParentId());
        dept.setfOrgId(dto.getOrgId());
        dept.setfDeptLevel(dto.getDeptLevel() != null ? dto.getDeptLevel() : 1);
        dept.setfSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        dept.setfManagerId(dto.getManagerId());
        dept.setfManagerName(dto.getManagerName());
        dept.setfTenantId(dto.getTenantId());
        dept.setfCreateTime(LocalDateTime.now());
        dept.setfUpdateTime(LocalDateTime.now());
        dept.setfDeleteMark(0);

        deptMapper.insert(dept);
        return dept;
    }

    @Override
    @Transactional
    public TbDepartment updateDept(String id, DepartmentDTO dto) {
        TbDepartment dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        dept.setfDeptName(dto.getDeptName());
        dept.setfParentId(dto.getParentId());
        dept.setfSortOrder(dto.getSortOrder());
        dept.setfManagerId(dto.getManagerId());
        dept.setfManagerName(dto.getManagerName());
        dept.setfUpdateTime(LocalDateTime.now());
        deptMapper.updateById(dept);
        return dept;
    }

    @Override
    @Transactional
    public void deleteDept(String id) {
        TbDepartment dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        deptMapper.deleteById(id);
    }

    // ==================== 角色管理 ====================

    @Override
    public PageResult<TbRole> getRolePage(int currentPage, int pageSize, String orgId, String roleType) {
        Page<TbRole> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbRole> wrapper = new LambdaQueryWrapper<>();
        if (orgId != null && !orgId.isEmpty()) {
            wrapper.eq(TbRole::getfOrgId, orgId);
        }
        if (roleType != null && !roleType.isEmpty()) {
            wrapper.eq(TbRole::getfRoleType, roleType);
        }
        wrapper.orderByDesc(TbRole::getfCreateTime);
        long total = roleMapper.selectCount(wrapper);
        IPage<TbRole> result = roleMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), total, currentPage, pageSize);
    }

    @Override
    public TbRole getRoleById(String id) {
        return roleMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbRole createRole(RoleDTO dto) {
        TbRole role = new TbRole();
        role.setfId(UUID.randomUUID().toString().replace("-", ""));
        role.setfRoleCode("ROLE" + System.currentTimeMillis());
        role.setfRoleName(dto.getRoleName());
        role.setfRoleType(dto.getRoleType());
        role.setfOrgId(dto.getOrgId());
        role.setfRoleDesc(dto.getRoleDesc());
        role.setfIsSystem(dto.getIsSystem() != null ? dto.getIsSystem() : 0);
        role.setfTenantId(dto.getTenantId());
        role.setfCreateTime(LocalDateTime.now());
        role.setfUpdateTime(LocalDateTime.now());
        role.setfDeleteMark(0);

        roleMapper.insert(role);
        return role;
    }

    @Override
    @Transactional
    public TbRole updateRole(String id, RoleDTO dto) {
        TbRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        if (role.getfIsSystem() != null && role.getfIsSystem() == 1) {
            throw new BusinessException("系统角色不可修改");
        }
        role.setfRoleName(dto.getRoleName());
        role.setfRoleDesc(dto.getRoleDesc());
        role.setfUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
        return role;
    }

    @Override
    @Transactional
    public void deleteRole(String id) {
        TbRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        if (role.getfIsSystem() != null && role.getfIsSystem() == 1) {
            throw new BusinessException("系统角色不可删除");
        }
        roleMapper.deleteById(id);
    }

    // ==================== 用户管理 ====================

    @Override
    public PageResult<TbUser> getUserPage(int currentPage, int pageSize, String orgId, String deptId, String keyword, Integer status) {
        Page<TbUser> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbUser> wrapper = new LambdaQueryWrapper<>();
        if (orgId != null && !orgId.isEmpty()) {
            wrapper.eq(TbUser::getfOrgId, orgId);
        }
        if (deptId != null && !deptId.isEmpty()) {
            wrapper.eq(TbUser::getfDeptId, deptId);
        }
        if (status != null) {
            wrapper.eq(TbUser::getfStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(TbUser::getfUsername, keyword)
                    .or().like(TbUser::getfRealName, keyword)
                    .or().like(TbUser::getfPhone, keyword));
        }
        wrapper.orderByDesc(TbUser::getfCreateTime);
        long total = userMapper.selectCount(wrapper);
        IPage<TbUser> result = userMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), total, currentPage, pageSize);
    }

    @Override
    public TbUser getUserById(String id) {
        return userMapper.selectById(id);
    }

    @Override
    public TbUser getUserByUsername(String username) {
        LambdaQueryWrapper<TbUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbUser::getfUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public TbUser createUser(UserDTO dto) {
        // 检查用户名唯一性
        if (getUserByUsername(dto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        TbUser user = new TbUser();
        user.setfId(UUID.randomUUID().toString().replace("-", ""));
        user.setfUsername(dto.getUsername());
        user.setfPassword(encryptPassword(dto.getPassword()));
        user.setfRealName(dto.getRealName());
        user.setfNickName(dto.getNickName());
        user.setfEmail(dto.getEmail());
        user.setfPhone(dto.getPhone());
        user.setfAvatar(dto.getAvatar());
        user.setfOrgId(dto.getOrgId());
        user.setfDeptId(dto.getDeptId());
        user.setfUserType(dto.getUserType());
        user.setfStatus(1);
        user.setfTenantId(dto.getTenantId());
        user.setfCreateTime(LocalDateTime.now());
        user.setfUpdateTime(LocalDateTime.now());
        user.setfDeleteMark(0);

        userMapper.insert(user);

        // 更新机构用户数量
        updateOrgUserCount(dto.getOrgId());

        return user;
    }

    @Override
    @Transactional
    public TbUser updateUser(String id, UserDTO dto) {
        TbUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setfRealName(dto.getRealName());
        user.setfNickName(dto.getNickName());
        user.setfEmail(dto.getEmail());
        user.setfPhone(dto.getPhone());
        user.setfAvatar(dto.getAvatar());
        user.setfOrgId(dto.getOrgId());
        user.setfDeptId(dto.getDeptId());
        user.setfUserType(dto.getUserType());
        if (dto.getStatus() != null) {
            user.setfStatus(dto.getStatus());
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setfPassword(encryptPassword(dto.getPassword()));
        }
        user.setfUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        TbUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 使用 deleteById 会自动触发 @TableLogic 软删除
        userMapper.deleteById(id);

        // 更新机构用户数量
        updateOrgUserCount(user.getfOrgId());
    }

    @Override
    @Transactional
    public void assignUserRole(String userId, List<String> roleIds) {
        // 删除原有角色
        LambdaQueryWrapper<TbUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbUserRole::getfUserId, userId);
        userRoleMapper.delete(wrapper);

        // 添加新角色
        for (String roleId : roleIds) {
            TbUserRole userRole = new TbUserRole();
            userRole.setfId(UUID.randomUUID().toString().replace("-", ""));
            userRole.setfUserId(userId);
            userRole.setfRoleId(roleId);
            userRole.setfCreateTime(LocalDateTime.now());
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    public List<TbRole> getUserRoles(String userId) {
        LambdaQueryWrapper<TbUserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(TbUserRole::getfUserId, userId);
        List<TbUserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);

        if (userRoles.isEmpty()) {
            return List.of();
        }

        List<String> roleIds = userRoles.stream().map(TbUserRole::getfRoleId).toList();
        return roleMapper.selectBatchIds(roleIds);
    }

    // ==================== 认证相关 ====================

    @Override
    public Map<String, Object> login(String username, String password) {
        return login(username, password, "TDS");
    }

    @Override
    public Map<String, Object> login(String username, String password, String appId) {
        TbUser user = getUserByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 获取租户ID
        String tenantId = user.getfTenantId();

        // 如果用户没有租户，分配一个默认租户（向后兼容）
        if (tenantId == null) {
            tenantId = getDefaultTenantIdByAppId(appId);
            user.setfTenantId(tenantId);
            userMapper.updateById(user);
        }

        // 根据appId验证租户访问权限
        String expectedTenantId = getTenantIdByAppId(appId);
        if (expectedTenantId != null && !expectedTenantId.equals(tenantId)) {
            throw new BusinessException("无权访问此应用");
        }

        String encryptedPassword = encryptPassword(password);
        String storedPassword = user.getfPassword();

        // 验证密码：支持SM3加密密码和明文密码（向后兼容）
        boolean passwordValid = false;
        if (encryptedPassword.equals(storedPassword)) {
            passwordValid = true;
        } else if (password.equals(storedPassword)) {
            // 明文密码匹配，需要升级为SM3加密
            passwordValid = true;
            // 升级密码加密
            user.setfPassword(encryptedPassword);
            userMapper.updateById(user);
        }

        if (!passwordValid) {
            throw new BusinessException("密码错误");
        }

        if (user.getfStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        // 更新登录信息
        user.setfLastLoginIp("127.0.0.1");
        user.setfLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成Token
        String token = generateToken();
        String refreshToken = generateRefreshToken();

        // 保存到Redis
        String sessionKey = TOKEN_PREFIX + token;
        redisTemplate.opsForHash().put(sessionKey, "userId", user.getfId());
        redisTemplate.opsForHash().put(sessionKey, "username", user.getfUsername());
        redisTemplate.opsForHash().put(sessionKey, "tenantId", tenantId);
        redisTemplate.opsForHash().put(sessionKey, "appId", appId);
        redisTemplate.expire(sessionKey, java.time.Duration.ofSeconds(TOKEN_EXPIRE_SECONDS));

        String refreshKey = REFRESH_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(refreshKey, token);
        redisTemplate.expire(refreshKey, java.time.Duration.ofSeconds(TOKEN_EXPIRE_SECONDS * 2L));

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getfId());
        result.put("username", user.getfUsername());
        result.put("realName", user.getfRealName());
        result.put("orgId", user.getfOrgId());
        result.put("userType", user.getfUserType());
        result.put("tenantId", tenantId);
        result.put("token", token);
        result.put("refreshToken", refreshToken);

        return result;
    }

    private String getDefaultTenantIdByAppId(String appId) {
        if (appId == null) return "TENANT_TDS";
        switch (appId.toUpperCase()) {
            case "DOS": return "TENANT_DOS";
            case "DATAR": return "TENANT_DATAR";
            default: return "TENANT_TDS";
        }
    }

    private String getTenantIdByAppId(String appId) {
        if (appId == null) return null;
        switch (appId.toUpperCase()) {
            case "TDS": return "TENANT_TDS";
            case "DOS": return "TENANT_DOS";
            case "DATAR": return "TENANT_DATAR";
            default: return null;
        }
    }

    @Override
    public Map<String, Object> ssoLogin(String token) {
        // 调用MaxKey服务验证token
        Map<String, Object> tokenInfo = maxKeyClient.introspectToken(token);

        Boolean active = (Boolean) tokenInfo.get("active");
        if (active == null || !active) {
            throw new BusinessException("SSO令牌无效或已过期");
        }

        // 获取用户信息
        Map<String, Object> userInfo = maxKeyClient.getUserInfo(token);
        String openid = (String) userInfo.get("openid");
        String username = (String) userInfo.get("preferred_username");
        if (username == null) {
            username = (String) userInfo.get("name");
        }
        String email = (String) userInfo.get("email");

        // 查询或创建本地用户
        TbUser user = getOrCreateSsoUser(openid, username, email);

        // 生成TDS本地Token
        String localToken = generateToken();
        String refreshToken = generateRefreshToken();

        // 保存到Redis
        String sessionKey = TOKEN_PREFIX + localToken;
        redisTemplate.opsForHash().put(sessionKey, "userId", user.getfId());
        redisTemplate.opsForHash().put(sessionKey, "username", user.getfUsername());
        redisTemplate.opsForHash().put(sessionKey, "openid", openid);
        redisTemplate.expire(sessionKey, java.time.Duration.ofSeconds(TOKEN_EXPIRE_SECONDS));

        String refreshKey = REFRESH_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(refreshKey, localToken);
        redisTemplate.expire(refreshKey, java.time.Duration.ofSeconds(TOKEN_EXPIRE_SECONDS * 2L));

        // 更新登录信息
        user.setfLastLoginIp("127.0.0.1");
        user.setfLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getfId());
        result.put("username", user.getfUsername());
        result.put("realName", user.getfRealName());
        result.put("orgId", user.getfOrgId());
        result.put("token", localToken);
        result.put("refreshToken", refreshToken);
        result.put("ssoType", "MaxKey");
        result.put("openid", openid);

        return result;
    }

    /**
     * 根据SSO用户信息获取或创建本地用户
     */
    private TbUser getOrCreateSsoUser(String openid, String username, String email) {
        // 先根据openid查找
        LambdaQueryWrapper<TbUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbUser::getfUsername, username).eq(TbUser::getfDeleteMark, 0);
        TbUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            // 创建新用户
            user = new TbUser();
            user.setfId(UUID.randomUUID().toString().replace("-", ""));
            user.setfUsername(username);
            user.setfRealName(username);
            user.setfEmail(email);
            user.setfPassword(""); // SSO用户无密码
            user.setfStatus(1);
            user.setfUserType("1"); // 普通用户
            user.setfCreateTime(LocalDateTime.now());
            userMapper.insert(user);
        }

        return user;
    }

    @Override
    public Map<String, Object> ssoLoginByCode(String code, String state) {
        // 用授权码换取访问令牌
        Map<String, Object> tokenResponse = maxKeyClient.exchangeCodeForToken(code);

        String accessToken = (String) tokenResponse.get("accessToken");
        String refreshToken = (String) tokenResponse.get("refreshToken");

        if (accessToken == null || accessToken.isEmpty()) {
            throw new BusinessException("SSO授权失败，无法获取访问令牌");
        }

        // 获取用户信息
        Map<String, Object> userInfo = maxKeyClient.getUserInfo(accessToken);
        String openid = (String) userInfo.get("openid");
        String username = (String) userInfo.get("preferred_username");
        if (username == null) {
            username = (String) userInfo.get("name");
        }
        String email = (String) userInfo.get("email");

        // 查询或创建本地用户
        TbUser user = getOrCreateSsoUser(openid, username, email);

        // 生成TDS本地Token
        String localToken = generateToken();
        String localRefreshToken = generateRefreshToken();

        // 保存到Redis
        String sessionKey = TOKEN_PREFIX + localToken;
        redisTemplate.opsForHash().put(sessionKey, "userId", user.getfId());
        redisTemplate.opsForHash().put(sessionKey, "username", user.getfUsername());
        redisTemplate.opsForHash().put(sessionKey, "openid", openid);
        redisTemplate.opsForHash().put(sessionKey, "ssoAccessToken", accessToken);
        redisTemplate.expire(sessionKey, java.time.Duration.ofSeconds(TOKEN_EXPIRE_SECONDS));

        String refreshKey = REFRESH_PREFIX + localRefreshToken;
        redisTemplate.opsForValue().set(refreshKey, localToken);
        redisTemplate.expire(refreshKey, java.time.Duration.ofSeconds(TOKEN_EXPIRE_SECONDS * 2L));

        // 更新登录信息
        user.setfLastLoginIp("127.0.0.1");
        user.setfLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getfId());
        result.put("username", user.getfUsername());
        result.put("realName", user.getfRealName());
        result.put("orgId", user.getfOrgId());
        result.put("token", localToken);
        result.put("refreshToken", localRefreshToken);
        result.put("ssoType", "MaxKey");
        result.put("openid", openid);
        // 可选：返回SSO访问令牌供前端使用
        result.put("ssoAccessToken", accessToken);

        return result;
    }

    @Override
    public void logout(String userId) {
        // 删除用户所有会话
        // 注意：生产环境建议使用 token -> sessionKey 的映射来精确删除
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        String refreshKey = REFRESH_PREFIX + refreshToken;
        String oldToken = redisTemplate.opsForValue().get(refreshKey);

        if (oldToken == null) {
            throw new BusinessException("RefreshToken已过期");
        }

        // 删除旧Token
        String oldSessionKey = TOKEN_PREFIX + oldToken;
        redisTemplate.delete(oldSessionKey);
        redisTemplate.delete(refreshKey);

        // 生成新Token
        String newToken = generateToken();
        String newRefreshToken = generateRefreshToken();

        // 保存新Token
        String sessionKey = TOKEN_PREFIX + newToken;
        // 获取旧会话中的用户信息
        Map<Object, Object> oldSession = redisTemplate.opsForHash().entries(oldSessionKey);
        if (!oldSession.isEmpty()) {
            redisTemplate.opsForHash().putAll(sessionKey, oldSession);
        }
        redisTemplate.expire(sessionKey, java.time.Duration.ofSeconds(TOKEN_EXPIRE_SECONDS));

        String newRefreshKey = REFRESH_PREFIX + newRefreshToken;
        redisTemplate.opsForValue().set(newRefreshKey, newToken);
        redisTemplate.expire(newRefreshKey, java.time.Duration.ofSeconds(TOKEN_EXPIRE_SECONDS * 2L));

        Map<String, Object> result = new HashMap<>();
        result.put("token", newToken);
        result.put("refreshToken", newRefreshToken);
        return result;
    }

    // ==================== 辅助方法 ====================

    private void updateOrgUserCount(String orgId) {
        if (orgId == null) return;
        TbOrganization org = orgMapper.selectById(orgId);
        if (org != null) {
            LambdaQueryWrapper<TbUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbUser::getfOrgId, orgId)
                    .eq(TbUser::getfDeleteMark, 0);
            long count = userMapper.selectCount(wrapper);
            org.setfUserCount((int) count);
            org.setfUpdateTime(LocalDateTime.now());
            orgMapper.updateById(org);
        }
    }

    private String encryptPassword(String password) {
        try {
            return SM2Util.hash(password);
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    private String generateToken() {
        return "TOKEN_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String generateRefreshToken() {
        return "REFRESH_" + UUID.randomUUID().toString().replace("-", "");
    }
}