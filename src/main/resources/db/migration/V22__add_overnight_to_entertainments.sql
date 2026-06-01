-- V22__add_overnight_to_entertainments.sql
-- Persist overnight schedule flag for entertainments
ALTER TABLE entertainments
    ADD COLUMN IF NOT EXISTS overnight BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE entertainments
SET overnight = TRUE
WHERE opening_time IS NOT NULL
  AND closing_time IS NOT NULL
  AND opening_time > closing_time;

