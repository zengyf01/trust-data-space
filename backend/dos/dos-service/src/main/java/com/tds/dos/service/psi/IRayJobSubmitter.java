package com.tds.dos.service.psi;

/**
 * Ray作业提交器接口
 * 负责将Python代码提交到Ray集群执行
 */
public interface IRayJobSubmitter {

    /**
     * 提交Python作业到Ray集群
     * @param rayHeadUrl Ray Head节点地址
     * @param jobName 作业名称
     * @param pythonCode Python代码
     * @return 作业ID
     */
    String submitJob(String rayHeadUrl, String jobName, String pythonCode) throws Exception;

    /**
     * 等待作业完成
     * @param jobId 作业ID
     * @param timeoutMs 超时时间(毫秒)
     * @return 作业状态
     */
    JobStatus waitForCompletion(String jobId, long timeoutMs) throws Exception;

    /**
     * 获取作业日志
     * @param jobId 作业ID
     * @return 作业日志
     */
    String getJobLogs(String jobId) throws Exception;

    /**
     * 获取作业状态
     * @param jobId 作业ID
     * @return 作业状态
     */
    JobStatus getJobStatus(String jobId) throws Exception;

    /**
     * 取消作业
     * @param jobId 作业ID
     * @return 是否取消成功
     */
    boolean cancelJob(String jobId) throws Exception;

    /**
     * 作业状态枚举
     */
    enum JobStatus {
        PENDING("PENDING", "等待中"),
        RUNNING("RUNNING", "运行中"),
        SUCCEEDED("SUCCEEDED", "成功"),
        FAILED("FAILED", "失败"),
        CANCELLED("CANCELLED", "已取消");

        private final String code;
        private final String description;

        JobStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }
}
