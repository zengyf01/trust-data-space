package com.tds.dos.service.msp.node;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tds.dos.common.core.PageResult;
import com.tds.dos.common.enums.NodeStatus;
import com.tds.dos.common.exception.BusinessException;
import com.tds.dos.dal.msp.entity.TbNode;
import com.tds.dos.dal.msp.mapper.TbNodeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Node Service implementation
 */
@Service
public class NodeServiceImpl implements INodeService {
    private static final Logger log = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private TbNodeMapper nodeMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String registerNode(NodeDTO dto) {
        String nodeId = dto.getNodeId() != null ? dto.getNodeId() : UUID.randomUUID().toString().replace("-", "");

        // 先物理删除已存在的记录（包括软删除的），避免唯一索引冲突
        nodeMapper.physicalDeleteByNodeId(nodeId);

        TbNode node = new TbNode();
        node.setfNodeId(nodeId);
        node.setfNodeName(dto.getNodeName());
        node.setfStatus(NodeStatus.ONLINE.getCode());
        node.setfNodeMode(dto.getNodeMode() != null ? dto.getNodeMode() : "RAY");
        node.setfEndpoint(dto.getEndpoint());
        node.setfExternalEndpoint(dto.getExternalEndpoint());
        node.setfRayEndpoint(dto.getRayEndpoint());
        node.setfLastHeartbeat(LocalDateTime.now());
        node.setfCreateTime(LocalDateTime.now());
        node.setfUpdateTime(LocalDateTime.now());
        node.setfDeleteMark(0);

        try {
            if (dto.getCapabilities() != null) {
                node.setfCapabilities(objectMapper.writeValueAsString(dto.getCapabilities()));
            }
            if (dto.getTags() != null) {
                node.setfTags(objectMapper.writeValueAsString(dto.getTags()));
            }
        } catch (Exception e) {
            log.warn("Failed to serialize capabilities/tags", e);
        }

        node.setfId(UUID.randomUUID().toString().replace("-", ""));
        nodeMapper.insert(node);

        log.info("Node registered: {}", nodeId);
        return nodeId;
    }

    @Override
    public boolean unregisterNode(String nodeId) {
        TbNode node = nodeMapper.selectOne(
            new LambdaQueryWrapper<TbNode>().eq(TbNode::getfNodeId, nodeId)
        );
        if (node == null) {
            throw new BusinessException("Node not found: " + nodeId);
        }
        nodeMapper.deleteById(node.getfId());
        log.info("Node unregistered: {}", nodeId);
        return true;
    }

    @Override
    public boolean heartbeat(String nodeId) {
        TbNode node = nodeMapper.selectOne(
            new LambdaQueryWrapper<TbNode>().eq(TbNode::getfNodeId, nodeId)
        );
        if (node == null) {
            throw new BusinessException("Node not found: " + nodeId);
        }
        node.setfLastHeartbeat(LocalDateTime.now());
        node.setfStatus(NodeStatus.ONLINE.getCode());
        node.setfUpdateTime(LocalDateTime.now());
        nodeMapper.updateById(node);
        return true;
    }

    @Override
    public TbNode getNode(String nodeId) {
        return nodeMapper.selectOne(
            new LambdaQueryWrapper<TbNode>().eq(TbNode::getfNodeId, nodeId)
        );
    }

    @Override
    public PageResult<TbNode> listNodes(int page, int size, NodeStatus status) {
        Page<TbNode> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TbNode> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(TbNode::getfStatus, status.getCode());
        }

        wrapper.orderByDesc(TbNode::getfCreateTime);
        IPage<TbNode> result = nodeMapper.selectPage(pageParam, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public void updateNodeName(String nodeId, String nodeName) {
        TbNode node = nodeMapper.selectOne(
            new LambdaQueryWrapper<TbNode>().eq(TbNode::getfNodeId, nodeId)
        );
        if (node == null) {
            throw new BusinessException("Node not found: " + nodeId);
        }
        node.setfNodeName(nodeName);
        node.setfUpdateTime(LocalDateTime.now());
        nodeMapper.updateById(node);
        log.info("Node {} name updated to: {}", nodeId, nodeName);
    }

    /**
     * 定时检测离线节点
     * 每分钟执行一次，检查所有在线节点的最后心跳时间
     * 如果超过1分钟没有心跳，则标记为离线
     */
    @Scheduled(fixedRate = 10000) // 每10秒执行一次
    public void checkOfflineNodes() {
        List<TbNode> onlineNodes = nodeMapper.selectList(
            new LambdaQueryWrapper<TbNode>()
                .eq(TbNode::getfStatus, NodeStatus.ONLINE.getCode())
                .eq(TbNode::getfDeleteMark, 0)
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusMinutes(1); // 超过1分钟没心跳视为离线

        for (TbNode node : onlineNodes) {
            if (node.getfLastHeartbeat() != null && node.getfLastHeartbeat().isBefore(threshold)) {
                node.setfStatus(NodeStatus.OFFLINE.getCode());
                node.setfUpdateTime(now);
                nodeMapper.updateById(node);
                log.info("Node {} marked as OFFLINE (last heartbeat: {})", node.getfNodeId(), node.getfLastHeartbeat());
            }
        }
    }
}
