package com.project.shopapp.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderProcessingResult {

    private boolean success;
    private String message;
    private String orderId;
    private Instant processedAt;
    private List<Long> productIds;

    public static OrderProcessingResult success(String message) {
        return OrderProcessingResult.builder()
                .success(true)
                .message(message)
                .processedAt(Instant.now())
                .build();
    }

    public static OrderProcessingResult failed(String message, List<Long> ids) {
        return OrderProcessingResult.builder()
                .success(false)
                .message(message)
                .processedAt(Instant.now())
                .productIds(ids)
                .build();
    }
}
