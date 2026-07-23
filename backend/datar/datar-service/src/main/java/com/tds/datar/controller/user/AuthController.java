package com.tds.datar.controller.user;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.dal.entity.TbUser;
import com.tds.datar.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestParam String username, @RequestParam String password) {
        try {
            TbUser user = userService.login(username, password);
            user.setfPassword(null);

            // 生成简单的token
            String token = UUID.randomUUID().toString().replace("-", "");

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", user.getfId());
            data.put("username", user.getfUsername());
            data.put("realName", user.getfRealName());
            data.put("userType", user.getfUserType());

            return ApiResponse.success(data);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestParam String userId) {
        return ApiResponse.success(null);
    }
}
