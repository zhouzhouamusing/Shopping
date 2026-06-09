package com.shopping.repository;

import com.shopping.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 分类数据访问层
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderBySortOrderAsc();
    List<Category> findByParentIdOrderBySortOrderAsc(Long parentId);
}
