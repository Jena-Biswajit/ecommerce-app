package com.example.catlog.domain;

import java.math.BigDecimal;

public class ProductSummary {
    private String productId;
    private String title;
    private String brand;
    private String slug;
    private BigDecimal price;
    private String currency;
    private Integer stockQuantity;

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}
