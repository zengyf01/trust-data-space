package com.tds.service.product;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dal.entity.TbDataProduct;

/**
 * 数据产品服务接口
 */
public interface IProductService {

    /**
     * 分页查询产品
     */
    IPage<TbDataProduct> getProductPage(int currentPage, int pageSize, String productName, Integer status);

    /**
     * 获取产品详情
     */
    TbDataProduct getProductById(String id);

    /**
     * 创建产品
     */
    TbDataProduct createProduct(ProductCreateDTO dto);

    /**
     * 更新产品
     */
    TbDataProduct updateProduct(String id, ProductCreateDTO dto);

    /**
     * 删除产品
     */
    void deleteProduct(String id);

    /**
     * 提交审核
     */
    TbDataProduct submitForAudit(String id);

    /**
     * 审核通过
     */
    TbDataProduct approveProduct(String id);

    /**
     * 审核拒绝
     */
    TbDataProduct rejectProduct(String id, String reason);

    /**
     * 下架产品
     */
    TbDataProduct offlineProduct(String id);
}