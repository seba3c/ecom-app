package com.ecommerce.project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ecommerce.project.dto.CategoryCreateRequest;
import com.ecommerce.project.dto.CategoryDetailResponse;
import com.ecommerce.project.dto.CategoryListResponse;
import com.ecommerce.project.dto.CategoryUpdateRequest;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void getAllCategories_returnsCategoryResponse() {
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("Electronics");
        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("Clothing");

        CategoryDetailResponse dto1 = new CategoryDetailResponse(1L, "Electronics");
        CategoryDetailResponse dto2 = new CategoryDetailResponse(2L, "Clothing");

        Page<Category> page = new PageImpl<>(List.of(cat1, cat2));
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(cat1, CategoryDetailResponse.class)).thenReturn(dto1);
        when(modelMapper.map(cat2, CategoryDetailResponse.class)).thenReturn(dto2);

        CategoryListResponse response = categoryService.getAllCategories(0, 50, "id", "asc");

        assertEquals(2, response.getContent().size());
        assertEquals("Electronics", response.getContent().get(0).getName());
        assertEquals("Clothing", response.getContent().get(1).getName());
    }

    @Test
    void getAllCategories_emptyList_returnsEmptyContent() {
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        CategoryListResponse response = categoryService.getAllCategories(0, 50, "id", "asc");

        assertTrue(response.getContent().isEmpty());
    }

    @Test
    void createCategory_success() {
        CategoryCreateRequest request = new CategoryCreateRequest("Electronics");
        Category mappedCategory = new Category();
        mappedCategory.setName("Electronics");
        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Electronics");
        CategoryDetailResponse resultDTO = new CategoryDetailResponse(1L, "Electronics");

        when(modelMapper.map(request, Category.class)).thenReturn(mappedCategory);
        when(categoryRepository.findByName("Electronics")).thenReturn(null);
        when(categoryRepository.save(mappedCategory)).thenReturn(savedCategory);
        when(modelMapper.map(savedCategory, CategoryDetailResponse.class)).thenReturn(resultDTO);

        CategoryDetailResponse result = categoryService.createCategory(request);

        assertEquals(1L, result.getId());
        assertEquals("Electronics", result.getName());
        verify(categoryRepository).save(mappedCategory);
    }

    @Test
    void createCategory_duplicateName_throwsAPIException() {
        CategoryCreateRequest request = new CategoryCreateRequest("Electronics");
        Category mappedCategory = new Category();
        mappedCategory.setName("Electronics");
        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Electronics");

        when(modelMapper.map(request, Category.class)).thenReturn(mappedCategory);
        when(categoryRepository.findByName("Electronics")).thenReturn(existingCategory);

        APIException ex = assertThrows(APIException.class, () -> categoryService.createCategory(request));

        assertTrue(ex.getMessage().contains("Electronics"));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_success() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        CategoryDetailResponse dto = new CategoryDetailResponse(1L, "Electronics");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(modelMapper.map(category, CategoryDetailResponse.class)).thenReturn(dto);

        CategoryDetailResponse result = categoryService.deleteCategory(1L);

        assertEquals(1L, result.getId());
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_notFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(99L));

        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void updateCategory_success() {
        CategoryUpdateRequest request = new CategoryUpdateRequest("Updated Name");
        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Old Name");
        CategoryDetailResponse resultDTO = new CategoryDetailResponse(1L, "Updated Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);
        when(modelMapper.map(existingCategory, CategoryDetailResponse.class)).thenReturn(resultDTO);

        CategoryDetailResponse result = categoryService.updateCategory(1L, request);

        assertEquals(1L, result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Name", existingCategory.getName());
        verify(categoryRepository).save(existingCategory);
    }

    @Test
    void updateCategory_notFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.updateCategory(99L, new CategoryUpdateRequest("Name")));

        verify(categoryRepository, never()).save(any());
    }
}
