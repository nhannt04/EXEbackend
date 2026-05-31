-- V11__create_cafes_table.sql
-- Tạo bảng riêng lưu trữ quán Cà phê đặc sản Hội An
CREATE TABLE IF NOT EXISTS cafes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    style VARCHAR(255),
    address VARCHAR(500),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    min_price INTEGER DEFAULT 0,
    max_price INTEGER DEFAULT 0,
    opening_time TIME DEFAULT '07:00:00',
    closing_time TIME DEFAULT '22:00:00',
    image_url VARCHAR(500)
);
