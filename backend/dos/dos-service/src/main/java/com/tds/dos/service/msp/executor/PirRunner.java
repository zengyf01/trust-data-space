package com.tds.dos.service.msp.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tds.dos.service.ray.IAgentClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * PIR C++ 二进制执行器
 * <p>
 * 通过 Agent 在节点上直接执行 /usr/local/bin/psi-main（DIRECT 模式）。
 * 节点镜像需内置 psi-main 二进制（通过 Dockerfile COPY --from=... 实现）。
 * <p>
 * 使用方式：
 * <pre>
 * // Setup：服务端 bob 执行
 * PirRunner.Result setupResult = pirRunner.run(
 *     nodeAgentEndpoint, setupJobId, setupJsonConfig, PirRunner.RunMode.SETUP, PirRunner.PirProtocol.SealPIR
 * );
 *
 * // Query：客户端 alice 执行
 * PirRunner.Result queryResult = pirRunner.run(
 *     clientAgentEndpoint, queryJobId, queryJsonConfig, PirRunner.RunMode.QUERY, PirRunner.PirProtocol.SealPIR
 * );
 * </pre>
 */
@Slf4j
@Component
public class PirRunner {

    /** C++ 二进制路径（节点内置） */
    public static final String BINARY_PATH = "/usr/local/bin/psi-main";

    /** 外部 cancel 触发的取消标志 */
    private static final ConcurrentMap<String, Boolean> CANCEL_FLAGS = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private IAgentClient agentClient;

    @Value("${privacy-compute.pir.docker-timeout-ms:540000}")
    private long pirTimeoutMs;

    /**
     * 运行阶段
     */
    public enum RunMode {
        SETUP,
        QUERY
    }

    /**
     * PIR 协议类型
     */
    public enum PirProtocol {
        SealPIR,
        APSI
    }

    /**
     * 执行结果
     */
    public static class Result {
        private final boolean success;
        private final String stdout;
        private final String stderr;
        private final int exitCode;
        private final String error;

        private Result(boolean success, String stdout, String stderr, int exitCode, String error) {
            this.success = success;
            this.stdout = stdout;
            this.stderr = stderr;
            this.exitCode = exitCode;
            this.error = error;
        }

        public static Result success(String stdout) {
            return new Result(true, stdout, null, 0, null);
        }

        public static Result failure(int exitCode, String stderr) {
            return new Result(false, null, stderr, exitCode, stderr);
        }

        public static Result error(String error) {
            return new Result(false, null, null, -1, error);
        }

        public boolean isSuccess() { return success; }
        public String getStdout() { return stdout; }
        public String getStderr() { return stderr; }
        public int getExitCode() { return exitCode; }
        public String getError() { return error; }

        /** 从 stdout 中解析 TDS_PIR_RESULT= 前缀的 JSON */
        public String parseResultJson() {
            if (stdout == null || stdout.isEmpty()) {
                return null;
            }
            int index = stdout.lastIndexOf("TDS_PIR_RESULT=");
            if (index < 0) {
                return null;
            }
            int start = index + "TDS_PIR_RESULT=".length();
            int end = stdout.indexOf('\n', start);
            return (end < 0 ? stdout.substring(start) : stdout.substring(start, end)).trim();
        }

        /** 从 stdout 中解析错误信息 */
        public String parseError() {
            if (stderr != null && !stderr.isEmpty()) {
                return stderr;
            }
            if (stdout == null || stdout.isEmpty()) {
                return error;
            }
            int index = stdout.lastIndexOf("TDS_PIR_ERROR=");
            if (index >= 0) {
                int start = index + "TDS_PIR_ERROR=".length();
                int end = stdout.indexOf('\n', start);
                return (end < 0 ? stdout.substring(start) : stdout.substring(start, end)).trim();
            }
            return error;
        }
    }

    /**
     * 在指定节点上执行 PIR 二进制
     *
     * @param agentEndpoint 节点 Agent 端点（如 http://192.168.1.100:8081）
     * @param jobId        作业 ID（用于日志追踪）
     * @param jsonConfig   JSON 配置文件内容
     * @param runMode      运行阶段（SETUP 或 QUERY）
     * @param pirProtocol  PIR 协议（SealPIR 或 APSI）
     * @return 执行结果
     */
    public Result run(String agentEndpoint, String jobId, String jsonConfig,
                      RunMode runMode, PirProtocol pirProtocol) {
        String configPath = writeConfigFile(jobId, jsonConfig);
        String command = String.format("%s --config %s", BINARY_PATH, configPath);

        log.info("[PIR] jobId={}, mode={}, protocol={}, cmd={}", jobId, runMode, pirProtocol, command);

        try {
            String script = buildRunnerScript(command, jobId, runMode);
            String submittedJobId = agentClient.submitJob(agentEndpoint, script, jobId);
            Result result = waitForCompletion(agentEndpoint, submittedJobId, jobId);
            return result;
        } catch (Exception e) {
            log.error("[PIR] 执行失败 jobId={}: {}", jobId, e.getMessage());
            return Result.error("PIR 执行异常: " + e.getMessage());
        } finally {
            cleanupConfigFile(configPath);
        }
    }

