package com.tds.dos.service.connector;

import com.tds.dos.common.exception.BusinessException;
import com.tds.dos.service.connector.dto.ConnectorInfo;
import com.tds.dos.service.connector.dto.TunnelCommandResult;
import com.tds.dos.service.connector.dto.TunnelSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * TDS连接器客户端实现 - DOS通过HTTP调用TDS接口控制连接器
 */
@Service
public class TdsConnectorClient implements ITdsConnectorClient {

    private static final Logger logger = LoggerFactory.getLogger(TdsConnectorClient.class);

    @Value("${tds.api.url:http://tds-api}")
    private String tdsApiUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public List<ConnectorInfo> getAvailableConnectors(String spaceId, Integer type) {
        logger.info("获取可用连接器: spaceId={}, type={}", spaceId, type);

        try {
            String url = tdsApiUrl + "/connector/page?pageNumber=1&pageSize=100";
            if (spaceId != null && !spaceId.isEmpty()) {
                url += "&spaceId=" + spaceId;
            }
            if (type != null) {
                url += "&type=" + type;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "200".equals(String.valueOf(body.get("code")))) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
                List<ConnectorInfo> result = new ArrayList<>();
                for (Map<String, Object> item : list) {
                    ConnectorInfo info = new ConnectorInfo();
                    info.setId((String) item.get("id"));
                    info.setSn((String) item.get("sn"));
                    info.setName((String) item.get("name"));
                    info.setType((Integer) item.get("type"));
                    info.setStatus((String) item.get("status"));
                    info.setIpAddress((String) item.get("ipAddress"));
                    info.setInstitutionId((String) item.get("institutionId"));
                    info.setInstitutionName((String) item.get("institutionName"));
                    result.add(info);
                }
                return result;
            }
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("获取可用连接器失败: spaceId={}", spaceId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public TunnelSession applyTunnelSession(String sn) {
        logger.info("申请隧道会话: sn={}", sn);

        try {
            String url = tdsApiUrl + "/tunnel/register";

            Map<String, Object> request = new HashMap<>();
            request.put("sn", sn);
            request.put("publicKey", ""); // 由TDS生成临时密钥对
            request.put("connectorType", 1);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "200".equals(String.valueOf(body.get("code")))) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                TunnelSession session = new TunnelSession();
                session.setSessionId((String) data.get("sessionId"));
                session.setSn(sn);
                session.setTunnelPort((Integer) data.get("tunnelPort"));
                session.setTunnelHost((String) data.get("tunnelHost"));
                session.setServerPublicKey((String) data.get("serverPublicKey"));
                session.setStatus(0); // PENDING_AUTH = 0
                return session;
            }
            throw new BusinessException("申请隧道会话失败");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("申请隧道会话异常: sn={}", sn, e);
            throw new BusinessException("申请隧道会话异常: " + e.getMessage());
        }
    }

    @Override
    public TunnelCommandResult executeViaTunnel(String sessionId, String command) {
        logger.info("通过隧道执行命令: sessionId={}, command={}", sessionId, command);

        try {
            String url = tdsApiUrl + "/tunnel/" + sessionId + "/command";

            Map<String, String> request = new HashMap<>();
            request.put("command", command);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "200".equals(String.valueOf(body.get("code")))) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                TunnelCommandResult result = new TunnelCommandResult();
                result.setSessionId(sessionId);
                result.setSuccess((Boolean) data.get("success"));
                result.setOutput((String) data.get("output"));
                result.setErrorMessage((String) data.get("errorMessage"));
                Object execTime = data.get("executionTimeMs");
                result.setExecutionTimeMs(execTime != null ? ((Number) execTime).longValue() : 0);
                return result;
            }
            return TunnelCommandResult.failure(sessionId, "命令执行失败");
        } catch (Exception e) {
            logger.error("通过隧道执行命令异常: sessionId={}", sessionId, e);
            return TunnelCommandResult.failure(sessionId, e.getMessage());
        }
    }

    @Override
    public void releaseTunnelSession(String sessionId) {
        logger.info("释放隧道会话: sessionId={}", sessionId);

        try {
            String url = tdsApiUrl + "/tunnel/" + sessionId;
            restTemplate.delete(url);
        } catch (Exception e) {
            logger.error("释放隧道会话异常: sessionId={}", sessionId, e);
        }
    }

    @Override
    public TunnelStatus getTunnelStatus(String sn) {
        logger.info("查询隧道状态: sn={}", sn);

        try {
            String url = tdsApiUrl + "/tunnel/" + sn + "/status";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "200".equals(String.valueOf(body.get("code")))) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                TunnelStatus status = new TunnelStatus();
                status.setSn(sn);
                status.setStatus((String) data.get("status"));
                status.setStatusCode((Integer) data.get("statusCode"));
                status.setSessionId((String) data.get("sessionId"));
                status.setTunnelPort((Integer) data.get("tunnelPort"));
                status.setTunnelHost((String) data.get("tunnelHost"));
                return status;
            }
            return null;
        } catch (Exception e) {
            logger.error("查询隧道状态异常: sn={}", sn, e);
            return null;
        }
    }

    @Override
    public String getServerPublicKey() {
        logger.info("获取服务端公钥");

        try {
            String url = tdsApiUrl + "/tunnel/server/publickey";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "200".equals(String.valueOf(body.get("code")))) {
                Map<String, String> data = (Map<String, String>) body.get("data");
                return data.get("publicKey");
            }
            return null;
        } catch (Exception e) {
            logger.error("获取服务端公钥异常", e);
            return null;
        }
    }
}
