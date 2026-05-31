-- V9__create_dishes_table.sql
-- Tạo bảng riêng lưu trữ Món ăn ngon Hội An (không dùng chung với bảng spots)
CREATE TABLE IF NOT EXISTS dishes (
    id BIGSERIAL PRIMARY KEY,
    dish_name VARCHAR(255) NOT NULL,
    restaurant_name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    min_price INTEGER DEFAULT 0,
    max_price INTEGER DEFAULT 0,
    opening_time TIME DEFAULT '08:00:00',
    closing_time TIME DEFAULT '22:00:00',
    image_url VARCHAR(500)
);
