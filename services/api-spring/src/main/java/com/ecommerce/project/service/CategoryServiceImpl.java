package com.ecommerce.project.service;

import com.ecommerce.project.dto.CategoryCreateRequest;
import com.ecommerce.project.dto.CategoryDetailResponse;
import com.ecommerce.project.dto.CategoryListResponse;
import com.ecommerce.project.dto.CategoryUpdateRequest;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.repository.CategoryRepository;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryListResponse getAllCategories(
            Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);

        List<CategoryDetailResponse> responses = categoryPage.getContent().stream()
                .map(category -> modelMapper.map(category, CategoryDetailResponse.class))
                .toList();

        return new CategoryListResponse(
                responses,
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalElements(),
                categoryPage.getTotalPages(),
                categoryPage.isLast());
    }

    @Override
    public CategoryDetailResponse createCategory(CategoryCreateRequest request) {
        Category category = modelMapper.map(request, Category.class);
        Category existing = categoryRepository.findByName(category.getName());
        if (existing != null) {
            throw new APIException("Category with the name " + category.getName() + " already exists");
        }
        return modelMapper.map(categoryRepository.save(category), CategoryDetailResponse.class);
    }

    @Override
    public CategoryDetailResponse deleteCategory(Long id) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoryRepository.deleteById(id);
        return modelMapper.map(category, CategoryDetailResponse.class);
    }

    @Override
    public CategoryDetailResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setName(request.getName());

        return modelMapper.map(categoryRepository.save(category), CategoryDetailResponse.class);
    }
}
