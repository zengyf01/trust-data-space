package com.tds.dos.msp.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.dos.msp.common.exception.BusinessException;
import com.tds.dos.msp.dal.entity.TbMspUser;
import com.tds.dos.msp.dal.mapper.TbMspUserMapper;
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
    private TbMspUserMapper userMapper;

    @Override
    @Transactional
    public TbMspUser createUser(CreateUserRequest request) {
        // Check if username already exists
        if (usernameExists(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        TbMspUser user = new TbMspUser();
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
    public TbMspUser updateUser(String userId, UpdateUserRequest request) {
        TbMspUser user = getUserById(userId);
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
        TbMspUser user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setfDeleteMark(1);
        user.setfUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("User deleted: {}", user.getfUsername());
    }

    @Override
    public TbMspUser getUserById(String userId) {
        return userMapper.selectOne(
            new LambdaQueryWrapper<TbMspUser>()
                .eq(TbMspUser::getfUserId, userId)
                .eq(TbMspUser::getfDeleteMark, 0)
        );
    }

    @Override
    public TbMspUser getUserByUsername(String username) {
        return userMapper.selectOne(
            new LambdaQueryWrapper<TbMspUser>()
                .eq(TbMspUser::getfUsername, username)
                .eq(TbMspUser::getfDeleteMark, 0)
        );
    }

    @Override
    public IPage<TbMspUser> getUserPage(int currentPage, int pageSize, String keyword) {
        Page<TbMspUser> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbMspUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMspUser::getfDeleteMark, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(TbMspUser::getfUsername, keyword)
                .or()
                .like(TbMspUser::getfEmail, keyword)
                .or()
                .like(TbMspUser::getfPhone, keyword)
            );
        }

        wrapper.orderByDesc(TbMspUser::getfCreateTime);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public List<TbMspUser> getAllUsers() {
        return userMapper.selectList(
            new LambdaQueryWrapper<TbMspUser>()
                .eq(TbMspUser::getfDeleteMark, 0)
                .orderByDesc(TbMspUser::getfCreateTime)
        );
    }

    @Override
    @Transactional
    public TbMspUser setUserEnabled(String userId, boolean enabled) {
        TbMspUser user = getUserById(userId);
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
        TbMspUser user = getUserById(userId);
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
            new LambdaQueryWrapper<TbMspUser>()
                .eq(TbMspUser::getfUsername, username)
                .eq(TbMspUser::getfDeleteMark, 0)
        ) > 0;
    }
}