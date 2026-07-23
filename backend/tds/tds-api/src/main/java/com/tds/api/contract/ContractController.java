package com.tds.api.contract;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.common.core.ApiResponse;
import com.tds.common.core.PageResult;
import com.tds.dal.entity.TbDigitalContract;
import com.tds.service.contract.ContractCreateDTO;
import com.tds.service.contract.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数字合约API控制器
 */
@RestController
@RequestMapping("/digitalContract")
public class ContractController {

    @Autowired
    private ContractService contractService;

    /**
     * 获取合约列表（分页）
     */
    @PostMapping("/contractList")
    public ApiResponse<PageResult<TbDigitalContract>> getContractList(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String contractCode,
            @RequestParam(required = false) Integer contractStatus) {

        IPage<TbDigitalContract> page = contractService.getContractPage(pageNumber, pageSize, contractCode, contractStatus);
        PageResult<TbDigitalContract> result = PageResult.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );
        return ApiResponse.success(result);
    }

    /**
     * 获取所有合约（按连接器筛选）
     */
    @PostMapping("/getAllDigitalContract")
    public ApiResponse<List<TbDigitalContract>> getAllContracts(
            @RequestParam(required = false) String connectorNumber) {
        List<TbDigitalContract> contracts = contractService.getAllContracts(connectorNumber);
        return ApiResponse.success(contracts);
    }

    /**
     * 获取合约详情
     */
    @GetMapping("/{id}")
    public ApiResponse<TbDigitalContract> getContract(@PathVariable String id) {
        TbDigitalContract contract = contractService.getContractById(id);
        if (contract == null) {
            return ApiResponse.error(404, "合约不存在");
        }
        return ApiResponse.success(contract);
    }

    /**
     * 创建合约
     */
    @PostMapping
    public ApiResponse<TbDigitalContract> createContract(@RequestBody ContractCreateDTO dto) {
        try {
            TbDigitalContract contract = contractService.createContract(dto);
            return ApiResponse.success(contract);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 供方签名
     */
    @PostMapping("/providerSign")
    public ApiResponse<TbDigitalContract> providerSign(
            @RequestParam String contractId,
            @RequestParam String signature) {
        try {
            TbDigitalContract contract = contractService.providerSign(contractId, signature);
            return ApiResponse.success(contract);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 需方签名
     */
    @PostMapping("/consumerSign")
    public ApiResponse<TbDigitalContract> consumerSign(
            @RequestParam String contractId,
            @RequestParam String signature) {
        try {
            TbDigitalContract contract = contractService.consumerSign(contractId, signature);
            return ApiResponse.success(contract);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 拒绝合约
     */
    @PostMapping("/reject")
    public ApiResponse<TbDigitalContract> rejectContract(
            @RequestParam String contractId,
            @RequestParam(required = false) String reason) {
        try {
            TbDigitalContract contract = contractService.rejectContract(contractId, reason);
            return ApiResponse.success(contract);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 终止合约
     */
    @PostMapping("/terminate")
    public ApiResponse<TbDigitalContract> terminateContract(
            @RequestParam String contractId,
            @RequestParam(required = false) String reason) {
        try {
            TbDigitalContract contract = contractService.terminateContract(contractId, reason);
            return ApiResponse.success(contract);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}