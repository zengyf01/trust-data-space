package com.tds.dos.service.ray;

import com.tds.dos.dal.msp.entity.TbRayCluster;

import java.util.List;

/**
 * Ray集群管理服务接口
 */
public interface IRayClusterService {

    /**
     * 创建Ray集群
     * @param nodeIds 参与节点ID列表，第一个节点作为Head
     * @return 集群ID
     */
    String createCluster(List<String> nodeIds);

    /**
     * 创建Ray集群（带名称）
     * @param clusterName 集群名称
     * @param nodeIds 参与节点ID列表
     * @return 集群ID
     */
    String createCluster(String clusterName, List<String> nodeIds);

    /**
     * 解散Ray集群（停止Worker，保留Head空闲）
     * @param clusterId 集群ID
     */
    void releaseCluster(String clusterId);

    /**
     * 完全销毁Ray集群（停止所有节点）
     * @param clusterId 集群ID
     */
    void destroyCluster(String clusterId);

    /**
     * 获取集群Head地址
     * @param clusterId 集群ID
     * @return Head地址
     */
    String getHeadAddress(String clusterId);

    /**
     * 获取集群状态
     * @param clusterId 集群ID
     * @return 集群状态
     */
    String getClusterStatus(String clusterId);

    /**
     * 获取节点当前所在的集群ID
     * @param nodeId 节点ID
     * @return 集群ID，如果没有则返回null
     */
    String getNodeClusterId(String nodeId);

    /**
     * 获取节点当前的Ray地址
     * @param nodeId 节点ID
     * @return Ray地址
     */
    String getNodeRayAddress(String nodeId);

    /**
     * 节点加入已有集群
     * @param clusterId 集群ID
     * @param nodeId 节点ID
     * @return 是否成功
     */
    boolean joinCluster(String clusterId, String nodeId);

    /**
     * 节点离开集群
     * @param clusterId 集群ID
     * @param nodeId 节点ID
     * @return 是否成功
     */
    boolean leaveCluster(String clusterId, String nodeId);

    /**
     * 获取所有集群列表
     * @return 集群列表
     */
    List<TbRayCluster> listClusters();

    /**
     * 根据集群ID获取集群详情
     * @param clusterId 集群ID
     * @return 集群信息
     */
    TbRayCluster getCluster(String clusterId);
}
