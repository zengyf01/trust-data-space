package com.tds.service.evidence;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dal.entity.TbEvidenceLog;
import com.tds.dal.entity.TbDataConsumeLog;
import com.tds.dal.entity.TbOperationLog;

/**
 * 存证服务接口
 */
public interface IEvidenceService {

    /**
     * 分页查询存证日志
     */
    IPage<TbEvidenceLog> getEvidencePage(int currentPage, int pageSize, String evidenceType, String contractId);

    /**
     * 获取存证详情
     */
    TbEvidenceLog getEvidenceById(String id);

    /**
     * 创建存证（模拟区块链上链）
     */
    TbEvidenceLog createEvidence(EvidenceLogDTO dto);

    /**
     * 验证存证
     */
    boolean verifyEvidence(String txHash);

    /**
     * 分页查询数据消费日志
     */
    IPage<TbDataConsumeLog> getDataConsumePage(int currentPage, int pageSize, String contractId, String tenantId);

    /**
     * 记录数据消费
     */
    TbDataConsumeLog recordDataConsume(DataConsumeDTO dto);

    /**
     * 分页查询操作日志
     */
    IPage<TbOperationLog> getOperationLogPage(int currentPage, int pageSize, String userId, String module);

    /**
     * 记录操作日志
     */
    void recordOperationLog(TbOperationLog log);
}