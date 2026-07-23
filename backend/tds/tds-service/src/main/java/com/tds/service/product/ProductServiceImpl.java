package com.tds.service.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.common.enums.ProductStatus;
import com.tds.common.exception.BusinessException;
import com.tds.dal.entity.TbDataProduct;
import com.tds.dal.mapper.TbDataProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 数据产品服务实现
 */
@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private TbDataProductMapper productMapper;

    @Override
    public IPage<TbDataProduct> getProductPage(int currentPage, int pageSize,
            String productName, Integer status) {
        Page<TbDataProduct> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbDataProduct> wrapper = new LambdaQueryWrapper<>();
        if (productName != null && !productName.isEmpty()) {
            wrapper.like(TbDataProduct::getProductName, productName);
        }
        if (status != null) {
            wrapper.eq(TbDataProduct::getStatus, status);
        }
        wrapper.orderByDesc(TbDataProduct::getfCreateTime);
        return productMapper.selectPage(page, wrapper);
    }

    @Override
    public TbDataProduct getProductById(String id) {
        return productMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbDataProduct createProduct(ProductCreateDTO dto) {
        TbDataProduct product = new TbDataProduct();
        product.setId(UUID.randomUUID().toString().replace("-", ""));
        product.setProductCode(generateProductCode());
        product.setProductName(dto.getProductName());
        product.setCatalogId(dto.getCatalogId());
        product.setProductDesc(dto.getProductDesc());
        product.setPricingModel(dto.getPricingModel());
        product.setPrice(dto.getPrice());
        product.setStatus(ProductStatus.DRAFT.getCode());
        product.setfTenantId(dto.getTenantId());
        product.setfCreateTime(LocalDateTime.now());
        product.setfUpdateTime(LocalDateTime.now());
        product.setfDeleteMark(0);

        productMapper.insert(product);
        return product;
    }

    @Override
    @Transactional
    public TbDataProduct updateProduct(String id, ProductCreateDTO dto) {
        TbDataProduct product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        product.setProductName(dto.getProductName());
        product.setCatalogId(dto.getCatalogId());
        product.setProductDesc(dto.getProductDesc());
        product.setPricingModel(dto.getPricingModel());
        product.setPrice(dto.getPrice());
        product.setfUpdateTime(LocalDateTime.now());

        productMapper.updateById(product);
        return product;
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        TbDataProduct product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        product.setfDeleteMark(1);
        product.setfUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    @Transactional
    public TbDataProduct submitForAudit(String id) {
        TbDataProduct product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (product.getStatus() != ProductStatus.DRAFT.getCode()
            && product.getStatus() != ProductStatus.REJECTED.getCode()) {
            throw new BusinessException("只有草稿或审核拒绝状态可以提交审核");
        }
        product.setStatus(ProductStatus.PENDING_AUDIT.getCode());
        product.setfUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        return product;
    }

    @Override
    @Transactional
    public TbDataProduct approveProduct(String id) {
        TbDataProduct product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (product.getStatus() != ProductStatus.PENDING_AUDIT.getCode()) {
            throw new BusinessException("只有待审核状态可以审核");
        }
        product.setStatus(ProductStatus.APPROVED.getCode());
        product.setfUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        return product;
    }

    @Override
    @Transactional
    public TbDataProduct rejectProduct(String id, String reason) {
        TbDataProduct product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (product.getStatus() != ProductStatus.PENDING_AUDIT.getCode()) {
            throw new BusinessException("只有待审核状态可以审核");
        }
        product.setStatus(ProductStatus.REJECTED.getCode());
        product.setRejectReason(reason);
        product.setfUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        return product;
    }

    @Override
    @Transactional
    public TbDataProduct offlineProduct(String id) {
        TbDataProduct product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        product.setStatus(ProductStatus.OFFLINE.getCode());
        product.setfUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        return product;
    }

    private String generateProductCode() {
        return "PRD" + System.currentTimeMillis();
    }
}