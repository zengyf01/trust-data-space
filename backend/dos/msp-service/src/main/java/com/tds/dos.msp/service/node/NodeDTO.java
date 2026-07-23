package com.tds.dos.msp.service.node;

import com.tds.dos.msp.common.enums.NodeStatus;
import java.util.List;

/**
 * Node DTO
 */
public class NodeDTO {
    private String nodeId;
    private String nodeName;
    private NodeStatus status;
    private String nodeMode;
    private String endpoint;
    private String externalEndpoint;
    private List<String> capabilities;
    private List<String> tags;

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public NodeStatus getStatus() { return status; }
    public void setStatus(NodeStatus status) { this.status = status; }
    public String getNodeMode() { return nodeMode; }
    public void setNodeMode(String nodeMode) { this.nodeMode = nodeMode; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getExternalEndpoint() { return externalEndpoint; }
    public void setExternalEndpoint(String externalEndpoint) { this.externalEndpoint = externalEndpoint; }
    public List<String> getCapabilities() { return capabilities; }
    public void setCapabilities(List<String> capabilities) { this.capabilities = capabilities; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}