package com.tds.dos.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件服务接口
 */
public interface IFileService {

    /**
     * 上传工单文件
     * @param workOrderId 工单ID
     * @param file 上传的文件
     * @return 文件存储路径
     */
    String uploadWorkOrderFile(String workOrderId, MultipartFile file) throws IOException;

    /**
     * 下载工单结果文件
     * @param workOrderId 工单ID
     * @param fileName 文件名
     * @param outputStream 输出流
     */
    void downloadWorkOrderFile(String workOrderId, String fileName, OutputStream outputStream) throws IOException;

    /**
     * 获取工单文件列表
     * @param workOrderId 工单ID
     * @return 文件名列表
     */
    String[] listWorkOrderFiles(String workOrderId) throws IOException;

    /**
     * 删除工单文件
     * @param workOrderId 工单ID
     * @param fileName 文件名
     */
    void deleteWorkOrderFile(String workOrderId, String fileName) throws IOException;

    /**
     * 检查文件是否存在
     * @param workOrderId 工单ID
     * @param fileName 文件名
     * @return 是否存在
     */
    boolean fileExists(String workOrderId, String fileName);
}
