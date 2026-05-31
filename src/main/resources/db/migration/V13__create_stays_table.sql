-- V13__create_stays_table.sql
-- Tạo bảng riêng lưu trữ Chỗ ở đặc sản Hội An (Hotels, Villas, Homestays)
CREATE TABLE IF NOT EXISTS stays (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    capacity VARCHAR(255),
    min_price INTEGER DEFAULT 0,
    max_price INTEGER DEFAULT 0,
    notes VARCHAR(1000),
    image_url VARCHAR(500)
);
