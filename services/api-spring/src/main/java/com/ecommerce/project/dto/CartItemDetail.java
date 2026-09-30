package com.ecommerce.project.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDetail {

    private Long id;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discount;
    private ProductDetailResponse product;
}
