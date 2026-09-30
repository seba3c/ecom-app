package com.ecommerce.project.service;

import com.ecommerce.project.dto.ProductCreateRequest;
import com.ecommerce.project.dto.ProductDetailResponse;
import com.ecommerce.project.dto.ProductListResponse;
import com.ecommerce.project.dto.ProductUpdateRequest;

public interface ProductService {

    ProductDetailResponse createProduct(Long categoryId, ProductCreateRequest request);

    ProductDetailResponse updateProduct(Long productId, ProductUpdateRequest request);

    ProductDetailResponse deleteProduct(Long productId);

    ProductListResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductDetailResponse getProductById(Long productId);

    ProductListResponse getProductsByKeyword(
            String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductListResponse getProductsByCategory(
            Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}
