package com.ecommerce.project.service;

import com.ecommerce.project.dto.OrderDetailResponse;

public interface OrderService {

    OrderDetailResponse placeOrder(
            Long userId,
            Long addressId,
            String paymentMethod,
            String pgName,
            String pgPaymentId,
            String pgStatus,
            String pgResponse);
}
