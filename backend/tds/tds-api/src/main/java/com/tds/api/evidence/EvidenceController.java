package com.tds.api.evidence;

import com.tds.common.core.ApiResponse;
import com.tds.dal.entity.TbEvidenceLog;
import com.tds.dal.entity.TbDataConsumeLog;
import com.tds.dal.entity.TbOperationLog;
import com.tds.service.evidence.DataConsumeDTO;
import com.tds.service.evidence.EvidenceLogDTO;
import com.tds.service.evidence.IEvidenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 存证管理
 */
@RestController
@RequestMapping("/evidence")
public class EvidenceController {

    @Autowired
    private IEvidenceService evidenceService;

    @GetMapping("/page")
    public ApiResponse<?> getEvidencePage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String evidenceType,
            @RequestParam(required = false) String contractId) {
        return ApiResponse.success(evidenceService.getEvidencePage(currentPage, pageSize, evidenceType, contractId));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getEvidenceById(@PathVariable String id) {
        return ApiResponse.success(evidenceService.getEvidenceById(id));
    }

    @PostMapping
    public ApiResponse<?> createEvidence(@RequestBody EvidenceLogDTO dto) {
        return ApiResponse.success(evidenceService.createEvidence(dto));
    }

    @GetMapping("/verify")
    public ApiResponse<?> verifyEvidence(@RequestParam String txHash) {
        boolean valid = evidenceService.verifyEvidence(txHash);
        return ApiResponse.success(Map.of("valid", valid, "txHash", txHash));
    }

    @GetMapping("/consume/page")
    public ApiResponse<?> getDataConsumePage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String contractId,
            @RequestParam(required = false) String tenantId) {
        return ApiResponse.success(evidenceService.getDataConsumePage(currentPage, pageSize, contractId, tenantId));
    }

    @PostMapping("/consume")
    public ApiResponse<?> recordDataConsume(@RequestBody DataConsumeDTO dto) {
        return ApiResponse.success(evidenceService.recordDataConsume(dto));
    }

    @GetMapping("/operationLog/page")
    public ApiResponse<?> getOperationLogPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String module) {
        return ApiResponse.success(evidenceService.getOperationLogPage(currentPage, pageSize, userId, module));
    }
}