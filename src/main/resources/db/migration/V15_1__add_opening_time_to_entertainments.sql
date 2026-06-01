-- V15_1__add_opening_time_to_entertainments.sql
-- Ensure entertainments has opening_time and closing_time columns before inserting data

ALTER TABLE entertainments
    ADD COLUMN IF NOT EXISTS opening_time TIME;

ALTER TABLE entertainments
    ADD COLUMN IF NOT EXISTS closing_time TIME;

-- No data population here; subsequent migrations (e.g. V16 and V21) will insert or update values as needed.

