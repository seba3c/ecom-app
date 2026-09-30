package com.ecommerce.project.service;

import com.ecommerce.project.dto.CategoryCreateRequest;
import com.ecommerce.project.dto.CategoryDetailResponse;
import com.ecommerce.project.dto.CategoryListResponse;
import com.ecommerce.project.dto.CategoryUpdateRequest;

public interface CategoryService {

    CategoryListResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    CategoryDetailResponse createCategory(CategoryCreateRequest request);

    CategoryDetailResponse deleteCategory(Long id);

    CategoryDetailResponse updateCategory(Long id, CategoryUpdateRequest request);
}
