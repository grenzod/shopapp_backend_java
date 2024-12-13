package com.project.shopapp.repositories;

import com.project.shopapp.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleAccountId(String googleAccountId);
    Optional<User> findByFacebookAccountId(String facebookAccountId);

    @Query("SELECT p FROM User p WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR p.fullName LIKE %:keyword% OR p.address LIKE %:keyword%) AND p.role.id = 1")
    Page<User> searchUsers(@Param("keyword") String keyword,
                                 Pageable pageable);
}
