package com.tds.dos.service.sandbox;

import java.util.List;
import java.util.Map;

/**
 * 安全沙盒服务接口
 */
public interface ISandboxService {

    /**
     * 创建沙盒
     * @param workOrderId 工单ID
     * @param image 镜像名称
     * @param cpu CPU核心数
     * @param memoryMB 内存MB
     * @param workDir 工作目录
     * @param sourceUrl 源代码URL（可选）
     * @return 沙盒信息
     */
    Map<String, Object> createSandbox(String workOrderId, String image, int cpu, int memoryMB,
                                       String workDir, String sourceUrl);

    /**
     * 销毁沙盒
     * @param podName Pod名称
     */
    void destroySandbox(String podName);

    /**
     * 停止沙盒
     * @param podName Pod名称
     */
    void stopSandbox(String podName);

    /**
     * 获取沙盒状态
     * @param podName Pod名称
     * @return 状态信息
     */
    Map<String, Object> getSandboxStatus(String podName);

    /**
     * 获取沙盒详情
     * @param podName Pod名称
     * @return 详细信息
     */
    Map<String, Object> getSandboxDetail(String podName);

    /**
     * 获取沙盒日志
     * @param podName Pod名称
     * @param tail 是否只获取尾部日志
     * @return 日志内容
     */
    String getSandboxLogs(String podName, boolean tail);

    /**
     * 获取沙盒事件
     * @param podName Pod名称
     * @return 事件列表
     */
    List<Map<String, Object>> getSandboxEvents(String podName);

    /**
     * 获取用户的沙盒列表
     * @param userId 用户ID
     * @return 沙盒列表
     */
    List<Map<String, Object>> listSandboxes(String userId);

    /**
     * 等待JupyterLab就绪
     * @param podName Pod名称
     * @param timeoutSeconds 超时秒数
     * @return JupyterLab URL
     */
    String waitForJupyterLab(String podName, int timeoutSeconds);
}