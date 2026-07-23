package com.tds.datar.controller.open;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.service.open.ConnectorOpenDTO;
import com.tds.datar.service.open.ConnectorOpenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 连接器开放接口
 */
@RestController
@RequestMapping("/open")
public class ConnectorOpenController {

    @Autowired
    private ConnectorOpenService connectorOpenService;

    /**
     * 注册连接器
     */
    @PostMapping("/connector")
    public ApiResponse<?> registerConnector(@RequestBody ConnectorOpenDTO dto) {
        return ApiResponse.success(connectorOpenService.registerConnector(dto));
    }

    /**
     * 发送心跳
     */
    @PostMapping("/connector/heartbeat")
    public ApiResponse<?> heartbeat(@RequestParam String sn) {
        connectorOpenService.heartbeat(sn);
        return ApiResponse.success(Map.of("sn", sn, "heartbeat", System.currentTimeMillis()));
    }

    /**
     * 获取连接器状态
     */
    @GetMapping("/connector/status")
    public ApiResponse<?> getConnectorStatus(@RequestParam String sn) {
        boolean online = connectorOpenService.isConnectorOnline(sn);
        return ApiResponse.success(Map.of("sn", sn, "online", online));
    }

    /**
     * 创建默认账号
     */
    @PostMapping("/account")
    public ApiResponse<?> createDefaultAccount(@RequestBody ConnectorOpenDTO dto) {
        return ApiResponse.success(connectorOpenService.createDefaultAccount(dto));
    }

    /**
     * 统一数据查询接口
     */
    @GetMapping("/data/query")
    public ApiResponse<?> queryData(
            @RequestParam String connectorId,
            @RequestParam String catalogId,
            @RequestParam(required = false) String condition,
            @RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.success(connectorOpenService.queryData(connectorId, catalogId, condition, limit));
    }

    /**
     * 统一数据推送接口
     */
    @PostMapping("/data/push")
    public ApiResponse<?> pushData(
            @RequestParam String connectorId,
            @RequestParam String catalogId,
            @RequestBody String data) {
        return ApiResponse.success(connectorOpenService.pushData(connectorId, catalogId, data));
    }

    /**
     * 转发API请求
     */
    @RequestMapping("/forward/**")
    public ApiResponse<?> forwardApi(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String method,
            @RequestBody(required = false) String requestBody) {
        return ApiResponse.success(Map.of("forwarded", true));
    }
}