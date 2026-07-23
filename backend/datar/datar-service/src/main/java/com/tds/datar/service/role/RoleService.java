package com.tds.datar.service.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.datar.common.core.PageResult;
import com.tds.datar.dal.entity.TbRole;
import com.tds.datar.dal.mapper.TbRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RoleService {

    @Autowired
    private TbRoleMapper roleMapper;

    public PageResult getRolePage(int currentPage, int pageSize) {
        Page<TbRole> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TbRole::getfCreateTime);
        IPage<TbRole> result = roleMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public TbRole getRoleById(String id) {
        return roleMapper.selectById(id);
    }

    public TbRole createRole(String roleName, String roleType, String roleDesc) {
        TbRole role = new TbRole();
        role.setfId(UUID.randomUUID().toString().replace("-", ""));
        role.setfRoleCode("ROLE_" + System.currentTimeMillis());
        role.setfRoleName(roleName);
        role.setfRoleType(roleType != null ? roleType : "BUSINESS");
        role.setfRoleDesc(roleDesc);
        role.setfIsSystem(0);
        role.setfCreateTime(LocalDateTime.now());
        roleMapper.insert(role);
        return role;
    }

    public TbRole updateRole(String id, String roleName, String roleType, String roleDesc) {
        TbRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        if (role.getfIsSystem() == 1) {
            throw new RuntimeException("系统角色不能修改");
        }
        if (roleName != null) role.setfRoleName(roleName);
        if (roleType != null) role.setfRoleType(roleType);
        if (roleDesc != null) role.setfRoleDesc(roleDesc);
        role.setfUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
        return role;
    }

    public void deleteRole(String id) {
        TbRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        if (role.getfIsSystem() == 1) {
            throw new RuntimeException("系统角色不能删除");
        }
        roleMapper.deleteById(id);
    }

    public List<TbRole> getAllRoles() {
        LambdaQueryWrapper<TbRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TbRole::getfCreateTime);
        return roleMapper.selectList(wrapper);
    }
}
