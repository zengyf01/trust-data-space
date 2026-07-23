package com.tds.dos.service.workorder.history;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.dos.dal.entity.TbWorkOrderHistory;
import com.tds.dos.dal.mapper.TbWorkOrderHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 工单历史服务实现
 */
@Service
public class WorkOrderHistoryServiceImpl implements IWorkOrderHistoryService {

    @Autowired
    private TbWorkOrderHistoryMapper historyMapper;

    @Override
    public IPage<TbWorkOrderHistory> getHistoryPage(int currentPage, int pageSize, String workOrderId) {
        Page<TbWorkOrderHistory> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbWorkOrderHistory> wrapper = new LambdaQueryWrapper<>();
        if (workOrderId != null && !workOrderId.isEmpty()) {
            wrapper.eq(TbWorkOrderHistory::getfWorkOrderId, workOrderId);
        }
        wrapper.orderByDesc(TbWorkOrderHistory::getfCreateTime);
        return historyMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void recordHistory(WorkOrderHistoryDTO dto) {
        TbWorkOrderHistory history = new TbWorkOrderHistory();
        history.setfId(UUID.randomUUID().toString().replace("-", ""));
        history.setfWorkOrderId(dto.getWorkOrderId());
        history.setfWorkOrderCode(dto.getWorkOrderCode());
        history.setfOperation(dto.getOperation());
        history.setfOperator(dto.getOperator());
        history.setfOperatorId(dto.getOperatorId());
        history.setfFromStatus(dto.getFromStatus());
        history.setfToStatus(dto.getToStatus());
        history.setfOperationDetail(dto.getOperationDetail());
        history.setfTenantId(dto.getTenantId());
        history.setfCreateTime(LocalDateTime.now());

        historyMapper.insert(history);
    }

    @Override
    public IPage<TbWorkOrderHistory> getFullHistory(String workOrderId) {
        return getHistoryPage(1, 100, workOrderId);
    }
}