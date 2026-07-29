package com.tds.dos.service.msp.node;

import com.tds.dos.common.core.PageResult;
import com.tds.dos.common.enums.NodeStatus;
import com.tds.dos.dal.msp.entity.TbNode;

/**
 * Node Service interface
 */
public interface INodeService {
    String registerNode(NodeDTO dto);
    boolean unregisterNode(String nodeId);
    boolean heartbeat(String nodeId);
    TbNode getNode(String nodeId);
    PageResult<TbNode> listNodes(int page, int size, NodeStatus status);
    void updateNodeName(String nodeId, String nodeName);
}
