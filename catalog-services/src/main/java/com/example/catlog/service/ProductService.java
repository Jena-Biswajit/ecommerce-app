package com.example.catlog.service;

import com.example.catlog.domain.AttributeValue;
import com.example.catlog.domain.ProductDetail;
import com.example.catlog.domain.ProductMedia;
import com.example.catlog.domain.ProductSummary;
import com.example.catlog.repo.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<ProductSummary> listProducts() {
        return repo.findAllSummaries();
    }

    public ProductDetail getProductBySlug(String slug) {
        ProductDetail d = repo.findBySlug(slug);
        if (d == null) return null;
        List<ProductMedia> media = repo.findMediaByProductId(d.getProductId());
        List<AttributeValue> attrs = repo.findAttributesByProductId(d.getProductId());
        d.setMedia(media);
        d.setAttributes(attrs);
        return d;
    }
}

