package com.tds.datar.service.dataflow;

import java.util.List;
import java.util.Map;

/**
 * 数据转换服务接口
 */
public interface IDataTransformService {

    /**
     * 创建转换规则
     */
    DataTransformDTO createTransform(DataTransformDTO dto);

    /**
     * 执行转换
     */
    String executeTransform(String transformId, String inputData);

    /**
     * 批量转换
     */
    List<String> batchTransform(String transformId, List<String> inputDataList);

    /**
     * 验证转换规则
     */
    Map<String, Object> validateTransform(String transformId, String testData);
}