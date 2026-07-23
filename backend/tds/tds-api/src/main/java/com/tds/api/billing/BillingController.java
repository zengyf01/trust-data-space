package com.tds.api.billing;

import com.tds.common.core.ApiResponse;
import com.tds.service.billing.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 计费管理
 */
@RestController
@RequestMapping("/billing")
public class BillingController {

    @Autowired
    private IBillingService billingService;

    // ==================== 计费模板管理 ====================

    @GetMapping("/template/page")
    public ApiResponse<?> getTemplatePage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String billingModel) {
        return ApiResponse.success(billingService.getTemplatePage(currentPage, pageSize, templateName, billingModel));
    }

    @GetMapping("/template/{id}")
    public ApiResponse<?> getTemplateById(@PathVariable String id) {
        return ApiResponse.success(billingService.getTemplateById(id));
    }

    @PostMapping("/template")
    public ApiResponse<?> createTemplate(@RequestBody BillingTemplateDTO dto) {
        return ApiResponse.success(billingService.createTemplate(dto));
    }

    @PutMapping("/template/{id}")
    public ApiResponse<?> updateTemplate(@PathVariable String id, @RequestBody BillingTemplateDTO dto) {
        return ApiResponse.success(billingService.updateTemplate(id, dto));
    }

    @DeleteMapping("/template/{id}")
    public ApiResponse<?> deleteTemplate(@PathVariable String id) {
        billingService.deleteTemplate(id);
        return ApiResponse.success(null);
    }

    // ==================== 产品定价管理 ====================

    @GetMapping("/pricing/page")
    public ApiResponse<?> getPricingPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String productId) {
        return ApiResponse.success(billingService.getPricingPage(currentPage, pageSize, productId));
    }

    @GetMapping("/pricing/{id}")
    public ApiResponse<?> getPricingById(@PathVariable String id) {
        return ApiResponse.success(billingService.getPricingById(id));
    }

    @GetMapping("/pricing/product/{productId}")
    public ApiResponse<?> getProductCurrentPricing(@PathVariable String productId) {
        return ApiResponse.success(billingService.getProductCurrentPricing(productId));
    }

    @PostMapping("/pricing")
    public ApiResponse<?> createPricing(@RequestBody ProductPricingDTO dto) {
        return ApiResponse.success(billingService.createPricing(dto));
    }

    @PutMapping("/pricing/{id}")
    public ApiResponse<?> updatePricing(@PathVariable String id, @RequestBody ProductPricingDTO dto) {
        return ApiResponse.success(billingService.updatePricing(id, dto));
    }

    @DeleteMapping("/pricing/{id}")
    public ApiResponse<?> deletePricing(@PathVariable String id) {
        billingService.deletePricing(id);
        return ApiResponse.success(null);
    }

    // ==================== 用量记录管理 ====================

    @GetMapping("/usage/page")
    public ApiResponse<?> getUsagePage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String usagePeriod) {
        return ApiResponse.success(billingService.getUsagePage(currentPage, pageSize, tenantId, productId, usagePeriod));
    }

    @PostMapping("/usage")
    public ApiResponse<?> recordUsage(@RequestBody UsageRecordDTO dto) {
        return ApiResponse.success(billingService.recordUsage(dto));
    }

    @GetMapping("/usage/summary")
    public ApiResponse<?> getUsageSummary(
            @RequestParam String tenantId,
            @RequestParam(required = false) String billingPeriod) {
        return ApiResponse.success(billingService.getUsageSummary(tenantId, billingPeriod));
    }

    // ==================== 账单管理 ====================

    @GetMapping("/bill/page")
    public ApiResponse<?> getBillPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String billingPeriod,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(billingService.getBillPage(currentPage, pageSize, tenantId, billingPeriod, status));
    }

    @GetMapping("/bill/{id}")
    public ApiResponse<?> getBillById(@PathVariable String id) {
        return ApiResponse.success(billingService.getBillById(id));
    }

    @PostMapping("/bill/generate")
    public ApiResponse<?> generateBill(
            @RequestParam String tenantId,
            @RequestParam String billingPeriod) {
        return ApiResponse.success(billingService.generateBill(tenantId, billingPeriod));
    }

    @PostMapping("/bill/{id}/confirm")
    public ApiResponse<?> confirmBill(@PathVariable String id) {
        return ApiResponse.success(billingService.confirmBill(id));
    }

    @PostMapping("/bill/{id}/pay")
    public ApiResponse<?> payBill(
            @PathVariable String id,
            @RequestParam String paymentMethod,
            @RequestParam BigDecimal amount) {
        return ApiResponse.success(billingService.payBill(id, paymentMethod, amount));
    }

    @DeleteMapping("/bill/{id}")
    public ApiResponse<?> deleteBill(@PathVariable String id) {
        billingService.deleteBill(id);
        return ApiResponse.success(null);
    }

    // ==================== 费用计算 ====================

    @GetMapping("/calculate")
    public ApiResponse<?> calculateCost(
            @RequestParam String productId,
            @RequestParam Long usageCount,
            @RequestParam(required = false) String billingModel,
            @RequestParam(required = false) BigDecimal unitPrice) {
        return ApiResponse.success(billingService.calculateCost(productId, usageCount, billingModel, unitPrice));
    }
}