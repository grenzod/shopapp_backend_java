package com.project.shopapp.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReservationResult {

    private boolean success;
    private String errorMessage;
    private List<Long> reservedProductIds;

    public static InventoryReservationResult success() {
        return InventoryReservationResult.builder()
                .success(true)
                .reservedProductIds(new ArrayList<>())
                .build();
    }

    public static InventoryReservationResult failed(String errorMessage, List<Long> ids) {
        return InventoryReservationResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .reservedProductIds(ids)
                .build();
    }
}
