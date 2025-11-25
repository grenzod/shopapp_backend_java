package com.project.shopapp.services.impl;

import com.project.shopapp.DTO.OrderDTO;
import com.project.shopapp.services.IOrderProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderProducer implements IOrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendOrderRequest(OrderDTO orderDTO) {
        kafkaTemplate.send("order-requests", orderDTO.getOrderId(), orderDTO);
    }

    @Override
    public void sendOrderResult(String orderId, boolean success, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("success", success);
        result.put("message", message);
        result.put("timestamp", new Date());

        kafkaTemplate.send("order-results", orderId, result);
    }
}
