package com.tds.service.connector;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.common.enums.ConnectorOperateType;
import com.tds.common.enums.ConnectorStatus;
import com.tds.common.enums.ConnectorVersionStatus;
import com.tds.common.exception.BusinessException;
import com.tds.dal.entity.TbConnector;
import com.tds.dal.entity.TbConnectorLog;
import com.tds.dal.entity.TbConnectorVersion;
import com.tds.dal.mapper.TbConnectorLogMapper;
import com.tds.dal.mapper.TbConnectorMapper;
import com.tds.dal.mapper.TbConnectorVersionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 连接器服务实现
 */
@Service
public class ConnectorServiceImpl implements IConnectorService {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorServiceImpl.class);

    /** 心跳Key前缀 */
    private static final String HEARTBEAT_KEY_PREFIX = "datar:connector:";
    private static final String HEARTBEAT_KEY_SUFFIX = ":heartbeat";

    /** 心跳TTL: 3分钟 */
    private static final long HEARTBEAT_TTL_MINUTES = 3;

    @Autowired
    private TbConnectorMapper connectorMapper;

    @Autowired
    private TbConnectorVersionMapper versionMapper;

    @Autowired
    private TbConnectorLogMapper logMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public IPage<TbConnector> getConnectorPage(int currentPage, int pageSize,
            String name, Integer type, Integer status) {
        Page<TbConnector> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbConnector> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(TbConnector::getName, name);
        }
        if (type != null) {
            wrapper.eq(TbConnector::getType, type);
        }
        if (status != null) {
            wrapper.eq(TbConnector::getStatus, status);
        }
        wrapper.orderByDesc(TbConnector::getfCreateTime);
        return connectorMapper.selectPage(page, wrapper);
    }

    @Override
    public TbConnector getConnectorById(String id) {
        return connectorMapper.selectById(id);
    }

    @Override
    public TbConnector getConnectorBySn(String sn) {
        LambdaQueryWrapper<TbConnector> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbConnector::getSn, sn);
        return connectorMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public TbConnector createConnector(ConnectorCreateDTO dto) {
        TbConnector connector = new TbConnector();
        connector.setId(UUID.randomUUID().toString().replace("-", ""));
        connector.setSn(generateSn());
        connector.setName(dto.getName());
        connector.setType(dto.getType());
        connector.setVersion(dto.getVersion());
        connector.setStatus(ConnectorStatus.PENDING_REGISTRATION.getCode());
        connector.setIpAddress(dto.getIpAddress());
        connector.setSshPort(dto.getSshPort());
        connector.setSshUsername(dto.getSshUsername());
        connector.setSshPassword(dto.getSshPassword());
        connector.setSshPrivateKey(dto.getSshPrivateKey());
        connector.setMacAddress(dto.getMacAddress());
        connector.setInstitutionId(dto.getInstitutionId());
        connector.setInstitutionName(dto.getInstitutionName());
        connector.setfSpaceId(dto.getSpaceId());
        connector.setRegion(dto.getRegion());
        connector.setDescription(dto.getDescription());
        connector.setRegisteredTime(LocalDateTime.now());
        connector.setfTenantId(dto.getTenantId());
        connector.setfCreateTime(LocalDateTime.now());
        connector.setfUpdateTime(LocalDateTime.now());
        connector.setfDeleteMark(0);

        connectorMapper.insert(connector);
        return connector;
    }

    @Override
    @Transactional
    public TbConnector updateConnector(String id, ConnectorCreateDTO dto) {
        TbConnector connector = connectorMapper.selectById(id);
        if (connector == null) {
            throw new BusinessException("连接器不存在");
        }
        connector.setName(dto.getName());
        connector.setType(dto.getType());
        connector.setVersion(dto.getVersion());
        connector.setIpAddress(dto.getIpAddress());
        connector.setSshPort(dto.getSshPort());
        connector.setSshUsername(dto.getSshUsername());
        connector.setSshPassword(dto.getSshPassword());
        connector.setSshPrivateKey(dto.getSshPrivateKey());
        connector.setMacAddress(dto.getMacAddress());
        connector.setInstitutionId(dto.getInstitutionId());
        connector.setInstitutionName(dto.getInstitutionName());
        connector.setRegion(dto.getRegion());
        connector.setDescription(dto.getDescription());
        connector.setfUpdateTime(LocalDateTime.now());

        connectorMapper.updateById(connector);
        return connector;
    }

    @Override
    @Transactional
    public void deleteConnector(String id) {
        TbConnector connector = connectorMapper.selectById(id);
        if (connector == null) {
            throw new BusinessException("连接器不存在");
        }
        connector.setfDeleteMark(1);
        connector.setfUpdateTime(LocalDateTime.now());
        connectorMapper.updateById(connector);
    }

    @Override
    public void heartbeat(String sn) {
        TbConnector connector = getConnectorBySn(sn);
        if (connector == null) {
            logger.warn("心跳上报失败：连接器不存在, sn={}", sn);
            return;
        }

        // 更新Redis心跳Key（TTL 3分钟）
        String heartbeatKey = HEARTBEAT_KEY_PREFIX + sn + HEARTBEAT_KEY_SUFFIX;
        redisTemplate.opsForValue().set(heartbeatKey, String.valueOf(System.currentTimeMillis()),
                HEARTBEAT_TTL_MINUTES, TimeUnit.MINUTES);

        // 更新数据库最后心跳时间
        connector.setLastHeartbeat(LocalDateTime.now());
        connector.setStatus(ConnectorStatus.ONLINE.getCode());
        connector.setfUpdateTime(LocalDateTime.now());
        connectorMapper.updateById(connector);

        logger.debug("心跳更新成功: sn={}", sn);
    }

    @Override
    public void checkConnectorStatus() {
        // 查询所有在线连接器
        LambdaQueryWrapper<TbConnector> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbConnector::getStatus, ConnectorStatus.ONLINE.getCode());
        List<TbConnector> onlineConnectors = connectorMapper.selectList(wrapper);

        for (TbConnector connector : onlineConnectors) {
            String heartbeatKey = HEARTBEAT_KEY_PREFIX + connector.getSn() + HEARTBEAT_KEY_SUFFIX;
            Boolean exists = redisTemplate.hasKey(heartbeatKey);

            if (exists == null || !exists) {
                // Redis key不存在或已过期，标记为离线
                connector.setStatus(ConnectorStatus.OFFLINE.getCode());
                connector.setfUpdateTime(LocalDateTime.now());
                connectorMapper.updateById(connector);
                logger.info("连接器离线: sn={}, name={}", connector.getSn(), connector.getName());
            }
        }
    }

    @Override
    public List<TbConnectorVersion> getConnectorVersions(String connectorId) {
        LambdaQueryWrapper<TbConnectorVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbConnectorVersion::getConnectorId, connectorId)
               .orderByDesc(TbConnectorVersion::getfCreateTime);
        return versionMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public TbConnectorVersion uploadVersion(ConnectorVersionDTO dto) {
        TbConnectorVersion version = new TbConnectorVersion();
        version.setId(UUID.randomUUID().toString().replace("-", ""));
        version.setConnectorId(dto.getConnectorId());
        version.setVersion(dto.getVersion());
        version.setFilePath(dto.getFilePath());
        version.setFileSize(dto.getFileSize());
        version.setFileMd5(dto.getFileMd5());
        version.setChangeLog(dto.getChangeLog());
        version.setStatus(ConnectorVersionStatus.INACTIVE.getCode());
        version.setfTenantId(dto.getTenantId());
        version.setfCreateTime(LocalDateTime.now());
        version.setfUpdateTime(LocalDateTime.now());
        version.setfDeleteMark(0);

        versionMapper.insert(version);
        return version;
    }

    @Override
    @Transactional
    public TbConnectorVersion activateVersion(String versionId) {
        TbConnectorVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException("版本不存在");
        }

        // 将该连接器的其他版本设为未激活
        LambdaQueryWrapper<TbConnectorVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbConnectorVersion::getConnectorId, version.getConnectorId());
        List<TbConnectorVersion> versions = versionMapper.selectList(wrapper);
        for (TbConnectorVersion v : versions) {
            if (!v.getId().equals(versionId)) {
                v.setStatus(ConnectorVersionStatus.INACTIVE.getCode());
                versionMapper.updateById(v);
            }
        }

        // 激活当前版本
        version.setStatus(ConnectorVersionStatus.ACTIVATED.getCode());
        version.setfUpdateTime(LocalDateTime.now());
        versionMapper.updateById(version);

        // 更新连接器当前版本
        TbConnector connector = connectorMapper.selectById(version.getConnectorId());
        if (connector != null) {
            connector.setVersion(version.getVersion());
            connector.setfUpdateTime(LocalDateTime.now());
            connectorMapper.updateById(connector);
        }

        return version;
    }

    @Override
    public List<TbConnectorLog> getConnectorLogs(String connectorId) {
        LambdaQueryWrapper<TbConnectorLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbConnectorLog::getConnectorId, connectorId)
               .orderByDesc(TbConnectorLog::getfCreateTime);
        return logMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public TbConnectorLog executeOperation(ConnectorOperateDTO dto) {
        TbConnector connector = connectorMapper.selectById(dto.getConnectorId());
        if (connector == null) {
            throw new BusinessException("连接器不存在");
        }

        TbConnectorLog log = new TbConnectorLog();
        log.setId(UUID.randomUUID().toString().replace("-", ""));
        log.setConnectorId(dto.getConnectorId());
        log.setOperateType(dto.getOperateType());
        log.setOperateContent(dto.getOperateContent());
        log.setStartTime(LocalDateTime.now());
        log.setfTenantId(connector.getfTenantId());
        log.setfCreateTime(LocalDateTime.now());
        log.setfDeleteMark(0);

        try {
            // 模拟SSH执行远程命令
            boolean success = executeSshCommand(connector, dto.getOperateType(), dto.getOperateContent());

            log.setOperateResult(success ? "SUCCESS" : "FAIL");
            if (!success) {
                log.setErrorMessage("操作执行失败");
            }
        } catch (Exception e) {
            log.setOperateResult("FAIL");
            log.setErrorMessage(e.getMessage());
            logger.error("连接器操作失败: connectorId={}, operateType={}",
                    dto.getConnectorId(), dto.getOperateType(), e);
        }

        log.setEndTime(LocalDateTime.now());
        if (log.getStartTime() != null && log.getEndTime() != null) {
            long seconds = ChronoUnit.SECONDS.between(log.getStartTime(), log.getEndTime());
            log.setDuration((int) seconds);
        }

        logMapper.insert(log);
        return log;
    }

    /**
     * 模拟SSH执行远程命令
     */
    private boolean executeSshCommand(TbConnector connector, String operateType, String operateContent) {
        // 实际生产环境应使用JSch或Apache MINA SSH实现
        // 这里仅做模拟实现
        ConnectorOperateType type = ConnectorOperateType.fromCode(operateType);
        if (type == null) {
            throw new BusinessException("不支持的操作类型: " + operateType);
        }

        logger.info("模拟SSH执行: connector={}, type={}, content={}",
                connector.getName(), operateType, operateContent);

        // 模拟执行时间
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return true;
    }

    /**
     * 生成连接器序列号
     */
    private String generateSn() {
        return "CN" + System.currentTimeMillis();
    }
}