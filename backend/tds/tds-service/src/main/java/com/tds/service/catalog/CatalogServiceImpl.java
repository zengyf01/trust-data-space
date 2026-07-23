package com.tds.service.catalog;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.common.enums.CatalogStatus;
import com.tds.common.exception.BusinessException;
import com.tds.dal.entity.TbCatalog;
import com.tds.dal.entity.TbCatalogField;
import com.tds.dal.mapper.TbCatalogFieldMapper;
import com.tds.dal.mapper.TbCatalogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 资源目录服务实现
 */
@Service
public class CatalogServiceImpl implements ICatalogService {

    @Autowired
    private TbCatalogMapper catalogMapper;

    @Autowired
    private TbCatalogFieldMapper catalogFieldMapper;

    @Override
    public IPage<TbCatalog> getCatalogPage(int currentPage, int pageSize,
            String catalogName, Integer status) {
        Page<TbCatalog> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbCatalog> wrapper = new LambdaQueryWrapper<>();
        if (catalogName != null && !catalogName.isEmpty()) {
            wrapper.like(TbCatalog::getCatalogName, catalogName);
        }
        if (status != null) {
            wrapper.eq(TbCatalog::getStatus, status);
        }
        wrapper.orderByDesc(TbCatalog::getfCreateTime);
        return catalogMapper.selectPage(page, wrapper);
    }

    @Override
    public TbCatalog getCatalogById(String id) {
        return catalogMapper.selectById(id);
    }

    @Override
    public List<TbCatalogField> getCatalogFields(String catalogId) {
        LambdaQueryWrapper<TbCatalogField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbCatalogField::getCatalogId, catalogId)
               .orderByAsc(TbCatalogField::getSortOrder);
        return catalogFieldMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public TbCatalog createCatalog(CatalogCreateDTO dto) {
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
        catalog.setfCreateTime(LocalDateTime.now());
        catalog.setfUpdateTime(LocalDateTime.now());
        catalog.setfDeleteMark(0);

        catalogMapper.insert(catalog);

        // 保存字段信息
        saveFields(catalog.getId(), dto.getFields(), dto.getTenantId());

        return catalog;
    }

    @Override
    @Transactional
    public TbCatalog updateCatalog(String id, CatalogCreateDTO dto) {
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

        // 更新字段信息：先删后插
        LambdaQueryWrapper<TbCatalogField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbCatalogField::getCatalogId, id);
        catalogFieldMapper.delete(wrapper);

        saveFields(catalog.getId(), dto.getFields(), dto.getTenantId());

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

    private void saveFields(String catalogId, List<CatalogFieldDTO> fields, String tenantId) {
        if (fields != null && !fields.isEmpty()) {
            for (CatalogFieldDTO fieldDTO : fields) {
                TbCatalogField field = new TbCatalogField();
                field.setId(UUID.randomUUID().toString().replace("-", ""));
                field.setCatalogId(catalogId);
                field.setFieldName(fieldDTO.getFieldName());
                field.setFieldType(fieldDTO.getFieldType());
                field.setFieldComment(fieldDTO.getFieldComment());
                field.setIsPrimaryKey(fieldDTO.getIsPrimaryKey());
                field.setIsNullable(fieldDTO.getIsNullable());
                field.setIsSensitive(fieldDTO.getIsSensitive());
                field.setDesensitizeRule(fieldDTO.getDesensitizeRule());
                field.setSortOrder(fieldDTO.getSortOrder());
                field.setfTenantId(tenantId);
                field.setfCreateTime(LocalDateTime.now());
                field.setfUpdateTime(LocalDateTime.now());
                field.setfDeleteMark(0);
                catalogFieldMapper.insert(field);
            }
        }
    }

    private String generateCatalogCode() {
        return "CAT" + System.currentTimeMillis();
    }
}