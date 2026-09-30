package com.ecommerce.project.controller;

import com.ecommerce.project.config.SwaggerConfig;
import com.ecommerce.project.dto.OrderCreateRequest;
import com.ecommerce.project.dto.OrderDetailResponse;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.util.AuthUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = SwaggerConfig.Tags.Order.NAME, description = SwaggerConfig.Tags.Order.DESCRIPTION)
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<OrderDetailResponse> placeOrder(@Valid @RequestBody OrderCreateRequest request) {
        Long userId = authUtils.loggedInUser().getId();
        OrderDetailResponse response = orderService.placeOrder(
                userId,
                request.getAddressId(),
                request.getPaymentMethod(),
                request.getPgName(),
                request.getPgPaymentId(),
                request.getPgStatus(),
                request.getPgResponse());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
