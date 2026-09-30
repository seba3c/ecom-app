package com.ecommerce.project.service;

import com.ecommerce.project.dto.CartDetailResponse;
import com.ecommerce.project.dto.CartListResponse;

public interface CartService {

    CartDetailResponse addProductToCart(Long userId, Long productId, Integer quantity);

    CartDetailResponse updateProductQuantity(Long userId, Long productId, Integer quantity);

    CartDetailResponse removeProductFromCart(Long userId, Long productId);

    CartDetailResponse getCart(Long userId);

    void clearCart(Long userId);

    CartListResponse getAllCarts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}
