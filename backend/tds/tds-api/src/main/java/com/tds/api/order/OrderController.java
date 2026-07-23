package com.tds.api.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.common.core.ApiResponse;
import com.tds.common.core.PageResult;
import com.tds.dal.entity.TbOrderHistory;
import com.tds.dal.entity.TbTradingOrder;
import com.tds.service.order.OrderCreateDTO;
import com.tds.service.order.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 交易订单API控制器
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderServiceImpl orderService;

    @PostMapping("/page")
    public ApiResponse<PageResult<TbTradingOrder>> getOrderPage(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) Integer orderStatus) {

        IPage<TbTradingOrder> page = orderService.getOrderPage(
                pageNumber, pageSize, orderCode, orderStatus);
        PageResult<TbTradingOrder> result = PageResult.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<TbTradingOrder> getOrder(@PathVariable String id) {
        TbTradingOrder order = orderService.getOrderById(id);
        return ApiResponse.success(order);
    }

    @GetMapping("/code/{orderCode}")
    public ApiResponse<TbTradingOrder> getOrderByCode(@PathVariable String orderCode) {
        TbTradingOrder order = orderService.getOrderByCode(orderCode);
        return ApiResponse.success(order);
    }

    @PostMapping
    public ApiResponse<TbTradingOrder> createOrder(@RequestBody OrderCreateDTO dto) {
        TbTradingOrder order = orderService.createOrder(dto);
        return ApiResponse.success(order);
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<TbTradingOrder> approveOrder(
            @PathVariable String id,
            @RequestParam(required = false) String approver,
            @RequestParam(required = false) String remark) throws Exception {
        TbTradingOrder order = orderService.approveOrder(id, approver, remark);
        return ApiResponse.success(order);
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<TbTradingOrder> rejectOrder(
            @PathVariable String id,
            @RequestParam String reason) {
        TbTradingOrder order = orderService.rejectOrder(id, reason);
        return ApiResponse.success(order);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<TbTradingOrder> cancelOrder(@PathVariable String id) {
        TbTradingOrder order = orderService.cancelOrder(id);
        return ApiResponse.success(order);
    }

    @GetMapping("/{id}/history")
    public ApiResponse<List<TbOrderHistory>> getOrderHistory(@PathVariable String id) {
        TbTradingOrder order = orderService.getOrderById(id);
        List<TbOrderHistory> history = orderService.getOrderHistory(order.getId());
        return ApiResponse.success(history);
    }

    @PutMapping("/{id}/deliveryApi")
    public ApiResponse<TbTradingOrder> updateDeliveryApiInfo(
            @PathVariable String id,
            @RequestParam String deliveryApiInfo) {
        TbTradingOrder order = orderService.updateDeliveryApiInfo(id, deliveryApiInfo);
        return ApiResponse.success(order);
    }
}