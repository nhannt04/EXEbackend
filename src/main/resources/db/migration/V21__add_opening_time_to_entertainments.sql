-- V21__add_opening_time_to_entertainments.sql
-- Thêm cột thời gian mở/đóng cửa cho bảng entertainments
ALTER TABLE entertainments
    ADD COLUMN IF NOT EXISTS opening_time TIME;

ALTER TABLE entertainments
    ADD COLUMN IF NOT EXISTS closing_time TIME;

-- Gán giá trị mặc định hợp lý theo loại khu vui chơi
UPDATE entertainments
SET opening_time = '06:00', closing_time = '18:00'
WHERE type = 'Biển';

UPDATE entertainments
SET opening_time = '08:00', closing_time = '21:00'
WHERE type = 'Vui chơi';

UPDATE entertainments
SET opening_time = '08:00', closing_time = '17:00'
WHERE type = 'Workshop';

