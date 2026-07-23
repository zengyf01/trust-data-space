package com.tds.datar.controller.product;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.service.product.export.ExportDTO;
import com.tds.datar.service.product.export.IExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据导出
 */
@RestController
@RequestMapping("/product/export")
public class ProductExportController {

    @Autowired
    private IExportService exportService;

    /**
     * 导出样例数据
     */
    @PostMapping
    public ApiResponse<?> exportSampleData(@RequestBody ExportDTO dto) {
        return ApiResponse.success(exportService.exportSampleData(dto));
    }

    /**
     * 导出为Excel
     */
    @GetMapping("/excel")
    public ApiResponse<?> exportToExcel(@RequestBody ExportDTO dto) {
        byte[] data = exportService.exportToExcel(dto);
        return ApiResponse.success(Map.of("data", new String(data), "size", data.length));
    }

    /**
     * 导出为JSON
     */
    @GetMapping("/json")
    public ApiResponse<?> exportToJson(@RequestBody ExportDTO dto) {
        return ApiResponse.success(Map.of("data", exportService.exportToJson(dto)));
    }

    /**
     * 导出为CSV
     */
    @GetMapping("/csv")
    public ApiResponse<?> exportToCsv(@RequestBody ExportDTO dto) {
        byte[] data = exportService.exportToCsv(dto);
        return ApiResponse.success(Map.of("data", new String(data), "size", data.length));
    }

    /**
     * 获取导出记录
     */
    @GetMapping("/record/{exportId}")
    public ApiResponse<?> getExportRecord(@PathVariable String exportId) {
        return ApiResponse.success(exportService.getExportRecord(exportId));
    }
}