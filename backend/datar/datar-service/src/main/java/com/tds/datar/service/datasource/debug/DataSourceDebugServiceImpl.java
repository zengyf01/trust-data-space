package com.tds.datar.service.datasource.debug;

import com.tds.datar.common.exception.BusinessException;
import com.tds.datar.dal.entity.TbDataSource;
import com.tds.datar.dal.mapper.TbDataSourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 数据源调试服务实现
 */
@Service
public class DataSourceDebugServiceImpl implements IDataSourceDebugService {

    @Autowired
    private TbDataSourceMapper dataSourceMapper;

    @Override
    public List<Map<String, Object>> previewData(String dataSourceId, String tableName, String condition, int limit) {
        TbDataSource dataSource = dataSourceMapper.selectById(dataSourceId);
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }

        // 模拟返回预览数据
        List<Map<String, Object>> result = new ArrayList<>();
        int rows = Math.min(limit > 0 ? limit : 10, 100);
        for (int i = 0; i < rows; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", i + 1);
            row.put("name", "item_" + i);
            row.put("value", "value_" + i);
            row.put("create_time", new Date());
            result.add(row);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> executeSql(String dataSourceId, String sql) {
        TbDataSource dataSource = dataSourceMapper.selectById(dataSourceId);
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }

        // 简化实现：实际应连接数据库执行SQL
        // 模拟返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("result", "SQL executed successfully");
        row.put("sql", sql);
        result.add(row);
        return result;
    }

    @Override
    public List<String> listSftpFiles(String dataSourceId, String path) {
        TbDataSource dataSource = dataSourceMapper.selectById(dataSourceId);
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }

        // 模拟返回文件列表
        List<String> files = new ArrayList<>();
        files.add(path + "/file1.csv");
        files.add(path + "/file2.csv");
        files.add(path + "/data/");
        files.add(path + "/report.xlsx");
        return files;
    }

    @Override
    public byte[] downloadSftpFile(String dataSourceId, String filePath) {
        TbDataSource dataSource = dataSourceMapper.selectById(dataSourceId);
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }

        // 模拟返回文件内容
        String content = "id,name,value\n1,item1,value1\n2,item2,value2\n";
        return content.getBytes();
    }

    @Override
    public List<Map<String, Object>> readCsvFile(String dataSourceId, String filePath, int limit) {
        TbDataSource dataSource = dataSourceMapper.selectById(dataSourceId);
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }

        // 模拟解析CSV文件
        List<Map<String, Object>> result = new ArrayList<>();
        int rows = Math.min(limit > 0 ? limit : 10, 100);
        for (int i = 0; i < rows; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", i + 1);
            row.put("name", "name_" + i);
            row.put("email", "email" + i + "@example.com");
            row.put("phone", "138000000" + String.format("%04d", i));
            result.add(row);
        }
        return result;
    }

    @Override
    public Map<String, Object> testHttpEndpoint(String dataSourceId, String method, String url, String headers, String body) {
        TbDataSource dataSource = dataSourceMapper.selectById(dataSourceId);
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }

        // 模拟HTTP测试响应
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", 200);
        result.put("statusText", "OK");
        result.put("method", method != null ? method : "GET");
        result.put("url", url);
        result.put("responseTime", 120);
        result.put("headers", Map.of(
            "Content-Type", "application/json",
            "Server", "nginx/1.18.0"
        ));
        result.put("body", "{\"code\": 200, \"msg\": \"success\", \"data\": {\"test\": true}}");
        return result;
    }
}