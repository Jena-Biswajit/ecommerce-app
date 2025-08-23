package com.example.catlog.controller;

import com.example.catlog.domain.ProductDetail;
import com.example.catlog.domain.ProductSummary;
import com.example.catlog.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService svc;

    public ProductController(ProductService productService) {
        this.svc = productService;
    }

    @GetMapping
    public List<ProductSummary> list() {
        return svc.listProducts();
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductDetail> bySlug(@PathVariable String slug) {
        ProductDetail d = svc.getProductBySlug(slug);
        if (d == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(d);
    }
}
