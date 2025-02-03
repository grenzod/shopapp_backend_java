package com.project.shopapp.DTO;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDTO {
    private String code;
    private String message;
    private String paymentUrl;
}
