package com.tds.dos.msp.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tds.dos.msp.common.core.ApiResponse;
import com.tds.dos.msp.common.exception.BusinessException;
import com.tds.dos.msp.dal.entity.TbMspUser;
import com.tds.dos.msp.dal.mapper.TbMspUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Auth Service implementation
 */
@Service("mspAuthService")
public class AuthServiceImpl implements IAuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private TbMspUserMapper userMapper;

    @Override
    public ApiResponse<LoginResponse> login(AuthDTO request) {
        TbMspUser user = userMapper.selectOne(
            new LambdaQueryWrapper<TbMspUser>()
                .eq(TbMspUser::getfUsername, request.getUsername())
                .eq(TbMspUser::getfDeleteMark, 0)
        );

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // Simple password check (in production, use proper hashing)
        if (!user.getfPassword().equals(request.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getfEnabled() == null || user.getfEnabled() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // Generate simple token (in production, use JWT)
        String token = UUID.randomUUID().toString().replace("-", "");

        log.info("User logged in: {}", request.getUsername());
        return ApiResponse.success(new LoginResponse(token, user.getfUserId(), user.getfUsername()));
    }

    @Override
    public ApiResponse<Void> logout(String token) {
        log.info("User logged out");
        return ApiResponse.success();
    }

    @Override
    public TbMspUser getCurrentUser(String userId) {
        return userMapper.selectOne(
            new LambdaQueryWrapper<TbMspUser>()
                .eq(TbMspUser::getfUserId, userId)
                .eq(TbMspUser::getfDeleteMark, 0)
        );
    }
}