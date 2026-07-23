package com.tds.datar.service.catalog;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.datar.dal.entity.TbCatalog;

/**
 * 资源目录服务接口
 */
public interface CatalogService {

    /**
     * 分页查询目录
     */
    IPage<TbCatalog> getCatalogPage(int currentPage, int pageSize, String catalogName, Integer status, String spaceId);

    /**
     * 获取目录详情
     */
    TbCatalog getCatalogById(String id);

    /**
     * 创建目录
     */
    TbCatalog createCatalog(CatalogDTO dto);

    /**
     * 更新目录
     */
    TbCatalog updateCatalog(String id, CatalogDTO dto);

    /**
     * 发布目录
     */
    TbCatalog publishCatalog(String id);

    /**
     * 下线目录
     */
    TbCatalog offlineCatalog(String id);

    /**
     * 删除目录
     */
    void deleteCatalog(String id);
}