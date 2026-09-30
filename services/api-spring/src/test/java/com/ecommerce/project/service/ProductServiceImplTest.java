package com.ecommerce.project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ecommerce.project.dto.CategoryDetailResponse;
import com.ecommerce.project.dto.ProductCreateRequest;
import com.ecommerce.project.dto.ProductDetailResponse;
import com.ecommerce.project.dto.ProductListResponse;
import com.ecommerce.project.dto.ProductUpdateRequest;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Product product;
    private ProductDetailResponse productDTO;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        product = new Product();
        product.setId(1L);
        product.setName("Laptop Pro");
        product.setDescription("High performance laptop");
        product.setQuantity(10);
        product.setPrice(BigDecimal.valueOf(999.99));
        product.setDiscount(BigDecimal.valueOf(0.1));
        product.setCategory(category);

        productDTO = new ProductDetailResponse(
                1L,
                "Laptop Pro",
                "High performance laptop",
                10,
                BigDecimal.valueOf(999.99),
                BigDecimal.valueOf(0.1),
                new CategoryDetailResponse(1L, "Electronics"));
    }

    @Test
    void createProduct_success() {
        ProductCreateRequest request = new ProductCreateRequest(
                "Laptop Pro", "High performance laptop", 10, BigDecimal.valueOf(999.99), BigDecimal.valueOf(0.1));
        Product mappedProduct = new Product();
        mappedProduct.setName("Laptop Pro");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByName("Laptop Pro")).thenReturn(null);
        when(modelMapper.map(request, Product.class)).thenReturn(mappedProduct);
        when(productRepository.save(mappedProduct)).thenReturn(product);
        when(modelMapper.map(product, ProductDetailResponse.class)).thenReturn(productDTO);

        ProductDetailResponse result = productService.createProduct(1L, request);

        assertEquals("Laptop Pro", result.getName());
        assertEquals(1L, result.getId());
        verify(productRepository).save(mappedProduct);
    }

    @Test
    void createProduct_categoryNotFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class, () -> productService.createProduct(99L, new ProductCreateRequest()));
    }

    @Test
    void createProduct_duplicateName_throwsAPIException() {
        ProductCreateRequest request =
                new ProductCreateRequest("Laptop Pro", "desc", 0, BigDecimal.ZERO, BigDecimal.ZERO);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByName("Laptop Pro")).thenReturn(product);

        APIException ex = assertThrows(APIException.class, () -> productService.createProduct(1L, request));

        assertTrue(ex.getMessage().contains("Laptop Pro"));
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_success() {
        ProductUpdateRequest request = new ProductUpdateRequest(
                "Laptop Pro X", "Updated desc", 5, BigDecimal.valueOf(1099.99), BigDecimal.valueOf(0.05));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(modelMapper.map(product, ProductDetailResponse.class)).thenReturn(productDTO);

        ProductDetailResponse result = productService.updateProduct(1L, request);

        assertNotNull(result);
        verify(productRepository).save(product);
        assertEquals("Laptop Pro X", product.getName());
        assertEquals("Updated desc", product.getDescription());
        assertEquals(5, product.getQuantity());
        assertEquals(BigDecimal.valueOf(1099.99), product.getPrice());
        assertEquals(BigDecimal.valueOf(0.05), product.getDiscount());
    }

    @Test
    void updateProduct_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class, () -> productService.updateProduct(99L, new ProductUpdateRequest()));

        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(modelMapper.map(product, ProductDetailResponse.class)).thenReturn(productDTO);

        ProductDetailResponse result = productService.deleteProduct(1L);

        assertEquals(1L, result.getId());
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(99L));

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void getAllProducts_returnsPaginatedResponse() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(product, ProductDetailResponse.class)).thenReturn(productDTO);

        ProductListResponse response = productService.getAllProducts(0, 50, "id", "asc");

        assertEquals(1, response.getContent().size());
        assertEquals("Laptop Pro", response.getContent().get(0).getName());
    }

    @Test
    void getProductById_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(modelMapper.map(product, ProductDetailResponse.class)).thenReturn(productDTO);

        ProductDetailResponse result = productService.getProductById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getProductById_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void getProductsByKeyword_returnsMatchingProducts() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        eq("laptop"), eq("laptop"), any(Pageable.class)))
                .thenReturn(page);
        when(modelMapper.map(product, ProductDetailResponse.class)).thenReturn(productDTO);

        ProductListResponse response = productService.getProductsByKeyword("laptop", 0, 50, "id", "asc");

        assertEquals(1, response.getContent().size());
    }

    @Test
    void getProductsByKeyword_noMatches_returnsEmptyResponse() {
        when(productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        eq("xyz"), eq("xyz"), any(Pageable.class)))
                .thenReturn(Page.empty());

        ProductListResponse response = productService.getProductsByKeyword("xyz", 0, 50, "id", "asc");

        assertTrue(response.getContent().isEmpty());
    }

    @Test
    void getProductsByCategory_success() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByCategory(eq(category), any(Pageable.class)))
                .thenReturn(page);
        when(modelMapper.map(product, ProductDetailResponse.class)).thenReturn(productDTO);

        ProductListResponse response = productService.getProductsByCategory(1L, 0, 50, "id", "asc");

        assertEquals(1, response.getContent().size());
    }

    @Test
    void getProductsByCategory_categoryNotFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class, () -> productService.getProductsByCategory(99L, 0, 50, "id", "asc"));
    }
}
