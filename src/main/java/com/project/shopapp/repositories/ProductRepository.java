package com.project.shopapp.repositories;

import com.project.shopapp.models.Entities.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);
    Page<Product> findAll(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR :categoryId = 0 OR p.category.id = :categoryId) " +
            "AND (:keyword IS NULL OR :keyword = '' OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("categoryId") Long categoryId,
                                 Pageable pageable);

    @Query("select p from Product p left join FETCH p.productImages where p.id = :productId")
    Optional<Product> getDetailProduct(@Param("productId") Long productId);

    @Query("select p from Product p where p.id in :productIds")
    List<Product> findProductsByIds(@Param("productIds") List<Long> productIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE products SET quantity = quantity - :quantity WHERE id = :productId AND quantity >= :quantity", nativeQuery = true)
    int decreaseQuantity(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Query(value = "SELECT p FROM products WHERE p.id = :productId", nativeQuery = true)
    Integer getAvailableQuantity(@Param("productId") Long productId);

    @Query(value = "SELECT p.id AS productId, p.quantity AS quantity " +
            "FROM products p " +
            "WHERE p.id IN :productIds", nativeQuery = true)
    List<Map<String, Object>> getQuantitiesByProductIds(@Param("productIds") List<Long> productIds);

}
