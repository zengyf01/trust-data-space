package com.tds.service.evidence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.common.enums.EvidenceType;
import com.tds.dal.entity.TbEvidenceLog;
import com.tds.dal.entity.TbDataConsumeLog;
import com.tds.dal.entity.TbOperationLog;
import com.tds.dal.mapper.TbEvidenceLogMapper;
import com.tds.dal.mapper.TbDataConsumeLogMapper;
import com.tds.dal.mapper.TbOperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 存证服务实现（模拟区块链上链）
 */
@Service
public class EvidenceServiceImpl implements IEvidenceService {

    @Autowired
    private TbEvidenceLogMapper evidenceLogMapper;

    @Autowired
    private TbDataConsumeLogMapper dataConsumeLogMapper;

    @Autowired
    private TbOperationLogMapper operationLogMapper;

    @Override
    public IPage<TbEvidenceLog> getEvidencePage(int currentPage, int pageSize,
            String evidenceType, String contractId) {
        Page<TbEvidenceLog> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbEvidenceLog> wrapper = new LambdaQueryWrapper<>();
        if (evidenceType != null && !evidenceType.isEmpty()) {
            wrapper.eq(TbEvidenceLog::getfEvidenceType, evidenceType);
        }
        if (contractId != null && !contractId.isEmpty()) {
            wrapper.eq(TbEvidenceLog::getfContractId, contractId);
        }
        wrapper.orderByDesc(TbEvidenceLog::getfCreateTime);
        return evidenceLogMapper.selectPage(page, wrapper);
    }

    @Override
    public TbEvidenceLog getEvidenceById(String id) {
        return evidenceLogMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbEvidenceLog createEvidence(EvidenceLogDTO dto) {
        TbEvidenceLog evidence = new TbEvidenceLog();
        evidence.setfId(UUID.randomUUID().toString().replace("-", ""));

        // 生成模拟交易哈希
        String txHash = generateTxHash(dto);
        evidence.setfTxHash(txHash);

        // 模拟区块信息
        evidence.setfBlockHash(generateBlockHash(txHash));
        evidence.setfBlockNumber(System.currentTimeMillis() / 1000);

        evidence.setfEvidenceType(dto.getEvidenceType());
        evidence.setfEvidenceData(dto.getEvidenceData());
        evidence.setfContractId(dto.getContractId());
        evidence.setfOrderId(dto.getOrderId());
        evidence.setfTenantId(dto.getTenantId());
        evidence.setfCreateUser(dto.getCreateUser());
        evidence.setfCreateTime(LocalDateTime.now());

        // 模拟区块链时间（有延迟）
        evidence.setfChainTime(LocalDateTime.now().plusSeconds(15));

        evidenceLogMapper.insert(evidence);
        return evidence;
    }

    @Override
    public boolean verifyEvidence(String txHash) {
        LambdaQueryWrapper<TbEvidenceLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbEvidenceLog::getfTxHash, txHash);
        TbEvidenceLog evidence = evidenceLogMapper.selectOne(wrapper);

        if (evidence == null) {
            return false;
        }

        // 验证数据完整性
        String computedHash = computeHash(evidence.getfEvidenceData());
        return computedHash.equals(evidence.getfTxHash());
    }

    @Override
    public IPage<TbDataConsumeLog> getDataConsumePage(int currentPage, int pageSize,
            String contractId, String tenantId) {
        Page<TbDataConsumeLog> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbDataConsumeLog> wrapper = new LambdaQueryWrapper<>();
        if (contractId != null && !contractId.isEmpty()) {
            wrapper.eq(TbDataConsumeLog::getfContractId, contractId);
        }
        if (tenantId != null && !tenantId.isEmpty()) {
            wrapper.eq(TbDataConsumeLog::getfTenantId, tenantId);
        }
        wrapper.orderByDesc(TbDataConsumeLog::getfCreateTime);
        return dataConsumeLogMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public TbDataConsumeLog recordDataConsume(DataConsumeDTO dto) {
        TbDataConsumeLog consumeLog = new TbDataConsumeLog();
        consumeLog.setfId(UUID.randomUUID().toString().replace("-", ""));
        consumeLog.setfContractId(dto.getContractId());
        consumeLog.setfOrderId(dto.getOrderId());
        consumeLog.setfProductId(dto.getProductId());
        consumeLog.setfConsumerTenantId(dto.getConsumerTenantId());
        consumeLog.setfProviderTenantId(dto.getProviderTenantId());
        consumeLog.setfConsumeType(dto.getConsumeType());
        consumeLog.setfApiEndpoint(dto.getApiEndpoint());
        consumeLog.setfApiCount(dto.getApiCount());
        consumeLog.setfDataVolume(dto.getDataVolume());
        consumeLog.setfTxHash(dto.getTxHash());
        consumeLog.setfTenantId(dto.getTenantId());
        consumeLog.setfCreateTime(LocalDateTime.now());
        consumeLog.setfConsumeTime(LocalDateTime.now());

        dataConsumeLogMapper.insert(consumeLog);
        return consumeLog;
    }

    @Override
    public IPage<TbOperationLog> getOperationLogPage(int currentPage, int pageSize,
            String userId, String module) {
        Page<TbOperationLog> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null && !userId.isEmpty()) {
            wrapper.eq(TbOperationLog::getfUserId, userId);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(TbOperationLog::getfModule, module);
        }
        wrapper.orderByDesc(TbOperationLog::getfCreateTime);
        return operationLogMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void recordOperationLog(TbOperationLog log) {
        if (log.getfId() == null) {
            log.setfId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (log.getfCreateTime() == null) {
            log.setfCreateTime(LocalDateTime.now());
        }
        operationLogMapper.insert(log);
    }

    /**
     * 生成交易哈希（模拟）
     */
    private String generateTxHash(EvidenceLogDTO dto) {
        String data = dto.getEvidenceType() + dto.getEvidenceData() +
                      System.currentTimeMillis() + UUID.randomUUID().toString();
        return computeHash(data);
    }

    /**
     * 生成区块哈希（模拟）
     */
    private String generateBlockHash(String txHash) {
        String data = txHash + System.currentTimeMillis();
        return computeHash(data);
    }

    /**
     * 计算SHA-256哈希
     */
    private String computeHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("哈希计算失败", e);
        }
    }
}