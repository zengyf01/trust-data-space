package com.tds.datar.service.dataflow;

import java.util.List;
import java.util.Map;

/**
 * 数据同步服务接口
 */
public interface IDataSyncService {

    /**
     * 创建同步任务
     */
    DataSyncDTO createSyncTask(DataSyncDTO dto);

    /**
     * 执行同步
     */
    Map<String, Object> executeSync(String syncId);

    /**
     * 停止同步
     */
    void stopSync(String syncId);

    /**
     * 获取同步状态
     */
    Map<String, Object> getSyncStatus(String syncId);

    /**
     * 同步历史记录
     */
    List<Map<String, Object>> getSyncHistory(String syncId, int limit);
}