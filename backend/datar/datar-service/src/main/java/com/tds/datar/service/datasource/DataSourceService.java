package com.tds.datar.service.datasource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.datar.dal.entity.TbDataSource;
import java.util.List;

/**
 * 数据源服务接口
 */
public interface DataSourceService {

    /**
     * 分页查询数据源
     */
    IPage<TbDataSource> getDataSourcePage(int currentPage, int pageSize, String sourceName, Integer sourceType, Integer status, String spaceId);

    /**
     * 获取数据源详情
     */
    TbDataSource getDataSourceById(String id);

    /**
     * 创建数据源
     */
    TbDataSource createDataSource(DataSourceDTO dto);

    /**
     * 更新数据源
     */
    TbDataSource updateDataSource(String id, DataSourceDTO dto);

    /**
     * 删除数据源
     */
    void deleteDataSource(String id);

    /**
     * 连接测试
     */
    boolean testConnection(DataSourceDTO dto);

    /**
     * 根据ID连接测试
     */
    boolean testConnectionById(String id);

    /**
     * 启用数据源
     */
    void enableDataSource(String id);

    /**
     * 禁用数据源
     */
    void disableDataSource(String id);

    /**
     * 获取数据源表列表
     */
    List<String> getTableList(String id);

    /**
     * 获取数据源字段列表
     */
    List<String> getColumnList(String id, String tableName);
}