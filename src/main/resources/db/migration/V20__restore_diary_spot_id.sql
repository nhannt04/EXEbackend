-- V20__remove_spot_id_from_diaries.sql
-- Drop the spot_id column from diaries table (no longer needed after removing spot association)
ALTER TABLE diaries DROP COLUMN IF EXISTS spot_id;
