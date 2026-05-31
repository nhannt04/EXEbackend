-- V17__create_rentals_table.sql
-- Tạo bảng riêng lưu trữ Dịch vụ cho thuê Hội An (Thuê máy ảnh, Thuê đồ, Thuê xe, Photobooth)
CREATE TABLE IF NOT EXISTS rentals (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    min_price INTEGER DEFAULT 0,
    max_price INTEGER DEFAULT 0,
    opening_time TIME DEFAULT '08:00:00',
    closing_time TIME DEFAULT '21:00:00',
    image_url VARCHAR(500)
);
