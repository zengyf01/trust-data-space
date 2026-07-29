package com.tds.dos.service.msp.datasource;

import com.tds.dos.common.core.PageResult;
import com.tds.dos.common.enums.DataSourceType;
import com.tds.dos.dal.msp.entity.TbDatasource;

/**
 * DataSource Service interface
 */
public interface IDataSourceService {
    String createDataSource(DataSourceDTO dto);
    boolean updateDataSource(String datasourceId, DataSourceDTO dto);
    boolean deleteDataSource(String datasourceId);
    TbDatasource getDataSource(String datasourceId);
    PageResult<TbDatasource> listDataSources(int page, int size, DataSourceType type, String nodeId);
    boolean testConnection(DataSourceDTO dto);
}
