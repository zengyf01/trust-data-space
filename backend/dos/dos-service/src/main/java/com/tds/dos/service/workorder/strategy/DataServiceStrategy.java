package com.tds.dos.service.workorder.strategy;

import com.jcraft.jsch.*;
import com.tds.dos.common.enums.WorkOrderStatus;
import com.tds.dos.dal.entity.TbWorkOrder;
import com.tds.dos.dal.mapper.TbWorkOrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

/**
 * 数据服务策略 - HTTP推送、数据库同步、SFTP传输
 */
@Service
public class DataServiceStrategy implements WorkOrderStrategy {

    private static final Logger log = LoggerFactory.getLogger(DataServiceStrategy.class);

    @Autowired
    private TbWorkOrderMapper workOrderMapper;

    @Value("${dataservice.http.timeout:60000}")
    private int httpTimeout;

    @Value("${dataservice.sftp.timeout:30000}")
    private int sftpTimeout;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getWorkOrderType() {
        return "DATA_SERVICE";
    }

    @Override
    public void preProcess(String workOrderId) {
        TbWorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            throw new RuntimeException("工单不存在");
        }
        workOrder.setWorkOrderStatus(WorkOrderStatus.PROCESSING.getCode());
        workOrderMapper.updateById(workOrder);

        try {
            Map<String, Object> params = objectMapper.readValue(
                    workOrder.getConfigJson(), Map.class);

            String deliveryType = (String) params.get("deliveryType");
            String targetUrl = (String) params.get("targetUrl");

            Map<String, Object> preCheckResult = new HashMap<>();

            if ("SFTP".equals(deliveryType)) {
                preCheckResult = preProcessSftp(params);
            } else if ("HTTP".equals(deliveryType)) {
                preCheckResult = preProcessHttp(params);
            } else if ("DATABASE".equals(deliveryType)) {
                preCheckResult = preProcessDatabase(params);
            } else {
                throw new RuntimeException("不支持的交付类型: " + deliveryType);
            }

            workOrder.setResultMessage(objectMapper.writeValueAsString(preCheckResult));
            workOrderMapper.updateById(workOrder);

        } catch (Exception e) {
            workOrder.setWorkOrderStatus(WorkOrderStatus.FAILED.getCode());
            workOrder.setResultMessage("预处理失败: " + e.getMessage());
            workOrderMapper.updateById(workOrder);
            throw new RuntimeException("预处理失败", e);
        }
    }

    @Override
    public void execute(String workOrderId) {
        TbWorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        try {
            Map<String, Object> params = objectMapper.readValue(
                    workOrder.getConfigJson(), Map.class);

            String deliveryType = (String) params.get("deliveryType");
            String result;

            if ("SFTP".equals(deliveryType)) {
                result = executeSftpTransfer(params);
            } else if ("HTTP".equals(deliveryType)) {
                result = executeHttpPush(params);
            } else if ("DATABASE".equals(deliveryType)) {
                result = executeDatabaseSync(params);
            } else {
                throw new RuntimeException("不支持的交付类型: " + deliveryType);
            }

            workOrder.setWorkOrderStatus(WorkOrderStatus.COMPLETED.getCode());
            workOrder.setResultMessage(result);
            workOrderMapper.updateById(workOrder);

        } catch (Exception e) {
            workOrder.setWorkOrderStatus(WorkOrderStatus.FAILED.getCode());
            workOrder.setResultMessage("执行失败: " + e.getMessage());
            workOrderMapper.updateById(workOrder);
            throw new RuntimeException("执行失败", e);
        }
    }

    @Override
    public void cancel(String workOrderId) {
        TbWorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        workOrder.setWorkOrderStatus(WorkOrderStatus.CANCELLED.getCode());
        workOrderMapper.updateById(workOrder);
    }

    // ==================== SFTP实现 ====================

    private Map<String, Object> preProcessSftp(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        String host = (String) params.get("sftpHost");
        String portStr = (String) params.get("sftpPort");
        String username = (String) params.get("sftpUsername");
        String password = (String) params.get("sftpPassword");
        String remotePath = (String) params.get("sftpRemotePath");

        int port = portStr != null ? Integer.parseInt(portStr) : 22;

        Session session = null;
        ChannelSftp sftp = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);

            if (password != null && !password.isEmpty()) {
                session.setPassword(password);
            }

            // 配置SFTP连接属性
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "password");
            session.setConfig(config);
            session.setTimeout(sftpTimeout);
            session.connect(sftpTimeout);

            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(sftpTimeout);

            // 检查远程目录是否存在
            try {
                SftpATTRS attrs = sftp.stat(remotePath);
                result.put("remotePathExists", true);
                result.put("remotePath", remotePath);
                result.put("remotePathPermissions", attrs.getPermissionsString());
            } catch (SftpException e) {
                result.put("remotePathExists", false);
                result.put("remotePath", remotePath);
                result.put("error", "远程目录不存在: " + remotePath);
            }

            result.put("status", "connected");
            result.put("host", host);
            result.put("port", port);
            result.put("username", username);
            log.info("SFTP预处理成功: {}@{}:{}", username, host, port);

        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", "SFTP连接失败: " + e.getMessage());
            log.error("SFTP预处理失败: {}@{}:{}", username, host, port, e);
        } finally {
            if (sftp != null && sftp.isConnected()) {
                sftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }

        return result;
    }

    private String executeSftpTransfer(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        String host = (String) params.get("sftpHost");
        String portStr = (String) params.get("sftpPort");
        String username = (String) params.get("sftpUsername");
        String password = (String) params.get("sftpPassword");
        String remotePath = (String) params.get("sftpRemotePath");
        String localFilePath = (String) params.get("localFilePath");
        String fileName = (String) params.get("fileName");

        int port = portStr != null ? Integer.parseInt(portStr) : 22;

        Session session = null;
        ChannelSftp sftp = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);

            if (password != null && !password.isEmpty()) {
                session.setPassword(password);
            }

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "password");
            session.setConfig(config);
            session.setTimeout(sftpTimeout);
            session.connect(sftpTimeout);

            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(sftpTimeout);

            // 确保远程目录存在
            try {
                sftp.stat(remotePath);
            } catch (SftpException e) {
                // 目录不存在，创建它
                createRemoteDirectory(sftp, remotePath);
            }

            // 执行文件传输
            long startTime = System.currentTimeMillis();

            if (localFilePath != null && !localFilePath.isEmpty()) {
                // 从本地文件上传
                File localFile = new File(localFilePath);
                if (!localFile.exists()) {
                    throw new RuntimeException("本地文件不存在: " + localFilePath);
                }

                String remoteFilePath = remotePath.endsWith("/")
                    ? remotePath + (fileName != null ? fileName : localFile.getName())
                    : remotePath + "/" + (fileName != null ? fileName : localFile.getName());

                sftp.put(new FileInputStream(localFile), remoteFilePath);

                result.put("transferMode", "upload");
                result.put("localFile", localFilePath);
                result.put("remoteFile", remoteFilePath);
                result.put("fileSize", localFile.length());
            } else {
                // 直接传输文件内容
                String fileContent = (String) params.get("fileContent");
                String targetFileName = fileName != null ? fileName : "data_" + System.currentTimeMillis() + ".txt";
                String remoteFilePath = remotePath.endsWith("/") ? remotePath + targetFileName : remotePath + "/" + targetFileName;

                byte[] contentBytes = fileContent != null ? fileContent.getBytes(StandardCharsets.UTF_8) : new byte[0];
                ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);
                sftp.put(inputStream, remoteFilePath);

                result.put("transferMode", "content");
                result.put("remoteFile", remoteFilePath);
                result.put("fileSize", contentBytes.length);
            }

            long endTime = System.currentTimeMillis();

            result.put("status", "success");
            result.put("duration", (endTime - startTime) / 1000.0);
            result.put("host", host);
            result.put("port", port);

            log.info("SFTP传输成功: {}@{}:{} -> {}, 耗时{}秒",
                    username, host, port, remotePath, (endTime - startTime) / 1000.0);

        } catch (Exception e) {
            result.put("status", "failed");
            result.put("error", "SFTP传输失败: " + e.getMessage());
            log.error("SFTP传输失败: {}@{}:{}", username, host, port, e);
            throw new RuntimeException("SFTP传输失败: " + e.getMessage(), e);
        } finally {
            if (sftp != null && sftp.isConnected()) {
                sftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }

        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"status\": \"error\", \"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 递归创建远程目录
     */
    private void createRemoteDirectory(ChannelSftp sftp, String remotePath) throws SftpException {
        String[] dirs = remotePath.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String dir : dirs) {
            if (dir.isEmpty()) continue;
            currentPath.append("/").append(dir);
            try {
                SftpATTRS attrs = sftp.stat(currentPath.toString());
                if (!attrs.isDir()) {
                    throw new SftpException(ChannelSftp.SSH_FX_FAILURE, "路径已存在但不是目录: " + currentPath);
                }
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    sftp.mkdir(currentPath.toString());
                    log.info("创建SFTP目录: {}", currentPath);
                } else {
                    throw e;
                }
            }
        }
    }

    // ==================== HTTP实现 ====================

    private Map<String, Object> preProcessHttp(Map<String, Object> params) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String targetUrl = (String) params.get("targetUrl");

        // 测试HTTP端点连通性
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpHead head = new HttpHead(targetUrl);
            try (CloseableHttpResponse response = httpClient.execute(head)) {
                result.put("reachable", true);
                result.put("statusCode", response.getCode());
                result.put("url", targetUrl);
            } catch (Exception e) {
                result.put("reachable", false);
                result.put("error", e.getMessage());
            }
        }

        return result;
    }

    private String executeHttpPush(Map<String, Object> params) throws Exception {
        Map<String, Object> result = new HashMap<>();

        String targetUrl = (String) params.get("targetUrl");
        String dataType = (String) params.get("dataType"); // JSON, FORM, XML
        String data = (String) params.get("data");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost httpPost = new HttpPost(targetUrl);
            httpPost.setHeader("Content-Type", getContentType(dataType));

            if ("JSON".equals(dataType)) {
                httpPost.setEntity(new StringEntity(data, StandardCharsets.UTF_8));
            } else if ("FORM".equals(dataType)) {
                List<NameValuePair> paramsList = parseFormData(data);
                httpPost.setEntity(new UrlEncodedFormEntity(paramsList, StandardCharsets.UTF_8));
            } else {
                httpPost.setEntity(new StringEntity(data, StandardCharsets.UTF_8));
            }

            long startTime = System.currentTimeMillis();
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                long endTime = System.currentTimeMillis();

                int statusCode = response.getCode();
                String responseBody = response.getEntity() != null ?
                        EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8) : "";

                result.put("status", statusCode >= 200 && statusCode < 300 ? "success" : "failed");
                result.put("statusCode", statusCode);
                result.put("responseTime", endTime - startTime);
                result.put("responseBody", responseBody.length() > 500 ?
                        responseBody.substring(0, 500) + "..." : responseBody);

                if (statusCode >= 200 && statusCode < 300) {
                    log.info("HTTP推送成功: {} -> {}", targetUrl, statusCode);
                } else {
                    log.warn("HTTP推送失败: {} -> {}", targetUrl, statusCode);
                }
            }
        }

        return objectMapper.writeValueAsString(result);
    }

    private String getContentType(String dataType) {
        if ("JSON".equals(dataType)) return "application/json";
        if ("FORM".equals(dataType)) return "application/x-www-form-urlencoded";
        if ("XML".equals(dataType)) return "application/xml";
        return "text/plain";
    }

    private List<NameValuePair> parseFormData(String data) {
        List<NameValuePair> params = new ArrayList<>();
        if (data != null && !data.isEmpty()) {
            String[] pairs = data.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    params.add(new NameValuePair() {
                        @Override
                        public String getName() { return kv[0]; }
                        @Override
                        public String getValue() { return kv[1]; }
                    });
                }
            }
        }
        return params;
    }

    // ==================== 数据库同步实现 ====================

    private Map<String, Object> preProcessDatabase(Map<String, Object> params) throws Exception {
        Map<String, Object> result = new HashMap<>();

        String jdbcUrl = (String) params.get("jdbcUrl");
        String dbUsername = (String) params.get("dbUsername");
        String dbPassword = (String) params.get("dbPassword");
        String sourceTable = (String) params.get("sourceTable");

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            DatabaseMetaData metaData = conn.getMetaData();

            // 检查源表是否存在
            String tableName = sourceTable;
            if (sourceTable.contains(".")) {
                tableName = sourceTable.split("\\.")[1];
            }

            try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                if (rs.next()) {
                    result.put("tableExists", true);
                    result.put("tableName", rs.getString("TABLE_NAME"));
                    result.put("schema", rs.getString("TABLE_SCHEM"));
                } else {
                    result.put("tableExists", false);
                    throw new RuntimeException("源表不存在: " + sourceTable);
                }
            }

            // 获取表行数
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + sourceTable)) {
                if (rs.next()) {
                    result.put("rowCount", rs.getInt(1));
                }
            }

            result.put("status", "connected");
            log.info("数据库预处理成功: {}", jdbcUrl);
        }

        return result;
    }

    private String executeDatabaseSync(Map<String, Object> params) throws Exception {
        Map<String, Object> result = new HashMap<>();

        String sourceJdbcUrl = (String) params.get("sourceJdbcUrl");
        String sourceUsername = (String) params.get("sourceUsername");
        String sourcePassword = (String) params.get("sourcePassword");
        String sourceTable = (String) params.get("sourceTable");
        String targetJdbcUrl = (String) params.get("targetJdbcUrl");
        String targetUsername = (String) params.get("targetUsername");
        String targetPassword = (String) params.get("targetPassword");
        String targetTable = (String) params.get("targetTable");
        Integer batchSize = params.get("batchSize") != null ? (Integer) params.get("batchSize") : 1000;
        String whereClause = (String) params.get("whereClause");

        try (Connection sourceConn = DriverManager.getConnection(sourceJdbcUrl, sourceUsername, sourcePassword);
             Connection targetConn = DriverManager.getConnection(targetJdbcUrl, targetUsername, targetPassword)) {

            // 获取源表数据
            String selectSql = "SELECT * FROM " + sourceTable +
                    (whereClause != null && !whereClause.isEmpty() ? " WHERE " + whereClause : "");

            long startTime = System.currentTimeMillis();
            int totalSynced = 0;

            try (Statement sourceStmt = sourceConn.createStatement();
                 ResultSet rs = sourceStmt.executeQuery(selectSql)) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                // 获取列名
                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    columns.add(metaData.getColumnName(i));
                }

                // 目标表插入
                String insertSql = buildInsertSql(targetTable, columns);

                try (PreparedStatement targetPstmt = targetConn.prepareStatement(insertSql)) {
                    targetConn.setAutoCommit(false);

                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            targetPstmt.setObject(i, rs.getObject(i));
                        }
                        targetPstmt.addBatch();
                        totalSynced++;

                        if (totalSynced % batchSize == 0) {
                            targetPstmt.executeBatch();
                            targetConn.commit();
                        }
                    }

                    // 处理剩余数据
                    if (totalSynced % batchSize != 0) {
                        targetPstmt.executeBatch();
                        targetConn.commit();
                    }
                }

                targetConn.setAutoCommit(true);
            }

            long endTime = System.currentTimeMillis();

            result.put("status", "success");
            result.put("synced", totalSynced);
            result.put("duration", (endTime - startTime) / 1000.0);
            result.put("sourceTable", sourceTable);
            result.put("targetTable", targetTable);

            log.info("数据库同步成功: {} -> {}, {}条记录, 耗时{}秒",
                    sourceTable, targetTable, totalSynced, (endTime - startTime) / 1000.0);
        }

        return objectMapper.writeValueAsString(result);
    }

    private String buildInsertSql(String tableName, List<String> columns) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            sql.append(i > 0 ? ", " : "").append(columns.get(i));
        }
        sql.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            sql.append(i > 0 ? ", " : "").append("?");
        }
        sql.append(")");
        return sql.toString();
    }
}