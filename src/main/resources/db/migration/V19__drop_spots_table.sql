-- V19__drop_spots_table.sql
-- Drop spots table and dependent constraints/columns

-- 1. Drop foreign key references to spots
ALTER TABLE itinerary_details DROP CONSTRAINT IF EXISTS itinerary_details_spot_id_fkey;
ALTER TABLE itinerary_details DROP COLUMN IF EXISTS spot_id;

ALTER TABLE diaries DROP CONSTRAINT IF EXISTS diaries_spot_id_fkey;
ALTER TABLE diaries DROP COLUMN IF EXISTS spot_id;

-- 2. Drop dependent tables
DROP TABLE IF EXISTS spot_images CASCADE;

-- 3. Drop spots table itself
DROP TABLE IF EXISTS spots CASCADE;
