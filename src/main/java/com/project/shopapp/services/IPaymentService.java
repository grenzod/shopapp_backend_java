package com.project.shopapp.services;

import com.project.shopapp.DTO.PaymentDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface IPaymentService {
    PaymentDTO createVnPayPayment(HttpServletRequest request);
}
