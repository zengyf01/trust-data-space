package com.tds.datar.controller.dataflow;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.service.dataflow.DataSyncDTO;
import com.tds.datar.service.dataflow.DataTransformDTO;
import com.tds.datar.service.dataflow.IDataSyncService;
import com.tds.datar.service.dataflow.IDataTransformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据流管理
 */
@RestController
@RequestMapping("/dataflow")
public class DataFlowController {

    @Autowired
    private IDataSyncService dataSyncService;

    @Autowired
    private IDataTransformService dataTransformService;

    // ==================== 数据同步接口 ====================

    @PostMapping("/sync")
    public ApiResponse<?> createSyncTask(@RequestBody DataSyncDTO dto) {
        return ApiResponse.success(dataSyncService.createSyncTask(dto));
    }

    @PostMapping("/sync/{syncId}/execute")
    public ApiResponse<?> executeSync(@PathVariable String syncId) {
        return ApiResponse.success(dataSyncService.executeSync(syncId));
    }

    @PostMapping("/sync/{syncId}/stop")
    public ApiResponse<?> stopSync(@PathVariable String syncId) {
        dataSyncService.stopSync(syncId);
        return ApiResponse.success(null);
    }

    @GetMapping("/sync/{syncId}/status")
    public ApiResponse<?> getSyncStatus(@PathVariable String syncId) {
        return ApiResponse.success(dataSyncService.getSyncStatus(syncId));
    }

    @GetMapping("/sync/{syncId}/history")
    public ApiResponse<?> getSyncHistory(
            @PathVariable String syncId,
            @RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.success(dataSyncService.getSyncHistory(syncId, limit));
    }

    // ==================== 数据转换接口 ====================

    @PostMapping("/transform")
    public ApiResponse<?> createTransform(@RequestBody DataTransformDTO dto) {
        return ApiResponse.success(dataTransformService.createTransform(dto));
    }

    @PostMapping("/transform/{transformId}/execute")
    public ApiResponse<?> executeTransform(
            @PathVariable String transformId,
            @RequestBody String inputData) {
        return ApiResponse.success(dataTransformService.executeTransform(transformId, inputData));
    }

    @PostMapping("/transform/{transformId}/batch")
    public ApiResponse<?> batchTransform(
            @PathVariable String transformId,
            @RequestBody List<String> inputDataList) {
        return ApiResponse.success(dataTransformService.batchTransform(transformId, inputDataList));
    }

    @PostMapping("/transform/{transformId}/validate")
    public ApiResponse<?> validateTransform(
            @PathVariable String transformId,
            @RequestBody String testData) {
        return ApiResponse.success(dataTransformService.validateTransform(transformId, testData));
    }
}