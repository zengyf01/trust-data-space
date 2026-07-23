package com.tds.dos.service.workorder.history;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dos.dal.entity.TbWorkOrderHistory;

/**
 * 工单历史服务接口
 */
public interface IWorkOrderHistoryService {

    /**
     * 分页查询工单历史
     */
    IPage<TbWorkOrderHistory> getHistoryPage(int currentPage, int pageSize, String workOrderId);

    /**
     * 记录工单操作历史
     */
    void recordHistory(WorkOrderHistoryDTO dto);

    /**
     * 获取工单完整历史
     */
    IPage<TbWorkOrderHistory> getFullHistory(String workOrderId);
}