package com.tds.dos.api.sandbox;

import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.service.sandbox.ISandboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 安全沙盒管理接口
 */
@RestController
@RequestMapping("/api/dos/sandbox")
public class SandboxController {

    @Autowired
    private ISandboxService sandboxService;

    /**
     * 创建沙盒
     */
    @PostMapping("/create")
    public ApiResponse<?> createSandbox(@RequestBody Map<String, Object> params) {
        String workOrderId = (String) params.get("workOrderId");
        String image = (String) params.get("image");
        Integer cpu = params.get("cpu") != null ? Integer.parseInt(params.get("cpu").toString()) : 2;
        Integer memoryMB = params.get("memoryMB") != null ? Integer.parseInt(params.get("memoryMB").toString()) : 4096;
        String workDir = (String) params.get("workDir");
        String sourceUrl = (String) params.get("sourceUrl");

        Map<String, Object> result = sandboxService.createSandbox(
            workOrderId, image, cpu, memoryMB, workDir, sourceUrl);
        return ApiResponse.success(result);
    }

    /**
     * 销毁沙盒
     */
    @PostMapping("/destroy")
    public ApiResponse<?> destroySandbox(@RequestParam String podName) {
        sandboxService.destroySandbox(podName);
        return ApiResponse.success(null);
    }

    /**
     * 停止沙盒
     */
    @PostMapping("/stop")
    public ApiResponse<?> stopSandbox(@RequestParam String podName) {
        sandboxService.stopSandbox(podName);
        return ApiResponse.success(null);
    }

    /**
     * 获取沙盒状态
     */
    @GetMapping("/status")
    public ApiResponse<?> getSandboxStatus(@RequestParam String podName) {
        Map<String, Object> status = sandboxService.getSandboxStatus(podName);
        return ApiResponse.success(status);
    }

    /**
     * 获取沙盒详情
     */
    @GetMapping("/detail")
    public ApiResponse<?> getSandboxDetail(@RequestParam String podName) {
        Map<String, Object> detail = sandboxService.getSandboxDetail(podName);
        return ApiResponse.success(detail);
    }

    /**
     * 获取沙盒日志
     */
    @GetMapping("/logs")
    public ApiResponse<?> getSandboxLogs(@RequestParam String podName,
                                         @RequestParam(defaultValue = "true") boolean tail) {
        String logs = sandboxService.getSandboxLogs(podName, tail);
        return ApiResponse.success(Map.of("logs", logs));
    }

    /**
     * 获取沙盒事件
     */
    @GetMapping("/events")
    public ApiResponse<?> getSandboxEvents(@RequestParam String podName) {
        List<Map<String, Object>> events = sandboxService.getSandboxEvents(podName);
        return ApiResponse.success(events);
    }

    /**
     * 获取沙盒列表
     */
    @GetMapping("/list")
    public ApiResponse<?> listSandboxes(@RequestParam String userId) {
        List<Map<String, Object>> sandboxes = sandboxService.listSandboxes(userId);
        return ApiResponse.success(sandboxes);
    }
}