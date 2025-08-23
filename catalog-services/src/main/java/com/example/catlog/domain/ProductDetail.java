package com.example.catlog.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ProductDetail {
    private String productId;
    private String groupId;
    private String title;
    private String brand;
    private String modelNumber;
    private String slug;
    private String shortDescription;
    private String longDescription;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private BigDecimal discountPercent;
    private String currency;
    private String availabilityStatus;
    private Integer stockQuantity;
    private BigDecimal rating;
    private LocalDate releaseDate;
    private Long categoryId;
    private String color;
    private String size;

    private List<ProductMedia> media;
    private List<AttributeValue> attributes;

    // getters & setters (generate all)
    // -- keep standard methods, no Lombok to avoid extra deps
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModelNumber() { return modelNumber; }
    public void setModelNumber(String modelNumber) { this.modelNumber = modelNumber; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public String getLongDescription() { return longDescription; }
    public void setLongDescription(String longDescription) { this.longDescription = longDescription; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(BigDecimal discountedPrice) { this.discountedPrice = discountedPrice; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public List<ProductMedia> getMedia() { return media; }
    public void setMedia(List<ProductMedia> media) { this.media = media; }
    public List<AttributeValue> getAttributes() { return attributes; }
    public void setAttributes(List<AttributeValue> attributes) { this.attributes = attributes; }
}
