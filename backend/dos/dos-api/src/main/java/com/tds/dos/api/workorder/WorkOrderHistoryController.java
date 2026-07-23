package com.tds.dos.api.workorder;

import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.dal.entity.TbWorkOrderHistory;
import com.tds.dos.service.workorder.history.IWorkOrderHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 工单历史
 */
@RestController
@RequestMapping("/workOrder/history")
public class WorkOrderHistoryController {

    @Autowired
    private IWorkOrderHistoryService historyService;

    @GetMapping("/page")
    public ApiResponse<?> getHistoryPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam String workOrderId) {
        return ApiResponse.success(historyService.getHistoryPage(currentPage, pageSize, workOrderId));
    }

    @GetMapping("/full")
    public ApiResponse<?> getFullHistory(@RequestParam String workOrderId) {
        return ApiResponse.success(historyService.getFullHistory(workOrderId));
    }
}