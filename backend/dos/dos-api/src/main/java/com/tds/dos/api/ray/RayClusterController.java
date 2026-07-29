package com.tds.dos.api.ray;

import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.service.ray.IRayClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ray集群管理API
 */
@RestController
@RequestMapping("/ray/cluster")
public class RayClusterController {

    @Autowired
    private IRayClusterService rayClusterService;

    /**
     * 创建Ray集群
     */
    @PostMapping("/create")
    public ApiResponse<Map<String, String>> createCluster(@RequestBody CreateClusterRequest request) {
        try {
            List<String> nodeIds = request.getNodeIds();
            if (nodeIds == null || nodeIds.isEmpty()) {
                return ApiResponse.error("节点列表不能为空");
            }

            String clusterId;
            if (request.getClusterName() != null && !request.getClusterName().isEmpty()) {
                clusterId = rayClusterService.createCluster(request.getClusterName(), nodeIds);
            } else {
                clusterId = rayClusterService.createCluster(nodeIds);
            }

            Map<String, String> data = new HashMap<>();
            data.put("clusterId", clusterId);
            data.put("headAddress", rayClusterService.getHeadAddress(clusterId));

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("创建集群失败: " + e.getMessage());
        }
    }

    /**
     * 释放集群（停止Worker，保留Head空闲）
     */
    @PostMapping("/release/{clusterId}")
    public ApiResponse<String> releaseCluster(@PathVariable String clusterId) {
        try {
            rayClusterService.releaseCluster(clusterId);
            return ApiResponse.success("集群已释放");
        } catch (Exception e) {
            return ApiResponse.error("释放集群失败: " + e.getMessage());
        }
    }

    /**
     * 销毁集群（停止所有节点）
     */
    @PostMapping("/destroy/{clusterId}")
    public ApiResponse<String> destroyCluster(@PathVariable String clusterId) {
        try {
            rayClusterService.destroyCluster(clusterId);
            return ApiResponse.success("集群已销毁");
        } catch (Exception e) {
            return ApiResponse.error("销毁集群失败: " + e.getMessage());
        }
    }

    /**
     * 获取集群信息
     */
    @GetMapping("/info/{clusterId}")
    public ApiResponse<Map<String, Object>> getClusterInfo(@PathVariable String clusterId) {
        try {
            String headAddress = rayClusterService.getHeadAddress(clusterId);
            String status = rayClusterService.getClusterStatus(clusterId);

            Map<String, Object> data = new HashMap<>();
            data.put("clusterId", clusterId);
            data.put("headAddress", headAddress);
            data.put("status", status);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("获取集群信息失败: " + e.getMessage());
        }
    }

    /**
     * 节点加入集群
     */
    @PostMapping("/join/{clusterId}/{nodeId}")
    public ApiResponse<String> joinCluster(@PathVariable String clusterId, @PathVariable String nodeId) {
        try {
            boolean success = rayClusterService.joinCluster(clusterId, nodeId);
            if (success) {
                return ApiResponse.success("节点已加入集群");
            } else {
                return ApiResponse.error("节点加入集群失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("节点加入集群失败: " + e.getMessage());
        }
    }

    /**
     * 节点离开集群
     */
    @PostMapping("/leave/{clusterId}/{nodeId}")
    public ApiResponse<String> leaveCluster(@PathVariable String clusterId, @PathVariable String nodeId) {
        try {
            boolean success = rayClusterService.leaveCluster(clusterId, nodeId);
            if (success) {
                return ApiResponse.success("节点已离开集群");
            } else {
                return ApiResponse.error("节点离开集群失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("节点离开集群失败: " + e.getMessage());
        }
    }

    /**
     * 获取节点所在的集群ID
     */
    @GetMapping("/node/{nodeId}")
    public ApiResponse<Map<String, String>> getNodeCluster(@PathVariable String nodeId) {
        try {
            String clusterId = rayClusterService.getNodeClusterId(nodeId);
            String rayAddress = rayClusterService.getNodeRayAddress(nodeId);

            Map<String, String> data = new HashMap<>();
            data.put("clusterId", clusterId != null ? clusterId : "");
            data.put("rayAddress", rayAddress != null ? rayAddress : "");

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("获取节点集群信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有集群列表
     */
    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> listClusters() {
        try {
            var clusters = rayClusterService.listClusters();
            List<Map<String, Object>> result = clusters.stream().map(cluster -> {
                Map<String, Object> item = new HashMap<>();
                item.put("clusterId", cluster.getfClusterId());
                item.put("clusterName", cluster.getfClusterName());
                item.put("headNodeId", cluster.getfHeadNodeId());
                item.put("headAddress", cluster.getfHeadAddress());
                item.put("status", cluster.getfStatus());
                item.put("participants", cluster.getfParticipants());
                item.put("createTime", cluster.getfCreateTime());
                item.put("updateTime", cluster.getfUpdateTime());
                return item;
            }).toList();
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("获取集群列表失败: " + e.getMessage());
        }
    }

    /**
     * 请求体
     */
    public static class CreateClusterRequest {
        private String clusterName;
        private List<String> nodeIds;

        public String getClusterName() { return clusterName; }
        public void setClusterName(String clusterName) { this.clusterName = clusterName; }
        public List<String> getNodeIds() { return nodeIds; }
        public void setNodeIds(List<String> nodeIds) { this.nodeIds = nodeIds; }
    }
}
