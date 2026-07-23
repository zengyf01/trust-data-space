package com.tds.datar.service.dataflow;

import com.tds.datar.common.exception.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据同步服务实现
 */
@Service
public class DataSyncServiceImpl implements IDataSyncService {

    private final Map<String, DataSyncDTO> syncTaskCache = new HashMap<>();
    private final Map<String, List<Map<String, Object>>> syncHistoryCache = new HashMap<>();

    @Override
    public DataSyncDTO createSyncTask(DataSyncDTO dto) {
        String syncId = UUID.randomUUID().toString().replace("-", "");
        dto.setSyncId(syncId);
        dto.setStatus("CREATED");
        syncTaskCache.put(syncId, dto);
        return dto;
    }

    @Override
    public Map<String, Object> executeSync(String syncId) {
        DataSyncDTO dto = syncTaskCache.get(syncId);
        if (dto == null) {
            throw new BusinessException("同步任务不存在");
        }

        dto.setStatus("RUNNING");

        // 模拟同步执行
        Map<String, Object> result = new HashMap<>();
        result.put("syncId", syncId);
        result.put("status", "COMPLETED");
        result.put("startTime", LocalDateTime.now());
        result.put("endTime", LocalDateTime.now().plusSeconds(10));
        result.put("recordsSynced", 1000);
        result.put("recordsFailed", 0);
        result.put("duration", 10000);

        dto.setStatus("COMPLETED");

        // 记录历史
        List<Map<String, Object>> history = syncHistoryCache.computeIfAbsent(syncId, k -> new ArrayList<>());
        Map<String, Object> record = new HashMap<>();
        record.put("executeTime", LocalDateTime.now());
        record.put("recordsSynced", 1000);
        record.put("status", "SUCCESS");
        history.add(0, record);

        return result;
    }

    @Override
    public void stopSync(String syncId) {
        DataSyncDTO dto = syncTaskCache.get(syncId);
        if (dto == null) {
            throw new BusinessException("同步任务不存在");
        }
        dto.setStatus("STOPPED");
    }

    @Override
    public Map<String, Object> getSyncStatus(String syncId) {
        DataSyncDTO dto = syncTaskCache.get(syncId);
        if (dto == null) {
            throw new BusinessException("同步任务不存在");
        }

        Map<String, Object> status = new HashMap<>();
        status.put("syncId", syncId);
        status.put("status", dto.getStatus());
        status.put("sourceTable", dto.getSourceTable());
        status.put("targetTable", dto.getTargetTable());
        status.put("syncType", dto.getSyncType());
        return status;
    }

    @Override
    public List<Map<String, Object>> getSyncHistory(String syncId, int limit) {
        List<Map<String, Object>> history = syncHistoryCache.get(syncId);
        if (history == null) {
            return Collections.emptyList();
        }
        return history.stream().limit(limit).toList();
    }
}