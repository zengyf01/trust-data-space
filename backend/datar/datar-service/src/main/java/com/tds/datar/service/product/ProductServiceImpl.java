package com.tds.datar.service.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.datar.common.enums.ProductStatus;
import com.tds.datar.common.exception.BusinessException;
import com.tds.datar.dal.entity.TbDataProduct;
import com.tds.datar.dal.mapper.TbDataProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 数据产品服务实现
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private TbDataProductMapper productMapper;

    @Override
    public IPage<TbDataProduct> getProductPage(int currentPage, int pageSize,
            String productName, Integer status, String spaceId) {
        Page<TbDataProduct> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbDataProduct> wrapper = new LambdaQueryWrapper<>();
        if (productName != null && !productName.isEmpty()) {
            wrapper.like(TbDataProduct::getProductName, productName);
        }
        if (status != null) {
            wrapper.eq(TbDataProduct::getStatus, status);
        }
        if (spaceId != null && !spaceId.isEmpty()) {
            wrapper.eq(TbDataProduct::getSpaceId, spaceId);
        }
        wrapper.orderByDesc(TbDataProduct::getCreateTime);
        wrapper.eq(TbDataProduct::getDeleteMark, 0);
        return productMapper.selectPage(page, wrapper);
    }

    @Override
    public TbDataProduct getProductById(String id) {
        return productMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbDataProduct createProduct(ProductDTO dto) {
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
        product.setSpaceId(dto.getSpaceId());
        product.setfCreateTime(LocalDateTime.now());
        product.setfUpdateTime(LocalDateTime.now());
        product.setfDeleteMark(0);

        productMapper.insert(product);
        return product;
    }

    @Override
    @Transactional
    public TbDataProduct updateProduct(String id, ProductDTO dto) {
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
    public TbDataProduct publishProduct(String id) {
        TbDataProduct product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (product.getStatus() != ProductStatus.DRAFT.getCode()) {
            throw new BusinessException("只有草稿状态可以发布");
        }
        product.setStatus(ProductStatus.PUBLISHED.getCode());
        product.setPublishTime(LocalDateTime.now());
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

    private String generateProductCode() {
        return "PRD" + System.currentTimeMillis();
    }
}