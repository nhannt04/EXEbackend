-- V6__multiple_images.sql
-- Cấu hình cấu trúc bảng tối ưu hóa lưu trữ nhiều ảnh cho Địa điểm (spots) và Bài viết (diaries)

-- 1. Tạo bảng spot_images lưu trữ nhiều ảnh cho Địa điểm
CREATE TABLE IF NOT EXISTS spot_images (
    id BIGSERIAL PRIMARY KEY,
    spot_id BIGINT NOT NULL REFERENCES spots(id) ON DELETE CASCADE,
    image_cf_id VARCHAR(255),
    image_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo chỉ mục (Index) tăng tốc độ truy vấn tìm kiếm ảnh theo địa điểm
CREATE INDEX IF NOT EXISTS idx_spot_images_spot_id ON spot_images(spot_id);

-- Di chuyển dữ liệu ảnh hiện có của địa điểm sang bảng mới
INSERT INTO spot_images (spot_id, image_cf_id, image_url)
SELECT id, image_cf_id, image_url FROM spots WHERE image_url IS NOT NULL AND image_url <> '';

-- 2. Tạo bảng diary_images lưu trữ nhiều ảnh cho Bài viết nhật ký
CREATE TABLE IF NOT EXISTS diary_images (
    id BIGSERIAL PRIMARY KEY,
    diary_id BIGINT NOT NULL REFERENCES diaries(id) ON DELETE CASCADE,
    image_cf_id VARCHAR(255),
    image_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo chỉ mục (Index) tăng tốc độ truy vấn tìm kiếm ảnh theo bài viết
CREATE INDEX IF NOT EXISTS idx_diary_images_diary_id ON diary_images(diary_id);

-- Di chuyển dữ liệu ảnh hiện có của bài viết sang bảng mới
INSERT INTO diary_images (diary_id, image_cf_id, image_url)
SELECT id, image_cf_id, image_url FROM diaries WHERE image_url IS NOT NULL AND image_url <> '';

-- 3. Xoá bỏ các cột đơn ảnh cũ đã lỗi thời trên bảng gốc để tối ưu hóa DB
ALTER TABLE spots DROP COLUMN IF EXISTS image_cf_id;
ALTER TABLE spots DROP COLUMN IF EXISTS image_url;

ALTER TABLE diaries DROP COLUMN IF EXISTS image_cf_id;
ALTER TABLE diaries DROP COLUMN IF EXISTS image_url;
