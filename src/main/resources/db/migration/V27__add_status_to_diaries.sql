-- V27__add_status_to_diaries.sql
-- Thêm cột status để theo dõi trạng thái hiển thị của bài viết nhật ký du ký
ALTER TABLE diaries ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'public';
