package com.tds.service.billing;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dal.entity.TbBill;
import com.tds.dal.entity.TbBillingTemplate;
import com.tds.dal.entity.TbProductPricing;
import com.tds.dal.entity.TbUsageRecord;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 计费服务接口
 */
public interface IBillingService {

    // ==================== 计费模板管理 ====================

    /**
     * 分页查询计费模板
     */
    IPage<TbBillingTemplate> getTemplatePage(int currentPage, int pageSize, String templateName, String billingModel);

    /**
     * 获取计费模板详情
     */
    TbBillingTemplate getTemplateById(String id);

    /**
     * 创建计费模板
     */
    TbBillingTemplate createTemplate(BillingTemplateDTO dto);

    /**
     * 更新计费模板
     */
    TbBillingTemplate updateTemplate(String id, BillingTemplateDTO dto);

    /**
     * 删除计费模板
     */
    void deleteTemplate(String id);

    // ==================== 产品定价管理 ====================

    /**
     * 分页查询产品定价
     */
    IPage<TbProductPricing> getPricingPage(int currentPage, int pageSize, String productId);

    /**
     * 获取产品定价详情
     */
    TbProductPricing getPricingById(String id);

    /**
     * 创建产品定价
     */
    TbProductPricing createPricing(ProductPricingDTO dto);

    /**
     * 更新产品定价
     */
    TbProductPricing updatePricing(String id, ProductPricingDTO dto);

    /**
     * 删除产品定价
     */
    void deletePricing(String id);

    /**
     * 获取产品当前定价
     */
    TbProductPricing getProductCurrentPricing(String productId);

    // ==================== 用量记录管理 ====================

    /**
     * 分页查询用量记录
     */
    IPage<TbUsageRecord> getUsagePage(int currentPage, int pageSize, String tenantId, String productId, String usagePeriod);

    /**
     * 记录用量
     */
    TbUsageRecord recordUsage(UsageRecordDTO dto);

    /**
     * 查询用量汇总
     */
    Map<String, Object> getUsageSummary(String tenantId, String billingPeriod);

    // ==================== 账单管理 ====================

    /**
     * 分页查询账单
     */
    IPage<TbBill> getBillPage(int currentPage, int pageSize, String tenantId, String billingPeriod, Integer status);

    /**
     * 获取账单详情
     */
    TbBill getBillById(String id);

    /**
     * 生成账单
     */
    TbBill generateBill(String tenantId, String billingPeriod);

    /**
     * 确认账单
     */
    TbBill confirmBill(String id);

    /**
     * 支付账单
     */
    TbBill payBill(String id, String paymentMethod, BigDecimal amount);

    /**
     * 删除账单
     */
    void deleteBill(String id);

    // ==================== 费用计算 ====================

    /**
     * 计算费用
     */
    BigDecimal calculateCost(String productId, Long usageCount, String billingModel, BigDecimal unitPrice);
}