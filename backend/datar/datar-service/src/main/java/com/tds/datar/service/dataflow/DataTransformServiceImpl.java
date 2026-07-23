package com.tds.datar.service.dataflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tds.datar.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据转换服务实现
 */
@Service
public class DataTransformServiceImpl implements IDataTransformService {

    private final Map<String, DataTransformDTO> transformCache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DataTransformDTO createTransform(DataTransformDTO dto) {
        String transformId = UUID.randomUUID().toString().replace("-", "");
        dto.setTransformId(transformId);
        transformCache.put(transformId, dto);
        return dto;
    }

    @Override
    public String executeTransform(String transformId, String inputData) {
        DataTransformDTO dto = transformCache.get(transformId);
        if (dto == null) {
            throw new BusinessException("转换规则不存在");
        }

        String inputFormat = dto.getInputFormat();
        String outputFormat = dto.getOutputFormat();

        // 模拟转换逻辑
        try {
            if ("CSV".equals(inputFormat) && "JSON".equals(outputFormat)) {
                return csvToJson(inputData);
            } else if ("JSON".equals(inputFormat) && "CSV".equals(outputFormat)) {
                return jsonToCsv(inputData);
            } else if ("XML".equals(inputFormat) && "JSON".equals(outputFormat)) {
                return xmlToJson(inputData);
            } else if ("JSON".equals(inputFormat) && "XML".equals(outputFormat)) {
                return jsonToXml(inputData);
            } else {
                return inputData; // 同格式直接返回
            }
        } catch (Exception e) {
            throw new BusinessException("转换失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> batchTransform(String transformId, List<String> inputDataList) {
        List<String> results = new ArrayList<>();
        for (String input : inputDataList) {
            results.add(executeTransform(transformId, input));
        }
        return results;
    }

    @Override
    public Map<String, Object> validateTransform(String transformId, String testData) {
        DataTransformDTO dto = transformCache.get(transformId);
        if (dto == null) {
            throw new BusinessException("转换规则不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("transformId", transformId);
        result.put("testData", testData);
        result.put("testResult", executeTransform(transformId, testData));
        return result;
    }

    // CSV转JSON模拟
    private String csvToJson(String csv) {
        return "[{\"id\":1,\"name\":\"item1\"},{\"id\":2,\"name\":\"item2\"}]";
    }

    // JSON转CSV模拟
    private String jsonToCsv(String json) {
        return "id,name\n1,item1\n2,item2";
    }

    // XML转JSON模拟
    private String xmlToJson(String xml) {
        return "{\"root\":{\"item\":{\"id\":1,\"name\":\"item1\"}}}";
    }

    // JSON转XML模拟
    private String jsonToXml(String json) {
        return "<root><item><id>1</id><name>item1</name></item></root>";
    }
}