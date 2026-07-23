package com.tds.datar.service.open;

import com.tds.datar.common.exception.BusinessException;
import com.tds.datar.dal.entity.TbDataSource;
import com.tds.datar.dal.entity.TbCatalog;
import com.tds.datar.dal.entity.TbDataProduct;
import com.tds.datar.dal.mapper.TbDataSourceMapper;
import com.tds.datar.dal.mapper.TbCatalogMapper;
import com.tds.datar.dal.mapper.TbDataProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 连接器开放接口服务实现
 */
@Service
public class ConnectorOpenServiceImpl implements ConnectorOpenService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private TbDataSourceMapper dataSourceMapper;

    @Autowired
    private TbCatalogMapper catalogMapper;

    @Autowired
    private TbDataProductMapper productMapper;

    private static final String CONNECTOR_HEARTBEAT_PREFIX = "datar:connector:";
    private static final long HEARTBEAT_EXPIRE_SECONDS = 180; // 3分钟

    @Override
    public ConnectorOpenDTO registerConnector(ConnectorOpenDTO dto) {
        // 生成连接器ID
        String connectorId = UUID.randomUUID().toString().replace("-", "");
        dto.setConnectorId(connectorId);

        // 生成凭证
        String appId = "CN" + System.currentTimeMillis();
        String appKey = UUID.randomUUID().toString().replace("-", "");
        dto.setAppId(appId);
        dto.setAppKey(appKey);

        // 保存连接器注册信息到Redis
        String connectorKey = "datar:connector:register:" + dto.getSn();
        redisTemplate.opsForHash().put(connectorKey, "connectorId", connectorId);
        redisTemplate.opsForHash().put(connectorKey, "connectorName", dto.getConnectorName());
        redisTemplate.opsForHash().put(connectorKey, "connectorType", dto.getConnectorType());
        redisTemplate.opsForHash().put(connectorKey, "address", dto.getAddress());
        redisTemplate.opsForHash().put(connectorKey, "port", String.valueOf(dto.getPort()));
        redisTemplate.opsForHash().put(connectorKey, "appId", appId);
        redisTemplate.opsForHash().put(connectorKey, "appKey", appKey);

        return dto;
    }

    @Override
    public void heartbeat(String sn) {
        String heartbeatKey = CONNECTOR_HEARTBEAT_PREFIX + sn + ":heartbeat";
        redisTemplate.opsForValue().set(heartbeatKey, String.valueOf(System.currentTimeMillis()));
        redisTemplate.expire(heartbeatKey, java.time.Duration.ofSeconds(HEARTBEAT_EXPIRE_SECONDS));
    }

    @Override
    public boolean isConnectorOnline(String sn) {
        String heartbeatKey = CONNECTOR_HEARTBEAT_PREFIX + sn + ":heartbeat";
        return Boolean.TRUE.equals(redisTemplate.hasKey(heartbeatKey));
    }

    @Override
    public List<?> queryData(String connectorId, String catalogId, String condition, int limit) {
        // 查询数据目录
        TbCatalog catalog = catalogMapper.selectById(catalogId);
        if (catalog == null) {
            throw new BusinessException("目录不存在");
        }

        // 模拟返回数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, 10); i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", i);
            row.put("data", "sample_data_" + i);
            row.put("catalogId", catalogId);
            result.add(row);
        }

        return result;
    }

    @Override
    public String pushData(String connectorId, String catalogId, String data) {
        // 模拟数据推送
        return "{\"code\": 200, \"msg\": \"success\", \"pushed\": true, \"records\": 100}";
    }

    @Override
    public ConnectorOpenDTO createDefaultAccount(ConnectorOpenDTO dto) {
        // 生成默认账号
        String defaultUsername = "user_" + UUID.randomUUID().toString().substring(0, 8);
        String defaultPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        dto.setDefaultUsername(defaultUsername);
        dto.setDefaultPassword(defaultPassword);

        // 保存到Redis
        String accountKey = "datar:account:" + dto.getConnectorId();
        redisTemplate.opsForHash().put(accountKey, "username", defaultUsername);
        redisTemplate.opsForHash().put(accountKey, "password", defaultPassword);

        return dto;
    }

    @Override
    public String forwardApi(String address, String apiPath, String method, String requestBody) {
        // 简化实现：实际应使用HTTP客户端转发请求
        return "{\"code\": 200, \"msg\": \"success\", \"data\": {\"forwarded\": true}}";
    }
}