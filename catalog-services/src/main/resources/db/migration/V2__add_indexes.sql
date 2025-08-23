-- Speeds up typical catalog filters
CREATE INDEX idx_product_group ON product(group_id);
CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_product_color ON product(color);
CREATE INDEX idx_product_size ON product(size);

-- Helpful for lookups by slug / brand
CREATE INDEX idx_product_brand ON product(brand);

-- Attribute filtering (range + equality)
CREATE INDEX idx_attr_val_attr_num ON product_attribute_value(attribute_id, value_number);
CREATE INDEX idx_attr_val_attr_str ON product_attribute_value(attribute_id, value_string(100));

-- Optional: text search accelerators (MySQL InnoDB FT works on CHARSET utf8mb4)
-- CREATE FULLTEXT INDEX ft_title_desc ON product(title, short_description, long_description);
