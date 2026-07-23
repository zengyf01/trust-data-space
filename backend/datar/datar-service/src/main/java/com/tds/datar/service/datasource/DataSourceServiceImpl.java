package com.tds.datar.service.datasource;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.datar.common.enums.DataSourceStatus;
import com.tds.datar.common.exception.BusinessException;
import com.tds.datar.dal.entity.TbDataSource;
import com.tds.datar.dal.mapper.TbDataSourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 数据源服务实现
 */
@Service
public class DataSourceServiceImpl implements DataSourceService {

    @Autowired
    private TbDataSourceMapper dataSourceMapper;

    @Override
    public IPage<TbDataSource> getDataSourcePage(int currentPage, int pageSize,
            String sourceName, Integer sourceType, Integer status, String spaceId) {
        Page<TbDataSource> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbDataSource> wrapper = new LambdaQueryWrapper<>();
        if (sourceName != null && !sourceName.isEmpty()) {
            wrapper.like(TbDataSource::getSourceName, sourceName);
        }
        if (sourceType != null) {
            wrapper.eq(TbDataSource::getSourceType, sourceType);
        }
        if (status != null) {
            wrapper.eq(TbDataSource::getStatus, status);
        }
        if (spaceId != null && !spaceId.isEmpty()) {
            wrapper.eq(TbDataSource::getSpaceId, spaceId);
        }
        wrapper.orderByDesc(TbDataSource::getCreateTime);
        wrapper.eq(TbDataSource::getDeleteMark, 0);
        return dataSourceMapper.selectPage(page, wrapper);
    }

    @Override
    public TbDataSource getDataSourceById(String id) {
        return dataSourceMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbDataSource createDataSource(DataSourceDTO dto) {
        TbDataSource ds = new TbDataSource();
        ds.setId(UUID.randomUUID().toString().replace("-", ""));
        ds.setSourceCode(generateSourceCode());
        ds.setSourceName(dto.getSourceName());
        ds.setSourceType(dto.getSourceType());
        ds.setHost(dto.getHost());
        ds.setPort(dto.getPort());
        ds.setDatabaseName(dto.getDatabaseName());
        ds.setUsername(dto.getUsername());
        ds.setPassword(dto.getPassword());
        ds.setBasePath(dto.getBasePath());
        ds.setConnParams(dto.getConnParams());
        ds.setStatus(DataSourceStatus.ENABLED.getCode());
        ds.setfTenantId(dto.getTenantId());
        ds.setSpaceId(dto.getSpaceId());
        ds.setfCreateTime(LocalDateTime.now());
        ds.setfUpdateTime(LocalDateTime.now());
        ds.setfDeleteMark(0);

        dataSourceMapper.insert(ds);
        return ds;
    }

    @Override
    @Transactional
    public TbDataSource updateDataSource(String id, DataSourceDTO dto) {
        TbDataSource ds = dataSourceMapper.selectById(id);
        if (ds == null) {
            throw new BusinessException("数据源不存在");
        }
        ds.setSourceName(dto.getSourceName());
        ds.setSourceType(dto.getSourceType());
        ds.setHost(dto.getHost());
        ds.setPort(dto.getPort());
        ds.setDatabaseName(dto.getDatabaseName());
        ds.setUsername(dto.getUsername());
        ds.setPassword(dto.getPassword());
        ds.setBasePath(dto.getBasePath());
        ds.setConnParams(dto.getConnParams());
        ds.setfUpdateTime(LocalDateTime.now());

        dataSourceMapper.updateById(ds);
        return ds;
    }

    @Override
    @Transactional
    public void deleteDataSource(String id) {
        TbDataSource ds = dataSourceMapper.selectById(id);
        if (ds == null) {
            throw new BusinessException("数据源不存在");
        }
        ds.setfDeleteMark(1);
        ds.setfUpdateTime(LocalDateTime.now());
        dataSourceMapper.updateById(ds);
    }

    @Override
    public boolean testConnection(DataSourceDTO dto) {
        // 模拟连接测试
        return true;
    }

    @Override
    public boolean testConnectionById(String id) {
        // 模拟连接测试
        return true;
    }

    @Override
    @Transactional
    public void enableDataSource(String id) {
        TbDataSource ds = dataSourceMapper.selectById(id);
        if (ds == null) {
            throw new BusinessException("数据源不存在");
        }
        ds.setStatus(DataSourceStatus.ENABLED.getCode());
        ds.setfUpdateTime(LocalDateTime.now());
        dataSourceMapper.updateById(ds);
    }

    @Override
    @Transactional
    public void disableDataSource(String id) {
        TbDataSource ds = dataSourceMapper.selectById(id);
        if (ds == null) {
            throw new BusinessException("数据源不存在");
        }
        ds.setStatus(DataSourceStatus.DISABLED.getCode());
        ds.setfUpdateTime(LocalDateTime.now());
        dataSourceMapper.updateById(ds);
    }

    @Override
    public java.util.List<String> getTableList(String id) {
        // 模拟返回表列表
        return java.util.List.of();
    }

    @Override
    public java.util.List<String> getColumnList(String id, String tableName) {
        // 模拟返回字段列表
        return java.util.List.of();
    }

    private String generateSourceCode() {
        return "DS" + System.currentTimeMillis();
    }
}