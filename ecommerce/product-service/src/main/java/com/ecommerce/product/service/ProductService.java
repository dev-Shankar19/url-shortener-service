package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductDto.ProductResponse createProduct(ProductDto.ProductRequest request) {
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new IllegalArgumentException("Product code already exists: " + request.getProductCode());
        }
        Product product = Product.builder()
            .productCode(request.getProductCode())
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .stockQuantity(request.getStockQuantity())
            .category(request.getCategory())
            .build();
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductDto.ProductResponse getProduct(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public ProductDto.ProductResponse getProductByCode(String code) {
        return toResponse(productRepository.findByProductCode(code)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + code)));
    }

    @Transactional(readOnly = true)
    public List<ProductDto.ProductResponse> getAllActiveProducts() {
        return productRepository.findByActiveTrue().stream()
            .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto.ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
            .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ProductDto.ProductResponse updateProduct(Long id, ProductDto.ProductRequest request) {
        Product product = findById(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductDto.ProductResponse updateStock(Long id, int delta) {
        Product product = findById(id);
        int newStock = product.getStockQuantity() + delta;
        if (newStock < 0) {
            throw new IllegalStateException("Insufficient stock. Available: " + product.getStockQuantity());
        }
        product.setStockQuantity(newStock);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deactivateProduct(Long id) {
        Product product = findById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    private ProductDto.ProductResponse toResponse(Product p) {
        return ProductDto.ProductResponse.builder()
            .id(p.getId()).productCode(p.getProductCode()).name(p.getName())
            .description(p.getDescription()).price(p.getPrice())
            .stockQuantity(p.getStockQuantity()).category(p.getCategory())
            .active(p.isActive()).createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
            .build();
    }
}
