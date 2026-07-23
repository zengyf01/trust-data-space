package com.tds.dos.msp.service.node;

import com.tds.dos.msp.common.core.PageResult;
import com.tds.dos.msp.common.enums.NodeStatus;
import com.tds.dos.msp.dal.entity.TbMspNode;

/**
 * Node Service interface
 */
public interface INodeService {
    String registerNode(NodeDTO dto);
    boolean unregisterNode(String nodeId);
    boolean heartbeat(String nodeId);
    TbMspNode getNode(String nodeId);
    PageResult<TbMspNode> listNodes(int page, int size, NodeStatus status);
}