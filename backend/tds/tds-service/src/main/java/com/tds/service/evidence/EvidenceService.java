package com.tds.service.evidence;

import com.tds.common.util.SM2Util;
import com.tds.dal.entity.TbDigitalContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * 区块链存证服务（模拟实现）
 * 实际生产环境应接入FISCO-BCOS区块链
 */
@Service
public class EvidenceService {

    private static final Logger log = LoggerFactory.getLogger(EvidenceService.class);

    /**
     * 记录合约创建存证
     */
    public void recordContractCreate(TbDigitalContract contract) throws Exception {
        EvidenceRecord record = new EvidenceRecord();
        record.setId(UUID.randomUUID().toString().replace("-", ""));
        record.setCategory("CONTRACT_CREATE");
        record.setDescription("数字合约创建");
        record.setBusinessId(contract.getId());
        record.setBusinessType("DIGITAL_CONTRACT");
        record.setEvidenceHash(SM2Util.hash(contract.getContractJson()));
        record.setOperator(contract.getProviderInstitutionName());
        record.setOperatorTime(Instant.now().toEpochMilli());
        record.setChainName("FISCO-MOCK");
        record.setTxHash("0x" + UUID.randomUUID().toString().replace("-", ""));
        record.setBlockNumber(System.currentTimeMillis() % 1000000);

        log.info("【模拟区块链存证】合约创建: category={}, businessId={}, txHash={}, blockNumber={}",
                record.getCategory(), record.getBusinessId(), record.getTxHash(), record.getBlockNumber());

        // TODO: 实际生产环境调用FISCO-BCOS接口上链
        // evidenceContract.submitEvidence(record);
    }

    /**
     * 记录合约签名存证
     */
    public void recordContractSign(TbDigitalContract contract, String signerType) throws Exception {
        EvidenceRecord record = new EvidenceRecord();
        record.setId(UUID.randomUUID().toString().replace("-", ""));
        record.setCategory("CONTRACT_SIGN");
        record.setDescription("数字合约" + signerType + "签名");
        record.setBusinessId(contract.getId());
        record.setBusinessType("DIGITAL_CONTRACT");
        record.setEvidenceHash(SM2Util.hash(contract.getContractCode() + signerType));
        record.setOperator(signerType.equals("PROVIDER") ? contract.getProviderInstitutionName() : contract.getUseInstitutionName());
        record.setOperatorTime(Instant.now().toEpochMilli());
        record.setChainName("FISCO-MOCK");
        record.setTxHash("0x" + UUID.randomUUID().toString().replace("-", ""));
        record.setBlockNumber(System.currentTimeMillis() % 1000000);

        log.info("【模拟区块链存证】合约签名: signerType={}, businessId={}, txHash={}, blockNumber={}",
                signerType, record.getBusinessId(), record.getTxHash(), record.getBlockNumber());
    }

    /**
     * 记录合约拒绝存证
     */
    public void recordContractReject(TbDigitalContract contract, String reason) throws Exception {
        EvidenceRecord record = new EvidenceRecord();
        record.setId(UUID.randomUUID().toString().replace("-", ""));
        record.setCategory("CONTRACT_REJECT");
        record.setDescription("数字合约拒绝: " + reason);
        record.setBusinessId(contract.getId());
        record.setBusinessType("DIGITAL_CONTRACT");
        record.setEvidenceHash(SM2Util.hash(contract.getContractCode() + "REJECTED"));
        record.setOperator(contract.getUseInstitutionName());
        record.setOperatorTime(Instant.now().toEpochMilli());
        record.setChainName("FISCO-MOCK");
        record.setTxHash("0x" + UUID.randomUUID().toString().replace("-", ""));
        record.setBlockNumber(System.currentTimeMillis() % 1000000);

        log.info("【模拟区块链存证】合约拒绝: businessId={}, reason={}", record.getBusinessId(), reason);
    }

    /**
     * 记录合约终止存证
     */
    public void recordContractTerminate(TbDigitalContract contract, String reason) throws Exception {
        EvidenceRecord record = new EvidenceRecord();
        record.setId(UUID.randomUUID().toString().replace("-", ""));
        record.setCategory("CONTRACT_TERMINATE");
        record.setDescription("数字合约终止: " + reason);
        record.setBusinessId(contract.getId());
        record.setBusinessType("DIGITAL_CONTRACT");
        record.setEvidenceHash(SM2Util.hash(contract.getContractCode() + "TERMINATED"));
        record.setOperator(contract.getProviderInstitutionName());
        record.setOperatorTime(Instant.now().toEpochMilli());
        record.setChainName("FISCO-MOCK");
        record.setTxHash("0x" + UUID.randomUUID().toString().replace("-", ""));
        record.setBlockNumber(System.currentTimeMillis() % 1000000);

        log.info("【模拟区块链存证】合约终止: businessId={}, reason={}", record.getBusinessId(), reason);
    }

    /**
     * 存证记录实体
     */
    public static class EvidenceRecord {
        private String id;
        private String category;
        private String description;
        private String businessId;
        private String businessType;
        private String evidenceHash;
        private String operator;
        private long operatorTime;
        private String chainName;
        private String txHash;
        private long blockNumber;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getBusinessId() { return businessId; }
        public void setBusinessId(String businessId) { this.businessId = businessId; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public String getEvidenceHash() { return evidenceHash; }
        public void setEvidenceHash(String evidenceHash) { this.evidenceHash = evidenceHash; }
        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }
        public long getOperatorTime() { return operatorTime; }
        public void setOperatorTime(long operatorTime) { this.operatorTime = operatorTime; }
        public String getChainName() { return chainName; }
        public void setChainName(String chainName) { this.chainName = chainName; }
        public String getTxHash() { return txHash; }
        public void setTxHash(String txHash) { this.txHash = txHash; }
        public long getBlockNumber() { return blockNumber; }
        public void setBlockNumber(long blockNumber) { this.blockNumber = blockNumber; }
    }
}