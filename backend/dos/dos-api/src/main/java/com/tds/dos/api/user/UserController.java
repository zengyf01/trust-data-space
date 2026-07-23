package com.tds.dos.api.user;

import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.common.core.PageResult;
import com.tds.dos.common.dto.UserDTO;
import com.tds.dos.service.auth.TdsUserCenterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User management controller - 对接TDS统一用户中心
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private TdsUserCenterClient tdsUserCenterClient;

    /**
     * 获取用户分页列表 - 从TDS用户中心获取
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<UserDTO>> getUserPage(
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            Map<String, Object> result = tdsUserCenterClient.getUserPage(currentPage, pageSize, keyword);
            if (result != null) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
                List<UserDTO> dtos = new ArrayList<>();
                if (list != null) {
                    for (Map<String, Object> item : list) {
                        UserDTO dto = new UserDTO();
                        dto.setUserId((String) item.get("fId"));
                        dto.setUsername((String) item.get("fUsername"));
                        dto.setEmail((String) item.get("fEmail"));
                        dto.setPhone((String) item.get("fPhone"));
                        dto.setStatus(String.valueOf(item.get("fStatus")));
                        dtos.add(dto);
                    }
                }
                // TDS返回的是嵌套结构: {list: [...], pagination: {total: 1, ...}}
                long total = 0;
                Map<String, Object> pagination = (Map<String, Object>) result.get("pagination");
                if (pagination != null && pagination.get("total") != null) {
                    total = ((Number) pagination.get("total")).longValue();
                }
                PageResult<UserDTO> pageResult = PageResult.of(dtos, total, currentPage, pageSize);
                return ApiResponse.success(pageResult);
            }
            return ApiResponse.success(PageResult.of(new ArrayList<>(), 0, currentPage, pageSize));
        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            return ApiResponse.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserDTO> getUserById(@PathVariable String userId) {
        try {
            Map<String, Object> user = tdsUserCenterClient.getUserById(userId);
            if (user != null) {
                UserDTO dto = new UserDTO();
                dto.setUserId((String) user.get("fId"));
                dto.setUsername((String) user.get("fUsername"));
                dto.setEmail((String) user.get("fEmail"));
                dto.setPhone((String) user.get("fPhone"));
                dto.setStatus(String.valueOf(user.get("fStatus")));
                return ApiResponse.success(dto);
            }
            return ApiResponse.error("用户不存在");
        } catch (Exception e) {
            logger.error("获取用户详情失败: userId={}", userId, e);
            return ApiResponse.error("获取用户详情失败");
        }
    }

    /**
     * 根据用户名获取用户
     */
    @GetMapping("/username/{username}")
    public ApiResponse<UserDTO> getUserByUsername(@PathVariable String username) {
        try {
            Map<String, Object> result = tdsUserCenterClient.getUserPage(1, 100, username);
            if (result != null) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
                if (list != null) {
                    for (Map<String, Object> item : list) {
                        if (username.equals(item.get("fUsername"))) {
                            UserDTO dto = new UserDTO();
                            dto.setUserId((String) item.get("fId"));
                            dto.setUsername((String) item.get("fUsername"));
                            dto.setEmail((String) item.get("fEmail"));
                            dto.setPhone((String) item.get("fPhone"));
                            return ApiResponse.success(dto);
                        }
                    }
                }
            }
            return ApiResponse.error("用户不存在");
        } catch (Exception e) {
            logger.error("获取用户失败: username={}", username, e);
            return ApiResponse.error("获取用户失败");
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ApiResponse<Void> createUser(@RequestBody Map<String, Object> request) {
        try {
            // 设置默认租户为DOS
            request.put("tenantId", "TENANT_DOS");
            tdsUserCenterClient.createUser(request);
            return ApiResponse.success(null);
        } catch (Exception e) {
            logger.error("创建用户失败", e);
            return ApiResponse.error("创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    public ApiResponse<Void> updateUser(@PathVariable String userId, @RequestBody Map<String, Object> request) {
        try {
            tdsUserCenterClient.updateUser(userId, request);
            return ApiResponse.success(null);
        } catch (Exception e) {
            logger.error("更新用户失败: userId={}", userId, e);
            return ApiResponse.error("更新用户失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable String userId) {
        try {
            boolean success = tdsUserCenterClient.deleteUser(userId);
            if (success) {
                return ApiResponse.success(null);
            }
            return ApiResponse.error("删除用户失败");
        } catch (Exception e) {
            logger.error("删除用户失败: userId={}", userId, e);
            return ApiResponse.error("删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有用户 - 暂不支持
     */
    @GetMapping("/list")
    public ApiResponse<List<UserDTO>> getAllUsers() {
        return ApiResponse.error("请使用分页查询");
    }
}
