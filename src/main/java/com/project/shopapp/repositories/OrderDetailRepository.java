package com.project.shopapp.repositories;

import com.project.shopapp.models.Entities.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long orderId);

    @Query(value = "SELECT od.* FROM order_details od JOIN orders o ON od.order_id = o.id WHERE o.order_id = :orderId AND od.product_id IN (:productIds)",
            nativeQuery = true)
    List<OrderDetail> findOrderDetailsNew(@Param("orderId") String orderId, @Param("productIds") List<Long> productIds);

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_details SET status = :status WHERE id IN :ids", nativeQuery = true)
    void updateStatusByIds(@Param("status") String status, @Param("ids") List<Long> ids);

}
