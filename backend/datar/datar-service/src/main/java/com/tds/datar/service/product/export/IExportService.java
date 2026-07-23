package com.tds.datar.service.product.export;

import java.util.Map;

/**
 * 数据导出服务接口
 */
public interface IExportService {

    /**
     * 导出样例数据
     */
    Map<String, Object> exportSampleData(ExportDTO dto);

    /**
     * 导出为Excel
     */
    byte[] exportToExcel(ExportDTO dto);

    /**
     * 导出为JSON
     */
    String exportToJson(ExportDTO dto);

    /**
     * 导出为CSV
     */
    byte[] exportToCsv(ExportDTO dto);

    /**
     * 获取导出记录
     */
    Map<String, Object> getExportRecord(String exportId);
}