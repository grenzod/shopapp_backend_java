package com.project.shopapp.services.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.project.shopapp.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.project.shopapp.DTO.CartItemDTO;
import com.project.shopapp.DTO.OrderDTO;
import com.project.shopapp.models.InventoryReservationResult;
import com.project.shopapp.models.OrderProcessingResult;
import com.project.shopapp.models.Enums.OrderStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderProcessingService {
    
    private final ProductService productService;
    private final DatabaseDistributedLock distributedLock;
    private final OrderRepository orderRepository;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 100;

    private static final Logger log = LoggerFactory.getLogger(OrderProcessingService.class);

    @Async("orderProcessingExecutor")
    public CompletableFuture<OrderProcessingResult> processOrder(OrderDTO orderDTO) {
        log.info("Starting order processing for order: {}", orderDTO.getOrderId());
        try {
            String lockKey = generateLockKey(orderDTO.getCartItems());
            boolean lockAcquired = distributedLock.acquireLock(lockKey, 10);
            if (!lockAcquired) {
                return CompletableFuture.completedFuture(OrderProcessingResult.failed("System busy, please try again", null));
            }
            try {
                InventoryReservationResult reservationResult = productService.reserveInventoryAtomically(orderDTO.getOrderId(), orderDTO.getCartItems());
                if (!reservationResult.isSuccess()) {
                    updateOrderStatus(orderDTO.getOrderId(), OrderStatus.CANCELLED, reservationResult.getErrorMessage());
                    return CompletableFuture.completedFuture(OrderProcessingResult.failed(reservationResult.getErrorMessage(), reservationResult.getReservedProductIds()));
                }
                updateOrderStatus(orderDTO.getOrderId(), OrderStatus.PROCESSING, "Successfully");
                return CompletableFuture.completedFuture(OrderProcessingResult.success("Order processed successfully"));
            } finally {
                distributedLock.releaseLock(lockKey);
            }
        } catch (Exception e) {
            log.error("Unexpected error processing order {}: {}", orderDTO.getOrderId(), e.getMessage(), e);
            return CompletableFuture.completedFuture(OrderProcessingResult.failed("System error: " + e.getMessage(), null));
        }
    }
    
    
    private String generateLockKey(List<CartItemDTO> cartItems) {
        List<Long> sortedProductIds = cartItems.stream()
            .map(CartItemDTO::getProductId)
            .sorted()
            .distinct()
            .toList();
        
        return "order_lock:" + sortedProductIds.stream()
            .map(String::valueOf)
            .collect(Collectors.joining("_"));
    }

    public void updateOrderStatus(String orderId, String status, String note) {
        orderRepository.findByOrderId(orderId).ifPresent(order -> {
            order.setStatus(status);
            order.setNote(note);
            orderRepository.save(order);
        });
    }
    
    // Retry mechanism với exponential backoff
    public CompletableFuture<OrderProcessingResult> processOrderWithRetry(OrderDTO orderDTO, int retryCount) {
        return processOrder(orderDTO)
            .thenCompose(result -> {
                if (result.isSuccess() || retryCount >= MAX_RETRY_ATTEMPTS) {
                    return CompletableFuture.completedFuture(result);
                }
                
                // Exponential backoff
                long delay = RETRY_DELAY_MS * (long) Math.pow(2, retryCount);
                log.info("Retrying order {} in {} ms (attempt {})", 
                        orderDTO.getOrderId(), delay, retryCount + 1);
                
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return OrderProcessingResult.failed("Retry interrupted", null);
                    }
                    return null;
                }).thenCompose(v -> processOrderWithRetry(orderDTO, retryCount + 1));
            });
    }
}
