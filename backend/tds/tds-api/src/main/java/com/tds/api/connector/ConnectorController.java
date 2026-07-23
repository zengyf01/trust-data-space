package com.tds.api.connector;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.common.core.ApiResponse;
import com.tds.common.core.PageResult;
import com.tds.dal.entity.TbConnector;
import com.tds.dal.entity.TbConnectorLog;
import com.tds.dal.entity.TbConnectorVersion;
import com.tds.service.connector.ConnectorCreateDTO;
import com.tds.service.connector.ConnectorOperateDTO;
import com.tds.service.connector.ConnectorServiceImpl;
import com.tds.service.connector.ConnectorVersionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 连接器API控制器
 */
@RestController
@RequestMapping("/connector")
public class ConnectorController {

    @Autowired
    private ConnectorServiceImpl connectorService;

    @PostMapping("/page")
    public ApiResponse<PageResult<TbConnector>> getConnectorPage(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {

        IPage<TbConnector> page = connectorService.getConnectorPage(
                pageNumber, pageSize, name, type, status);
        PageResult<TbConnector> result = PageResult.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<TbConnector> getConnector(@PathVariable String id) {
        TbConnector connector = connectorService.getConnectorById(id);
        return ApiResponse.success(connector);
    }

    @GetMapping("/sn/{sn}")
    public ApiResponse<TbConnector> getConnectorBySn(@PathVariable String sn) {
        TbConnector connector = connectorService.getConnectorBySn(sn);
        return ApiResponse.success(connector);
    }

    @PostMapping
    public ApiResponse<TbConnector> createConnector(@RequestBody ConnectorCreateDTO dto) {
        TbConnector connector = connectorService.createConnector(dto);
        return ApiResponse.success(connector);
    }

    @PutMapping("/{id}")
    public ApiResponse<TbConnector> updateConnector(
            @PathVariable String id,
            @RequestBody ConnectorCreateDTO dto) {
        TbConnector connector = connectorService.updateConnector(id, dto);
        return ApiResponse.success(connector);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteConnector(@PathVariable String id) {
        connectorService.deleteConnector(id);
        return ApiResponse.success();
    }

    @PostMapping("/heartbeat/{sn}")
    public ApiResponse<Void> heartbeat(@PathVariable String sn) {
        connectorService.heartbeat(sn);
        return ApiResponse.success();
    }

    @PostMapping("/checkStatus")
    public ApiResponse<Void> checkConnectorStatus() {
        connectorService.checkConnectorStatus();
        return ApiResponse.success();
    }

    @GetMapping("/{id}/versions")
    public ApiResponse<List<TbConnectorVersion>> getConnectorVersions(@PathVariable String id) {
        List<TbConnectorVersion> versions = connectorService.getConnectorVersions(id);
        return ApiResponse.success(versions);
    }

    @PostMapping("/version")
    public ApiResponse<TbConnectorVersion> uploadVersion(@RequestBody ConnectorVersionDTO dto) {
        TbConnectorVersion version = connectorService.uploadVersion(dto);
        return ApiResponse.success(version);
    }

    @PostMapping("/version/{id}/activate")
    public ApiResponse<TbConnectorVersion> activateVersion(@PathVariable String id) {
        TbConnectorVersion version = connectorService.activateVersion(id);
        return ApiResponse.success(version);
    }

    @GetMapping("/{id}/logs")
    public ApiResponse<List<TbConnectorLog>> getConnectorLogs(@PathVariable String id) {
        List<TbConnectorLog> logs = connectorService.getConnectorLogs(id);
        return ApiResponse.success(logs);
    }

    @PostMapping("/operate")
    public ApiResponse<TbConnectorLog> executeOperation(@RequestBody ConnectorOperateDTO dto) {
        TbConnectorLog log = connectorService.executeOperation(dto);
        return ApiResponse.success(log);
    }
}