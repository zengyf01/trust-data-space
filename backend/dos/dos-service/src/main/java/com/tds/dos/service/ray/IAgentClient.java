package com.tds.dos.service.ray;

/**
 * Agent客户端接口 - 用于DOS平台调用节点Agent
 */
public interface IAgentClient {

    /**
     * 启动Ray Head节点
     * @param agentEndpoint Agent HTTP地址，如 http://192.168.1.100:8081
     * @param rayPort Ray服务端口
     * @return Ray地址，如 ray://192.168.1.100:10001
     */
    String startHead(String agentEndpoint, int rayPort);

    /**
     * 启动Ray Worker节点，加入已有集群
     * @param agentEndpoint Agent HTTP地址
     * @param headAddress Head节点地址，如 ray://192.168.1.100:10001
     * @param rayPort Ray服务端口
     * @return Worker的Ray地址，如 ray://192.168.1.101:10001，失败返回null
     */
    String startWorker(String agentEndpoint, String headAddress, int rayPort);

    /**
     * 停止Ray节点
     * @param agentEndpoint Agent HTTP地址
     * @return 是否成功
     */
    boolean stopRay(String agentEndpoint);

    /**
     * 查询Ray状态
     * @param agentEndpoint Agent HTTP地址
     * @return Ray状态信息
     */
    RayStatus getRayStatus(String agentEndpoint);

    /**
     * 提交任务到Ray集群执行
     * @param agentEndpoint Agent HTTP地址（任务提交到的节点）
     * @param script Python脚本内容
     * @param taskId 任务ID
     * @return 任务提交成功返回jobId
     */
    String submitJob(String agentEndpoint, String script, String taskId);

    /**
     * 查询任务状态
     * @param agentEndpoint Agent HTTP地址
     * @param jobId 任务ID
     * @return 任务状态
     */
    TaskStatus getTaskStatus(String agentEndpoint, String jobId);

    /**
     * 停止任务
     * @param agentEndpoint Agent HTTP地址
     * @param jobId 任务ID
     * @return 是否成功
     */
    boolean stopJob(String agentEndpoint, String jobId);

    /**
     * 下载任务输出文件
     * @param agentEndpoint Agent HTTP地址
     * @param jobId 任务ID（仅用于 Agent 鉴权 SUCCEEDED 状态）
     * @param filePath 容器内文件绝对路径
     * @return 文件字节内容
     */
    byte[] downloadTaskFile(String agentEndpoint, String jobId, String filePath);

    /**
     * 直接下载节点上的文件（旁路，不依赖 jobId 鉴权）
     * @param agentEndpoint Agent HTTP地址
     * @param filePath 节点上文件绝对路径
     * @return 文件字节内容
     */
    byte[] downloadNodeFile(String agentEndpoint, String filePath);

    /**
     * Ray状态信息
     */
    class RayStatus {
        private boolean running;
        private String clusterId;
        private String rayAddress;
        /** 节点容器的真实IP，用于节点间SecretFlow通信。不要从rayAddress解析，那可能是过期地址 */
        private String nodeIp;

        public boolean isRunning() { return running; }
        public void setRunning(boolean running) { this.running = running; }
        public String getClusterId() { return clusterId; }
        public void setClusterId(String clusterId) { this.clusterId = clusterId; }
        public String getRayAddress() { return rayAddress; }
        public void setRayAddress(String rayAddress) { this.rayAddress = rayAddress; }
        public String getNodeIp() { return nodeIp; }
        public void setNodeIp(String nodeIp) { this.nodeIp = nodeIp; }
    }

    /**
     * 任务状态信息
     */
    class TaskStatus {
        private String status;  // PENDING/RUNNING/SUCCEEDED/FAILED/STOPPED
        private String result;
        private String error;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
