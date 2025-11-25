package com.project.shopapp.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO {
    @JsonProperty("order_id")
    private String orderId = "LOAD_TEST_" + System.currentTimeMillis() + "_" + ((int)(Math.random() * 10));

    @JsonProperty("user_id")
    @Min(value = 1, message = "User's ID must be > 0")
    private Long userId;

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    @JsonProperty("phone_number")
    @NotBlank(message = "Phone number is required")
    @Size(min = 10, message = "Phone number must be at 10 characters")
    private String phoneNumber;

    private String address;

    private String note;

    @JsonProperty("total_money")
    @Min(value = 0, message = "Total money must be >= 0")
    private Long totalMoney;

    @JsonProperty("coupon_code")
    private String couponCode;

    @JsonProperty("cart_items")
    private List<CartItemDTO> cartItems;
}
