package com.tds.dos.service.ray;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tds.dos.common.enums.NodeStatus;
import com.tds.dos.dal.msp.entity.TbNode;
import com.tds.dos.dal.msp.entity.TbRayCluster;
import com.tds.dos.dal.msp.mapper.TbNodeMapper;
import com.tds.dos.dal.msp.mapper.TbRayClusterMapper;
import com.tds.dos.service.msp.node.INodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Ray集群管理服务实现
 */
@Slf4j
@Service
public class RayClusterServiceImpl implements IRayClusterService {

    @Autowired
    private TbRayClusterMapper clusterMapper;

    @Autowired
    private TbNodeMapper nodeMapper;

    @Autowired
    private INodeService nodeService;

    @Autowired
    private IAgentClient agentClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String createCluster(List<String> nodeIds) {
        return createCluster(null, nodeIds);
    }

    @Override
    public String createCluster(String clusterName, List<String> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            throw new IllegalArgumentException("节点列表不能为空");
        }

        String clusterId = UUID.randomUUID().toString().replace("-", "");

        // 1. 获取所有节点信息
        List<TbNode> nodes = nodeIds.stream()
            .map(id -> nodeService.getNode(id))
            .filter(node -> node != null)
            .collect(Collectors.toList());

        if (nodes.isEmpty()) {
            throw new RuntimeException("未找到有效的节点");
        }

        // 2. 创建集群记录
        TbRayCluster cluster = new TbRayCluster();
        cluster.setfId(UUID.randomUUID().toString().replace("-", ""));
        cluster.setfClusterId(clusterId);
        cluster.setfClusterName(clusterName != null ? clusterName : "Cluster-" + clusterId.substring(0, 8));
        cluster.setfHeadNodeId(nodeIds.get(0));
        cluster.setfStatus("CREATING");
        try {
            cluster.setfParticipants(objectMapper.writeValueAsString(nodeIds));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("序列化节点列表失败", e);
        }
        cluster.setfCreateTime(LocalDateTime.now());
        cluster.setfUpdateTime(LocalDateTime.now());
        cluster.setfDeleteMark(0);
        clusterMapper.insert(cluster);

        // 3. 启动Head节点
        TbNode headNode = nodes.get(0);
        int rayPort = headNode.getfRayPort() != null ? headNode.getfRayPort() : 6379;

        String headAddress;
        try {
            headAddress = agentClient.startHead(headNode.getfEndpoint(), rayPort);
        } catch (Exception e) {
            log.error("Failed to start Ray Head on node {}, cleaning up cluster record", headNode.getfNodeId());
            cluster.setfStatus("FAILED");
            cluster.setfUpdateTime(LocalDateTime.now());
            clusterMapper.updateById(cluster);
            throw new RuntimeException("启动Ray Head失败: " + e.getMessage(), e);
        }

        // 4. 更新集群状态
        cluster.setfHeadAddress(headAddress);
        cluster.setfStatus("RUNNING");
        cluster.setfUpdateTime(LocalDateTime.now());
        clusterMapper.updateById(cluster);

        // 5. 更新Head节点状态
        headNode.setfRayStatus("RUNNING");
        headNode.setfRayEndpoint(headAddress);
        headNode.setfUpdateTime(LocalDateTime.now());
        nodeMapper.updateById(headNode);

        // 6. 启动Worker节点（PSI/FL/VFL任务不需要Worker，仍尝试启动但失败不阻断主流程）
        boolean allWorkersStarted = true;
        for (int i = 1; i < nodes.size(); i++) {
            TbNode workerNode = nodes.get(i);
            int workerPort = workerNode.getfRayPort() != null ? workerNode.getfRayPort() : 6379;

            log.info("Starting worker {} on node {}, head={}", i, workerNode.getfNodeId(), headAddress);
            try {
                String workerRayAddress = agentClient.startWorker(workerNode.getfEndpoint(), headAddress, workerPort);
                if (workerRayAddress != null) {
                    workerNode.setfRayStatus("RUNNING");
                    workerNode.setfRayEndpoint(workerRayAddress);
                    workerNode.setfUpdateTime(LocalDateTime.now());
                    nodeMapper.updateById(workerNode);
                    log.info("Worker {} started successfully, ray address: {}", workerNode.getfNodeId(), workerRayAddress);
                } else {
                    log.warn("Worker {} start returned null, marking as incomplete", workerNode.getfNodeId());
                    allWorkersStarted = false;
                }
            } catch (Exception e) {
                log.warn("Failed to start Worker on node {}: {}, marking as incomplete", workerNode.getfNodeId(), e.getMessage());
                allWorkersStarted = false;
            }
        }

