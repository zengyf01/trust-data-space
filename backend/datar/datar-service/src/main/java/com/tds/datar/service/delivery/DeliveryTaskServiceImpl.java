package com.tds.datar.service.delivery;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.datar.common.enums.DeliveryTaskStatus;
import com.tds.datar.common.enums.DeliveryTaskType;
import com.tds.datar.common.exception.BusinessException;
import com.tds.datar.dal.entity.TbDeliveryTask;
import com.tds.datar.dal.entity.TbWorkOrder;
import com.tds.datar.dal.mapper.TbDeliveryTaskMapper;
import com.tds.datar.dal.mapper.TbWorkOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 交付任务服务实现
 */
@Service
public class DeliveryTaskServiceImpl implements IDeliveryTaskService {

    @Autowired
    private TbDeliveryTaskMapper deliveryTaskMapper;

    @Autowired
    private TbWorkOrderMapper workOrderMapper;

    @Override
    public IPage<TbDeliveryTask> getTaskPage(int currentPage, int pageSize,
            String workOrderId, String taskType, Integer status) {
        Page<TbDeliveryTask> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbDeliveryTask> wrapper = new LambdaQueryWrapper<>();
        if (workOrderId != null && !workOrderId.isEmpty()) {
            wrapper.eq(TbDeliveryTask::getfWorkOrderId, workOrderId);
        }
        if (taskType != null && !taskType.isEmpty()) {
            wrapper.eq(TbDeliveryTask::getfTaskType, taskType);
        }
        if (status != null) {
            wrapper.eq(TbDeliveryTask::getfStatus, status);
        }
        wrapper.orderByDesc(TbDeliveryTask::getfCreateTime);
        return deliveryTaskMapper.selectPage(page, wrapper);
    }

    @Override
    public TbDeliveryTask getTaskById(String id) {
        return deliveryTaskMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbDeliveryTask createTask(DeliveryTaskDTO dto) {
        TbDeliveryTask task = new TbDeliveryTask();
        task.setfId(UUID.randomUUID().toString().replace("-", ""));
        task.setfWorkOrderId(dto.getWorkOrderId());
        task.setfTaskType(dto.getTaskType());
        task.setfStatus(DeliveryTaskStatus.PENDING.getCode());
        task.setfSandboxId(dto.getSandboxId());
        task.setfWorkDirectory(dto.getWorkDirectory());
        task.setfImageName(dto.getImageName());
        task.setfImageTag(dto.getImageTag());
        task.setfSourceUrl(dto.getSourceUrl());
        task.setfTenantId(dto.getTenantId());
        task.setfCreateTime(LocalDateTime.now());
        task.setfUpdateTime(LocalDateTime.now());
        task.setfDeleteMark(0);

        deliveryTaskMapper.insert(task);
        return task;
    }

    @Override
    @Transactional
    public TbDeliveryTask updateTaskStatus(String id, Integer status,
            String buildLog, String errorMessage) {
        TbDeliveryTask task = deliveryTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("交付任务不存在");
        }
        task.setfStatus(status);
        if (buildLog != null) {
            task.setfBuildLog(buildLog);
        }
        if (errorMessage != null) {
            task.setfErrorMessage(errorMessage);
        }
        task.setfUpdateTime(LocalDateTime.now());
        deliveryTaskMapper.updateById(task);
        return task;
    }

