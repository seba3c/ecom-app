package com.ecommerce.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.project.dto.OrderDetailResponse;
import com.ecommerce.project.dto.OrderItemDetail;
import com.ecommerce.project.dto.PaymentDetail;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.OrderStatus;
import com.ecommerce.project.model.User;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.service.UserDetailsServiceImpl;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.util.AuthUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private AuthUtils authUtils;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private User sampleUser() {
        User user = new User("testuser", "test@example.com", "password");
        user.setId(1L);
        return user;
    }

    private OrderDetailResponse sampleOrderResponse() {
        PaymentDetail payment = new PaymentDetail(1L, "card", "Stripe", "pi_123", "succeeded", "Payment successful");
        OrderItemDetail item = new OrderItemDetail(1L, 2, BigDecimal.valueOf(999.99), BigDecimal.ZERO, null);
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(1L);
        response.setEmail("test@example.com");
        response.setItems(List.of(item));
        response.setOrderDate(LocalDateTime.of(2026, 4, 27, 10, 0));
        response.setTotalAmount(BigDecimal.valueOf(1999.98));
        response.setStatus(OrderStatus.PENDING);
        response.setShippingAddressId(1L);
        response.setPayment(payment);
        return response;
    }

    @Test
    void placeOrder_success_returns201() throws Exception {
        when(authUtils.loggedInUser()).thenReturn(sampleUser());
        when(orderService.placeOrder(eq(1L), any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleOrderResponse());

        mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                    "addressId": 1,
                                    "paymentMethod": "card",
                                    "pgName": "Stripe",
                                    "pgPaymentId": "pi_123",
                                    "pgStatus": "succeeded",
                                    "pgResponse": "Payment successful"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(1999.98))
                .andExpect(jsonPath("$.payment.pgName").value("Stripe"));
    }

    @Test
    void placeOrder_invalidRequest_missingAddressId_returns400() throws Exception {
        mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                    "paymentMethod": "card",
                                    "pgName": "Stripe"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placeOrder_invalidRequest_shortPaymentMethod_returns400() throws Exception {
        mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                    "addressId": 1,
                                    "paymentMethod": "cc",
                                    "pgName": "Stripe"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placeOrder_cartEmpty_returns400() throws Exception {
        when(authUtils.loggedInUser()).thenReturn(sampleUser());
        when(orderService.placeOrder(eq(1L), any(), any(), any(), any(), any(), any()))
                .thenThrow(new APIException("Cart is empty"));

        mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                    "addressId": 1,
                                    "paymentMethod": "card",
                                    "pgName": "Stripe"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placeOrder_addressNotFound_returns404() throws Exception {
        when(authUtils.loggedInUser()).thenReturn(sampleUser());
        when(orderService.placeOrder(eq(1L), any(), any(), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Address", "id", 99L));

        mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                    "addressId": 99,
                                    "paymentMethod": "card",
                                    "pgName": "Stripe"
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
