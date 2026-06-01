-- V26__create_diary_reactions_table.sql
-- Lưu 1 reaction duy nhất (LIKE / DISLIKE) cho mỗi user trên mỗi bài viết
CREATE TABLE IF NOT EXISTS diary_reactions (
    id BIGSERIAL PRIMARY KEY,
    diary_id BIGINT REFERENCES diaries(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    reaction_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_diary_reactions_diary_user UNIQUE (diary_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_diary_reactions_diary_id ON diary_reactions(diary_id);
CREATE INDEX IF NOT EXISTS idx_diary_reactions_user_id ON diary_reactions(user_id);