    @Override
    @Transactional
    public void deleteTask(String id) {
        TbDeliveryTask task = deliveryTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("交付任务不存在");
        }
        task.setfDeleteMark(1);
        task.setfUpdateTime(LocalDateTime.now());
        deliveryTaskMapper.updateById(task);
    }

    // ==================== 交付操作 ====================

    @Override
    @Transactional
    public TbDeliveryTask initSandbox(String workOrderId, String sandboxId, String tenantId) {
        TbWorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            throw new BusinessException("工单不存在");
        }

        TbDeliveryTask task = new TbDeliveryTask();
        task.setfId(UUID.randomUUID().toString().replace("-", ""));
        task.setfWorkOrderId(workOrderId);
        task.setfTaskType(DeliveryTaskType.SANDBOX_INIT.getCode());
        task.setfStatus(DeliveryTaskStatus.PENDING.getCode());
        task.setfSandboxId(sandboxId);
        task.setfWorkDirectory("/data/sandbox/" + sandboxId);
        task.setfTenantId(tenantId);
        task.setfCreateTime(LocalDateTime.now());
        task.setfUpdateTime(LocalDateTime.now());
        task.setfDeleteMark(0);

        deliveryTaskMapper.insert(task);
        return task;
    }

    @Override
    @Transactional
    public TbDeliveryTask buildImage(String taskId, String imageName, String imageTag, String sourcePath) {
        TbDeliveryTask task = deliveryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("交付任务不存在");
        }

        task.setfImageName(imageName);
        task.setfImageTag(imageTag);
        task.setfSourcePath(sourcePath);
        task.setfTaskType(DeliveryTaskType.IMAGE_BUILD.getCode());
        task.setfUpdateTime(LocalDateTime.now());
        deliveryTaskMapper.updateById(task);

        return task;
    }

    @Override
    @Transactional
    public TbDeliveryTask downloadSource(String taskId, String sourceUrl, String workDirectory) {
        TbDeliveryTask task = deliveryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("交付任务不存在");
        }

        task.setfSourceUrl(sourceUrl);
        task.setfWorkDirectory(workDirectory);
        task.setfTaskType(DeliveryTaskType.SOURCE_DOWNLOAD.getCode());
        task.setfUpdateTime(LocalDateTime.now());
        deliveryTaskMapper.updateById(task);

        return task;
    }

    @Override
    @Transactional
    public TbDeliveryTask executeTask(String id) {
        TbDeliveryTask task = deliveryTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("交付任务不存在");
        }

        long startTime = System.currentTimeMillis();
        task.setfStatus(DeliveryTaskStatus.RUNNING.getCode());
        task.setfUpdateTime(LocalDateTime.now());
        deliveryTaskMapper.updateById(task);

        StringBuilder buildLog = new StringBuilder();
        try {
            String taskType = task.getfTaskType();

            if (DeliveryTaskType.SANDBOX_INIT.getCode().equals(taskType)) {
                buildLog.append(executeSandboxInit(task));
            } else if (DeliveryTaskType.IMAGE_BUILD.getCode().equals(taskType)) {
                buildLog.append(executeImageBuild(task));
            } else if (DeliveryTaskType.SOURCE_DOWNLOAD.getCode().equals(taskType)) {
                buildLog.append(executeSourceDownload(task));
            }

            long duration = System.currentTimeMillis() - startTime;
            task.setfStatus(DeliveryTaskStatus.SUCCESS.getCode());
            task.setfDuration(duration);
            task.setfBuildLog(buildLog.toString());
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            task.setfStatus(DeliveryTaskStatus.FAILED.getCode());
            task.setfDuration(duration);
            task.setfErrorMessage(e.getMessage());
            task.setfBuildLog(buildLog.toString());
        }

        task.setfUpdateTime(LocalDateTime.now());
        deliveryTaskMapper.updateById(task);
        return task;
    }

    /**
     * 执行沙盒初始化
     */
    private String executeSandboxInit(TbDeliveryTask task) {
        StringBuilder log = new StringBuilder();
        log.append("[").append(LocalDateTime.now()).append("] 开始初始化沙盒...\n");
        log.append("  沙盒ID: ").append(task.getfSandboxId()).append("\n");
        log.append("  工作目录: ").append(task.getfWorkDirectory()).append("\n");

        // 模拟创建工作目录
        try {
            ProcessBuilder pb = new ProcessBuilder("mkdir", "-p", task.getfWorkDirectory());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 模拟执行
            TimeUnit.SECONDS.sleep(2);

            log.append("[").append(LocalDateTime.now()).append("] 沙盒初始化完成\n");
            log.append("  状态: 成功\n");
        } catch (Exception e) {
            log.append("[").append(LocalDateTime.now()).append("] 沙盒初始化失败: ").append(e.getMessage()).append("\n");
        }

        return log.toString();
    }

    /**
     * 执行镜像构建
     */
    private String executeImageBuild(TbDeliveryTask task) {
        StringBuilder log = new StringBuilder();
        log.append("[").append(LocalDateTime.now()).append("] 开始构建镜像...\n");
        log.append("  镜像名称: ").append(task.getfImageName()).append("\n");
        log.append("  镜像标签: ").append(task.getfImageTag()).append("\n");
        log.append("  源码路径: ").append(task.getfSourcePath()).append("\n");

        try {
            // 模拟Docker构建
            String dockerfile = task.getfSourcePath() + "/Dockerfile";
            log.append("  Dockerfile: ").append(dockerfile).append("\n");

            // 模拟构建过程
            for (int i = 1; i <= 3; i++) {
                TimeUnit.SECONDS.sleep(1);
                log.append("  构建步骤 ").append(i).append("/3...\n");
            }

            String imageFullName = task.getfImageName() + ":" + task.getfImageTag();
            log.append("[").append(LocalDateTime.now()).append("] 镜像构建完成\n");
            log.append("  镜像: ").append(imageFullName).append("\n");
            log.append("  状态: 成功\n");
        } catch (Exception e) {
            log.append("[").append(LocalDateTime.now()).append("] 镜像构建失败: ").append(e.getMessage()).append("\n");
        }

        return log.toString();
    }

    /**
     * 执行源码下载
     */
    private String executeSourceDownload(TbDeliveryTask task) {
        StringBuilder log = new StringBuilder();
        log.append("[").append(LocalDateTime.now()).append("] 开始下载源码...\n");
        log.append("  源码URL: ").append(task.getfSourceUrl()).append("\n");
        log.append("  保存路径: ").append(task.getfWorkDirectory()).append("\n");

        try {
            // 模拟下载
            ProcessBuilder pb = new ProcessBuilder("curl", "-o", task.getfWorkDirectory(), task.getfSourceUrl());
            pb.redirectErrorStream(true);

            // 模拟下载过程
            TimeUnit.SECONDS.sleep(2);

            log.append("[").append(LocalDateTime.now()).append("] 源码下载完成\n");
            log.append("  状态: 成功\n");
        } catch (Exception e) {
            log.append("[").append(LocalDateTime.now()).append("] 源码下载失败: ").append(e.getMessage()).append("\n");
        }

        return log.toString();
    }
}