-- V15__create_entertainments_table.sql
-- Tạo bảng riêng lưu trữ Khu vui chơi Hội An (Biển, Vui chơi, Workshop)
CREATE TABLE IF NOT EXISTS entertainments (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    interests VARCHAR(500),
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    min_price INTEGER DEFAULT 0,
    max_price INTEGER DEFAULT 0,
    image_url VARCHAR(500),
    overnight BOOLEAN NOT NULL DEFAULT FALSE
);
