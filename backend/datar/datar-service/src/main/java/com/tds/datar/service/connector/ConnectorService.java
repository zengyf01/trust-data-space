package com.tds.datar.service.connector;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.datar.common.core.PageResult;
import com.tds.datar.dal.entity.TbConnector;
import com.tds.datar.dal.entity.TbConnectorLog;
import com.tds.datar.dal.entity.TbConnectorVersion;
import com.tds.datar.dal.mapper.TbConnectorLogMapper;
import com.tds.datar.dal.mapper.TbConnectorMapper;
import com.tds.datar.dal.mapper.TbConnectorVersionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConnectorService {

    @Autowired
    private TbConnectorMapper connectorMapper;

    @Autowired
    private TbConnectorVersionMapper versionMapper;

    @Autowired
    private TbConnectorLogMapper logMapper;

    public PageResult getConnectorPage(int currentPage, int pageSize, String keyword, Integer type, Integer status, String spaceId) {
        Page<TbConnector> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbConnector> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(TbConnector::getfName, keyword)
                    .or().like(TbConnector::getfSn, keyword));
        }
        if (type != null) {
            wrapper.eq(TbConnector::getfType, type);
        }
        if (status != null) {
            wrapper.eq(TbConnector::getfStatus, status);
        }
        if (spaceId != null && !spaceId.isEmpty()) {
            wrapper.eq(TbConnector::getfSpaceId, spaceId);
        }
        wrapper.orderByDesc(TbConnector::getfCreateTime);
        IPage<TbConnector> result = connectorMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public TbConnector getConnectorById(String id) {
        return connectorMapper.selectById(id);
    }

    public TbConnector getConnectorBySn(String sn) {
        LambdaQueryWrapper<TbConnector> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbConnector::getfSn, sn);
        return connectorMapper.selectOne(wrapper);
    }

    public TbConnector createConnector(String name, String sn, Integer type, String ipAddress,
                                       Integer sshPort, String sshUsername, String sshPassword,
                                       String institutionName, String region, String description, String spaceId) {
        // 检查SN是否已存在
        TbConnector existing = getConnectorBySn(sn);
        if (existing != null) {
            throw new RuntimeException("连接器SN已存在");
        }

        TbConnector connector = new TbConnector();
        connector.setfId(UUID.randomUUID().toString().replace("-", ""));
        connector.setfSn(sn);
        connector.setfName(name);
        connector.setfType(type != null ? type : 1);
        connector.setfStatus(3); // 离线待注册
        connector.setfIpAddress(ipAddress);
        connector.setfSshPort(sshPort);
        connector.setfSshUsername(sshUsername);
        connector.setfSshPassword(sshPassword);
        connector.setfInstitutionName(institutionName);
        connector.setfRegion(region);
        connector.setfDescription(description);
        connector.setfSpaceId(spaceId);
        connector.setfIsSystem(0);
        connector.setfRegisteredTime(LocalDateTime.now());
        connector.setfCreateTime(LocalDateTime.now());
        connector.setfDeleteMark(0);
        connectorMapper.insert(connector);
        return connector;
    }

    public TbConnector updateConnector(String id, String name, Integer type, String ipAddress,
                                       Integer sshPort, String sshUsername, String sshPassword,
                                       String institutionName, String region, String description) {
        TbConnector connector = connectorMapper.selectById(id);
        if (connector == null) {
            throw new RuntimeException("连接器不存在");
        }
        if (name != null) connector.setfName(name);
        if (type != null) connector.setfType(type);
        if (ipAddress != null) connector.setfIpAddress(ipAddress);
        if (sshPort != null) connector.setfSshPort(sshPort);
        if (sshUsername != null) connector.setfSshUsername(sshUsername);
        if (sshPassword != null) connector.setfSshPassword(sshPassword);
        if (institutionName != null) connector.setfInstitutionName(institutionName);
        if (region != null) connector.setfRegion(region);
        if (description != null) connector.setfDescription(description);
        connector.setfUpdateTime(LocalDateTime.now());
        connectorMapper.updateById(connector);
        return connector;
    }

    public void deleteConnector(String id) {
        TbConnector connector = connectorMapper.selectById(id);
        if (connector == null) {
            throw new RuntimeException("连接器不存在");
        }
        if (connector.getfIsSystem() == 1) {
            throw new RuntimeException("系统连接器不能删除");
        }
        connector.setfDeleteMark(1);
        connectorMapper.updateById(connector);
    }

    public void heartbeat(String sn) {
        TbConnector connector = getConnectorBySn(sn);
        if (connector != null) {
            connector.setfLastHeartbeat(LocalDateTime.now());
            connector.setfStatus(1); // 在线
            connector.setfUpdateTime(LocalDateTime.now());
            connectorMapper.updateById(connector);
        }
    }

    public void checkConnectorStatus() {
        LambdaQueryWrapper<TbConnector> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbConnector::getfStatus, 1);
        List<TbConnector> onlineConnectors = connectorMapper.selectList(wrapper);

        for (TbConnector connector : onlineConnectors) {
            if (connector.getfLastHeartbeat() != null) {
                // 超过3分钟无心跳，标记为离线
                if (connector.getfLastHeartbeat().plusMinutes(3).isBefore(LocalDateTime.now())) {
                    connector.setfStatus(2);
                    connector.setfUpdateTime(LocalDateTime.now());
                    connectorMapper.updateById(connector);
                }
            }
        }
    }

    public List<TbConnectorVersion> getConnectorVersions(String connectorId) {
        LambdaQueryWrapper<TbConnectorVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbConnectorVersion::getfConnectorId, connectorId)
                .orderByDesc(TbConnectorVersion::getfCreateTime);
        return versionMapper.selectList(wrapper);
    }

    public TbConnectorVersion uploadVersion(String connectorId, String version, String changeLog) {
        TbConnectorVersion v = new TbConnectorVersion();
        v.setfId(UUID.randomUUID().toString().replace("-", ""));
        v.setfConnectorId(connectorId);
        v.setfVersion(version);
        v.setfChangeLog(changeLog);
        v.setfStatus(0);
        v.setfCreateTime(LocalDateTime.now());
        v.setfDeleteMark(0);
        versionMapper.insert(v);
        return v;
    }

    public TbConnectorVersion activateVersion(String versionId) {
        TbConnectorVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new RuntimeException("版本不存在");
        }

        // 先停用该连接器的所有版本
        LambdaQueryWrapper<TbConnectorVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbConnectorVersion::getfConnectorId, version.getfConnectorId());
        List<TbConnectorVersion> allVersions = versionMapper.selectList(wrapper);
        for (TbConnectorVersion v : allVersions) {
            if (v.getfStatus() == 2) {
                v.setfStatus(0);
                versionMapper.updateById(v);
            }
        }

        // 激活指定版本
        version.setfStatus(2);
        versionMapper.updateById(version);

        // 更新连接器当前版本
        TbConnector connector = connectorMapper.selectById(version.getfConnectorId());
        if (connector != null) {
            connector.setfVersion(version.getfVersion());
            connector.setfUpdateTime(LocalDateTime.now());
            connectorMapper.updateById(connector);
        }

        return version;
    }

    public List<TbConnectorLog> getConnectorLogs(String connectorId) {
        LambdaQueryWrapper<TbConnectorLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbConnectorLog::getfConnectorId, connectorId)
                .orderByDesc(TbConnectorLog::getfCreateTime);
        return logMapper.selectList(wrapper);
    }

    public TbConnectorLog executeOperation(String connectorId, String operateType, String operateContent) {
        TbConnector connector = connectorMapper.selectById(connectorId);
        if (connector == null) {
            throw new RuntimeException("连接器不存在");
        }

        TbConnectorLog log = new TbConnectorLog();
        log.setfId(UUID.randomUUID().toString().replace("-", ""));
        log.setfConnectorId(connectorId);
        log.setfOperateType(operateType);
        log.setfOperateContent(operateContent);
        log.setfStartTime(LocalDateTime.now());
        log.setfCreateTime(LocalDateTime.now());
        log.setfDeleteMark(0);

        // 模拟操作执行
        try {
            // 实际场景中这里会调用SSH执行远程命令
            log.setfOperateResult("SUCCESS");
            log.setfEndTime(LocalDateTime.now());
            log.setfDuration(0);
        } catch (Exception e) {
            log.setfOperateResult("FAIL");
            log.setfErrorMessage(e.getMessage());
            log.setfEndTime(LocalDateTime.now());
        }

        logMapper.insert(log);
        return log;
    }

    public List<TbConnector> getAllConnectors() {
        LambdaQueryWrapper<TbConnector> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TbConnector::getfCreateTime);
        return connectorMapper.selectList(wrapper);
    }
}