CREATE TABLE tracking_logs (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(255),
    target_id VARCHAR(255),
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
