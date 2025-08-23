INSERT INTO category (name, slug) VALUES
('Mobiles', 'mobiles');

INSERT INTO product_group (group_id, group_name, description) VALUES
('11111111-1111-1111-1111-111111111111', 'Pixel 8a', 'Google Pixel 8a series');

INSERT INTO product (
    product_id, group_id, title, brand, model_number, slug,
    short_description, price, discounted_price, discount_percent,
    currency, availability_status, stock_quantity, rating, release_date,
    category_id, color, size
) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 '11111111-1111-1111-1111-111111111111',
 'Pixel 8a (Charcoal, 128GB)', 'Google', 'G0-PX8A', 'pixel-8a-charcoal-128',
 '128GB variant in Charcoal', 45999.00, 43999.00, 4.35,
 'INR', 'IN_STOCK', 25, 4.4, '2024-05-15', 1, 'Charcoal', '128GB'),

('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 '11111111-1111-1111-1111-111111111111',
 'Pixel 8a (Porcelain, 256GB)', 'Google', 'G0-PX8A', 'pixel-8a-porcelain-256',
 '256GB variant in Porcelain', 50999.00, 48999.00, 3.92,
 'INR', 'IN_STOCK', 12, 4.5, '2024-05-15', 1, 'Porcelain', '256GB');

INSERT INTO product_attribute (name, value_type) VALUES
('RAM', 'NUMBER'),
('Display Size', 'NUMBER'),
('Panel', 'STRING');

INSERT INTO product_attribute_value (product_id, attribute_id, value_number) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 1, 8.0),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 1, 12.0),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 2, 6.10),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 2, 6.10);

INSERT INTO product_attribute_value (product_id, attribute_id, value_string) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 3, 'AMOLED'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 3, 'AMOLED');

INSERT INTO product_media (product_id, media_url, thumbnail_url, sort_order) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'https://cdn.example.com/pixel8a/charcoal-1.jpg', 'https://cdn.example.com/pixel8a/charcoal-1-thumb.jpg', 1),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'https://cdn.example.com/pixel8a/porcelain-1.jpg', 'https://cdn.example.com/pixel8a/porcelain-1-thumb.jpg', 1);
