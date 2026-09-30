package com.ecommerce.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.project.dto.CategoryDetailResponse;
import com.ecommerce.project.dto.ProductCreateRequest;
import com.ecommerce.project.dto.ProductDetailResponse;
import com.ecommerce.project.dto.ProductListResponse;
import com.ecommerce.project.dto.ProductUpdateRequest;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.service.UserDetailsServiceImpl;
import com.ecommerce.project.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDetailResponse sampleProduct() {
        return new ProductDetailResponse(
                1L,
                "Laptop Pro",
                "High performance laptop",
                10,
                BigDecimal.valueOf(999.99),
                BigDecimal.valueOf(0.1),
                new CategoryDetailResponse(1L, "Electronics"));
    }

    private ProductListResponse singlePageResponse() {
        return new ProductListResponse(List.of(sampleProduct()), 0, 50, 1L, 1, true);
    }

    @Test
    void createProduct_returns201() throws Exception {
        when(productService.createProduct(eq(1L), any(ProductCreateRequest.class)))
                .thenReturn(sampleProduct());

        mockMvc.perform(post("/api/admin/categories/1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductCreateRequest(
                                "Laptop Pro",
                                "High performance laptop",
                                10,
                                BigDecimal.valueOf(999.99),
                                BigDecimal.valueOf(0.1)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop Pro"));
    }

    @Test
    void createProduct_categoryNotFound_returns404() throws Exception {
        when(productService.createProduct(eq(99L), any(ProductCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category", "id", 99L));

        mockMvc.perform(post("/api/admin/categories/99/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductCreateRequest("Laptop Pro", "desc", 0, BigDecimal.ZERO, BigDecimal.ZERO))))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_duplicateName_returns400() throws Exception {
        when(productService.createProduct(eq(1L), any(ProductCreateRequest.class)))
                .thenThrow(new APIException("Product with the name Laptop Pro already exists"));

        mockMvc.perform(post("/api/admin/categories/1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductCreateRequest("Laptop Pro", "desc", 0, BigDecimal.ZERO, BigDecimal.ZERO))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Product with the name Laptop Pro already exists"));
    }

    @Test
    void updateProduct_returns200() throws Exception {
        when(productService.updateProduct(eq(1L), any(ProductUpdateRequest.class)))
                .thenReturn(sampleProduct());

        mockMvc.perform(put("/api/admin/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductUpdateRequest(
                                "Laptop Pro", "desc", 10, BigDecimal.valueOf(999.99), BigDecimal.valueOf(0.1)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop Pro"));
    }

    @Test
    void updateProduct_notFound_returns404() throws Exception {
        when(productService.updateProduct(eq(99L), any(ProductUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product", "id", 99L));

        mockMvc.perform(put("/api/admin/products/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductUpdateRequest(
                                "Laptop Pro", "desc", 10, BigDecimal.valueOf(999.99), BigDecimal.valueOf(0.1)))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_returns200() throws Exception {
        when(productService.deleteProduct(1L)).thenReturn(sampleProduct());

        mockMvc.perform(delete("/api/admin/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop Pro"));
    }

    @Test
    void deleteProduct_notFound_returns404() throws Exception {
        when(productService.deleteProduct(99L)).thenThrow(new ResourceNotFoundException("Product", "id", 99L));

        mockMvc.perform(delete("/api/admin/products/99")).andExpect(status().isNotFound());
    }

    @Test
    void getAllProducts_returns200() throws Exception {
        when(productService.getAllProducts(any(), any(), any(), any())).thenReturn(singlePageResponse());

        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Laptop Pro"));
    }

    @Test
    void getProductById_returns200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleProduct());

        mockMvc.perform(get("/api/public/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop Pro"));
    }

    @Test
    void getProductById_notFound_returns404() throws Exception {
        when(productService.getProductById(99L)).thenThrow(new ResourceNotFoundException("Product", "id", 99L));

        mockMvc.perform(get("/api/public/products/99")).andExpect(status().isNotFound());
    }

    @Test
    void getProductsByKeyword_returns200() throws Exception {
        when(productService.getProductsByKeyword(eq("laptop"), any(), any(), any(), any()))
                .thenReturn(singlePageResponse());

        mockMvc.perform(get("/api/public/products/keyword/laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getProductsByCategory_returns200() throws Exception {
        when(productService.getProductsByCategory(eq(1L), any(), any(), any(), any()))
                .thenReturn(singlePageResponse());

        mockMvc.perform(get("/api/public/categories/1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getProductsByCategory_categoryNotFound_returns404() throws Exception {
        when(productService.getProductsByCategory(eq(99L), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Category", "id", 99L));

        mockMvc.perform(get("/api/public/categories/99/products")).andExpect(status().isNotFound());
    }
}
