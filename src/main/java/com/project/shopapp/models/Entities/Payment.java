package com.project.shopapp.models.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    private BigDecimal amount;

    private String status;

    @Column(name = "payment_code")
    private String paymentCode;

    @Column(name = "payment_date")
    private Date paymentDate;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "expected_payment_date")
    private LocalDate expectedPaymentDate;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updateAt;

    private String notes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private List<PaymentHistory> paymentHistories;

}
