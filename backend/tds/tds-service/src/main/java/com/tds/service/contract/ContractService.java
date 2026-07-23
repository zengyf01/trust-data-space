package com.tds.service.contract;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.common.enums.ContractStatus;
import com.tds.common.util.SM2Util;
import com.tds.dal.entity.TbDigitalContract;
import com.tds.dal.mapper.TbDigitalContractMapper;
import com.tds.service.evidence.EvidenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 数字合约服务
 */
@Service
public class ContractService {

    @Autowired
    private TbDigitalContractMapper contractMapper;

    @Autowired
    private EvidenceService evidenceService;

    /**
     * 分页查询合约列表
     */
    public IPage<TbDigitalContract> getContractPage(int currentPage, int pageSize, String contractCode, Integer contractStatus) {
        Page<TbDigitalContract> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbDigitalContract> wrapper = new LambdaQueryWrapper<>();
        if (contractCode != null && !contractCode.isEmpty()) {
            wrapper.like(TbDigitalContract::getContractCode, contractCode);
        }
        if (contractStatus != null) {
            wrapper.eq(TbDigitalContract::getContractStatus, contractStatus);
        }
        wrapper.orderByDesc(TbDigitalContract::getfCreateTime);
        return contractMapper.selectPage(page, wrapper);
    }

    /**
     * 查询所有合约（按连接器筛选）
     */
    public List<TbDigitalContract> getAllContracts(String connectorNumber) {
        LambdaQueryWrapper<TbDigitalContract> wrapper = new LambdaQueryWrapper<>();
        if (connectorNumber != null && !connectorNumber.isEmpty()) {
            wrapper.eq(TbDigitalContract::getProviderConnectorAddress, connectorNumber)
                  .or()
                  .eq(TbDigitalContract::getUseConnectorAddress, connectorNumber);
        }
        wrapper.orderByDesc(TbDigitalContract::getfCreateTime);
        return contractMapper.selectList(wrapper);
    }

    /**
     * 获取合约详情
     */
    public TbDigitalContract getContractById(String id) {
        return contractMapper.selectById(id);
    }

    /**
     * 获取合约详情（按合约编号）
     */
    public TbDigitalContract getContractByCode(String contractCode) {
        LambdaQueryWrapper<TbDigitalContract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbDigitalContract::getContractCode, contractCode);
        return contractMapper.selectOne(wrapper);
    }

    /**
     * 创建合约
     */
    @Transactional
    public TbDigitalContract createContract(ContractCreateDTO dto) throws Exception {
        TbDigitalContract contract = new TbDigitalContract();
        contract.setId(UUID.randomUUID().toString().replace("-", ""));
        contract.setOrderCode(dto.getOrderCode());
        contract.setContractCode(generateContractCode());
        contract.setContractType(dto.getContractType());
        contract.setContractStatus(ContractStatus.PENDING.getCode());
        contract.setContractJson(dto.getContractJson());
        contract.setResourceSnapshot(dto.getResourceSnapshot());
        try {
            contract.setContractAbstract(SM2Util.hash(dto.getContractJson()));
        } catch (Exception e) {
            throw new RuntimeException("生成合约摘要失败", e);
        }
        contract.setContractStartTime(dto.getContractStartTime());
        contract.setContractEndTime(dto.getContractEndTime());

        // 供应方信息
        contract.setProviderInstitutionId(dto.getProviderInstitutionId());
        contract.setProviderInstitutionName(dto.getProviderInstitutionName());
        contract.setProviderContactName(dto.getProviderContactName());
        contract.setProviderPhone(dto.getProviderPhone());
        contract.setProviderEmail(dto.getProviderEmail());
        contract.setProviderConnectorAddress(dto.getProviderConnectorAddress());
        contract.setProviderInstitutionAddress(dto.getProviderInstitutionAddress());
        contract.setProviderPublicKey(dto.getProviderPublicKey());

        // 使用方信息
        contract.setUseInstitutionId(dto.getUseInstitutionId());
        contract.setUseInstitutionName(dto.getUseInstitutionName());
        contract.setUseContactName(dto.getUseContactName());
        contract.setUsePhone(dto.getUsePhone());
        contract.setUseEmail(dto.getUseEmail());
        contract.setUseConnectorAddress(dto.getUseConnectorAddress());
        contract.setUseInstitutionAddress(dto.getUseInstitutionAddress());
        contract.setUsePublicKey(dto.getUsePublicKey());

        contract.setfTenantId(dto.getTenantId());
        contract.setfCreateTime(LocalDateTime.now());
        contract.setfUpdateTime(LocalDateTime.now());
        contract.setfDeleteMark(0);

        contractMapper.insert(contract);

        // 记录存证（模拟区块链）
        evidenceService.recordContractCreate(contract);

        return contract;
    }

    /**
     * 供方签名
     */
    @Transactional
    public TbDigitalContract providerSign(String contractId, String signature) throws Exception {
        TbDigitalContract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new RuntimeException("合约不存在");
        }
        if (contract.getProviderSignature() != null) {
            throw new RuntimeException("供方已签名");
        }

        contract.setProviderSignature(signature);
        contract.setProviderSignTime(LocalDateTime.now());

        // 如果需方已签名，则进入执行状态
        if (contract.getUseSignature() != null) {
            contract.setContractStatus(ContractStatus.EXECUTING.getCode());
            evidenceService.recordContractSign(contract, "PROVIDER");
            evidenceService.recordContractSign(contract, "CONSUMER");
        } else {
            contract.setContractStatus(ContractStatus.SIGNING.getCode());
            evidenceService.recordContractSign(contract, "PROVIDER");
        }

        contract.setfUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);

        return contract;
    }

    /**
     * 需方签名
     */
    @Transactional
    public TbDigitalContract consumerSign(String contractId, String signature) throws Exception {
        TbDigitalContract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new RuntimeException("合约不存在");
        }
        if (contract.getUseSignature() != null) {
            throw new RuntimeException("需方已签名");
        }
        if (contract.getProviderSignature() == null) {
            throw new RuntimeException("供方尚未签名");
        }

        contract.setUseSignature(signature);
        contract.setUseSignTime(LocalDateTime.now());
        contract.setContractStatus(ContractStatus.EXECUTING.getCode());
        contract.setfUpdateTime(LocalDateTime.now());

        contractMapper.updateById(contract);

        // 记录存证
        evidenceService.recordContractSign(contract, "CONSUMER");

        return contract;
    }

    /**
     * 拒绝合约
     */
    @Transactional
    public TbDigitalContract rejectContract(String contractId, String reason) throws Exception {
        TbDigitalContract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new RuntimeException("合约不存在");
        }

        contract.setContractStatus(ContractStatus.REJECTED.getCode());
        contract.setfUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);

        // 记录存证
        evidenceService.recordContractReject(contract, reason);

        return contract;
    }

    /**
     * 终止合约
     */
    @Transactional
    public TbDigitalContract terminateContract(String contractId, String reason) throws Exception {
        TbDigitalContract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new RuntimeException("合约不存在");
        }

        contract.setContractStatus(ContractStatus.TERMINATED.getCode());
        contract.setfUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);

        // 记录存证
        evidenceService.recordContractTerminate(contract, reason);

        return contract;
    }

    /**
     * 生成合约编号
     */
    private String generateContractCode() {
        return "CTR" + System.currentTimeMillis();
    }
}