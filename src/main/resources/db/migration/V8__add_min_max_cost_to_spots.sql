-- V8__add_min_max_cost_to_spots.sql
-- Thêm cột min_cost và max_cost vào bảng spots và khởi tạo giá trị từ average_cost

ALTER TABLE spots ADD COLUMN IF NOT EXISTS min_cost INTEGER DEFAULT 0;
ALTER TABLE spots ADD COLUMN IF NOT EXISTS max_cost INTEGER DEFAULT 0;

-- Khởi tạo giá trị ban đầu cho các bản ghi cũ
UPDATE spots SET min_cost = ROUND(average_cost * 0.8 / 1000) * 1000, max_cost = ROUND(average_cost * 1.2 / 1000) * 1000 WHERE average_cost > 0;
UPDATE spots SET min_cost = 0, max_cost = 0 WHERE average_cost = 0;
