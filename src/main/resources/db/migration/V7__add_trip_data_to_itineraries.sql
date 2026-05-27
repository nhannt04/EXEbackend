-- V7__add_trip_data_to_itineraries.sql
-- Bổ sung các cột lưu trữ dữ liệu lịch trình AI tối ưu vào bảng itineraries

ALTER TABLE itineraries ADD COLUMN IF NOT EXISTS title VARCHAR(255) DEFAULT 'Lịch trình Hội An';
ALTER TABLE itineraries ADD COLUMN IF NOT EXISTS trip_data TEXT;
