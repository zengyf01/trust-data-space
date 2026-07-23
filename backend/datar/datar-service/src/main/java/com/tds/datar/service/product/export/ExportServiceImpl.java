package com.tds.datar.service.product.export;

import com.tds.datar.common.exception.BusinessException;
import com.tds.datar.dal.entity.TbCatalog;
import com.tds.datar.dal.entity.TbDataProduct;
import com.tds.datar.dal.mapper.TbCatalogMapper;
import com.tds.datar.dal.mapper.TbDataProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据导出服务实现
 */
@Service
public class ExportServiceImpl implements IExportService {

    @Autowired
    private TbDataProductMapper productMapper;

    @Autowired
    private TbCatalogMapper catalogMapper;

    private final Map<String, Map<String, Object>> exportRecords = new HashMap<>();

    @Override
    public Map<String, Object> exportSampleData(ExportDTO dto) {
        TbDataProduct product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("产品不存在");
        }

        String exportId = UUID.randomUUID().toString().replace("-", "");
        int sampleCount = dto.getSampleCount() != null ? dto.getSampleCount() : 10;

        Map<String, Object> result = new HashMap<>();
        result.put("exportId", exportId);
        result.put("productId", dto.getProductId());
        result.put("productName", product.getProductName());
        result.put("format", dto.getExportFormat());
        result.put("sampleCount", sampleCount);
        result.put("exportTime", LocalDateTime.now());

        // 根据格式导出
        if ("EXCEL".equals(dto.getExportFormat())) {
            result.put("data", exportToExcel(dto));
            result.put("contentType", "application/vnd.ms-excel");
        } else if ("JSON".equals(dto.getExportFormat())) {
            result.put("data", exportToJson(dto));
            result.put("contentType", "application/json");
        } else if ("CSV".equals(dto.getExportFormat())) {
            result.put("data", exportToCsv(dto));
            result.put("contentType", "text/csv");
        }

        // 保存导出记录
        exportRecords.put(exportId, result);

        return result;
    }

    @Override
    public byte[] exportToExcel(ExportDTO dto) {
        // 简化实现：实际应使用Apache POI生成Excel
        String content = "ID\tName\tValue\n1\tItem1\tValue1\n2\tItem2\tValue2";
        return content.getBytes();
    }

    @Override
    public String exportToJson(ExportDTO dto) {
        int sampleCount = dto.getSampleCount() != null ? dto.getSampleCount() : 10;
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < sampleCount; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", i + 1);
            row.put("name", "sample_" + i);
            row.put("value", "value_" + i);
            row.put("createTime", LocalDateTime.now().toString());
            data.add(row);
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"productId\": \"").append(dto.getProductId()).append("\",\n");
        json.append("  \"exportTime\": \"").append(LocalDateTime.now()).append("\",\n");
        json.append("  \"sampleCount\": ").append(sampleCount).append(",\n");
        json.append("  \"data\": [\n");
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> row = data.get(i);
            json.append("    {");
            json.append("\"id\":").append(row.get("id"));
            json.append(",\"name\":\"").append(row.get("name")).append("\"");
            json.append(",\"value\":\"").append(row.get("value")).append("\"");
            json.append(",\"createTime\":\"").append(row.get("createTime")).append("\"");
            json.append("}");
            if (i < data.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}");
        return json.toString();
    }

    @Override
    public byte[] exportToCsv(ExportDTO dto) {
        int sampleCount = dto.getSampleCount() != null ? dto.getSampleCount() : 10;
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Name,Value,CreateTime\n");
        for (int i = 0; i < sampleCount; i++) {
            csv.append(i + 1).append(",");
            csv.append("sample_").append(i).append(",");
            csv.append("value_").append(i).append(",");
            csv.append(LocalDateTime.now()).append("\n");
        }
        return csv.toString().getBytes();
    }

    @Override
    public Map<String, Object> getExportRecord(String exportId) {
        Map<String, Object> record = exportRecords.get(exportId);
        if (record == null) {
            throw new BusinessException("导出记录不存在");
        }
        return record;
    }
}