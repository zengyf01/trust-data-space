package com.tds.datar.controller.order;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.service.order.OrderDTO;
import com.tds.datar.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/page")
    public ApiResponse<?> getOrderPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String buyerSpaceId,
            @RequestParam(required = false) String sellerSpaceId) {
        return ApiResponse.success(orderService.getOrderPage(currentPage, pageSize, orderCode, status, buyerSpaceId, sellerSpaceId));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getOrderById(@PathVariable String id) {
        return ApiResponse.success(orderService.getOrderById(id));
    }

    @PostMapping
    public ApiResponse<?> createOrder(@RequestBody OrderDTO dto) {
        return ApiResponse.success(orderService.createOrder(dto));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<?> updateOrderStatus(
            @PathVariable String id,
            @RequestParam Integer status) {
        return ApiResponse.success(orderService.updateOrderStatus(id, status));
    }

    @PostMapping("/{id}/sign")
    public ApiResponse<?> signOrder(
            @PathVariable String id,
            @RequestParam String contractId) {
        return ApiResponse.success(orderService.signOrder(id, contractId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return ApiResponse.success(null);
    }
}