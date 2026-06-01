-- V24__add_status_to_itineraries.sql
-- Thêm cột status để theo dõi trạng thái lộ trình của người dùng
ALTER TABLE itineraries ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'NOT_STARTED';
