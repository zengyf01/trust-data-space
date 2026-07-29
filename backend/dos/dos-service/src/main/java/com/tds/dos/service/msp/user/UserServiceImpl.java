package com.tds.dos.service.msp.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.dos.common.exception.BusinessException;
import com.tds.dos.dal.msp.entity.TbUser;
import com.tds.dos.dal.msp.mapper.TbUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * User Service implementation
 */
@Service
public class UserServiceImpl implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private TbUserMapper userMapper;

    @Override
    @Transactional
    public TbUser createUser(CreateUserRequest request) {
        // Check if username already exists
        if (usernameExists(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        TbUser user = new TbUser();
        user.setfId(UUID.randomUUID().toString().replace("-", ""));
        user.setfUserId(UUID.randomUUID().toString().replace("-", ""));
        user.setfUsername(request.getUsername());
        user.setfPassword(request.getPassword()); // In production, should hash the password
        user.setfEmail(request.getEmail());
        user.setfPhone(request.getPhone());
        user.setfRole(request.getRole());
        user.setfEnabled(request.getEnabled() != null ? request.getEnabled() : 1);
        user.setfStatus("ACTIVE");
        user.setfCreateTime(LocalDateTime.now());
        user.setfUpdateTime(LocalDateTime.now());
        user.setfDeleteMark(0);

        userMapper.insert(user);
        log.info("User created: {}", user.getfUsername());
        return user;
    }

    @Override
    @Transactional
    public TbUser updateUser(String userId, UpdateUserRequest request) {
        TbUser user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (request.getEmail() != null) {
            user.setfEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setfPhone(request.getPhone());
        }
        if (request.getRole() != null) {
            user.setfRole(request.getRole());
        }
        if (request.getEnabled() != null) {
            user.setfEnabled(request.getEnabled());
        }
        if (request.getStatus() != null) {
            user.setfStatus(request.getStatus());
        }
        user.setfUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
        log.info("User updated: {}", user.getfUsername());
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        TbUser user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setfDeleteMark(1);
        user.setfUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("User deleted: {}", user.getfUsername());
    }

    @Override
    public TbUser getUserById(String userId) {
        return userMapper.selectOne(
            new LambdaQueryWrapper<TbUser>()
                .eq(TbUser::getfUserId, userId)
                .eq(TbUser::getfDeleteMark, 0)
        );
    }

    @Override
    public TbUser getUserByUsername(String username) {
        return userMapper.selectOne(
            new LambdaQueryWrapper<TbUser>()
                .eq(TbUser::getfUsername, username)
                .eq(TbUser::getfDeleteMark, 0)
        );
    }

    @Override
    public IPage<TbUser> getUserPage(int currentPage, int pageSize, String keyword) {
        Page<TbUser> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbUser::getfDeleteMark, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(TbUser::getfUsername, keyword)
                .or()
                .like(TbUser::getfEmail, keyword)
                .or()
                .like(TbUser::getfPhone, keyword)
            );
        }

        wrapper.orderByDesc(TbUser::getfCreateTime);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public List<TbUser> getAllUsers() {
        return userMapper.selectList(
            new LambdaQueryWrapper<TbUser>()
                .eq(TbUser::getfDeleteMark, 0)
                .orderByDesc(TbUser::getfCreateTime)
        );
    }

    @Override
    @Transactional
    public TbUser setUserEnabled(String userId, boolean enabled) {
        TbUser user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setfEnabled(enabled ? 1 : 0);
        user.setfUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("User {} enabled={}", user.getfUsername(), enabled);
        return user;
    }

    @Override
    @Transactional
    public void resetPassword(String userId, String newPassword) {
        TbUser user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setfPassword(newPassword); // In production, should hash the password
        user.setfUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("Password reset for user: {}", user.getfUsername());
    }

    @Override
    public boolean usernameExists(String username) {
        return userMapper.selectCount(
            new LambdaQueryWrapper<TbUser>()
                .eq(TbUser::getfUsername, username)
                .eq(TbUser::getfDeleteMark, 0)
        ) > 0;
    }
}
