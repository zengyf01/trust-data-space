package com.tds.datar.service.open;

import com.tds.datar.service.open.ConnectorOpenDTO;
import com.tds.datar.dal.entity.TbDataSource;
import com.tds.datar.dal.entity.TbCatalog;
import com.tds.datar.dal.entity.TbDataProduct;
import java.util.List;

/**
 * 连接器开放接口服务
 */
public interface ConnectorOpenService {

    /**
     * 注册连接器
     */
    ConnectorOpenDTO registerConnector(ConnectorOpenDTO dto);

    /**
     * 发送心跳
     */
    void heartbeat(String sn);

    /**
     * 获取连接器状态
     */
    boolean isConnectorOnline(String sn);

    /**
     * 统一数据查询接口
     */
    List<?> queryData(String connectorId, String catalogId, String condition, int limit);

    /**
     * 统一数据推送接口
     */
    String pushData(String connectorId, String catalogId, String data);

    /**
     * 创建默认账号
     */
    ConnectorOpenDTO createDefaultAccount(ConnectorOpenDTO dto);

    /**
     * 转发API请求
     */
    String forwardApi(String address, String apiPath, String method, String requestBody);
}