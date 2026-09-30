package com.ecommerce.project.service;

import com.ecommerce.project.dto.CartDetailResponse;
import com.ecommerce.project.dto.CartItemDetail;
import com.ecommerce.project.dto.CartListResponse;
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
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDetailResponse addProductToCart(Long userId, Long productId, Integer quantity) {

        Cart cart = fetchOrCreateCart(userId);

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getQuantity() < quantity) {
            throw new APIException("Insufficient stock for product: " + product.getName());
        }

        if (cartItemRepository.findByCartAndProduct(cart, product).isPresent()) {
            throw new APIException("Product already in cart. Use PUT to update quantity.");
        }

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPrice(product.getPrice());
        item.setDiscount(product.getDiscount());
        cart.getCartItems().add(item);

        recalculateTotalPrice(cart);
        return toCartDetailResponse(cartRepository.save(cart));
    }

    @Override
    public CartDetailResponse updateProductQuantity(Long userId, Long productId, Integer quantity) {

        Cart cart = fetchCart(userId);

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        CartItem item = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElseThrow(() -> new APIException("Product not found in cart"));

        if (product.getQuantity() < quantity) {
            throw new APIException("Insufficient stock for product: " + product.getName());
        }
        item.setQuantity(quantity);

        recalculateTotalPrice(cart);
        return toCartDetailResponse(cartRepository.save(cart));
    }

    @Override
    public CartDetailResponse removeProductFromCart(Long userId, Long productId) {

        Cart cart = fetchCart(userId);

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        CartItem item = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElseThrow(() -> new APIException("Product not found in cart"));

        cart.getCartItems().remove(item);
        item.setCart(null);
        recalculateTotalPrice(cart);
        return toCartDetailResponse(cartRepository.save(cart));
    }

    @Override
    public CartDetailResponse getCart(Long userId) {
        Cart cart = fetchOrCreateCart(userId);
        return toCartDetailResponse(cart);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = fetchCart(userId);
        new ArrayList<>(cart.getCartItems()).forEach(ci -> {
            cart.getCartItems().remove(ci);
            ci.setCart(null);
        });
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    @Override
    public CartListResponse getAllCarts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        return toCartListResponse(cartRepository.findAll(toPageable(pageNumber, pageSize, sortBy, sortOrder)));
    }

    private Pageable toPageable(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    private CartListResponse toCartListResponse(Page<Cart> page) {
        List<CartDetailResponse> carts =
                page.getContent().stream().map(this::toCartDetailResponse).toList();
        return new CartListResponse(
                carts, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    private void recalculateTotalPrice(Cart cart) {
        BigDecimal total = cart.getCartItems().stream()
                .map(i -> i.getPrice().subtract(i.getDiscount()).multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
    }

    private CartDetailResponse toCartDetailResponse(Cart cart) {
        CartDetailResponse response = modelMapper.map(cart, CartDetailResponse.class);
        response.setCartItems(cart.getCartItems().stream()
                .map(item -> modelMapper.map(item, CartItemDetail.class))
                .toList());
        return response;
    }

    private Cart fetchOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository
                    .findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    private Cart fetchCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseThrow(() -> new APIException("Cart not found"));
    }
}
