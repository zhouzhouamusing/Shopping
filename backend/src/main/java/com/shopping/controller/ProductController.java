package com.shopping.controller;

import com.shopping.dto.Result;
import com.shopping.entity.Product;
import com.shopping.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器 - 商品列表、搜索、详情（公开接口）
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 获取商品列表（支持分页、分类筛选、关键词搜索、排序）
     */
    @GetMapping
    public Result<Page<Product>> getProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort) {
        return productService.getProductList(page, size, categoryId, keyword, sort);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    /**
     * 获取热门商品
     */
    @GetMapping("/hot")
    public Result<Page<Product>> getHotProducts(@RequestParam(defaultValue = "8") int size) {
        return productService.getHotProducts(size);
    }

    /**
     * 获取最新商品
     */
    @GetMapping("/new")
    public Result<Page<Product>> getNewProducts(@RequestParam(defaultValue = "8") int size) {
        return productService.getNewProducts(size);
    }
}
