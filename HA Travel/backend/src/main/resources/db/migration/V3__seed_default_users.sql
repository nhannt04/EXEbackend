-- V3__seed_default_users.sql
-- Seed sẵn 2 tài khoản kiểm thử chính để chạy ngay tính năng Auth JWT
-- Cả hai tài khoản đều sử dụng MẬT KHẨU MẶC ĐỊNH: 12345678 (Đã được SHA-256 hash và Base64 encode chuẩn: 73l8gRjwLftklgfdXT+MdiMEjJwGPVMsyVxe16iYpk8=)

INSERT INTO users (email, password_hash, full_name, role, avatar_url) VALUES
-- 1. Tài khoản du khách thường (traveler@histra.vn)
(
    'traveler@histra.vn', 
    '73l8gRjwLftklgfdXT+MdiMEjJwGPVMsyVxe16iYpk8=', 
    'Nguyễn Du Khách', 
    'USER', 
    NULL
),

-- 2. Tài khoản quản trị viên (admin@histra.vn)
(
    'admin@histra.vn', 
    '73l8gRjwLftklgfdXT+MdiMEjJwGPVMsyVxe16iYpk8=', 
    'Trần Admin', 
    'ADMIN', 
    NULL
);