        if (!allWorkersStarted) {
            cluster.setfStatus("PARTIAL");
            cluster.setfUpdateTime(LocalDateTime.now());
            clusterMapper.updateById(cluster);
            log.warn("Ray cluster {} created with partial workers, head={}", clusterId, headAddress);
        }

        log.info("Created Ray cluster {}, head: {}, participants: {}", clusterId, headAddress, nodeIds);
        return clusterId;
    }

    @Override
    public void releaseCluster(String clusterId) {
        TbRayCluster cluster = getClusterById(clusterId);
        if (cluster == null) {
            log.warn("Cluster {} not found", clusterId);
            return;
        }

        if (!"RUNNING".equals(cluster.getfStatus())) {
            log.warn("Cluster {} is not running, status: {}", clusterId, cluster.getfStatus());
            return;
        }

        // 只停止Worker节点，保留Head空闲
        try {
            List<String> nodeIds = objectMapper.readValue(cluster.getfParticipants(), List.class);
            String headAddress = cluster.getfHeadAddress();

            // 停止所有Worker
            for (int i = 1; i < nodeIds.size(); i++) {
                TbNode node = nodeService.getNode(nodeIds.get(i));
                if (node != null) {
                    agentClient.stopRay(node.getfEndpoint());
                    node.setfRayStatus("IDLE");
                    node.setfRayEndpoint(null);
                    node.setfUpdateTime(LocalDateTime.now());
                    nodeMapper.updateById(node);
                }
            }

            // Head保持空闲，但更新状态
            TbNode headNode = nodeService.getNode(cluster.getfHeadNodeId());
            if (headNode != null) {
                headNode.setfRayStatus("IDLE");
                // 保留headAddress，因为Head还在运行
                headNode.setfUpdateTime(LocalDateTime.now());
                nodeMapper.updateById(headNode);
            }

            cluster.setfStatus("STOPPED");
            cluster.setfUpdateTime(LocalDateTime.now());
            clusterMapper.updateById(cluster);

            log.info("Released cluster {}, head kept running at {}", clusterId, headAddress);
        } catch (Exception e) {
            log.error("Error releasing cluster {}: {}", clusterId, e.getMessage());
            throw new RuntimeException("释放集群失败", e);
        }
    }

    @Override
    public void destroyCluster(String clusterId) {
        TbRayCluster cluster = getClusterById(clusterId);
        if (cluster == null) {
            log.warn("Cluster {} not found", clusterId);
            return;
        }

        try {
            List<String> nodeIds = objectMapper.readValue(cluster.getfParticipants(), List.class);

            // 停止所有节点
            for (String nodeId : nodeIds) {
                TbNode node = nodeService.getNode(nodeId);
                if (node != null) {
                    agentClient.stopRay(node.getfEndpoint());
                    node.setfRayStatus("IDLE");
                    node.setfRayEndpoint(null);
                    node.setfUpdateTime(LocalDateTime.now());
                    nodeMapper.updateById(node);
                }
            }

            // 删除集群记录
            cluster.setfDeleteMark(1);
            cluster.setfUpdateTime(LocalDateTime.now());
            clusterMapper.updateById(cluster);

            log.info("Destroyed cluster {}", clusterId);
        } catch (Exception e) {
            log.error("Error destroying cluster {}: {}", clusterId, e.getMessage());
            throw new RuntimeException("销毁集群失败", e);
        }
    }

    @Override
    public String getHeadAddress(String clusterId) {
        TbRayCluster cluster = getClusterById(clusterId);
        return cluster != null ? cluster.getfHeadAddress() : null;
    }

    @Override
    public String getClusterStatus(String clusterId) {
        TbRayCluster cluster = getClusterById(clusterId);
        return cluster != null ? cluster.getfStatus() : null;
    }

    @Override
    public String getNodeClusterId(String nodeId) {
        // 查找节点所在的集群
        LambdaQueryWrapper<TbRayCluster> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TbRayCluster::getfParticipants, nodeId);
        wrapper.eq(TbRayCluster::getfDeleteMark, 0);
        wrapper.in(TbRayCluster::getfStatus, "CREATING", "RUNNING");

        TbRayCluster cluster = clusterMapper.selectOne(wrapper);
        return cluster != null ? cluster.getfClusterId() : null;
    }

    @Override
    public String getNodeRayAddress(String nodeId) {
        TbNode node = nodeService.getNode(nodeId);
        return node != null ? node.getfRayEndpoint() : null;
    }

    @Override
    public boolean joinCluster(String clusterId, String nodeId) {
        TbRayCluster cluster = getClusterById(clusterId);
        if (cluster == null) {
            throw new RuntimeException("集群不存在: " + clusterId);
        }

        if (!"RUNNING".equals(cluster.getfStatus())) {
            throw new RuntimeException("集群未在运行状态: " + cluster.getfStatus());
        }

        TbNode node = nodeService.getNode(nodeId);
        if (node == null) {
            throw new RuntimeException("节点不存在: " + nodeId);
        }

        int rayPort = node.getfRayPort() != null ? node.getfRayPort() : 6379;
        String workerRayAddress = agentClient.startWorker(node.getfEndpoint(), cluster.getfHeadAddress(), rayPort);

        if (workerRayAddress != null) {
            node.setfRayStatus("RUNNING");
            node.setfRayEndpoint(workerRayAddress);  // 使用Worker自己的Ray地址
            node.setfUpdateTime(LocalDateTime.now());
            nodeMapper.updateById(node);

            // 更新集群参与节点列表
            try {
                List<String> participants = objectMapper.readValue(cluster.getfParticipants(), List.class);
                if (!participants.contains(nodeId)) {
                    participants.add(nodeId);
                    cluster.setfParticipants(objectMapper.writeValueAsString(participants));
                    cluster.setfUpdateTime(LocalDateTime.now());
                    clusterMapper.updateById(cluster);
                }
            } catch (Exception e) {
                log.warn("Failed to update cluster participants: {}", e.getMessage());
            }

            log.info("Node {} joined cluster {}", nodeId, clusterId);
        }

        return workerRayAddress != null;
    }

    @Override
    public boolean leaveCluster(String clusterId, String nodeId) {
        TbRayCluster cluster = getClusterById(clusterId);
        if (cluster == null) {
            log.warn("Cluster {} not found", clusterId);
            return false;
        }

        TbNode node = nodeService.getNode(nodeId);
        if (node == null) {
            log.warn("Node {} not found", nodeId);
            return false;
        }

        // 如果是Head节点，不能离开
        if (nodeId.equals(cluster.getfHeadNodeId())) {
            throw new RuntimeException("Head节点不能离开集群");
        }

        boolean success = agentClient.stopRay(node.getfEndpoint());

        if (success) {
            node.setfRayStatus("IDLE");
            node.setfRayEndpoint(null);
            node.setfUpdateTime(LocalDateTime.now());
            nodeMapper.updateById(node);

            // 更新集群参与节点列表
            try {
                List<String> participants = objectMapper.readValue(cluster.getfParticipants(), List.class);
                participants.remove(nodeId);
                cluster.setfParticipants(objectMapper.writeValueAsString(participants));
                cluster.setfUpdateTime(LocalDateTime.now());
                clusterMapper.updateById(cluster);
            } catch (Exception e) {
                log.warn("Failed to update cluster participants: {}", e.getMessage());
            }

            log.info("Node {} left cluster {}", nodeId, clusterId);
        }

        return success;
    }

    @Override
    public List<TbRayCluster> listClusters() {
        return clusterMapper.selectList(
            new LambdaQueryWrapper<TbRayCluster>()
                .eq(TbRayCluster::getfDeleteMark, 0)
                .orderByDesc(TbRayCluster::getfCreateTime)
        );
    }

    @Override
    public TbRayCluster getCluster(String clusterId) {
        return getClusterById(clusterId);
    }

    private TbRayCluster getClusterById(String clusterId) {
        return clusterMapper.selectOne(
            new LambdaQueryWrapper<TbRayCluster>()
                .eq(TbRayCluster::getfClusterId, clusterId)
                .eq(TbRayCluster::getfDeleteMark, 0)
        );
    }
}
