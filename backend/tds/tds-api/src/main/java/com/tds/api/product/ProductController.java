package com.tds.api.product;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.common.core.ApiResponse;
import com.tds.common.core.PageResult;
import com.tds.dal.entity.TbDataProduct;
import com.tds.service.product.ProductCreateDTO;
import com.tds.service.product.ProductServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 数据产品API控制器
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductServiceImpl productService;

    @PostMapping("/page")
    public ApiResponse<PageResult<TbDataProduct>> getProductPage(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer status) {

        IPage<TbDataProduct> page = productService.getProductPage(
                pageNumber, pageSize, productName, status);
        PageResult<TbDataProduct> result = PageResult.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<TbDataProduct> getProduct(@PathVariable String id) {
        TbDataProduct product = productService.getProductById(id);
        return ApiResponse.success(product);
    }

    @PostMapping
    public ApiResponse<TbDataProduct> createProduct(@RequestBody ProductCreateDTO dto) {
        TbDataProduct product = productService.createProduct(dto);
        return ApiResponse.success(product);
    }

    @PutMapping("/{id}")
    public ApiResponse<TbDataProduct> updateProduct(
            @PathVariable String id,
            @RequestBody ProductCreateDTO dto) {
        TbDataProduct product = productService.updateProduct(id, dto);
        return ApiResponse.success(product);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/submitAudit")
    public ApiResponse<TbDataProduct> submitForAudit(@PathVariable String id) {
        TbDataProduct product = productService.submitForAudit(id);
        return ApiResponse.success(product);
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<TbDataProduct> approveProduct(@PathVariable String id) {
        TbDataProduct product = productService.approveProduct(id);
        return ApiResponse.success(product);
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<TbDataProduct> rejectProduct(
            @PathVariable String id,
            @RequestParam String reason) {
        TbDataProduct product = productService.rejectProduct(id, reason);
        return ApiResponse.success(product);
    }

    @PostMapping("/{id}/offline")
    public ApiResponse<TbDataProduct> offlineProduct(@PathVariable String id) {
        TbDataProduct product = productService.offlineProduct(id);
        return ApiResponse.success(product);
    }
}