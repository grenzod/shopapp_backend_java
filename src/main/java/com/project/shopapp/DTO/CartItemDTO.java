package com.project.shopapp.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemDTO {
    @JsonProperty("product_id")
    private Long productId;

    private int quantity;
}
