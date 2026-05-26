-- V4__seed_community_diaries.sql
-- Seed dữ liệu mẫu bài viết du ký và bình luận cho mạng xã hội cộng đồng HISTRA

-- 1. Thêm 2 bài nhật ký mẫu liên kết tới địa điểm thực tế
INSERT INTO diaries (user_id, category, spot_id, content_vi, content_en, image_url, likes_count) VALUES
-- Bài viết của Nguyễn Du Khách (user_id = 1) liên kết với Chùa Cầu (spot_id = 1)
(
    1, 
    'healing', 
    1, 
    'Một buổi sáng bình yên tản bộ quanh phố cổ Hội An, ngắm nhìn Chùa Cầu cổ kính dưới ánh nắng ban mai lấp lánh. Mọi muộn phiền như tan biến hết!', 
    'A peaceful morning walking around Hoi An ancient town, watching the historic Japanese Covered Bridge under the sparkling morning sunlight. All worries seem to fade away!', 
    'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=600&q=80',
    42
),

-- Bài viết của Trần Admin (user_id = 2) liên kết với Faifo Coffee (spot_id = 2)
(
    2, 
    'food', 
    2, 
    'Góc nhìn từ sân thượng Faifo Coffee chưa bao giờ làm mình thất vọng. Nhâm nhi tách cafe cốt dừa mát lạnh và ngắm những mái ngói rêu phong cổ kính thật tuyệt vời!', 
    'The view from Faifo Coffee rooftop never disappoints. Sipping a cold coconut coffee and watching the mossy ancient tiled roofs is absolutely wonderful!', 
    'https://images.unsplash.com/photo-1447078806655-409295609806?auto=format&fit=crop&w=600&q=80',
    28
);

-- 2. Thêm bình luận tương tác cho bài viết mẫu
INSERT INTO comments (diary_id, user_id, content) VALUES
-- Bình luận của Trần Admin (user_id = 2) vào bài viết của Nguyễn Du Khách (diary_id = 1)
(
    1,
    2,
    'Hình ảnh chụp đẹp xuất sắc bạn ơi! Mình cũng vừa ghé Chùa Cầu sáng nay.'
);

INSERT INTO comments (diary_id, user_id, parent_comment_id, content) VALUES
-- Phản hồi (Bình luận con) của Nguyễn Du Khách (user_id = 1) trả lời Trần Admin (parent_comment_id = 1)
(
    1,
    1,
    1,
    'Cảm ơn bạn nhé! Sáng nay trời Hội An nắng đẹp lắm ạ.'
);
