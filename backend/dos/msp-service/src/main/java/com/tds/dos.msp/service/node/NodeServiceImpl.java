package com.tds.dos.msp.service.node;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tds.dos.msp.common.core.PageResult;
import com.tds.dos.msp.common.enums.NodeStatus;
import com.tds.dos.msp.common.exception.BusinessException;
import com.tds.dos.msp.dal.entity.TbMspNode;
import com.tds.dos.msp.dal.mapper.TbMspNodeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Node Service implementation
 */
@Service
public class NodeServiceImpl implements INodeService {
    private static final Logger log = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private TbMspNodeMapper nodeMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String registerNode(NodeDTO dto) {
        String nodeId = dto.getNodeId() != null ? dto.getNodeId() : UUID.randomUUID().toString().replace("-", "");

        TbMspNode existing = nodeMapper.selectOne(
            new LambdaQueryWrapper<TbMspNode>().eq(TbMspNode::getfNodeId, nodeId)
        );

        TbMspNode node = new TbMspNode();
        node.setfNodeId(nodeId);
        node.setfNodeName(dto.getNodeName());
        node.setfStatus(NodeStatus.ONLINE.getCode());
        node.setfNodeMode(dto.getNodeMode() != null ? dto.getNodeMode() : "RAY");
        node.setfEndpoint(dto.getEndpoint());
        node.setfExternalEndpoint(dto.getExternalEndpoint());
        node.setfLastHeartbeat(LocalDateTime.now());
        node.setfCreateTime(LocalDateTime.now());
        node.setfUpdateTime(LocalDateTime.now());

        try {
            node.setfCapabilities(objectMapper.writeValueAsString(dto.getCapabilities()));
            node.setfTags(objectMapper.writeValueAsString(dto.getTags()));
        } catch (Exception e) {
            log.warn("Failed to serialize capabilities/tags", e);
        }

        if (existing != null) {
            node.setfId(existing.getfId());
            nodeMapper.updateById(node);
        } else {
            node.setfId(UUID.randomUUID().toString().replace("-", ""));
            nodeMapper.insert(node);
        }

        log.info("Node registered: {}", nodeId);
        return nodeId;
    }

    @Override
    public boolean unregisterNode(String nodeId) {
        TbMspNode node = nodeMapper.selectOne(
            new LambdaQueryWrapper<TbMspNode>().eq(TbMspNode::getfNodeId, nodeId)
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
        TbMspNode node = nodeMapper.selectOne(
            new LambdaQueryWrapper<TbMspNode>().eq(TbMspNode::getfNodeId, nodeId)
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
    public TbMspNode getNode(String nodeId) {
        return nodeMapper.selectOne(
            new LambdaQueryWrapper<TbMspNode>().eq(TbMspNode::getfNodeId, nodeId)
        );
    }

    @Override
    public PageResult<TbMspNode> listNodes(int page, int size, NodeStatus status) {
        Page<TbMspNode> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TbMspNode> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(TbMspNode::getfStatus, status.getCode());
        }

        wrapper.orderByDesc(TbMspNode::getfCreateTime);
        IPage<TbMspNode> result = nodeMapper.selectPage(pageParam, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }
}