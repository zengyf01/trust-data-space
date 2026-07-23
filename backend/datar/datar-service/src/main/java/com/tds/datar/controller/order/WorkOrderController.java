package com.tds.datar.controller.order;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.service.order.OrderService;
import com.tds.datar.service.order.WorkOrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 工单管理
 */
@RestController
@RequestMapping("/workOrder")
public class WorkOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/page")
    public ApiResponse<?> getWorkOrderPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(orderService.getWorkOrderPage(currentPage, pageSize, orderId, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getWorkOrderById(@PathVariable String id) {
        return ApiResponse.success(orderService.getWorkOrderById(id));
    }

    @PostMapping
    public ApiResponse<?> createWorkOrder(@RequestBody WorkOrderDTO dto) {
        return ApiResponse.success(orderService.createWorkOrder(dto));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<?> updateWorkOrderStatus(
            @PathVariable String id,
            @RequestParam Integer status,
            @RequestParam(required = false) String outputResult,
            @RequestParam(required = false) String errorMessage) {
        return ApiResponse.success(orderService.updateWorkOrderStatus(id, status, outputResult, errorMessage));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteWorkOrder(@PathVariable String id) {
        orderService.deleteWorkOrder(id);
        return ApiResponse.success(null);
    }
}