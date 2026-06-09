package com.shopping.controller;

import com.shopping.dto.Result;
import com.shopping.entity.Category;
import com.shopping.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器 - 获取商品分类（公开接口）
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    /**
     * 获取所有分类
     */
    @GetMapping
    public Result<List<Category>> getAllCategories() {
        return Result.success(categoryRepository.findAllByOrderBySortOrderAsc());
    }
}
