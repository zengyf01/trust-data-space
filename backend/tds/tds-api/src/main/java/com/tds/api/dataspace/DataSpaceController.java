package com.tds.api.dataspace;

import com.tds.common.core.ApiResponse;
import com.tds.service.dataspace.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据空间管理
 */
@RestController
@RequestMapping("/dataspace")
public class DataSpaceController {

    @Autowired
    private IDataSpaceService dataSpaceService;

    // ==================== 数据空间管理 ====================

    @GetMapping("/page")
    public ApiResponse<?> getSpacePage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String spaceName,
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(dataSpaceService.getSpacePage(currentPage, pageSize, spaceName, ownerId, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getSpaceById(@PathVariable String id) {
        return ApiResponse.success(dataSpaceService.getSpaceById(id));
    }

    @GetMapping("/code/{spaceCode}")
    public ApiResponse<?> getSpaceByCode(@PathVariable String spaceCode) {
        return ApiResponse.success(dataSpaceService.getSpaceByCode(spaceCode));
    }

    @PostMapping
    public ApiResponse<?> createSpace(@RequestBody DataSpaceDTO dto) {
        return ApiResponse.success(dataSpaceService.createSpace(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateSpace(@PathVariable String id, @RequestBody DataSpaceDTO dto) {
        return ApiResponse.success(dataSpaceService.updateSpace(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteSpace(@PathVariable String id) {
        dataSpaceService.deleteSpace(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<?> approveSpace(
            @PathVariable String id,
            @RequestParam Integer status) {
        return ApiResponse.success(dataSpaceService.approveSpace(id, status));
    }

    @PostMapping("/{id}/freeze")
    public ApiResponse<?> freezeSpace(
            @PathVariable String id,
            @RequestParam Integer status) {
        return ApiResponse.success(dataSpaceService.freezeSpace(id, status));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<?> getUserSpaces(@PathVariable String userId) {
        return ApiResponse.success(dataSpaceService.getUserSpaces(userId));
    }

    // ==================== 成员管理 ====================

    @GetMapping("/member/page")
    public ApiResponse<?> getMemberPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam String spaceId,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(dataSpaceService.getMemberPage(currentPage, pageSize, spaceId, role, status));
    }

    @GetMapping("/member/{id}")
    public ApiResponse<?> getMemberById(@PathVariable String id) {
        return ApiResponse.success(dataSpaceService.getMemberById(id));
    }

    @PostMapping("/member")
    public ApiResponse<?> addMember(@RequestBody DataSpaceMemberDTO dto) {
        return ApiResponse.success(dataSpaceService.addMember(dto));
    }

    @PutMapping("/member/{id}/role")
    public ApiResponse<?> updateMemberRole(
            @PathVariable String id,
            @RequestParam Integer role) {
        return ApiResponse.success(dataSpaceService.updateMemberRole(id, role));
    }

    @PostMapping("/member/{id}/approve")
    public ApiResponse<?> approveMember(
            @PathVariable String id,
            @RequestParam Integer status) {
        return ApiResponse.success(dataSpaceService.approveMember(id, status));
    }

    @DeleteMapping("/member/{id}")
    public ApiResponse<?> removeMember(@PathVariable String id) {
        dataSpaceService.removeMember(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/member/role")
    public ApiResponse<?> getMemberRole(
            @RequestParam String spaceId,
            @RequestParam String userId) {
        return ApiResponse.success(dataSpaceService.getMemberRole(spaceId, userId));
    }

    // ==================== 资源管理 ====================

    @GetMapping("/resource/page")
    public ApiResponse<?> getResourcePage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam String spaceId,
            @RequestParam(required = false) String resourceType) {
        return ApiResponse.success(dataSpaceService.getResourcePage(currentPage, pageSize, spaceId, resourceType));
    }

    @PostMapping("/resource")
    public ApiResponse<?> addResource(@RequestBody DataSpaceResourceDTO dto) {
        return ApiResponse.success(dataSpaceService.addResource(dto));
    }

    @PutMapping("/resource/{id}/access")
    public ApiResponse<?> updateResourceAccess(
            @PathVariable String id,
            @RequestParam Integer accessLevel) {
        return ApiResponse.success(dataSpaceService.updateResourceAccess(id, accessLevel));
    }

    @DeleteMapping("/resource/{id}")
    public ApiResponse<?> removeResource(@PathVariable String id) {
        dataSpaceService.removeResource(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/resource/user/{userId}")
    public ApiResponse<?> getUserAccessibleResources(
            @PathVariable String userId,
            @RequestParam(required = false) String resourceType) {
        return ApiResponse.success(dataSpaceService.getUserAccessibleResources(userId, resourceType));
    }
}