package com.tds.api.catalog;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.common.core.ApiResponse;
import com.tds.common.core.PageResult;
import com.tds.dal.entity.TbCatalog;
import com.tds.dal.entity.TbCatalogField;
import com.tds.service.catalog.CatalogCreateDTO;
import com.tds.service.catalog.CatalogServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源目录API控制器
 */
@RestController
@RequestMapping("/catalog")
public class CatalogController {

    @Autowired
    private CatalogServiceImpl catalogService;

    @PostMapping("/page")
    public ApiResponse<PageResult<TbCatalog>> getCatalogPage(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String catalogName,
            @RequestParam(required = false) Integer status) {

        IPage<TbCatalog> page = catalogService.getCatalogPage(
                pageNumber, pageSize, catalogName, status);
        PageResult<TbCatalog> result = PageResult.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<TbCatalog> getCatalog(@PathVariable String id) {
        TbCatalog catalog = catalogService.getCatalogById(id);
        return ApiResponse.success(catalog);
    }

    @GetMapping("/{id}/fields")
    public ApiResponse<List<TbCatalogField>> getCatalogFields(@PathVariable String id) {
        List<TbCatalogField> fields = catalogService.getCatalogFields(id);
        return ApiResponse.success(fields);
    }

    @PostMapping
    public ApiResponse<TbCatalog> createCatalog(@RequestBody CatalogCreateDTO dto) {
        TbCatalog catalog = catalogService.createCatalog(dto);
        return ApiResponse.success(catalog);
    }

    @PutMapping("/{id}")
    public ApiResponse<TbCatalog> updateCatalog(
            @PathVariable String id,
            @RequestBody CatalogCreateDTO dto) {
        TbCatalog catalog = catalogService.updateCatalog(id, dto);
        return ApiResponse.success(catalog);
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<TbCatalog> publishCatalog(@PathVariable String id) {
        TbCatalog catalog = catalogService.publishCatalog(id);
        return ApiResponse.success(catalog);
    }

    @PostMapping("/{id}/offline")
    public ApiResponse<TbCatalog> offlineCatalog(@PathVariable String id) {
        TbCatalog catalog = catalogService.offlineCatalog(id);
        return ApiResponse.success(catalog);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCatalog(@PathVariable String id) {
        catalogService.deleteCatalog(id);
        return ApiResponse.success();
    }
}