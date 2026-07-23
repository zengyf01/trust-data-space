package com.tds.dos.service.workorder;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.dos.common.enums.WorkOrderStatus;
import com.tds.dos.common.exception.BusinessException;
import com.tds.dos.dal.entity.TbWorkOrder;
import com.tds.dos.dal.mapper.TbWorkOrderMapper;
import com.tds.dos.service.workorder.strategy.WorkOrderStrategy;
import com.tds.dos.service.workorder.strategy.WorkOrderStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * 工单服务实现
 */
@Service
public class WorkOrderServiceImpl implements IWorkOrderService {

    @Autowired
    private TbWorkOrderMapper workOrderMapper;

    @Autowired
    private WorkOrderStrategyFactory strategyFactory;

    @Override
    public IPage<TbWorkOrder> getWorkOrderPage(int currentPage, int pageSize,
            String orderCode, Integer workOrderType, Integer workOrderStatus, String spaceId) {
        Page<TbWorkOrder> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbWorkOrder> wrapper = new LambdaQueryWrapper<>();
        if (orderCode != null && !orderCode.isEmpty()) {
            wrapper.like(TbWorkOrder::getOrderCode, orderCode);
        }
        if (workOrderType != null) {
            wrapper.eq(TbWorkOrder::getWorkOrderType, workOrderType);
        }
        if (workOrderStatus != null) {
            wrapper.eq(TbWorkOrder::getWorkOrderStatus, workOrderStatus);
        }
        if (spaceId != null && !spaceId.isEmpty()) {
            wrapper.eq(TbWorkOrder::getfSpaceId, spaceId);
        }
        wrapper.eq(TbWorkOrder::getfDeleteMark, 0);
        wrapper.orderByDesc(TbWorkOrder::getfCreateTime);
        return workOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public TbWorkOrder getWorkOrderById(String id) {
        return workOrderMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbWorkOrder createWorkOrder(WorkOrderCreateDTO dto) {
        TbWorkOrder workOrder = new TbWorkOrder();
        workOrder.setId(UUID.randomUUID().toString().replace("-", ""));
        workOrder.setWorkOrderCode(generateWorkOrderCode());
        workOrder.setOrderCode(dto.getOrderCode());
        workOrder.setWorkOrderType(dto.getWorkOrderType());
        workOrder.setWorkOrderStatus(WorkOrderStatus.PENDING.getCode());
        workOrder.setConfigJson(dto.getConfigJson());
        workOrder.setCreator(dto.getCreator());
        workOrder.setCreatorId(dto.getCreatorId());
        workOrder.setfTenantId(dto.getTenantId());
        workOrder.setfSpaceId(dto.getSpaceId());
        workOrder.setfCreateTime(LocalDateTime.now());
        workOrder.setfUpdateTime(LocalDateTime.now());
        workOrder.setfDeleteMark(0);

        workOrderMapper.insert(workOrder);
        return workOrder;
    }

    @Override
    @Transactional
    public TbWorkOrder startProcess(String id) {
        TbWorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw new BusinessException("工单不存在");
        }
        if (workOrder.getWorkOrderStatus() != WorkOrderStatus.PENDING.getCode()) {
            throw new BusinessException("只有待处理状态可以开始处理");
        }
        workOrder.setWorkOrderStatus(WorkOrderStatus.PROCESSING.getCode());
        workOrder.setStartTime(LocalDateTime.now());
        workOrder.setfUpdateTime(LocalDateTime.now());
        workOrderMapper.updateById(workOrder);
        return workOrder;
    }

    @Override
    @Transactional
    public TbWorkOrder completeWorkOrder(String id, String resultMessage, String outputFilePath, String outputFileUrl) {
        TbWorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw new BusinessException("工单不存在");
        }
        if (workOrder.getWorkOrderStatus() != WorkOrderStatus.PROCESSING.getCode()) {
            throw new BusinessException("只有处理中状态可以完成");
        }
        workOrder.setWorkOrderStatus(WorkOrderStatus.COMPLETED.getCode());
        workOrder.setResultMessage(resultMessage);
        workOrder.setOutputFilePath(outputFilePath);
        workOrder.setOutputFileUrl(outputFileUrl);
        workOrder.setEndTime(LocalDateTime.now());
        workOrder.setfUpdateTime(LocalDateTime.now());

        if (workOrder.getStartTime() != null && workOrder.getEndTime() != null) {
            long seconds = ChronoUnit.SECONDS.between(workOrder.getStartTime(), workOrder.getEndTime());
            workOrder.setDuration((int) seconds);
        }

        workOrderMapper.updateById(workOrder);
        return workOrder;
    }

    @Override
    @Transactional
    public TbWorkOrder failWorkOrder(String id, String errorMessage) {
        TbWorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw new BusinessException("工单不存在");
        }
        workOrder.setWorkOrderStatus(WorkOrderStatus.FAILED.getCode());
        workOrder.setResultMessage(errorMessage);
        workOrder.setEndTime(LocalDateTime.now());
        workOrder.setfUpdateTime(LocalDateTime.now());

        if (workOrder.getStartTime() != null && workOrder.getEndTime() != null) {
            long seconds = ChronoUnit.SECONDS.between(workOrder.getStartTime(), workOrder.getEndTime());
            workOrder.setDuration((int) seconds);
        }

        workOrderMapper.updateById(workOrder);
        return workOrder;
    }

    @Override
    @Transactional
    public TbWorkOrder cancelWorkOrder(String id) {
        TbWorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw new BusinessException("工单不存在");
        }
        if (workOrder.getWorkOrderStatus() == WorkOrderStatus.COMPLETED.getCode() ||
            workOrder.getWorkOrderStatus() == WorkOrderStatus.CANCELLED.getCode()) {
            throw new BusinessException("当前状态不允许取消");
        }
        workOrder.setWorkOrderStatus(WorkOrderStatus.CANCELLED.getCode());
        workOrder.setfUpdateTime(LocalDateTime.now());
        workOrderMapper.updateById(workOrder);
        return workOrder;
    }

    @Override
    @Transactional
    public void deleteWorkOrder(String id) {
        TbWorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw new BusinessException("工单不存在");
        }
        if (workOrder.getWorkOrderStatus() == WorkOrderStatus.PROCESSING.getCode()) {
            throw new BusinessException("处理中的工单不允许删除");
        }
        // 使用 UpdateWrapper 直接更新 f_delete_mark 字段
        workOrderMapper.update(null,
            new LambdaUpdateWrapper<TbWorkOrder>()
                .eq(TbWorkOrder::getId, id)
                .set(TbWorkOrder::getfDeleteMark, 1)
                .set(TbWorkOrder::getfUpdateTime, LocalDateTime.now())
        );
    }

    private String generateWorkOrderCode() {
        return "WO" + System.currentTimeMillis();
    }

    /**
     * 使用策略模式执行工单
     */
    public void executeWithStrategy(String id) {
        TbWorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw new BusinessException("工单不存在");
        }

        WorkOrderStrategy strategy = strategyFactory.getStrategy(String.valueOf(workOrder.getWorkOrderType()));
        strategy.preProcess(id);
        strategy.execute(id);
    }

    /**
     * 取消工单（策略模式）
     */
    public void cancelWithStrategy(String id) {
        TbWorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw new BusinessException("工单不存在");
        }

        WorkOrderStrategy strategy = strategyFactory.getStrategy(String.valueOf(workOrder.getWorkOrderType()));
        strategy.cancel(id);
    }
}