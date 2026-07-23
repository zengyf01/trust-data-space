package com.tds.datar.controller.catalog;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.service.catalog.CatalogDTO;
import com.tds.datar.service.catalog.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 资源目录管理
 */
@RestController
@RequestMapping("/catalog")
public class CatalogController {

    @Autowired
    private CatalogService catalogService;

    @GetMapping("/page")
    public ApiResponse<?> getCatalogPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String catalogName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String spaceId) {
        return ApiResponse.success(catalogService.getCatalogPage(currentPage, pageSize, catalogName, status, spaceId));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getCatalogById(@PathVariable String id) {
        return ApiResponse.success(catalogService.getCatalogById(id));
    }

    @PostMapping
    public ApiResponse<?> createCatalog(@RequestBody CatalogDTO dto) {
        return ApiResponse.success(catalogService.createCatalog(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateCatalog(@PathVariable String id, @RequestBody CatalogDTO dto) {
        return ApiResponse.success(catalogService.updateCatalog(id, dto));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<?> publishCatalog(@PathVariable String id) {
        return ApiResponse.success(catalogService.publishCatalog(id));
    }

    @PostMapping("/{id}/offline")
    public ApiResponse<?> offlineCatalog(@PathVariable String id) {
        return ApiResponse.success(catalogService.offlineCatalog(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteCatalog(@PathVariable String id) {
        catalogService.deleteCatalog(id);
        return ApiResponse.success(null);
    }
}