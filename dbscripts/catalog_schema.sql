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
-- Table: product_group
-- Groups similar products (e.g., same model, different color/size)
-- =========================================================
CREATE TABLE product_group (
    group_id CHAR(36) PRIMARY KEY, -- UUID
    group_name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- Table: product
-- Everything is a product; grouped via group_id; filterable by attributes
-- =========================================================
CREATE TABLE product (
    product_id CHAR(36) PRIMARY KEY, -- UUID stored as string
    group_id CHAR(36), -- FK to product_group
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
    color VARCHAR(50),  -- Attribute filtering
    size VARCHAR(50),   -- Could be 'M', '9', '256GB', etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(category_id),
    FOREIGN KEY (group_id) REFERENCES product_group(group_id)
);

-- =========================================================
-- Table: product_media
-- Product media
-- =========================================================
CREATE TABLE product_media (
    media_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id CHAR(36) NOT NULL,
    media_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE product_attribute (
     attribute_id BIGINT AUTO_INCREMENT PRIMARY KEY,
     name VARCHAR(50) NOT NULL,          -- e.g., 'RAM', 'Display Size', 'Feature'
     value_type ENUM('STRING','NUMBER') NOT NULL DEFAULT 'STRING' -- type of value
 );

 CREATE TABLE product_attribute_value (
     product_id CHAR(36) NOT NULL,
     attribute_id BIGINT NOT NULL,
     value_string VARCHAR(255) DEFAULT NULL,  -- for 'AMOLED', 'Blue'
     value_number DECIMAL(10,2) DEFAULT NULL, -- for RAM=8, size=6.5
     PRIMARY KEY (product_id, attribute_id),
     FOREIGN KEY (product_id) REFERENCES product(product_id),
     FOREIGN KEY (attribute_id) REFERENCES product_attribute(attribute_id)
 );

