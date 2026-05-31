-- V23__add_spot_id_to_diaries.sql
-- Re-introduce spot_id into diaries (nullable) to allow linking diary posts to aggregated spots

ALTER TABLE diaries
    ADD COLUMN IF NOT EXISTS spot_id BIGINT;

-- Add foreign key constraint if spots table exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'spots') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'diaries_spot_id_fkey' AND table_name = 'diaries') THEN
            ALTER TABLE diaries
                ADD CONSTRAINT diaries_spot_id_fkey FOREIGN KEY (spot_id) REFERENCES spots(id) ON DELETE SET NULL;
        END IF;
    END IF;
END$$;

