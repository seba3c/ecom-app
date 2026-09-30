package com.ecommerce.project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ecommerce.project.dto.OrderDetailResponse;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.repository.*;
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

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;
    private Address address;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "password");
        user.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setName("Laptop Pro");
        product.setQuantity(10);
        product.setPrice(BigDecimal.valueOf(999.99));
        product.setDiscount(BigDecimal.ZERO);

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setPrice(product.getPrice());
        cartItem.setDiscount(product.getDiscount());

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setTotalPrice(BigDecimal.valueOf(1999.98));
        cart.setCartItems(new ArrayList<>(List.of(cartItem)));

        address = new Address();
        address.setId(1L);
    }

    @Test
    void placeOrder_success_createsOrderAndClearsCart() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setUser(user);
        savedOrder.setOrderDate(java.time.LocalDateTime.now());
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setShippingAddress(address);
        savedOrder.setTotalAmount(BigDecimal.valueOf(1999.98));
        OrderItem savedItem = new OrderItem();
        savedItem.setId(1L);
        savedItem.setProduct(product);
        savedItem.setQuantity(2);
        savedItem.setPrice(BigDecimal.valueOf(999.99));
        savedItem.setDiscount(BigDecimal.ZERO);
        savedOrder.setItems(List.of(savedItem));
        Payment savedPayment = new Payment("card", "Stripe", "pi_123", "succeeded", "OK");
        savedPayment.setId(1L);
        savedOrder.setPayment(savedPayment);

        OrderDetailResponse mockResponse = new OrderDetailResponse();
        mockResponse.setId(1L);
        mockResponse.setStatus(OrderStatus.PENDING);
        mockResponse.setTotalAmount(BigDecimal.valueOf(1999.98));

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(modelMapper.map(any(Order.class), eq(OrderDetailResponse.class))).thenReturn(mockResponse);

        OrderDetailResponse result = orderService.placeOrder(1L, 1L, "card", "Stripe", "pi_123", "succeeded", "OK");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(1L, result.getShippingAddressId());
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCart(1L);
        assertEquals(8, product.getQuantity());
    }

    @Test
    void placeOrder_cartNotFound_throwsAPIException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(
                APIException.class,
                () -> orderService.placeOrder(1L, 1L, "card", "Stripe", "pi_123", "succeeded", "OK"));
    }

    @Test
    void placeOrder_emptyCart_throwsAPIException() {
        cart.setCartItems(new ArrayList<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThrows(
                APIException.class,
                () -> orderService.placeOrder(1L, 1L, "card", "Stripe", "pi_123", "succeeded", "OK"));
    }

    @Test
    void placeOrder_addressNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.placeOrder(1L, 99L, "card", "Stripe", "pi_123", "succeeded", "OK"));
    }

    @Test
    void placeOrder_insufficientStock_throwsAPIException() {
        product.setQuantity(1);
        cartItem.setQuantity(5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        assertThrows(
                APIException.class,
                () -> orderService.placeOrder(1L, 1L, "card", "Stripe", "pi_123", "succeeded", "OK"));
    }

    @Test
    void placeOrder_success_decreasesProductStock() {
        int initialStock = product.getQuantity();
        int orderedQty = cartItem.getQuantity();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setUser(user);
        savedOrder.setOrderDate(java.time.LocalDateTime.now());
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setShippingAddress(address);
        savedOrder.setTotalAmount(BigDecimal.valueOf(1999.98));
        OrderItem savedItem = new OrderItem();
        savedItem.setId(1L);
        savedItem.setProduct(product);
        savedItem.setQuantity(orderedQty);
        savedItem.setPrice(BigDecimal.valueOf(999.99));
        savedItem.setDiscount(BigDecimal.ZERO);
        savedOrder.setItems(List.of(savedItem));
        Payment savedPayment = new Payment("card", "Stripe", "pi_123", "succeeded", "OK");
        savedPayment.setId(1L);
        savedOrder.setPayment(savedPayment);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(modelMapper.map(any(Order.class), eq(OrderDetailResponse.class))).thenReturn(new OrderDetailResponse());

        orderService.placeOrder(1L, 1L, "card", "Stripe", "pi_123", "succeeded", "OK");

        assertEquals(initialStock - orderedQty, product.getQuantity());
        verify(productRepository).save(product);
        verify(cartService).clearCart(1L);
    }
}
