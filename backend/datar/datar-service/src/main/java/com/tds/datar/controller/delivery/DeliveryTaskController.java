package com.tds.datar.controller.delivery;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.service.delivery.DeliveryTaskDTO;
import com.tds.datar.service.delivery.IDeliveryTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 交付任务管理
 */
@RestController
@RequestMapping("/delivery")
public class DeliveryTaskController {

    @Autowired
    private IDeliveryTaskService deliveryTaskService;

    // ==================== 任务管理 ====================

    @GetMapping("/page")
    public ApiResponse<?> getTaskPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String workOrderId,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(deliveryTaskService.getTaskPage(currentPage, pageSize, workOrderId, taskType, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getTaskById(@PathVariable String id) {
        return ApiResponse.success(deliveryTaskService.getTaskById(id));
    }

    @PostMapping
    public ApiResponse<?> createTask(@RequestBody DeliveryTaskDTO dto) {
        return ApiResponse.success(deliveryTaskService.createTask(dto));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<?> updateTaskStatus(
            @PathVariable String id,
            @RequestParam Integer status,
            @RequestParam(required = false) String buildLog,
            @RequestParam(required = false) String errorMessage) {
        return ApiResponse.success(deliveryTaskService.updateTaskStatus(id, status, buildLog, errorMessage));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteTask(@PathVariable String id) {
        deliveryTaskService.deleteTask(id);
        return ApiResponse.success(null);
    }

    // ==================== 交付操作 ====================

    @PostMapping("/sandbox/init")
    public ApiResponse<?> initSandbox(
            @RequestParam String workOrderId,
            @RequestParam String sandboxId,
            @RequestParam String tenantId) {
        return ApiResponse.success(deliveryTaskService.initSandbox(workOrderId, sandboxId, tenantId));
    }

    @PostMapping("/image/build")
    public ApiResponse<?> buildImage(
            @RequestParam String taskId,
            @RequestParam String imageName,
            @RequestParam String imageTag,
            @RequestParam String sourcePath) {
        return ApiResponse.success(deliveryTaskService.buildImage(taskId, imageName, imageTag, sourcePath));
    }

    @PostMapping("/source/download")
    public ApiResponse<?> downloadSource(
            @RequestParam String taskId,
            @RequestParam String sourceUrl,
            @RequestParam String workDirectory) {
        return ApiResponse.success(deliveryTaskService.downloadSource(taskId, sourceUrl, workDirectory));
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<?> executeTask(@PathVariable String id) {
        return ApiResponse.success(deliveryTaskService.executeTask(id));
    }
}