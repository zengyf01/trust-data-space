package com.tds.datar.service.delivery;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.datar.dal.entity.TbDeliveryTask;

/**
 * 交付任务服务接口
 */
public interface IDeliveryTaskService {

    /**
     * 分页查询交付任务
     */
    IPage<TbDeliveryTask> getTaskPage(int currentPage, int pageSize, String workOrderId, String taskType, Integer status);

    /**
     * 获取任务详情
     */
    TbDeliveryTask getTaskById(String id);

    /**
     * 创建交付任务
     */
    TbDeliveryTask createTask(DeliveryTaskDTO dto);

    /**
     * 更新任务状态
     */
    TbDeliveryTask updateTaskStatus(String id, Integer status, String buildLog, String errorMessage);

    /**
     * 删除任务
     */
    void deleteTask(String id);

    // ==================== 交付操作 ====================

    /**
     * 初始化沙盒工作目录
     */
    TbDeliveryTask initSandbox(String workOrderId, String sandboxId, String tenantId);

    /**
     * 构建镜像
     */
    TbDeliveryTask buildImage(String taskId, String imageName, String imageTag, String sourcePath);

    /**
     * 下载源码
     */
    TbDeliveryTask downloadSource(String taskId, String sourceUrl, String workDirectory);

    /**
     * 执行交付任务
     */
    TbDeliveryTask executeTask(String id);
}