package com.tds.datar.service.datasource.debug;

import java.util.List;
import java.util.Map;

/**
 * 数据源调试服务接口
 */
public interface IDataSourceDebugService {

    /**
     * 数据预览
     */
    List<Map<String, Object>> previewData(String dataSourceId, String tableName, String condition, int limit);

    /**
     * 执行SQL查询
     */
    List<Map<String, Object>> executeSql(String dataSourceId, String sql);

    /**
     * SFTP文件列表
     */
    List<String> listSftpFiles(String dataSourceId, String path);

    /**
     * SFTP文件下载
     */
    byte[] downloadSftpFile(String dataSourceId, String filePath);

    /**
     * 读取CSV文件
     */
    List<Map<String, Object>> readCsvFile(String dataSourceId, String filePath, int limit);

    /**
     * HTTP接口测试
     */
    Map<String, Object> testHttpEndpoint(String dataSourceId, String method, String url, String headers, String body);
}