package com.tds.datar.controller.datasource;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.service.datasource.debug.DataSourceDebugDTO;
import com.tds.datar.service.datasource.debug.IDataSourceDebugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 数据源调试
 */
@RestController
@RequestMapping("/datasource/debug")
public class DataSourceDebugController {

    @Autowired
    private IDataSourceDebugService debugService;

    /**
     * 数据预览
     */
    @GetMapping("/preview")
    public ApiResponse<?> previewData(
            @RequestParam String dataSourceId,
            @RequestParam String tableName,
            @RequestParam(required = false) String condition,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(debugService.previewData(dataSourceId, tableName, condition, limit));
    }

    /**
     * 执行SQL查询
     */
    @PostMapping("/sql")
    public ApiResponse<?> executeSql(
            @RequestParam String dataSourceId,
            @RequestBody String sql) {
        return ApiResponse.success(debugService.executeSql(dataSourceId, sql));
    }

    /**
     * SFTP文件列表
     */
    @GetMapping("/sftp/files")
    public ApiResponse<?> listSftpFiles(
            @RequestParam String dataSourceId,
            @RequestParam(defaultValue = "/") String path) {
        return ApiResponse.success(debugService.listSftpFiles(dataSourceId, path));
    }

    /**
     * SFTP文件下载
     */
    @GetMapping("/sftp/download")
    public ApiResponse<?> downloadSftpFile(
            @RequestParam String dataSourceId,
            @RequestParam String filePath) {
        byte[] content = debugService.downloadSftpFile(dataSourceId, filePath);
        return ApiResponse.success(Map.of("fileSize", content.length, "content", new String(content)));
    }

    /**
     * 读取CSV文件
     */
    @GetMapping("/csv")
    public ApiResponse<?> readCsvFile(
            @RequestParam String dataSourceId,
            @RequestParam String filePath,
            @RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.success(debugService.readCsvFile(dataSourceId, filePath, limit));
    }

    /**
     * HTTP接口测试
     */
    @PostMapping("/http")
    public ApiResponse<?> testHttpEndpoint(
            @RequestParam String dataSourceId,
            @RequestParam(defaultValue = "GET") String method,
            @RequestParam String url,
            @RequestParam(required = false) String headers,
            @RequestBody(required = false) String body) {
        return ApiResponse.success(debugService.testHttpEndpoint(dataSourceId, method, url, headers, body));
    }
}