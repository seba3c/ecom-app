package com.ecommerce.project.service;

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
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ProductDetailResponse createProduct(Long categoryId, ProductCreateRequest request) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        if (productRepository.findByName(request.getName()) != null) {
            throw new APIException("Product with the name " + request.getName() + " already exists");
        }

        Product product = modelMapper.map(request, Product.class);
        product.setCategory(category);
        return modelMapper.map(productRepository.save(product), ProductDetailResponse.class);
    }

    @Override
    public ProductDetailResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product existing = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setQuantity(request.getQuantity());
        existing.setPrice(request.getPrice());
        existing.setDiscount(request.getDiscount());

        return modelMapper.map(productRepository.save(existing), ProductDetailResponse.class);
    }

    @Override
    public ProductDetailResponse deleteProduct(Long productId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        productRepository.deleteById(productId);
        return modelMapper.map(product, ProductDetailResponse.class);
    }

    @Override
    public ProductListResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        return toProductListResponse(productRepository.findAll(toPageable(pageNumber, pageSize, sortBy, sortOrder)));
    }

    @Override
    public ProductDetailResponse getProductById(Long productId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return modelMapper.map(product, ProductDetailResponse.class);
    }

    @Override
    public ProductListResponse getProductsByKeyword(
            String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Page<Product> page = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword, toPageable(pageNumber, pageSize, sortBy, sortOrder));
        return toProductListResponse(page);
    }

    @Override
    public ProductListResponse getProductsByCategory(
            Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        Page<Product> page =
                productRepository.findByCategory(category, toPageable(pageNumber, pageSize, sortBy, sortOrder));
        return toProductListResponse(page);
    }

    private Pageable toPageable(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    private ProductListResponse toProductListResponse(Page<Product> page) {
        List<ProductDetailResponse> dtos = page.getContent().stream()
                .map(p -> modelMapper.map(p, ProductDetailResponse.class))
                .toList();
        return new ProductListResponse(
                dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}
