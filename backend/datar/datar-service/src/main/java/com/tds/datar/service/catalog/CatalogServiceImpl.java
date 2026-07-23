package com.tds.datar.service.catalog;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.datar.common.enums.CatalogStatus;
import com.tds.datar.common.exception.BusinessException;
import com.tds.datar.dal.entity.TbCatalog;
import com.tds.datar.dal.mapper.TbCatalogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 资源目录服务实现
 */
@Service
public class CatalogServiceImpl implements CatalogService {

    @Autowired
    private TbCatalogMapper catalogMapper;

    @Override
    public IPage<TbCatalog> getCatalogPage(int currentPage, int pageSize,
            String catalogName, Integer status, String spaceId) {
        Page<TbCatalog> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbCatalog> wrapper = new LambdaQueryWrapper<>();
        if (catalogName != null && !catalogName.isEmpty()) {
            wrapper.like(TbCatalog::getCatalogName, catalogName);
        }
        if (status != null) {
            wrapper.eq(TbCatalog::getStatus, status);
        }
        if (spaceId != null && !spaceId.isEmpty()) {
            wrapper.eq(TbCatalog::getSpaceId, spaceId);
        }
        wrapper.orderByDesc(TbCatalog::getCreateTime);
        wrapper.eq(TbCatalog::getDeleteMark, 0);
        return catalogMapper.selectPage(page, wrapper);
    }

    @Override
    public TbCatalog getCatalogById(String id) {
        return catalogMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbCatalog createCatalog(CatalogDTO dto) {
        TbCatalog catalog = new TbCatalog();
        catalog.setId(UUID.randomUUID().toString().replace("-", ""));
        catalog.setCatalogCode(generateCatalogCode());
        catalog.setCatalogName(dto.getCatalogName());
        catalog.setDataSourceId(dto.getDataSourceId());
        catalog.setSchemaName(dto.getSchemaName());
        catalog.setTableName(dto.getTableName());
        catalog.setDescription(dto.getDescription());
        catalog.setVersion(1);
        catalog.setStatus(CatalogStatus.DRAFT.getCode());
        catalog.setfTenantId(dto.getTenantId());
        catalog.setSpaceId(dto.getSpaceId());
        catalog.setfCreateTime(LocalDateTime.now());
        catalog.setfUpdateTime(LocalDateTime.now());
        catalog.setfDeleteMark(0);

        catalogMapper.insert(catalog);
        return catalog;
    }

    @Override
    @Transactional
    public TbCatalog updateCatalog(String id, CatalogDTO dto) {
        TbCatalog catalog = catalogMapper.selectById(id);
        if (catalog == null) {
            throw new BusinessException("目录不存在");
        }
        catalog.setCatalogName(dto.getCatalogName());
        catalog.setDataSourceId(dto.getDataSourceId());
        catalog.setSchemaName(dto.getSchemaName());
        catalog.setTableName(dto.getTableName());
        catalog.setDescription(dto.getDescription());
        catalog.setVersion(catalog.getVersion() + 1);
        catalog.setfUpdateTime(LocalDateTime.now());

        catalogMapper.updateById(catalog);
        return catalog;
    }

    @Override
    @Transactional
    public TbCatalog publishCatalog(String id) {
        TbCatalog catalog = catalogMapper.selectById(id);
        if (catalog == null) {
            throw new BusinessException("目录不存在");
        }
        if (catalog.getStatus() != CatalogStatus.DRAFT.getCode()) {
            throw new BusinessException("只有草稿状态可以发布");
        }
        catalog.setStatus(CatalogStatus.PUBLISHED.getCode());
        catalog.setfUpdateTime(LocalDateTime.now());
        catalogMapper.updateById(catalog);
        return catalog;
    }

    @Override
    @Transactional
    public TbCatalog offlineCatalog(String id) {
        TbCatalog catalog = catalogMapper.selectById(id);
        if (catalog == null) {
            throw new BusinessException("目录不存在");
        }
        catalog.setStatus(CatalogStatus.OFFLINE.getCode());
        catalog.setfUpdateTime(LocalDateTime.now());
        catalogMapper.updateById(catalog);
        return catalog;
    }

    @Override
    @Transactional
    public void deleteCatalog(String id) {
        TbCatalog catalog = catalogMapper.selectById(id);
        if (catalog == null) {
            throw new BusinessException("目录不存在");
        }
        catalog.setfDeleteMark(1);
        catalog.setfUpdateTime(LocalDateTime.now());
        catalogMapper.updateById(catalog);
    }

    private String generateCatalogCode() {
        return "CAT" + System.currentTimeMillis();
    }
}