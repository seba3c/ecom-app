package com.ecommerce.project.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDetailResponse {
    private Long id;
    private BigDecimal totalPrice;
    private List<CartItemDetail> cartItems = new ArrayList<>();
}
