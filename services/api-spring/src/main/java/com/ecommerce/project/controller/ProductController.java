package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConfig;
import com.ecommerce.project.config.SwaggerConfig;
import com.ecommerce.project.dto.ProductCreateRequest;
import com.ecommerce.project.dto.ProductDetailResponse;
import com.ecommerce.project.dto.ProductListResponse;
import com.ecommerce.project.dto.ProductUpdateRequest;
import com.ecommerce.project.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = SwaggerConfig.Tags.Product.NAME, description = SwaggerConfig.Tags.Product.DESCRIPTION)
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/admin/categories/{categoryId}/products")
    public ResponseEntity<ProductDetailResponse> createProduct(
            @PathVariable Long categoryId, @Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(categoryId, request));
    }

    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDetailResponse> updateProduct(
            @PathVariable Long productId, @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.updateProduct(productId, request));
    }

    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDetailResponse> deleteProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.deleteProduct(productId));
    }

    @GetMapping("/public/products")
    public ResponseEntity<ProductListResponse> getAllProducts(
            @RequestParam(defaultValue = AppConfig.Pagination.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConfig.Pagination.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(defaultValue = AppConfig.Pagination.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConfig.Pagination.SORT_PRODUCTS_DIR, required = false) String sortOrder) {
        return ResponseEntity.ok(productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/public/products/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductListResponse> getProductsByKeyword(
            @PathVariable String keyword,
            @RequestParam(defaultValue = AppConfig.Pagination.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConfig.Pagination.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(defaultValue = AppConfig.Pagination.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConfig.Pagination.SORT_PRODUCTS_DIR, required = false) String sortOrder) {
        return ResponseEntity.ok(productService.getProductsByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductListResponse> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = AppConfig.Pagination.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConfig.Pagination.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(defaultValue = AppConfig.Pagination.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConfig.Pagination.SORT_PRODUCTS_DIR, required = false) String sortOrder) {
        return ResponseEntity.ok(
                productService.getProductsByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder));
    }
}
