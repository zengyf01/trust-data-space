package com.tds.datar.controller.connector;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.dal.entity.TbConnector;
import com.tds.datar.dal.entity.TbConnectorLog;
import com.tds.datar.dal.entity.TbConnectorVersion;
import com.tds.datar.service.connector.ConnectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/connector")
public class ConnectorController {

    @Autowired
    private ConnectorService connectorService;

    @GetMapping("/page")
    public ApiResponse<?> getConnectorPage(
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String spaceId) {
        return ApiResponse.success(connectorService.getConnectorPage(currentPage, pageSize, keyword, type, status, spaceId));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getConnectorById(@PathVariable String id) {
        return ApiResponse.success(connectorService.getConnectorById(id));
    }

    @GetMapping("/sn/{sn}")
    public ApiResponse<?> getConnectorBySn(@PathVariable String sn) {
        return ApiResponse.success(connectorService.getConnectorBySn(sn));
    }

    @PostMapping
    public ApiResponse<?> createConnector(@RequestBody java.util.Map<String, Object> params) {
        String name = (String) params.get("name");
        String sn = (String) params.get("sn");
        Integer type = params.get("type") != null ? ((Number) params.get("type")).intValue() : 1;
        String ipAddress = (String) params.get("ipAddress");
        Integer sshPort = params.get("sshPort") != null ? ((Number) params.get("sshPort")).intValue() : null;
        String sshUsername = (String) params.get("sshUsername");
        String sshPassword = (String) params.get("sshPassword");
        String institutionName = (String) params.get("institutionName");
        String region = (String) params.get("region");
        String description = (String) params.get("description");
        String spaceId = (String) params.get("spaceId");

        TbConnector connector = connectorService.createConnector(
                name, sn, type, ipAddress, sshPort, sshUsername, sshPassword,
                institutionName, region, description, spaceId);
        return ApiResponse.success(connector);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateConnector(@PathVariable String id, @RequestBody java.util.Map<String, Object> params) {
        String name = (String) params.get("name");
        Integer type = params.get("type") != null ? ((Number) params.get("type")).intValue() : null;
        String ipAddress = (String) params.get("ipAddress");
        Integer sshPort = params.get("sshPort") != null ? ((Number) params.get("sshPort")).intValue() : null;
        String sshUsername = (String) params.get("sshUsername");
        String sshPassword = (String) params.get("sshPassword");
        String institutionName = (String) params.get("institutionName");
        String region = (String) params.get("region");
        String description = (String) params.get("description");

        try {
            TbConnector connector = connectorService.updateConnector(
                    id, name, type, ipAddress, sshPort, sshUsername, sshPassword,
                    institutionName, region, description);
            return ApiResponse.success(connector);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteConnector(@PathVariable String id) {
        try {
            connectorService.deleteConnector(id);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/heartbeat/{sn}")
    public ApiResponse<?> heartbeat(@PathVariable String sn) {
        connectorService.heartbeat(sn);
        return ApiResponse.success(null);
    }

    @PostMapping("/checkStatus")
    public ApiResponse<?> checkConnectorStatus() {
        connectorService.checkConnectorStatus();
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/versions")
    public ApiResponse<?> getConnectorVersions(@PathVariable String id) {
        return ApiResponse.success(connectorService.getConnectorVersions(id));
    }

    @PostMapping("/version")
    public ApiResponse<?> uploadVersion(@RequestBody java.util.Map<String, Object> params) {
        String connectorId = (String) params.get("connectorId");
        String version = (String) params.get("version");
        String changeLog = (String) params.get("changeLog");

        TbConnectorVersion v = connectorService.uploadVersion(connectorId, version, changeLog);
        return ApiResponse.success(v);
    }

    @PostMapping("/version/{id}/activate")
    public ApiResponse<?> activateVersion(@PathVariable String id) {
        try {
            TbConnectorVersion v = connectorService.activateVersion(id);
            return ApiResponse.success(v);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/logs")
    public ApiResponse<?> getConnectorLogs(@PathVariable String id) {
        return ApiResponse.success(connectorService.getConnectorLogs(id));
    }

    @PostMapping("/operate")
    public ApiResponse<?> executeOperation(@RequestBody java.util.Map<String, Object> params) {
        String connectorId = (String) params.get("connectorId");
        String operateType = (String) params.get("operateType");
        String operateContent = (String) params.get("operateContent");

        TbConnectorLog log = connectorService.executeOperation(connectorId, operateType, operateContent);
        return ApiResponse.success(log);
    }

    @GetMapping("/all")
    public ApiResponse<?> getAllConnectors() {
        return ApiResponse.success(connectorService.getAllConnectors());
    }
}