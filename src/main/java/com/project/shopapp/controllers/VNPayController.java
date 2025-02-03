package com.project.shopapp.controllers;

import com.project.shopapp.DTO.OrderDTO;
import com.project.shopapp.DTO.PaymentDTO;
import com.project.shopapp.models.Order;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.responses.ResponseObject;
import com.project.shopapp.services.IOrderService;
import com.project.shopapp.services.IPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/payment")
public class VNPayController {
    private final IPaymentService paymentService;
    private final IOrderService iOrderService;
    private final OrderRepository orderRepository;

    @PostMapping("/vn-pay")
    public ResponseEntity<?> pay(@Valid @RequestBody OrderDTO orderDTO, HttpServletRequest request) throws Exception {
        Order order = iOrderService.createOrder(orderDTO);
        PaymentDTO paymentDTO = paymentService.createVnPayPayment(request);
        order.setTrackingNumber(paymentDTO.getCode());
        orderRepository.save(order);
        return ResponseEntity.ok().body(
                new ResponseObject(
                        "Successfully",
                        HttpStatus.OK,
                        paymentDTO
                )
        );
    }

    @PostMapping("/vn-pay-callback")
    public ResponseEntity<?> payCallbackHandler(@RequestParam("status") String status,
                                                @RequestParam("tracking_number") String trackingNumber) throws Exception {
        if (status.equals("00")) {
            Order order = orderRepository.findByTrackingNumber(trackingNumber);
            order.setStatus("Success");
            orderRepository.save(order);
            return ResponseEntity.ok().body(
                    new ResponseObject(
                        "Success",
                        HttpStatus.OK,
                        PaymentDTO.builder().code("00").message("Success").paymentUrl("").build()
                    )
            );
        }
        else {
            return ResponseEntity.ok().body(
                    new ResponseObject(
                        "Fail",
                        HttpStatus.BAD_REQUEST,
                            null
                    )
            );
        }
    }
}
