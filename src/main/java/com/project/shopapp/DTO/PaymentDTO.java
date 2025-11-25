package com.project.shopapp.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDTO {
    private String code;
    private String message;
    private String paymentUrl;
}