    /**
     * 取消指定的 PIR 作业
     */
    public void cancel(String jobId) {
        CANCEL_FLAGS.put(jobId, Boolean.TRUE);
        log.info("[PIR] 取消标志已设置: {}", jobId);
    }

    /**
     * 构建在节点上执行的脚本
     */
    private String buildRunnerScript(String command, String jobId, RunMode runMode) {
        StringBuilder script = new StringBuilder();
        script.append("#!/bin/bash\n");
        script.append("set -e\n");
        script.append("exec 2>&1\n");
        script.append("echo \"TDS_PIR_STARTED=1 jobId=").append(jobId).append(" mode=").append(runMode).append("\"\n");
        script.append(command).append("\n");
        script.append("EXIT_CODE=$?\n");
        script.append("if [ $EXIT_CODE -eq 0 ]; then\n");
        script.append("  echo \"TDS_PIR_RESULT={\\\"taskId\\\":\\\"").append(jobId).append("\\\",\\\"status\\\":\\\"SUCCEEDED\\\"}\"\n");
        script.append("else\n");
        script.append("  echo \"TDS_PIR_ERROR={\\\"taskId\\\":\\\"").append(jobId).append("\\\",\\\"exitCode\\\":$EXIT_CODE}\"\n");
        script.append("fi\n");
        script.append("exit $EXIT_CODE\n");
        return script.toString();
    }

    /**
     * 将 JSON 配置写入临时文件
     */
    private String writeConfigFile(String jobId, String jsonConfig) {
        String fileName = "pir_config_" + jobId.replaceAll("[^A-Za-z0-9_-]", "_") + ".json";
        String configPath = "/tmp/" + fileName;
        try (FileWriter writer = new FileWriter(configPath)) {
            writer.write(jsonConfig);
        } catch (Exception e) {
            throw new RuntimeException("写入PIR配置文件失败: " + configPath, e);
        }
        return configPath;
    }

    /**
     * 清理临时配置文件
     */
    private void cleanupConfigFile(String configPath) {
        try {
            new File(configPath).delete();
        } catch (Exception e) {
            log.warn("[PIR] 清理配置文件失败: {}", configPath);
        }
    }

    /**
     * 轮询等待作业完成
     */
    private Result waitForCompletion(String agentEndpoint, String submittedJobId, String originalJobId) {
        long deadline = System.currentTimeMillis() + pirTimeoutMs;
        int consecutiveFailures = 0;

        while (System.currentTimeMillis() < deadline) {
            if (Boolean.TRUE.equals(CANCEL_FLAGS.get(originalJobId))) {
                try {
                    agentClient.stopJob(agentEndpoint, submittedJobId);
                } catch (Exception e) {
                    log.warn("[PIR] 停止作业失败: {}", e.getMessage());
                }
                return Result.error("PIR任务已取消");
            }

            try {
                IAgentClient.TaskStatus status = agentClient.getTaskStatus(agentEndpoint, submittedJobId);
                if (status == null) {
                    consecutiveFailures++;
                    continue;
                }
                consecutiveFailures = 0;

                String state = status.getStatus();
                log.info("[PIR] jobId={}, status={}", submittedJobId, state);

                if ("SUCCEEDED".equals(state)) {
                    String stdout = status.getResult() != null ? status.getResult() : "";
                    return Result.success(stdout);
                } else if ("FAILED".equals(state)) {
                    String stderr = status.getError() != null ? status.getError() : "作业失败";
                    return Result.failure(-1, stderr);
                } else if ("STOPPED".equals(state)) {
                    return Result.error("作业被停止");
                }
            } catch (Exception e) {
                consecutiveFailures++;
                log.warn("[PIR] 查询状态异常: {}", e.getMessage());
                if (consecutiveFailures >= 5) {
                    return Result.error("连续查询失败 " + consecutiveFailures + " 次: " + e.getMessage());
                }
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Result.error("等待被中断");
            }
        }

        return Result.error("PIR执行超时（" + (pirTimeoutMs / 1000) + "秒）");
    }
}
