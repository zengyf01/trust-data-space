package com.tds.dos.msp.service.datasource;

import com.tds.dos.msp.common.core.PageResult;
import com.tds.dos.msp.common.enums.DataSourceType;
import com.tds.dos.msp.dal.entity.TbMspDatasource;

/**
 * DataSource Service interface
 */
public interface IDataSourceService {
    String createDataSource(DataSourceDTO dto);
    boolean updateDataSource(String datasourceId, DataSourceDTO dto);
    boolean deleteDataSource(String datasourceId);
    TbMspDatasource getDataSource(String datasourceId);
    PageResult<TbMspDatasource> listDataSources(int page, int size, DataSourceType type, String nodeId);
    boolean testConnection(DataSourceDTO dto);
}