package com.tds.datar.controller.product;

import com.tds.datar.common.core.ApiResponse;
import com.tds.datar.service.product.ProductDTO;
import com.tds.datar.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 数据产品管理
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/page")
    public ApiResponse<?> getProductPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String spaceId) {
        return ApiResponse.success(productService.getProductPage(currentPage, pageSize, productName, status, spaceId));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getProductById(@PathVariable String id) {
        return ApiResponse.success(productService.getProductById(id));
    }

    @PostMapping
    public ApiResponse<?> createProduct(@RequestBody ProductDTO dto) {
        return ApiResponse.success(productService.createProduct(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateProduct(@PathVariable String id, @RequestBody ProductDTO dto) {
        return ApiResponse.success(productService.updateProduct(id, dto));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<?> publishProduct(@PathVariable String id) {
        return ApiResponse.success(productService.publishProduct(id));
    }

    @PostMapping("/{id}/offline")
    public ApiResponse<?> offlineProduct(@PathVariable String id) {
        return ApiResponse.success(productService.offlineProduct(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ApiResponse.success(null);
    }
}