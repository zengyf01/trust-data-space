package com.tds.datar.controller.user;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.dal.entity.TbUser;
import com.tds.datar.dal.entity.TbUserRole;
import com.tds.datar.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/page")
    public ApiResponse<?> getUserPage(
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(userService.getUserPage(currentPage, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getUserById(@PathVariable String id) {
        TbUser user = userService.getUserById(id);
        if (user != null) {
            user.setfPassword(null);
        }
        return ApiResponse.success(user);
    }

    @PostMapping
    public ApiResponse<?> createUser(@RequestBody Map<String, Object> params) {
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        String realName = (String) params.get("realName");
        String phone = (String) params.get("phone");
        String email = (String) params.get("email");
        Integer status = params.get("status") != null ? ((Number) params.get("status")).intValue() : 1;

        TbUser user = userService.createUser(username, password, realName, phone, email, status);
        user.setfPassword(null);
        return ApiResponse.success(user);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateUser(@PathVariable String id, @RequestBody Map<String, Object> params) {
        String realName = (String) params.get("realName");
        String phone = (String) params.get("phone");
        String email = (String) params.get("email");
        Integer status = params.get("status") != null ? ((Number) params.get("status")).intValue() : null;

        TbUser user = userService.updateUser(id, realName, phone, email, status);
        user.setfPassword(null);
        return ApiResponse.success(user);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{userId}/role")
    public ApiResponse<?> assignUserRoles(@PathVariable String userId, @RequestParam String roleIds) {
        List<String> roleIdList = List.of(roleIds.split(","));
        userService.assignRoles(userId, roleIdList);
        return ApiResponse.success(null);
    }

    @GetMapping("/{userId}/roles")
    public ApiResponse<?> getUserRoles(@PathVariable String userId) {
        List<TbUserRole> roles = userService.getUserRoles(userId);
        return ApiResponse.success(roles);
    }
}
