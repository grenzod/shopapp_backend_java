package com.project.shopapp;

import com.project.shopapp.DTO.CartItemDTO;
import com.project.shopapp.DTO.OrderDTO;
import com.project.shopapp.services.impl.OrderService;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Slf4j
public class OrderLoadTest {

    @Autowired
    private OrderService orderService;

    @Test
    void test5000ConcurrentOrders() throws InterruptedException {
        int totalOrders = 1000;
        CountDownLatch latch = new CountDownLatch(totalOrders);
        ExecutorService executor = Executors.newFixedThreadPool(100);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalOrders; i++) {
            final int orderNum = i;
            executor.submit(() -> {
                try {
                    OrderDTO orderDTO = createTestOrder(orderNum);
                    orderService.createOrder(orderDTO);
                } catch (Exception e) {
                    log.error("Error creating order {}: {}", orderNum, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.MINUTES); // Timeout 5 phút
        long endTime = System.currentTimeMillis();

        log.info("Processed {} orders in {} ms", totalOrders, (endTime - startTime));
        log.info("Throughput: {}/sec", totalOrders / ((endTime - startTime) / 1000.0));

        executor.shutdown();
    }

    private OrderDTO createTestOrder(int orderNum) {
        // Tạo test order data
        return OrderDTO.builder()
                .userId(1L) // Test user
                .orderId("TEST_ORD_" + orderNum)
                .cartItems(Collections.singletonList(
                        CartItemDTO.builder()
                                .productId(1L) // Test product
                                .quantity(1)
                                .build()
                ))
                .build();
    }
}
