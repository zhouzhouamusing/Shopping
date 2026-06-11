package com.shopping.repository;

import com.shopping.entity.MemberLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MemberLevelRepository extends JpaRepository<MemberLevel, Long> {

    Optional<MemberLevel> findByLevelCode(Integer levelCode);

    List<MemberLevel> findAllByOrderByLevelCodeAsc();

    Optional<MemberLevel> findTopByMinSpendingLessThanEqualOrderByLevelCodeDesc(BigDecimal spending);
}
