package com.project.shopapp.services.impl;

import com.github.javafaker.Faker;
import com.project.shopapp.DTO.CartItemDTO;
import com.project.shopapp.DTO.OrderDTO;
import com.project.shopapp.DTO.ProductDTO;
import com.project.shopapp.DTO.UserDTO;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.IUserService;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FakeService {
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final IUserService iUserService;
    private final ProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(FakeService.class);

    @Async("taskExecutor")
    public CompletableFuture<Void> createFakeUser(int index) {
        try {
            Faker faker = new Faker();
            String userName = faker.name().username();
            String phoneNumber = "0" + faker.phoneNumber().subscriberNumber(8);

            if (userRepository.existsByPhoneNumber(phoneNumber)) {
                return CompletableFuture.completedFuture(null);
            }

            String passWord = "175003";
            String random = faker.random().hex(6);
            String email = "test" + random + "@gmail.com";

            UserDTO userDTO = UserDTO.builder()
                    .fullName(userName)
                    .phoneNumber(phoneNumber)
                    .password(passWord)
                    .email(email)
                    .roleId(1)
                    .build();

            iUserService.createUser(userDTO);
        } catch (Exception e) {
            logger.error("Error creating fake user at index: {}", index, e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async("productExecutor")
    public CompletableFuture<Void> createFakeProduct(int index) {
        try {
            Faker faker = new Faker();
            String productName = faker.commerce().productName();

            if (productService.existsByName(productName)) {
                return CompletableFuture.completedFuture(null);
            }

            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .price((float)faker.number().numberBetween(10, 1000))
                    .description(faker.lorem().sentence())
                    .thumbnail(null)
                    .categoryId((long)faker.number().numberBetween(1, 4))
                    .quantity(faker.number().numberBetween(1, 10))
                    .build();
            productService.createProduct(productDTO);
        } catch (Exception e) {
            logger.error("Error creating fake user at index: {}", index, e);
        }

        return CompletableFuture.completedFuture(null);
    }

    public void executeLoadTest(int totalOrders, int numThreads) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(totalOrders);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalOrders; i++) {
            final int orderNum = i;
            executor.submit(() -> {
                try {
                    OrderDTO orderDTO = createTestOrder(orderNum);
                    orderService.createOrder(orderDTO);
                } catch (Exception e) {
                    logger.error("Error creating order {}: {}", orderNum, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.MINUTES); // Timeout 5 phút
        long endTime = System.currentTimeMillis();

        logger.info("Processed {} orders in {} ms", totalOrders, (endTime - startTime));
        logger.info("Throughput: {}/sec", totalOrders / ((endTime - startTime) / 1000.0));

        executor.shutdown();
    }

    private OrderDTO createTestOrder(int orderNum) {
        Faker faker = new Faker();
        List<CartItemDTO> cartItems = new ArrayList<>();
        int totalAmount = 0;

        // Tạo 1-5 cart items ngẫu nhiên
        int numberOfItems = faker.number().numberBetween(1, 6);
        for (int i = 0; i < numberOfItems; i++) {
            Long productId = faker.number().numberBetween(1L, 400L);
            int quantity = faker.number().numberBetween(1, 5);
        
            cartItems.add(CartItemDTO.builder()
                .productId(productId)
                .quantity(quantity)
                .build());
                
            totalAmount += quantity;
        }

        return OrderDTO.builder()
            .userId(faker.number().numberBetween(1L, 10000L))
            .orderId("LOAD_TEST_" + System.currentTimeMillis() + "_" + orderNum)
            .fullName(faker.name().fullName())
            .phoneNumber(faker.phoneNumber().cellPhone())
            .address(faker.address().fullAddress())
            .email(faker.internet().emailAddress())
            .cartItems(cartItems)
            .totalMoney((long) totalAmount)
            .build();
    }

}
