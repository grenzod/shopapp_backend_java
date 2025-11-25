package com.project.shopapp.services.impl;

import com.project.shopapp.DTO.CartItemDTO;
import com.project.shopapp.DTO.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Entities.Order;
import com.project.shopapp.models.Entities.OrderDetail;
import com.project.shopapp.models.Entities.Product;
import com.project.shopapp.models.Entities.User;
import com.project.shopapp.models.Enums.OrderStatus;
import com.project.shopapp.models.OrderProcessingResult;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.responses.OrderResponse;
import com.project.shopapp.services.IOrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderProducer orderProducer;
    private final ModelMapper modelMapper;
    private final OrderProcessingService orderProcessingService;
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Override
    public Order createOrder(OrderDTO orderDTO) throws Exception {
        User user = userRepository
                .findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("Can not found user"));

        validateOrderDTO(orderDTO);

        Order order = getOrder(orderDTO, user);
        ;
        Order savedOrder = orderRepository.save(order);

        orderProducer.sendOrderRequest(orderDTO);

        return savedOrder;
    }

    private Order getOrder(OrderDTO orderDTO, User user) {
        Order order = new Order();
        order.setOrderId(orderDTO.getOrderId());
        order.setFullName(orderDTO.getFullName());
        order.setEmail(orderDTO.getEmail());
        order.setPhoneNumber(orderDTO.getPhoneNumber());
        order.setAddress(orderDTO.getAddress());
        order.setNote(orderDTO.getNote());
        order.setTotalMoney(orderDTO.getTotalMoney());
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.PENDING);
        order.setActive(true);

        List<OrderDetail> orderDetails = orderDTO.getCartItems().stream().map(a -> {
            Product product = null;
            try {
                product = productRepository.findById(a.getProductId())
                        .orElseThrow(() -> new DataNotFoundException("Product not found"));
            } catch (DataNotFoundException e) {
                throw new RuntimeException(e);
            }

            return OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .price(product.getPrice())
                    .numberOfProducts(a.getQuantity())
                    .totalMoney(product.getPrice() * a.getQuantity())
                    .status(a.getStatus())
                    .build();
        }).toList();

        order.setOrderDetails(orderDetails);

        return order;
    }

    private void validateOrderDTO(OrderDTO orderDTO) {
        if (orderDTO.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (orderDTO.getCartItems() == null || orderDTO.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cart items cannot be empty");
        }
        for (CartItemDTO item : orderDTO.getCartItems()) {
            if (item.getProductId() == null) {
                throw new IllegalArgumentException("Product ID is required");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }
        }
    }

    @KafkaListener(topics = "order-requests", groupId = "order-processor", concurrency = "${spring.kafka.listener.concurrency:5}")
    public void processOrder(List<OrderDTO> orderDTOs, Acknowledgment ack) {
        try {
            List<CompletableFuture<OrderProcessingResult>> futures = orderDTOs.stream()
                    .map(orderDTO -> orderProcessingService.processOrderWithRetry(orderDTO, 0))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenAccept(v -> {
                        List<OrderProcessingResult> results = futures.stream()
                                .map(future -> {
                                    try {
                                        return future.get();
                                    }
                                    catch (Exception e) {
                                        log.error("Error getting future result: {}", e.getMessage());
                                        return OrderProcessingResult.failed("Processing error: " + e.getMessage(), null);
                                    }
                                })
                                .toList();

                        processBatchResults(orderDTOs, results);
                        ack.acknowledge(); // tác dụng thông báo cho kafka rằng đã xử lý thành công message
                        log.info("Batch processed successfully, offsets committed");
                    }).exceptionally(ex -> {
                        log.error("Batch processing failed with exception: {}", ex.getMessage(), ex);
                        return null;
                    });

        } catch (Exception e) {
            log.error("Error processing order batch: {}", e.getMessage(), e);
        }
    }

    private void processBatchResults(List<OrderDTO> orderDTOS, List<OrderProcessingResult> results) {
        for (int i = 0; i < results.size(); i++) {
            OrderProcessingResult result = results.get(i);
            OrderDTO orderDTO = orderDTOS.get(i);

            if (result.isSuccess()) {
                orderProducer.sendOrderResult(orderDTO.getOrderId(), true, result.getMessage());
            } else {
                orderProducer.sendOrderResult(orderDTO.getOrderId(), false, result.getMessage());
                orderProcessingService.updateOrderStatus(orderDTO.getOrderId(), OrderStatus.CANCELLED, result.getMessage());
            }
        }
    }

    public Order getOrderStatus(String id) throws DataNotFoundException {
        return orderRepository.findByOrderId(id).orElseThrow(() -> new DataNotFoundException("Order not found"));
    }

    @Override
    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Order updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException {
        Order orderExisting = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        User userExisting = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        modelMapper.map(orderDTO, orderExisting);
        orderExisting.setUser(userExisting);

        return orderRepository.save(orderExisting);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if(order != null) {
            order.setActive(false);
            orderRepository.save(order);
        }
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Page<OrderResponse> findByKey(String key, Pageable pageable) {
        Page<Order> orders = orderRepository.findByKey(key, pageable);
        return orders.map(OrderResponse::fromOrder);
    }
}
