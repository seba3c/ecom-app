package com.ecommerce.project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ecommerce.project.dto.CartDetailResponse;
import com.ecommerce.project.dto.CartItemDetail;
import com.ecommerce.project.dto.CartListResponse;
import com.ecommerce.project.dto.ProductDetailResponse;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.security.repository.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;
    private CartDetailResponse cartDTO;
    private CartItemDetail cartItemDTO;
    private ProductDetailResponse productDTO;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "password");
        user.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setName("Laptop Pro");
        product.setQuantity(10);
        product.setPrice(BigDecimal.valueOf(999.99));
        product.setDiscount(BigDecimal.valueOf(0.00));

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setCartItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setPrice(product.getPrice());
        cartItem.setDiscount(product.getDiscount());

        productDTO = new ProductDetailResponse(
                1L, "Laptop Pro", "desc", 10, BigDecimal.valueOf(999.99), BigDecimal.ZERO, null);

        cartItemDTO = new CartItemDetail(1L, 2, BigDecimal.valueOf(999.99), BigDecimal.ZERO, productDTO);

        cartDTO = new CartDetailResponse();
        cartDTO.setId(1L);
        cartDTO.setTotalPrice(BigDecimal.ZERO);
    }

    // --- addProductToCart ---

    @Test
    void addProductToCart_success_createsItemAndReturnsCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());
        when(cartRepository.save(cart)).thenReturn(cart);
        when(modelMapper.map(cart, CartDetailResponse.class)).thenReturn(cartDTO);
        when(modelMapper.map(any(CartItem.class), eq(CartItemDetail.class))).thenReturn(cartItemDTO);

        CartDetailResponse result = cartService.addProductToCart(1L, 1L, 2);

        assertNotNull(result);
        assertEquals(1, cart.getCartItems().size());
        verify(cartRepository).save(cart);
    }

    @Test
    void addProductToCart_noExistingCart_createsNewCart() {
        Cart savedCart = new Cart();
        savedCart.setId(2L);
        savedCart.setUser(user);
        savedCart.setCartItems(new ArrayList<>());
        savedCart.setTotalPrice(BigDecimal.ZERO);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(savedCart, product)).thenReturn(Optional.empty());
        when(modelMapper.map(savedCart, CartDetailResponse.class)).thenReturn(cartDTO);

        CartDetailResponse result = cartService.addProductToCart(1L, 1L, 1);

        assertNotNull(result);
    }

    @Test
    void addProductToCart_productNotFound_throwsResourceNotFoundException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addProductToCart(1L, 99L, 1));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void addProductToCart_insufficientStock_throwsAPIException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(APIException.class, () -> cartService.addProductToCart(1L, 1L, 100));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void addProductToCart_productAlreadyInCart_throwsAPIException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(cartItem));

        assertThrows(APIException.class, () -> cartService.addProductToCart(1L, 1L, 2));
        verify(cartRepository, never()).save(any());
    }

    // --- updateProductQuantity ---

    @Test
    void updateProductQuantity_success_updatesQuantity() {
        cart.getCartItems().add(cartItem);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(modelMapper.map(cart, CartDetailResponse.class)).thenReturn(cartDTO);
        when(modelMapper.map(cartItem, CartItemDetail.class)).thenReturn(cartItemDTO);

        CartDetailResponse result = cartService.updateProductQuantity(1L, 1L, 5);

        assertNotNull(result);
        assertEquals(5, cartItem.getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void updateProductQuantity_cartNotFound_throwsAPIException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(APIException.class, () -> cartService.updateProductQuantity(1L, 1L, 3));
    }

    @Test
    void updateProductQuantity_itemNotInCart_throwsAPIException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        assertThrows(APIException.class, () -> cartService.updateProductQuantity(1L, 1L, 3));
    }

    // --- removeProductFromCart ---

    @Test
    void removeProductFromCart_success_removesItem() {
        cart.getCartItems().add(cartItem);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(modelMapper.map(cart, CartDetailResponse.class)).thenReturn(cartDTO);

        cartService.removeProductFromCart(1L, 1L);

        assertTrue(cart.getCartItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void removeProductFromCart_itemNotInCart_throwsAPIException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        assertThrows(APIException.class, () -> cartService.removeProductFromCart(1L, 1L));
    }

    // --- getCart ---

    @Test
    void getCart_existingCart_returnsCartDTO() {
        cart.getCartItems().add(cartItem);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(modelMapper.map(cart, CartDetailResponse.class)).thenReturn(cartDTO);
        when(modelMapper.map(cartItem, CartItemDetail.class)).thenReturn(cartItemDTO);

        CartDetailResponse result = cartService.getCart(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getCartItems().size());
    }

    @Test
    void getCart_noExistingCart_createsAndReturnsEmptyCart() {
        Cart newCart = new Cart();
        newCart.setId(2L);
        newCart.setUser(user);
        newCart.setCartItems(new ArrayList<>());
        newCart.setTotalPrice(BigDecimal.ZERO);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);
        when(modelMapper.map(newCart, CartDetailResponse.class)).thenReturn(new CartDetailResponse());

        CartDetailResponse result = cartService.getCart(1L);

        assertNotNull(result);
        assertTrue(result.getCartItems().isEmpty());
    }

    // --- clearCart ---

    @Test
    void clearCart_success_clearsItemsAndResetsTotal() {
        cart.getCartItems().add(cartItem);
        cart.setTotalPrice(BigDecimal.valueOf(1999.98));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        cartService.clearCart(1L);

        assertTrue(cart.getCartItems().isEmpty());
        assertEquals(BigDecimal.ZERO, cart.getTotalPrice());
        verify(cartRepository).save(cart);
    }

    // --- getAllCarts ---

    @Test
    void getAllCarts_returnsAllCarts() {
        cart.getCartItems().add(cartItem);
        when(cartRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(cart)));
        when(modelMapper.map(cart, CartDetailResponse.class)).thenReturn(new CartDetailResponse());

        CartListResponse response = cartService.getAllCarts(0, 50, "id", "asc");

        assertEquals(1, response.getContent().size());
    }

    @Test
    void getAllCarts_noCarts_returnsEmptyList() {
        when(cartRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        CartListResponse response = cartService.getAllCarts(0, 50, "id", "asc");

        assertTrue(response.getContent().isEmpty());
    }
}
