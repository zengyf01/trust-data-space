package com.tds.service.billing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.common.enums.BillStatus;
import com.tds.common.enums.BillingModelType;
import com.tds.common.exception.BusinessException;
import com.tds.dal.entity.TbBill;
import com.tds.dal.entity.TbBillingTemplate;
import com.tds.dal.entity.TbProductPricing;
import com.tds.dal.entity.TbUsageRecord;
import com.tds.dal.mapper.TbBillMapper;
import com.tds.dal.mapper.TbBillingTemplateMapper;
import com.tds.dal.mapper.TbProductPricingMapper;
import com.tds.dal.mapper.TbUsageRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 计费服务实现
 */
@Service
public class BillingServiceImpl implements IBillingService {

    @Autowired
    private TbBillingTemplateMapper templateMapper;

    @Autowired
    private TbProductPricingMapper pricingMapper;

    @Autowired
    private TbUsageRecordMapper usageRecordMapper;

    @Autowired
    private TbBillMapper billMapper;

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    // ==================== 计费模板管理 ====================

    @Override
    public IPage<TbBillingTemplate> getTemplatePage(int currentPage, int pageSize,
            String templateName, String billingModel) {
        Page<TbBillingTemplate> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbBillingTemplate> wrapper = new LambdaQueryWrapper<>();
        if (templateName != null && !templateName.isEmpty()) {
            wrapper.like(TbBillingTemplate::getfTemplateName, templateName);
        }
        if (billingModel != null && !billingModel.isEmpty()) {
            wrapper.eq(TbBillingTemplate::getfBillingModel, billingModel);
        }
        wrapper.orderByDesc(TbBillingTemplate::getfCreateTime);
        return templateMapper.selectPage(page, wrapper);
    }

    @Override
    public TbBillingTemplate getTemplateById(String id) {
        return templateMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbBillingTemplate createTemplate(BillingTemplateDTO dto) {
        TbBillingTemplate template = new TbBillingTemplate();
        template.setfId(UUID.randomUUID().toString().replace("-", ""));
        template.setfTemplateCode("BT" + System.currentTimeMillis());
        template.setfTemplateName(dto.getTemplateName());
        template.setfBillingModel(dto.getBillingModel());
        template.setfBasePrice(dto.getBasePrice());
        template.setfUnitPrice(dto.getUnitPrice());
        template.setfUnit(dto.getUnit());
        template.setfFreeQuota(dto.getFreeQuota() != null ? dto.getFreeQuota() : 0);
        template.setfDescription(dto.getDescription());
        template.setfTenantId(dto.getTenantId());
        template.setfCreateTime(LocalDateTime.now());
        template.setfUpdateTime(LocalDateTime.now());
        template.setfDeleteMark(0);

        templateMapper.insert(template);
        return template;
    }

    @Override
    @Transactional
    public TbBillingTemplate updateTemplate(String id, BillingTemplateDTO dto) {
        TbBillingTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("计费模板不存在");
        }
        template.setfTemplateName(dto.getTemplateName());
        template.setfBillingModel(dto.getBillingModel());
        template.setfBasePrice(dto.getBasePrice());
        template.setfUnitPrice(dto.getUnitPrice());
        template.setfUnit(dto.getUnit());
        if (dto.getFreeQuota() != null) {
            template.setfFreeQuota(dto.getFreeQuota());
        }
        template.setfDescription(dto.getDescription());
        template.setfUpdateTime(LocalDateTime.now());
        templateMapper.updateById(template);
        return template;
    }

