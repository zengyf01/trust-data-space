package com.tds.service.connector;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dal.entity.TbConnector;
import com.tds.dal.entity.TbConnectorLog;
import com.tds.dal.entity.TbConnectorVersion;

import java.util.List;

/**
 * 连接器服务接口
 */
public interface IConnectorService {

    /**
     * 分页查询连接器
     */
    IPage<TbConnector> getConnectorPage(int currentPage, int pageSize, String name, Integer type, Integer status);

    /**
     * 获取连接器详情
     */
    TbConnector getConnectorById(String id);

    /**
     * 获取连接器详情（按SN）
     */
    TbConnector getConnectorBySn(String sn);

    /**
     * 创建连接器
     */
    TbConnector createConnector(ConnectorCreateDTO dto);

    /**
     * 更新连接器
     */
    TbConnector updateConnector(String id, ConnectorCreateDTO dto);

    /**
     * 删除连接器
     */
    void deleteConnector(String id);

    /**
     * 心跳上报
     */
    void heartbeat(String sn);

    /**
     * 检测连接器状态（离线检测）
     */
    void checkConnectorStatus();

    /**
     * 获取连接器版本列表
     */
    List<TbConnectorVersion> getConnectorVersions(String connectorId);

    /**
     * 上传新版本
     */
    TbConnectorVersion uploadVersion(ConnectorVersionDTO dto);

    /**
     * 激活版本
     */
    TbConnectorVersion activateVersion(String versionId);

    /**
     * 获取连接器操作日志
     */
    List<TbConnectorLog> getConnectorLogs(String connectorId);

    /**
     * 执行远程操作（部署/升级/卸载）
     */
    TbConnectorLog executeOperation(ConnectorOperateDTO dto);
}