package com.tds.datar.controller.user;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.dal.entity.TbRole;
import com.tds.datar.service.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/page")
    public ApiResponse<?> getRolePage(
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(roleService.getRolePage(currentPage, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getRoleById(@PathVariable String id) {
        return ApiResponse.success(roleService.getRoleById(id));
    }

    @PostMapping
    public ApiResponse<?> createRole(@RequestBody java.util.Map<String, Object> params) {
        String roleName = (String) params.get("roleName");
        String roleType = (String) params.get("roleType");
        String roleDesc = (String) params.get("roleDesc");

        TbRole role = roleService.createRole(roleName, roleType, roleDesc);
        return ApiResponse.success(role);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateRole(@PathVariable String id, @RequestBody java.util.Map<String, Object> params) {
        String roleName = (String) params.get("roleName");
        String roleType = (String) params.get("roleType");
        String roleDesc = (String) params.get("roleDesc");

        try {
            TbRole role = roleService.updateRole(id, roleName, roleType, roleDesc);
            return ApiResponse.success(role);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteRole(@PathVariable String id) {
        try {
            roleService.deleteRole(id);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ApiResponse<?> getAllRoles() {
        return ApiResponse.success(roleService.getAllRoles());
    }
}