    @Override
    @Transactional
    public void deleteTemplate(String id) {
        TbBillingTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("计费模板不存在");
        }
        template.setfDeleteMark(1);
        template.setfUpdateTime(LocalDateTime.now());
        templateMapper.updateById(template);
    }

    // ==================== 产品定价管理 ====================

    @Override
    public IPage<TbProductPricing> getPricingPage(int currentPage, int pageSize, String productId) {
        Page<TbProductPricing> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbProductPricing> wrapper = new LambdaQueryWrapper<>();
        if (productId != null && !productId.isEmpty()) {
            wrapper.eq(TbProductPricing::getfProductId, productId);
        }
        wrapper.orderByDesc(TbProductPricing::getfCreateTime);
        return pricingMapper.selectPage(page, wrapper);
    }

    @Override
    public TbProductPricing getPricingById(String id) {
        return pricingMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbProductPricing createPricing(ProductPricingDTO dto) {
        TbProductPricing pricing = new TbProductPricing();
        pricing.setfId(UUID.randomUUID().toString().replace("-", ""));
        pricing.setfProductId(dto.getProductId());
        pricing.setfTemplateId(dto.getTemplateId());
        pricing.setfBillingModel(dto.getBillingModel());
        pricing.setfPrice(dto.getPrice());
        pricing.setfUnitPrice(dto.getUnitPrice());
        pricing.setfUnit(dto.getUnit());
        pricing.setfMinQuota(dto.getMinQuota() != null ? dto.getMinQuota() : 1);
        pricing.setfMaxQuota(dto.getMaxQuota());
        pricing.setfStartTime(dto.getStartTime() != null ? dto.getStartTime() : LocalDateTime.now());
        pricing.setfEndTime(dto.getEndTime());
        pricing.setfTenantId(dto.getTenantId());
        pricing.setfCreateTime(LocalDateTime.now());
        pricing.setfUpdateTime(LocalDateTime.now());
        pricing.setfDeleteMark(0);

        pricingMapper.insert(pricing);
        return pricing;
    }

    @Override
    @Transactional
    public TbProductPricing updatePricing(String id, ProductPricingDTO dto) {
        TbProductPricing pricing = pricingMapper.selectById(id);
        if (pricing == null) {
            throw new BusinessException("产品定价不存在");
        }
        pricing.setfTemplateId(dto.getTemplateId());
        pricing.setfBillingModel(dto.getBillingModel());
        pricing.setfPrice(dto.getPrice());
        pricing.setfUnitPrice(dto.getUnitPrice());
        pricing.setfUnit(dto.getUnit());
        if (dto.getMinQuota() != null) {
            pricing.setfMinQuota(dto.getMinQuota());
        }
        if (dto.getMaxQuota() != null) {
            pricing.setfMaxQuota(dto.getMaxQuota());
        }
        if (dto.getStartTime() != null) {
            pricing.setfStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            pricing.setfEndTime(dto.getEndTime());
        }
        pricing.setfUpdateTime(LocalDateTime.now());
        pricingMapper.updateById(pricing);
        return pricing;
    }

    @Override
    @Transactional
    public void deletePricing(String id) {
        TbProductPricing pricing = pricingMapper.selectById(id);
        if (pricing == null) {
            throw new BusinessException("产品定价不存在");
        }
        pricing.setfDeleteMark(1);
        pricing.setfUpdateTime(LocalDateTime.now());
        pricingMapper.updateById(pricing);
    }

    @Override
    public TbProductPricing getProductCurrentPricing(String productId) {
        LambdaQueryWrapper<TbProductPricing> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbProductPricing::getfProductId, productId)
                .le(TbProductPricing::getfStartTime, LocalDateTime.now())
                .ge(TbProductPricing::getfEndTime, LocalDateTime.now())
                .orderByDesc(TbProductPricing::getfCreateTime)
                .last("LIMIT 1");
        return pricingMapper.selectOne(wrapper);
    }

    // ==================== 用量记录管理 ====================

    @Override
    public IPage<TbUsageRecord> getUsagePage(int currentPage, int pageSize,
            String tenantId, String productId, String usagePeriod) {
        Page<TbUsageRecord> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbUsageRecord> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null && !tenantId.isEmpty()) {
            wrapper.eq(TbUsageRecord::getfTenantId, tenantId);
        }
        if (productId != null && !productId.isEmpty()) {
            wrapper.eq(TbUsageRecord::getfProductId, productId);
        }
        if (usagePeriod != null && !usagePeriod.isEmpty()) {
            wrapper.eq(TbUsageRecord::getfUsagePeriod, usagePeriod);
        }
        wrapper.orderByDesc(TbUsageRecord::getfUsageTime);
        return usageRecordMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public TbUsageRecord recordUsage(UsageRecordDTO dto) {
        TbUsageRecord record = new TbUsageRecord();
        record.setfId(UUID.randomUUID().toString().replace("-", ""));
        record.setfTenantId(dto.getTenantId());
        record.setfProductId(dto.getProductId());
        record.setfOrderId(dto.getOrderId());
        record.setfUsageType(dto.getUsageType());
        record.setfUsageCount(dto.getUsageCount());
        record.setfUnitPrice(dto.getUnitPrice());
        record.setfAmount(dto.getAmount());
        record.setfUsagePeriod(dto.getUsagePeriod() != null ?
                dto.getUsagePeriod() : LocalDateTime.now().format(PERIOD_FORMATTER));
        record.setfUsageTime(dto.getUsageTime() != null ? dto.getUsageTime() : LocalDateTime.now());
        record.setfDescription(dto.getDescription());
        record.setfCreateTime(LocalDateTime.now());

        usageRecordMapper.insert(record);
        return record;
    }

    @Override
    public Map<String, Object> getUsageSummary(String tenantId, String billingPeriod) {
        LambdaQueryWrapper<TbUsageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbUsageRecord::getfTenantId, tenantId);
        if (billingPeriod != null && !billingPeriod.isEmpty()) {
            wrapper.eq(TbUsageRecord::getfUsagePeriod, billingPeriod);
        }

        List<TbUsageRecord> records = usageRecordMapper.selectList(wrapper);

        Map<String, Object> summary = new HashMap<>();
        long totalApiCalls = 0;
        long totalDataVolume = 0;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (TbUsageRecord record : records) {
            if ("API_CALL".equals(record.getfUsageType())) {
                totalApiCalls += record.getfUsageCount();
            } else if ("DATA_VOLUME".equals(record.getfUsageType())) {
                totalDataVolume += record.getfUsageCount();
            }
            if (record.getfAmount() != null) {
                totalCost = totalCost.add(record.getfAmount());
            }
        }

        summary.put("totalApiCalls", totalApiCalls);
        summary.put("totalDataVolume", totalDataVolume);
        summary.put("totalCost", totalCost);
        summary.put("recordCount", records.size());

        return summary;
    }

    // ==================== 账单管理 ====================

    @Override
    public IPage<TbBill> getBillPage(int currentPage, int pageSize,
            String tenantId, String billingPeriod, Integer status) {
        Page<TbBill> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbBill> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null && !tenantId.isEmpty()) {
            wrapper.eq(TbBill::getfTenantId, tenantId);
        }
        if (billingPeriod != null && !billingPeriod.isEmpty()) {
            wrapper.eq(TbBill::getfBillingPeriod, billingPeriod);
        }
        if (status != null) {
            wrapper.eq(TbBill::getfStatus, status);
        }
        wrapper.orderByDesc(TbBill::getfCreateTime);
        return billMapper.selectPage(page, wrapper);
    }

    @Override
    public TbBill getBillById(String id) {
        return billMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbBill generateBill(String tenantId, String billingPeriod) {
        // 查询该租户该账期的用量汇总
        Map<String, Object> usageSummary = getUsageSummary(tenantId, billingPeriod);
        BigDecimal totalAmount = (BigDecimal) usageSummary.get("totalCost");

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("该账期无用量记录，无需生成账单");
        }

        TbBill bill = new TbBill();
        bill.setfId(UUID.randomUUID().toString().replace("-", ""));
        bill.setfBillCode("BILL" + System.currentTimeMillis());
        bill.setfTenantId(tenantId);
        bill.setfBillingPeriod(billingPeriod);
        bill.setfTotalAmount(totalAmount);
        bill.setfPaidAmount(BigDecimal.ZERO);
        bill.setfPendingAmount(totalAmount);
        bill.setfStatus(BillStatus.PENDING.getCode());
        bill.setfDueDate(LocalDateTime.now().plusDays(30));
        bill.setfCreateTime(LocalDateTime.now());
        bill.setfUpdateTime(LocalDateTime.now());
        bill.setfDeleteMark(0);

        billMapper.insert(bill);
        return bill;
    }

    @Override
    @Transactional
    public TbBill confirmBill(String id) {
        TbBill bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        if (bill.getfStatus() != BillStatus.PENDING.getCode()) {
            throw new BusinessException("账单状态不是待结算，无法确认");
        }
        bill.setfStatus(BillStatus.CONFIRMED.getCode());
        bill.setfUpdateTime(LocalDateTime.now());
        billMapper.updateById(bill);
        return bill;
    }

    @Override
    @Transactional
    public TbBill payBill(String id, String paymentMethod, BigDecimal amount) {
        TbBill bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        if (amount.compareTo(bill.getfPendingAmount()) > 0) {
            throw new BusinessException("支付金额超过待支付金额");
        }

        BigDecimal newPaidAmount = bill.getfPaidAmount().add(amount);
        BigDecimal newPendingAmount = bill.getfTotalAmount().subtract(newPaidAmount);

        bill.setfPaidAmount(newPaidAmount);
        bill.setfPendingAmount(newPendingAmount);
        bill.setfPaymentMethod(paymentMethod);

        if (newPendingAmount.compareTo(BigDecimal.ZERO) == 0) {
            bill.setfStatus(BillStatus.PAID.getCode());
            bill.setfPaidTime(LocalDateTime.now());
        }

        bill.setfUpdateTime(LocalDateTime.now());
        billMapper.updateById(bill);
        return bill;
    }

    @Override
    @Transactional
    public void deleteBill(String id) {
        TbBill bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        bill.setfDeleteMark(1);
        bill.setfUpdateTime(LocalDateTime.now());
        billMapper.updateById(bill);
    }

    // ==================== 费用计算 ====================

    @Override
    public BigDecimal calculateCost(String productId, Long usageCount,
            String billingModel, BigDecimal unitPrice) {
        if (usageCount == null || usageCount <= 0) {
            return BigDecimal.ZERO;
        }

        if (unitPrice == null) {
            unitPrice = BigDecimal.ZERO;
        }

        // 获取产品定价信息
        TbProductPricing pricing = getProductCurrentPricing(productId);
        if (pricing != null) {
            if (pricing.getfUnitPrice() != null) {
                unitPrice = pricing.getfUnitPrice();
            }
            if (pricing.getfTemplateId() != null) {
                TbBillingTemplate template = getTemplateById(pricing.getfTemplateId());
                if (template != null && template.getfFreeQuota() != null && template.getfFreeQuota() > 0) {
                    usageCount = Math.max(0, usageCount - template.getfFreeQuota());
                }
            }
        }

        return unitPrice.multiply(BigDecimal.valueOf(usageCount));
    }
}