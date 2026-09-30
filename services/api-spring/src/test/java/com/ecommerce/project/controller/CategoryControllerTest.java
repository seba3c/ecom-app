package com.ecommerce.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.project.dto.CategoryCreateRequest;
import com.ecommerce.project.dto.CategoryDetailResponse;
import com.ecommerce.project.dto.CategoryListResponse;
import com.ecommerce.project.dto.CategoryUpdateRequest;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.service.UserDetailsServiceImpl;
import com.ecommerce.project.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCategories_returns200WithCategories() throws Exception {
        CategoryListResponse response = new CategoryListResponse(
                List.of(new CategoryDetailResponse(1L, "Electronics"), new CategoryDetailResponse(2L, "Clothing")),
                0,
                50,
                2L,
                1,
                true);
        when(categoryService.getAllCategories(any(), any(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Electronics"))
                .andExpect(jsonPath("$.content[1].name").value("Clothing"));
    }

    @Test
    void getAllCategories_empty_returns200() throws Exception {
        when(categoryService.getAllCategories(any(), any(), any(), any()))
                .thenReturn(new CategoryListResponse(List.of(), 0, 50, 0L, 0, true));

        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void createCategory_returns201() throws Exception {
        CategoryDetailResponse resultDTO = new CategoryDetailResponse(1L, "Electronics");
        when(categoryService.createCategory(any(CategoryCreateRequest.class))).thenReturn(resultDTO);

        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryCreateRequest("Electronics"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void createCategory_duplicate_returns400() throws Exception {
        when(categoryService.createCategory(any(CategoryCreateRequest.class)))
                .thenThrow(new APIException("Category with the name Electronics already exists"));

        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryCreateRequest("Electronics"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category with the name Electronics already exists"));
    }

    @Test
    void deleteCategory_returns200() throws Exception {
        CategoryDetailResponse dto = new CategoryDetailResponse(1L, "Electronics");
        when(categoryService.deleteCategory(1L)).thenReturn(dto);

        mockMvc.perform(delete("/api/admin/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void deleteCategory_notFound_returns404() throws Exception {
        when(categoryService.deleteCategory(99L)).thenThrow(new ResourceNotFoundException("Category", "id", 99L));

        mockMvc.perform(delete("/api/admin/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category with id: 99 not found"));
    }

    @Test
    void updateCategory_returns200() throws Exception {
        CategoryDetailResponse resultDTO = new CategoryDetailResponse(1L, "Updated");
        when(categoryService.updateCategory(eq(1L), any(CategoryUpdateRequest.class)))
                .thenReturn(resultDTO);

        mockMvc.perform(put("/api/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryUpdateRequest("Updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateCategory_notFound_returns404() throws Exception {
        when(categoryService.updateCategory(eq(99L), any(CategoryUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category", "id", 99L));

        mockMvc.perform(put("/api/admin/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryUpdateRequest("Updated"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category with id: 99 not found"));
    }
}
