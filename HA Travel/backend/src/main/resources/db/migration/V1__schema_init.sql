-- V1__schema_init.sql
-- Khởi tạo cấu trúc các bảng cho dự án HISTRA trên Neon PostgreSQL

-- 1. Bảng lưu trữ thông tin Người dùng (users)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'USER',
    avatar_cf_id VARCHAR(255),
    avatar_url VARCHAR(500),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng lưu trữ thông tin Địa điểm du lịch (spots)
CREATE TABLE IF NOT EXISTS spots (
    id BIGSERIAL PRIMARY KEY,
    name_vi VARCHAR(255) NOT NULL,
    name_en VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    tags VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    average_cost INTEGER DEFAULT 0,
    estimated_duration_minutes INTEGER DEFAULT 60,
    opening_time TIME DEFAULT '08:00:00',
    closing_time TIME DEFAULT '22:00:00',
    crowd_level VARCHAR(10) DEFAULT 'medium',
    rating DOUBLE PRECISION DEFAULT 5.0,
    suitable_for VARCHAR(100) NOT NULL,
    time_of_day VARCHAR(50) NOT NULL,
    image_cf_id VARCHAR(255),
    image_url VARCHAR(500),
    description_vi TEXT,
    description_en TEXT
);

-- 3. Bảng lưu trữ Khóa phiên làm việc bảo mật (refresh_tokens)
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL
);

-- 4. Bảng lưu trữ Lộ trình du lịch (itineraries)
CREATE TABLE IF NOT EXISTS itineraries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    destination VARCHAR(100) DEFAULT 'Hội An',
    total_days INTEGER NOT NULL,
    total_budget DOUBLE PRECISION NOT NULL,
    travel_style VARCHAR(50) NOT NULL,
    group_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Bảng lưu trữ Chi tiết điểm dừng từng ngày (itinerary_details)
CREATE TABLE IF NOT EXISTS itinerary_details (
    id BIGSERIAL PRIMARY KEY,
    itinerary_id BIGINT REFERENCES itineraries(id) ON DELETE CASCADE,
    spot_id BIGINT REFERENCES spots(id) ON DELETE RESTRICT,
    day_number INTEGER NOT NULL,
    time_slot VARCHAR(50) NOT NULL,
    order_index INTEGER NOT NULL
);

-- 6. Bảng lưu trữ Bài viết mạng xã hội (diaries)
CREATE TABLE IF NOT EXISTS diaries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    spot_id BIGINT REFERENCES spots(id) ON DELETE SET NULL,
    content_vi TEXT NOT NULL,
    content_en TEXT NOT NULL,
    image_cf_id VARCHAR(255),
    image_url VARCHAR(500),
    likes_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Bảng lưu trữ Yêu thích bài viết (diary_likes)
CREATE TABLE IF NOT EXISTS diary_likes (
    diary_id BIGINT REFERENCES diaries(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (diary_id, user_id)
);

-- 8. Bảng lưu trữ Bình luận bài viết (comments)
CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    diary_id BIGINT REFERENCES diaries(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    parent_comment_id BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. Bảng lưu trữ Hướng dẫn viên địa phương (experts)
CREATE TABLE IF NOT EXISTS experts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    expertise VARCHAR(255) NOT NULL,
    description_vi TEXT,
    description_en TEXT,
    is_online BOOLEAN DEFAULT FALSE,
    rating DOUBLE PRECISION DEFAULT 5.0
);

-- 10. Bảng lưu trữ Hỏi đáp trực tuyến với chuyên gia (expert_inquiries)
CREATE TABLE IF NOT EXISTS expert_inquiries (
    id BIGSERIAL PRIMARY KEY,
    expert_id BIGINT REFERENCES experts(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    answer TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
