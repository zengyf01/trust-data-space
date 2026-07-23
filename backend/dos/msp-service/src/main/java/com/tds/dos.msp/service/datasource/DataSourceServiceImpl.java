package com.tds.dos.msp.service.datasource;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.dos.msp.common.core.PageResult;
import com.tds.dos.msp.common.enums.DataSourceType;
import com.tds.dos.msp.common.exception.BusinessException;
import com.tds.dos.msp.dal.entity.TbMspDatasource;
import com.tds.dos.msp.dal.mapper.TbMspDatasourceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DataSource Service implementation
 */
@Service
public class DataSourceServiceImpl implements IDataSourceService {
    private static final Logger log = LoggerFactory.getLogger(DataSourceServiceImpl.class);

    @Autowired
    private TbMspDatasourceMapper datasourceMapper;

    @Override
    public String createDataSource(DataSourceDTO dto) {
        String datasourceId = dto.getDatasourceId() != null ? dto.getDatasourceId()
            : UUID.randomUUID().toString().replace("-", "");

        TbMspDatasource ds = new TbMspDatasource();
        ds.setfId(UUID.randomUUID().toString().replace("-", ""));
        ds.setfDatasourceId(datasourceId);
        ds.setfNodeId(dto.getNodeId());
        ds.setfName(dto.getName());
        ds.setfType(dto.getType() != null ? dto.getType().getCode() : DataSourceType.MYSQL.getCode());
        ds.setfHost(dto.getHost());
        ds.setfPort(dto.getPort());
        ds.setfDatabaseName(dto.getDatabaseName());
        ds.setfUsername(dto.getUsername());
        ds.setfPassword(dto.getPassword());
        ds.setfTableName(dto.getTableName());
        ds.setfColumns(dto.getColumns());
        ds.setfCreateTime(LocalDateTime.now());
        ds.setfUpdateTime(LocalDateTime.now());

        datasourceMapper.insert(ds);
        log.info("DataSource created: {}", datasourceId);
        return datasourceId;
    }

    @Override
    public boolean updateDataSource(String datasourceId, DataSourceDTO dto) {
        TbMspDatasource ds = datasourceMapper.selectOne(
            new LambdaQueryWrapper<TbMspDatasource>().eq(TbMspDatasource::getfDatasourceId, datasourceId)
        );
        if (ds == null) {
            throw new BusinessException("DataSource not found: " + datasourceId);
        }

        if (dto.getName() != null) ds.setfName(dto.getName());
        if (dto.getType() != null) ds.setfType(dto.getType().getCode());
        if (dto.getHost() != null) ds.setfHost(dto.getHost());
        if (dto.getPort() != null) ds.setfPort(dto.getPort());
        if (dto.getDatabaseName() != null) ds.setfDatabaseName(dto.getDatabaseName());
        if (dto.getUsername() != null) ds.setfUsername(dto.getUsername());
        if (dto.getPassword() != null) ds.setfPassword(dto.getPassword());
        if (dto.getTableName() != null) ds.setfTableName(dto.getTableName());
        if (dto.getColumns() != null) ds.setfColumns(dto.getColumns());
        ds.setfUpdateTime(LocalDateTime.now());

        datasourceMapper.updateById(ds);
        return true;
    }

    @Override
    public boolean deleteDataSource(String datasourceId) {
        TbMspDatasource ds = datasourceMapper.selectOne(
            new LambdaQueryWrapper<TbMspDatasource>().eq(TbMspDatasource::getfDatasourceId, datasourceId)
        );
        if (ds == null) {
            throw new BusinessException("DataSource not found: " + datasourceId);
        }
        datasourceMapper.deleteById(ds.getfId());
        return true;
    }

    @Override
    public TbMspDatasource getDataSource(String datasourceId) {
        return datasourceMapper.selectOne(
            new LambdaQueryWrapper<TbMspDatasource>().eq(TbMspDatasource::getfDatasourceId, datasourceId)
        );
    }

    @Override
    public PageResult<TbMspDatasource> listDataSources(int page, int size, DataSourceType type, String nodeId) {
        Page<TbMspDatasource> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TbMspDatasource> wrapper = new LambdaQueryWrapper<>();

        if (type != null) {
            wrapper.eq(TbMspDatasource::getfType, type.getCode());
        }
        if (nodeId != null) {
            wrapper.eq(TbMspDatasource::getfNodeId, nodeId);
        }

        wrapper.orderByDesc(TbMspDatasource::getfCreateTime);
        IPage<TbMspDatasource> result = datasourceMapper.selectPage(pageParam, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public boolean testConnection(DataSourceDTO dto) {
        // Simplified connection test - in real implementation would actually connect
        log.info("Testing connection for datasource: {}", dto.getName());
        return true;
    }
}