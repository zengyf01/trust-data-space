package com.tds.dos.service.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件系统文件服务实现
 */
@Slf4j
@Service
public class FileServiceImpl implements IFileService {

    @Value("${file.storage.path:/var/data/dos}")
    private String storagePath;

    @Value("${file.max.size:104857600}")  // 100MB
    private long maxFileSize;

    @Override
    public String uploadWorkOrderFile(String workOrderId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超过限制: " + maxFileSize);
        }

        // 创建工单目录
        Path workOrderDir = Paths.get(storagePath, "workorder", workOrderId);
        Files.createDirectories(workOrderDir);

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String uniqueFileName = System.currentTimeMillis() + "_" + originalFilename;
        Path filePath = workOrderDir.resolve(uniqueFileName);

        // 保存文件
        file.transferTo(filePath.toFile());

        log.info("文件上传成功: workOrderId={}, fileName={}, size={}",
                workOrderId, originalFilename, file.getSize());

        return "workorder/" + workOrderId + "/" + uniqueFileName;
    }

    @Override
    public void downloadWorkOrderFile(String workOrderId, String fileName, OutputStream outputStream)
            throws IOException {
        Path filePath = Paths.get(storagePath, "workorder", workOrderId, fileName);

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("文件不存在: " + fileName);
        }

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }

        log.info("文件下载成功: workOrderId={}, fileName={}", workOrderId, fileName);
    }

    @Override
    public String[] listWorkOrderFiles(String workOrderId) throws IOException {
        Path workOrderDir = Paths.get(storagePath, "workorder", workOrderId);

        if (!Files.exists(workOrderDir)) {
            return new String[0];
        }

        return Files.list(workOrderDir)
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .toArray(String[]::new);
    }

    @Override
    public void deleteWorkOrderFile(String workOrderId, String fileName) throws IOException {
        Path filePath = Paths.get(storagePath, "workorder", workOrderId, fileName);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("文件删除成功: workOrderId={}, fileName={}", workOrderId, fileName);
        }
    }

    @Override
    public boolean fileExists(String workOrderId, String fileName) {
        Path filePath = Paths.get(storagePath, "workorder", workOrderId, fileName);
        return Files.exists(filePath);
    }

    /**
     * 获取文件输入流
     */
    public InputStream getFileInputStream(String workOrderId, String fileName) throws IOException {
        Path filePath = Paths.get(storagePath, "workorder", workOrderId, fileName);

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("文件不存在: " + fileName);
        }

        return Files.newInputStream(filePath);
    }

    /**
     * 获取文件大小
     */
    public long getFileSize(String workOrderId, String fileName) throws IOException {
        Path filePath = Paths.get(storagePath, "workorder", workOrderId, fileName);

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("文件不存在: " + fileName);
        }

        return Files.size(filePath);
    }
}
