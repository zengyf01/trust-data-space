package com.tds.datar.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.datar.common.core.PageResult;
import com.tds.datar.dal.entity.TbUser;
import com.tds.datar.dal.entity.TbUserRole;
import com.tds.datar.dal.mapper.TbUserMapper;
import com.tds.datar.dal.mapper.TbUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private TbUserMapper userMapper;

    @Autowired
    private TbUserRoleMapper userRoleMapper;

    public PageResult getUserPage(int currentPage, int pageSize, String keyword, Integer status) {
        Page<TbUser> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(TbUser::getfUsername, keyword)
                    .or().like(TbUser::getfRealName, keyword));
        }
        if (status != null) {
            wrapper.eq(TbUser::getfStatus, status);
        }
        IPage<TbUser> result = userMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public TbUser getUserById(String id) {
        return userMapper.selectById(id);
    }

    public TbUser getUserByUsername(String username) {
        LambdaQueryWrapper<TbUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbUser::getfUsername, username);
        return userMapper.selectOne(wrapper);
    }

    public TbUser createUser(String username, String password, String realName, String phone, String email, Integer status) {
        TbUser user = new TbUser();
        user.setfId(UUID.randomUUID().toString().replace("-", ""));
        user.setfUsername(username);
        user.setfPassword(hashPassword(password));
        user.setfRealName(realName);
        user.setfPhone(phone);
        user.setfEmail(email);
        user.setfStatus(status != null ? status : 1);
        user.setfUserType("LOCAL");
        user.setfCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    public TbUser updateUser(String id, String realName, String phone, String email, Integer status) {
        TbUser user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (realName != null) user.setfRealName(realName);
        if (phone != null) user.setfPhone(phone);
        if (email != null) user.setfEmail(email);
        if (status != null) user.setfStatus(status);
        user.setfUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }

    public void deleteUser(String id) {
        userMapper.deleteById(id);
    }

    public void assignRoles(String userId, List<String> roleIds) {
        // 删除现有角色
        LambdaQueryWrapper<TbUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbUserRole::getfUserId, userId);
        userRoleMapper.delete(wrapper);

        // 添加新角色
        for (String roleId : roleIds) {
            TbUserRole ur = new TbUserRole();
            ur.setfId(UUID.randomUUID().toString().replace("-", ""));
            ur.setfUserId(userId);
            ur.setfRoleId(roleId);
            ur.setfCreateTime(LocalDateTime.now());
            userRoleMapper.insert(ur);
        }
    }

    public List<TbUserRole> getUserRoles(String userId) {
        LambdaQueryWrapper<TbUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbUserRole::getfUserId, userId);
        return userRoleMapper.selectList(wrapper);
    }

    public TbUser login(String username, String password) {
        TbUser user = getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getfStatus() == 0) {
            throw new RuntimeException("用户已禁用");
        }
        String hashed = hashPassword(password);
        if (!hashed.equals(user.getfPassword())) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    private String hashPassword(String password) {
        return DigestUtils.md5DigestAsHex(("datar" + password + "salt").getBytes(StandardCharsets.UTF_8));
    }
}
