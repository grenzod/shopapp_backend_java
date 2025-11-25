package com.project.shopapp.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemDTO {
    @JsonProperty("product_id")
    private Long productId;

    private String name;

    private String status = "In stock";

    private int quantity = -1;
}
