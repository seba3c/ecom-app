package com.ecommerce.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.project.dto.CartDetailResponse;
import com.ecommerce.project.dto.CartItemDetail;
import com.ecommerce.project.dto.CartListResponse;
import com.ecommerce.project.dto.ProductDetailResponse;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.User;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.service.UserDetailsServiceImpl;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.util.AuthUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private AuthUtils authUtils;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        when(authUtils.loggedInUser()).thenReturn(user);
    }

    private CartDetailResponse sampleCart() {
        ProductDetailResponse product = new ProductDetailResponse(
                1L, "Laptop Pro", "desc", 10, BigDecimal.valueOf(999.99), BigDecimal.ZERO, null);
        CartItemDetail item = new CartItemDetail(1L, 2, BigDecimal.valueOf(999.99), BigDecimal.ZERO, product);
        CartDetailResponse cart = new CartDetailResponse();
        cart.setId(1L);
        cart.setTotalPrice(BigDecimal.valueOf(1999.98));
        cart.setCartItems(List.of(item));
        return cart;
    }

    @Test
    void addProductToCart_success_returns201() throws Exception {
        when(cartService.addProductToCart(anyLong(), anyLong(), any())).thenReturn(sampleCart());

        mockMvc.perform(post("/api/my_cart/1/quantity/2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cartItems[0].quantity").value(2));
    }

    @Test
    void addProductToCart_invalidQuantity_returns400() throws Exception {
        mockMvc.perform(post("/api/my_cart/1/quantity/0")).andExpect(status().isBadRequest());
    }

    @Test
    void addProductToCart_productNotFound_returns404() throws Exception {
        when(cartService.addProductToCart(anyLong(), eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Product", "id", 99L));

        mockMvc.perform(post("/api/my_cart/99/quantity/1")).andExpect(status().isNotFound());
    }

    @Test
    void addProductToCart_alreadyInCart_returns400() throws Exception {
        when(cartService.addProductToCart(anyLong(), anyLong(), any()))
                .thenThrow(new APIException("Product already in cart. Use PUT to update quantity."));

        mockMvc.perform(post("/api/my_cart/1/quantity/2")).andExpect(status().isBadRequest());
    }

    @Test
    void updateProductQuantity_success_returns200() throws Exception {
        CartDetailResponse updated = sampleCart();
        when(cartService.updateProductQuantity(anyLong(), anyLong(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/my_cart/1/quantity/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateProductQuantity_invalidQuantity_returns400() throws Exception {
        mockMvc.perform(put("/api/my_cart/1/quantity/0")).andExpect(status().isBadRequest());
    }

    @Test
    void updateProductQuantity_itemNotFound_returns400() throws Exception {
        when(cartService.updateProductQuantity(anyLong(), eq(99L), any()))
                .thenThrow(new APIException("Product not found in cart"));

        mockMvc.perform(put("/api/my_cart/99/quantity/3")).andExpect(status().isBadRequest());
    }

    @Test
    void removeProductFromCart_success_returns200() throws Exception {
        CartDetailResponse emptyCart = new CartDetailResponse();
        emptyCart.setId(1L);
        emptyCart.setTotalPrice(BigDecimal.ZERO);
        emptyCart.setCartItems(new ArrayList<>());
        when(cartService.removeProductFromCart(anyLong(), anyLong())).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/my_cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getCart_success_returns200() throws Exception {
        when(cartService.getCart(anyLong())).thenReturn(sampleCart());

        mockMvc.perform(get("/api/my_cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalPrice").value(1999.98));
    }

    @Test
    void getAllCarts_success_returns200() throws Exception {
        CartListResponse response = new CartListResponse(List.of(sampleCart()), 0, 50, 1L, 1, true);
        when(cartService.getAllCarts(any(), any(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/admin/carts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }
}
