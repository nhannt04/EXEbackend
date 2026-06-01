-- V25__add_dislikes_count_and_itinerary_id_to_diaries.sql
-- Thêm cột dislikes_count và itinerary_id để lưu thông tin tương tác Không thích và liên kết Lịch trình
ALTER TABLE diaries ADD COLUMN IF NOT EXISTS dislikes_count INTEGER DEFAULT 0;
ALTER TABLE diaries ADD COLUMN IF NOT EXISTS itinerary_id BIGINT;
