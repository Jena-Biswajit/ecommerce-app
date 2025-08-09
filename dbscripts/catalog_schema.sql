-- Create database
CREATE DATABASE IF NOT EXISTS ecommerce_db;
USE ecommerce_db;

-- =========================================================
-- Table: category
-- Hierarchical product categories (e.g., Mobiles → Smartphones)
-- =========================================================
CREATE TABLE category (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    slug VARCHAR(150) UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES category(category_id)
);

-- =========================================================
-- Table: product
-- Core product details
-- =========================================================
CREATE TABLE product (
    product_id CHAR(36) PRIMARY KEY, -- UUID stored as string
    title VARCHAR(255) NOT NULL,
    brand VARCHAR(100),
    model_number VARCHAR(100),
    slug VARCHAR(255) UNIQUE NOT NULL,
    short_description TEXT,
    long_description TEXT,
    price DECIMAL(10,2) NOT NULL,
    discounted_price DECIMAL(10,2),
    discount_percent DECIMAL(5,2),
    currency CHAR(3) DEFAULT 'INR',
    availability_status ENUM('IN_STOCK','OUT_OF_STOCK','PREORDER') DEFAULT 'IN_STOCK',
    stock_quantity INT DEFAULT 0,
    rating DECIMAL(2,1) DEFAULT 0.0,
    release_date DATE,
    category_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(category_id)
);

-- =========================================================
-- Table: product_variant
-- Handles color, RAM, storage combinations
-- =========================================================
CREATE TABLE product_variant (
    variant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id CHAR(36) NOT NULL,
    color VARCHAR(50),
    ram VARCHAR(50),
    storage VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    discounted_price DECIMAL(10,2),
    stock_quantity INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(product_id)
);

-- =========================================================
-- Table: product_image
-- Product & variant images
-- =========================================================
CREATE TABLE product_image (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id CHAR(36) NOT NULL,
    variant_id BIGINT DEFAULT NULL,
    image_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    FOREIGN KEY (variant_id) REFERENCES product_variant(variant_id)
);

-- =========================================================
-- Table: product_tag
-- Tags like AMOLED, 120Hz, etc.
-- =========================================================
CREATE TABLE product_tag (
    tag_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- =========================================================
-- Table: product_tag_map
-- Many-to-many relationship between product and tags
-- =========================================================
CREATE TABLE product_tag_map (
    product_id CHAR(36) NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, tag_id),
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    FOREIGN KEY (tag_id) REFERENCES product_tag(tag_id)
);

-- =========================================================
-- Table: product_audit_log
-- For tracking admin changes
-- =========================================================
CREATE TABLE product_audit_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id CHAR(36) NOT NULL,
    admin_user VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    change_details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(product_id)
);
