package com.project.shopapp.services;

import com.project.shopapp.DTO.OrderDTO;

public interface IOrderProducer {
    void sendOrderRequest(OrderDTO orderDTO);
    void sendOrderResult(String orderId, boolean success, String message);
}
