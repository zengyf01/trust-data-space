package com.tds.dos.api.workorder;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.common.core.PageResult;
import com.tds.dos.dal.entity.TbWorkOrder;
import com.tds.dos.service.file.FileServiceImpl;
import com.tds.dos.service.workorder.IWorkOrderService;
import com.tds.dos.service.workorder.WorkOrderCreateDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 工单API控制器
 */
@RestController
@RequestMapping("/workOrder")
public class WorkOrderController {

    @Autowired
    private IWorkOrderService workOrderService;

    @Autowired
    private FileServiceImpl fileService;

    @GetMapping("/page")
    public ApiResponse<PageResult<TbWorkOrder>> getWorkOrderPage(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) Integer workOrderType,
            @RequestParam(required = false) Integer workOrderStatus,
            @RequestParam(required = false) String spaceId) {

        IPage<TbWorkOrder> page = workOrderService.getWorkOrderPage(
                pageNumber, pageSize, orderCode, workOrderType, workOrderStatus, spaceId);
        PageResult<TbWorkOrder> result = PageResult.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<TbWorkOrder> getWorkOrder(@PathVariable String id) {
        TbWorkOrder workOrder = workOrderService.getWorkOrderById(id);
        return ApiResponse.success(workOrder);
    }

    @PostMapping
    public ApiResponse<TbWorkOrder> createWorkOrder(@RequestBody WorkOrderCreateDTO dto) {
        TbWorkOrder workOrder = workOrderService.createWorkOrder(dto);
        return ApiResponse.success(workOrder);
    }

    @PostMapping("/{id}/start")
    public ApiResponse<TbWorkOrder> startProcess(@PathVariable String id) {
        TbWorkOrder workOrder = workOrderService.startProcess(id);
        return ApiResponse.success(workOrder);
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<TbWorkOrder> completeWorkOrder(
            @PathVariable String id,
            @RequestParam(required = false) String resultMessage,
            @RequestParam(required = false) String outputFilePath,
            @RequestParam(required = false) String outputFileUrl) {
        TbWorkOrder workOrder = workOrderService.completeWorkOrder(id, resultMessage, outputFilePath, outputFileUrl);
        return ApiResponse.success(workOrder);
    }

    @PostMapping("/{id}/fail")
    public ApiResponse<TbWorkOrder> failWorkOrder(
            @PathVariable String id,
            @RequestParam String errorMessage) {
        TbWorkOrder workOrder = workOrderService.failWorkOrder(id, errorMessage);
        return ApiResponse.success(workOrder);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<TbWorkOrder> cancelWorkOrder(@PathVariable String id) {
        TbWorkOrder workOrder = workOrderService.cancelWorkOrder(id);
        return ApiResponse.success(workOrder);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWorkOrder(@PathVariable String id) {
        workOrderService.deleteWorkOrder(id);
        return ApiResponse.success();
    }

    // ==================== 文件上传下载 ====================

    /**
     * 上传工单文件
     */
    @PostMapping("/{id}/upload")
    public ApiResponse<Map<String, String>> uploadFile(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileService.uploadWorkOrderFile(id, file);
            Map<String, String> result = new HashMap<>();
            result.put("filePath", filePath);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", String.valueOf(file.getSize()));
            return ApiResponse.success(result);
        } catch (IOException e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载工单结果文件
     */
    @GetMapping("/{id}/download/{fileName}")
    public void downloadFile(
            @PathVariable String id,
            @PathVariable String fileName,
            HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

            // 写入文件内容
            fileService.downloadWorkOrderFile(id, fileName, response.getOutputStream());

        } catch (IOException e) {
            try {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件不存在: " + e.getMessage());
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 获取工单文件列表
     */
    @GetMapping("/{id}/files")
    public ApiResponse<String[]> listFiles(@PathVariable String id) {
        try {
            String[] files = fileService.listWorkOrderFiles(id);
            return ApiResponse.success(files);
        } catch (IOException e) {
            return ApiResponse.error("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除工单文件
     */
    @DeleteMapping("/{id}/files/{fileName}")
    public ApiResponse<Void> deleteFile(
            @PathVariable String id,
            @PathVariable String fileName) {
        try {
            fileService.deleteWorkOrderFile(id, fileName);
            return ApiResponse.success();
        } catch (IOException e) {
            return ApiResponse.error("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     */
    @GetMapping("/{id}/files/{fileName}/exists")
    public ApiResponse<Boolean> fileExists(
            @PathVariable String id,
            @PathVariable String fileName) {
        boolean exists = fileService.fileExists(id, fileName);
        return ApiResponse.success(exists);
    }
}
