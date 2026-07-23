package com.tds.datar.controller.datasource;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.service.datasource.DataSourceDTO;
import com.tds.datar.service.datasource.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据源管理
 */
@RestController
@RequestMapping("/datasource")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @GetMapping("/page")
    public ApiResponse<?> getDataSourcePage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String dataSourceName,
            @RequestParam(required = false) Integer sourceType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String spaceId) {
        return ApiResponse.success(dataSourceService.getDataSourcePage(currentPage, pageSize, dataSourceName, sourceType, status, spaceId));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getDataSourceById(@PathVariable String id) {
        return ApiResponse.success(dataSourceService.getDataSourceById(id));
    }

    @PostMapping
    public ApiResponse<?> createDataSource(@RequestBody DataSourceDTO dto) {
        return ApiResponse.success(dataSourceService.createDataSource(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateDataSource(@PathVariable String id, @RequestBody DataSourceDTO dto) {
        return ApiResponse.success(dataSourceService.updateDataSource(id, dto));
    }

    @PostMapping("/{id}/test")
    public ApiResponse<?> testConnection(@PathVariable String id) {
        return ApiResponse.success(dataSourceService.testConnectionById(id));
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<?> enableDataSource(@PathVariable String id) {
        dataSourceService.enableDataSource(id);
        return ApiResponse.success("success");
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<?> disableDataSource(@PathVariable String id) {
        dataSourceService.disableDataSource(id);
        return ApiResponse.success("success");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteDataSource(@PathVariable String id) {
        dataSourceService.deleteDataSource(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/tables")
    public ApiResponse<?> getTableList(@PathVariable String id) {
        return ApiResponse.success(dataSourceService.getTableList(id));
    }

    @GetMapping("/{id}/columns")
    public ApiResponse<?> getColumnList(
            @PathVariable String id,
            @RequestParam String tableName) {
        return ApiResponse.success(dataSourceService.getColumnList(id, tableName));
    }
}