package com.tds.datar.service.product;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.datar.dal.entity.TbDataProduct;

/**
 * 数据产品服务接口
 */
public interface ProductService {

    /**
     * 分页查询产品
     */
    IPage<TbDataProduct> getProductPage(int currentPage, int pageSize, String productName, Integer status, String spaceId);

    /**
     * 获取产品详情
     */
    TbDataProduct getProductById(String id);

    /**
     * 创建产品
     */
    TbDataProduct createProduct(ProductDTO dto);

    /**
     * 更新产品
     */
    TbDataProduct updateProduct(String id, ProductDTO dto);

    /**
     * 发布产品
     */
    TbDataProduct publishProduct(String id);

    /**
     * 下架产品
     */
    TbDataProduct offlineProduct(String id);

    /**
     * 删除产品
     */
    void deleteProduct(String id);
}