package com.shopping.service;

import com.shopping.dto.ProductRequest;
import com.shopping.dto.Result;
import com.shopping.entity.Product;
import com.shopping.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 商品服务 - 处理商品查询和管理
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 获取商品列表（分页）
     */
    public Result<Page<Product>> getProductList(int page, int size, Long categoryId, String keyword, String sort) {
        Pageable pageable;

        // 排序处理
        switch (sort != null ? sort : "default") {
            case "price_asc":
                pageable = PageRequest.of(page, size, Sort.by("price").ascending());
                break;
            case "price_desc":
                pageable = PageRequest.of(page, size, Sort.by("price").descending());
                break;
            case "sales":
                pageable = PageRequest.of(page, size, Sort.by("sales").descending());
                break;
            case "newest":
                pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                break;
            default:
                pageable = PageRequest.of(page, size, Sort.by("sales").descending());
        }

        Page<Product> products;

        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productRepository.searchByKeyword(keyword.trim(), pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryIdAndStatus(categoryId, 1, pageable);
        } else {
            products = productRepository.findByStatus(1, pageable);
        }

        return Result.success(products);
    }

    /**
     * 获取商品详情
     */
    public Result<Product> getProductDetail(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return Result.error(404, "商品不存在");
        }
        return Result.success(product);
    }

    /**
     * 获取热门商品
     */
    public Result<Page<Product>> getHotProducts(int size) {
        Page<Product> products = productRepository.findHotProducts(PageRequest.of(0, size));
        return Result.success(products);
    }

    /**
     * 获取最新商品
     */
    public Result<Page<Product>> getNewProducts(int size) {
        Page<Product> products = productRepository.findNewProducts(PageRequest.of(0, size));
        return Result.success(products);
    }

    /**
     * 添加商品（后台管理）
     */
    public Result<Product> addProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStock(request.getStock());
        product.setMainImage(request.getMainImage());
        product.setImages(request.getImages());
        product.setCategoryId(request.getCategoryId());
        product.setStatus(request.getStatus());

        productRepository.save(product);
        return Result.success(product);
    }

    /**
     * 更新商品（后台管理）
     */
    public Result<Product> updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return Result.error(404, "商品不存在");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStock(request.getStock());
        product.setMainImage(request.getMainImage());
        product.setImages(request.getImages());
        product.setCategoryId(request.getCategoryId());
        product.setStatus(request.getStatus());

        productRepository.save(product);
        return Result.success(product);
    }

    /**
     * 删除商品（后台管理）
     */
    public Result<Void> deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            return Result.error(404, "商品不存在");
        }
        productRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 获取所有商品（后台管理-分页）
     */
    public Result<Page<Product>> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return Result.success(productRepository.findAll(pageable));
    }
}
