package com.project.shopapp.repositories;

import com.project.shopapp.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(long id);

    @Query("select p from Order p where " +
            "(:key is null or :key = '' or p.fullName like %:key% " +
            "or p.address like %:key% or p.note like %:key%)")
    Page<Order> findByKey(String key, Pageable pageable);
}
