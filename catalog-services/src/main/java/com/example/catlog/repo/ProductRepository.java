package com.example.catlog.repo;

import com.example.catlog.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbc;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    public List<ProductSummary> findAllSummaries() {
        final String sql =
                "SELECT product_id, title, brand, slug, price, currency, stock_quantity " +
                        "FROM product ORDER BY created_at DESC";
        return jdbc.query(sql, new RowMapper<ProductSummary>() {
            public ProductSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
                ProductSummary p = new ProductSummary();
                p.setProductId(rs.getString("product_id"));
                p.setTitle(rs.getString("title"));
                p.setBrand(rs.getString("brand"));
                p.setSlug(rs.getString("slug"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setCurrency(rs.getString("currency"));
                p.setStockQuantity((Integer) rs.getObject("stock_quantity"));
                return p;
            }
        });
    }

    public ProductDetail findBySlug(String slug) {
        final String sql =
                "SELECT * FROM product WHERE slug = ?";
        List<ProductDetail> list = jdbc.query(sql, new Object[]{slug}, new RowMapper<ProductDetail>() {
            public ProductDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
                ProductDetail d = new ProductDetail();
                d.setProductId(rs.getString("product_id"));
                d.setGroupId(rs.getString("group_id"));
                d.setTitle(rs.getString("title"));
                d.setBrand(rs.getString("brand"));
                d.setModelNumber(rs.getString("model_number"));
                d.setSlug(rs.getString("slug"));
                d.setShortDescription(rs.getString("short_description"));
                d.setLongDescription(rs.getString("long_description"));
                d.setPrice(rs.getBigDecimal("price"));
                d.setDiscountedPrice(rs.getBigDecimal("discounted_price"));
                d.setDiscountPercent(rs.getBigDecimal("discount_percent"));
                d.setCurrency(rs.getString("currency"));
                d.setAvailabilityStatus(rs.getString("availability_status"));
                d.setStockQuantity((Integer) rs.getObject("stock_quantity"));
                d.setRating(rs.getBigDecimal("rating"));
                if (rs.getDate("release_date") != null) {
                    d.setReleaseDate(rs.getDate("release_date").toLocalDate());
                }
                d.setCategoryId((Long) rs.getObject("category_id"));
                d.setColor(rs.getString("color"));
                d.setSize(rs.getString("size"));
                return d;
            }
        });
        return list.isEmpty() ? null : list.get(0);
    }

    public List<ProductMedia> findMediaByProductId(String productId) {
        final String sql =
                "SELECT media_id, media_url, thumbnail_url, sort_order " +
                        "FROM product_media WHERE product_id = ? ORDER BY sort_order";
        return jdbc.query(sql, new Object[]{productId}, new RowMapper<ProductMedia>() {
            public ProductMedia mapRow(ResultSet rs, int rowNum) throws SQLException {
                ProductMedia m = new ProductMedia();
                m.setMediaId((Long) rs.getObject("media_id"));
                m.setMediaUrl(rs.getString("media_url"));
                m.setThumbnailUrl(rs.getString("thumbnail_url"));
                m.setSortOrder((Integer) rs.getObject("sort_order"));
                return m;
            }
        });
    }

    public List<AttributeValue> findAttributesByProductId(String productId) {
        final String sql =
                "SELECT pav.attribute_id, pa.name, pa.value_type, pav.value_string, pav.value_number " +
                        "FROM product_attribute_value pav " +
                        "JOIN product_attribute pa ON pa.attribute_id = pav.attribute_id " +
                        "WHERE pav.product_id = ? ORDER BY pav.attribute_id";
        return jdbc.query(sql, new Object[]{productId}, new RowMapper<AttributeValue>() {
            public AttributeValue mapRow(ResultSet rs, int rowNum) throws SQLException {
                AttributeValue av = new AttributeValue();
                av.setAttributeId((Long) rs.getObject("attribute_id"));
                av.setName(rs.getString("name"));
                av.setValueType(rs.getString("value_type"));
                av.setValueString(rs.getString("value_string"));
                av.setValueNumber(rs.getBigDecimal("value_number"));
                return av;
            }
        });
    }
}
