package com.tds.service.deploy;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dal.entity.TbDeployInstance;
import com.tds.dal.entity.TbDeployNode;
import com.tds.dal.entity.TbLocalAccount;

import java.util.List;
import java.util.Map;

/**
 * 分布式部署服务接口
 */
public interface IDeployService {

    // ==================== 部署节点管理 ====================

    /**
     * 分页查询部署节点
     */
    IPage<TbDeployNode> getNodePage(int currentPage, int pageSize, String nodeType, Integer status);

    /**
     * 获取节点详情
     */
    TbDeployNode getNodeById(String id);

    /**
     * 创建部署节点
     */
    TbDeployNode createNode(DeployNodeDTO dto);

    /**
     * 更新部署节点
     */
    TbDeployNode updateNode(String id, DeployNodeDTO dto);

    /**
     * 删除部署节点
     */
    void deleteNode(String id);

    /**
     * 节点心跳
     */
    TbDeployNode heartbeat(String nodeCode);

    /**
     * 获取所有在线节点
     */
    List<TbDeployNode> getOnlineNodes();

    // ==================== 部署实例管理 ====================

    /**
     * 分页查询部署实例
     */
    IPage<TbDeployInstance> getInstancePage(int currentPage, int pageSize, String nodeId, String serviceType);

    /**
     * 获取实例详情
     */
    TbDeployInstance getInstanceById(String id);

    /**
     * 创建部署实例
     */
    TbDeployInstance createInstance(DeployInstanceDTO dto);

    /**
     * 更新部署实例
     */
    TbDeployInstance updateInstance(String id, DeployInstanceDTO dto);

    /**
     * 删除部署实例
     */
    void deleteInstance(String id);

    /**
     * 启动实例
     */
    TbDeployInstance startInstance(String id);

    /**
     * 停止实例
     */
    TbDeployInstance stopInstance(String id);

    /**
     * 重启实例
     */
    TbDeployInstance restartInstance(String id);

    // ==================== 本地账户管理 ====================

    /**
     * 分页查询本地账户
     */
    IPage<TbLocalAccount> getAccountPage(int currentPage, int pageSize, String orgId, String accountType);

    /**
     * 获取账户详情
     */
    TbLocalAccount getAccountById(String id);

    /**
     * 创建本地账户
     */
    TbLocalAccount createAccount(LocalAccountDTO dto);

    /**
     * 更新本地账户
     */
    TbLocalAccount updateAccount(String id, LocalAccountDTO dto);

    /**
     * 删除本地账户
     */
    void deleteAccount(String id);

    /**
     * 本地认证
     */
    Map<String, Object> localAuth(String accountCode, String credential);

    /**
     * 切换部署模式
     */
    Map<String, Object> switchDeployMode(String mode, String nodeId);

    /**
     * 离线模式操作
     */
    Map<String, Object> offlineOperation(String operation, Map<String, Object> params);
}