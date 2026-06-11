package com.shopping.repository;

import com.shopping.entity.UserMembership;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {

    Optional<UserMembership> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT um FROM UserMembership um WHERE um.userId = :userId")
    Optional<UserMembership> findByUserIdForUpdate(Long userId);
}
