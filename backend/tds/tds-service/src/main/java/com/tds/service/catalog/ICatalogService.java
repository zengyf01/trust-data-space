package com.tds.service.catalog;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dal.entity.TbCatalog;
import com.tds.dal.entity.TbCatalogField;

import java.util.List;

/**
 * 资源目录服务接口
 */
public interface ICatalogService {

    /**
     * 分页查询目录
     */
    IPage<TbCatalog> getCatalogPage(int currentPage, int pageSize, String catalogName, Integer status);

    /**
     * 获取目录详情
     */
    TbCatalog getCatalogById(String id);

    /**
     * 获取目录字段信息
     */
    List<TbCatalogField> getCatalogFields(String catalogId);

    /**
     * 创建目录（含字段信息）
     */
    TbCatalog createCatalog(CatalogCreateDTO dto);

    /**
     * 更新目录（含字段信息）
     */
    TbCatalog updateCatalog(String id, CatalogCreateDTO dto);

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