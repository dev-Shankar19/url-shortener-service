package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Service", description = "Manage product catalogue and inventory")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a product")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created",
            content = @Content(schema = @Schema(implementation = ProductDto.ProductResponse.class))),
        @ApiResponse(responseCode = "409", description = "Product code already exists")
    })
    public ResponseEntity<ProductDto.ProductResponse> createProduct(
            @Valid @RequestBody ProductDto.ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @GetMapping
    @Operation(summary = "List all active products")
    public ResponseEntity<List<ProductDto.ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllActiveProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductDto.ProductResponse> getProduct(
            @Parameter(description = "Product ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("/code/{productCode}")
    @Operation(summary = "Get product by product code")
    public ResponseEntity<ProductDto.ProductResponse> getProductByCode(
            @Parameter(description = "Product code", example = "PROD-101") @PathVariable String productCode) {
        return ResponseEntity.ok(productService.getProductByCode(productCode));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<List<ProductDto.ProductResponse>> getByCategory(
            @Parameter(description = "Category", example = "Electronics") @PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ResponseEntity<ProductDto.ProductResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @RequestBody ProductDto.ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update product stock",
               description = "Use a positive delta to add stock, negative to reduce. Prevents stock from going below 0.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stock updated"),
        @ApiResponse(responseCode = "400", description = "Insufficient stock")
    })
    public ResponseEntity<ProductDto.ProductResponse> updateStock(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @RequestBody ProductDto.StockUpdateRequest request) {
        return ResponseEntity.ok(productService.updateStock(id, request.getDelta()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a product")
    public ResponseEntity<Void> deactivateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }
}
