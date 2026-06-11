package com.shopping.repository;

import com.shopping.entity.CategoryPointsRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryPointsRuleRepository extends JpaRepository<CategoryPointsRule, Long> {

    Optional<CategoryPointsRule> findByCategoryId(Long categoryId);
}
