package com.tds.dos.service.workorder.strategy;

import com.tds.dos.common.enums.WorkOrderStatus;
import com.tds.dos.common.exception.BusinessException;
import com.tds.dos.dal.entity.TbWorkOrder;
import com.tds.dos.dal.mapper.TbWorkOrderMapper;
import com.tds.dos.service.connector.ITdsConnectorClient;
import com.tds.dos.service.connector.dto.ConnectorInfo;
import com.tds.dos.service.connector.dto.TunnelCommandResult;
import com.tds.dos.service.connector.dto.TunnelSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 隧道工单策略 - DOS通过TDS隧道控制连接器执行工单
 * 用于替代DataServiceStrategy，实现通过GMSSL隧道与连接器通信
 */
@Service
public class TunnelWorkOrderStrategy implements WorkOrderStrategy {

    private static final Logger log = LoggerFactory.getLogger(TunnelWorkOrderStrategy.class);

    @Autowired
    private TbWorkOrderMapper workOrderMapper;

    @Autowired
    private ITdsConnectorClient tdsConnectorClient;

    // 会话缓存：workOrderId -> TunnelSession
    private final Map<String, TunnelSession> sessionCache = new ConcurrentHashMap<>();

    @Override
    public String getWorkOrderType() {
        return "TUNNEL_EXECUTION";
    }

    @Override
    public void preProcess(String workOrderId) {
        log.info("Tunnel策略预处理: workOrderId={}", workOrderId);

        TbWorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            throw new BusinessException("工单不存在: " + workOrderId);
        }

        try {
            // 1. 从TDS获取可用连接器
            List<com.tds.dos.service.connector.dto.ConnectorInfo> connectors = tdsConnectorClient.getAvailableConnectors(
                workOrder.getfSpaceId(),
                workOrder.getWorkOrderType()
            );

            if (connectors == null || connectors.isEmpty()) {
                throw new BusinessException("没有可用的连接器");
            }

            // 选择第一个可用的连接器
            ConnectorInfo connector = connectors.get(0);
            log.info("选择连接器: sn={}, name={}", connector.getSn(), connector.getName());

            // 2. 申请隧道会话
            TunnelSession session = tdsConnectorClient.applyTunnelSession(connector.getSn());
            sessionCache.put(workOrderId, session);

            // 3. 更新工单状态为处理中
            workOrder.setWorkOrderStatus(WorkOrderStatus.PROCESSING.getCode());
            workOrder.setStartTime(LocalDateTime.now());
            workOrder.setfUpdateTime(LocalDateTime.now());
            workOrderMapper.updateById(workOrder);

            log.info("Tunnel策略预处理完成: workOrderId={}, sessionId={}", workOrderId, session.getSessionId());

        } catch (Exception e) {
            log.error("Tunnel策略预处理失败: workOrderId={}", workOrderId, e);
            throw new BusinessException("预处理失败: " + e.getMessage());
        }
    }

    @Override
    public void execute(String workOrderId) {
        log.info("Tunnel策略执行: workOrderId={}", workOrderId);

        TbWorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            throw new BusinessException("工单不存在: " + workOrderId);
        }

        TunnelSession session = sessionCache.get(workOrderId);
        if (session == null) {
            throw new BusinessException("隧道会话不存在，请先执行预处理");
        }

        try {
            // 从工单配置中获取命令
            String configJson = workOrder.getConfigJson();
            String command = buildCommandFromConfig(configJson);

            log.info("通过隧道下发命令: workOrderId={}, sessionId={}, command={}", workOrderId, session.getSessionId(), command);

            // 通过隧道执行命令
            TunnelCommandResult result = tdsConnectorClient.executeViaTunnel(session.getSessionId(), command);

            if (result.isSuccess()) {
                log.info("Tunnel策略执行成功: workOrderId={}, output={}", workOrderId, result.getOutput());

                // 更新工单状态为已完成
                workOrder.setWorkOrderStatus(WorkOrderStatus.COMPLETED.getCode());
                workOrder.setResultMessage(result.getOutput());
                workOrder.setEndTime(LocalDateTime.now());
                workOrder.setfUpdateTime(LocalDateTime.now());

                if (workOrder.getStartTime() != null) {
                    long seconds = java.time.Duration.between(workOrder.getStartTime(), workOrder.getEndTime()).getSeconds();
                    workOrder.setDuration((int) seconds);
                }
            } else {
                log.error("Tunnel策略执行失败: workOrderId={}, error={}", workOrderId, result.getErrorMessage());

                // 更新工单状态为失败
                workOrder.setWorkOrderStatus(WorkOrderStatus.FAILED.getCode());
                workOrder.setResultMessage(result.getErrorMessage());
                workOrder.setEndTime(LocalDateTime.now());
                workOrder.setfUpdateTime(LocalDateTime.now());
            }

            workOrderMapper.updateById(workOrder);

        } catch (Exception e) {
            log.error("Tunnel策略执行异常: workOrderId={}", workOrderId, e);

            workOrder.setWorkOrderStatus(WorkOrderStatus.FAILED.getCode());
            workOrder.setResultMessage(e.getMessage());
            workOrder.setEndTime(LocalDateTime.now());
            workOrder.setfUpdateTime(LocalDateTime.now());
            workOrderMapper.updateById(workOrder);

            throw new BusinessException("执行异常: " + e.getMessage());
        } finally {
            // 清理会话缓存
            sessionCache.remove(workOrderId);
        }
    }

    @Override
    public void cancel(String workOrderId) {
        log.info("Tunnel策略取消: workOrderId={}", workOrderId);

        TbWorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            throw new BusinessException("工单不存在: " + workOrderId);
        }

        TunnelSession session = sessionCache.get(workOrderId);
        if (session != null) {
            try {
                // 通过隧道发送取消命令
                tdsConnectorClient.executeViaTunnel(session.getSessionId(), "cancel");
            } catch (Exception e) {
                log.warn("发送取消命令失败: workOrderId={}", workOrderId, e);
            }

            // 释放隧道会话
            tdsConnectorClient.releaseTunnelSession(session.getSessionId());
            sessionCache.remove(workOrderId);
        }

        // 更新工单状态为已取消
        workOrder.setWorkOrderStatus(WorkOrderStatus.CANCELLED.getCode());
        workOrder.setfUpdateTime(LocalDateTime.now());
        workOrderMapper.updateById(workOrder);

        log.info("Tunnel策略取消完成: workOrderId={}", workOrderId);
    }

    /**
     * 从配置JSON构建命令
     */
    private String buildCommandFromConfig(String configJson) {
        // 默认命令，实际应从配置解析
        if (configJson == null || configJson.isEmpty() || "{}".equals(configJson)) {
            return "echo 'default command'";
        }

        try {
            // 简单解析，实际应使用ObjectMapper
            if (configJson.contains("\"command\"")) {
                int start = configJson.indexOf("\"command\"") + 10;
                int end = configJson.indexOf("\"", start);
                return configJson.substring(start, end);
            }
        } catch (Exception e) {
            log.warn("解析配置JSON失败: {}", configJson);
        }

        return "echo 'default command'";
    }
}
