-- V2__seed_spots_hoi_an.sql
-- Seed dữ liệu mẫu gồm 5 địa điểm thực tế tại Hội An chất lượng cao để chạy thử bộ gợi ý AI

INSERT INTO spots (
    name_vi, name_en, category, tags, latitude, longitude, 
    average_cost, estimated_duration_minutes, opening_time, closing_time, 
    crowd_level, rating, suitable_for, time_of_day, image_url, description_vi, description_en
) VALUES
-- 1. Chùa Cầu (Sightseeing)
(
    'Chùa Cầu Nhật Bản', 'Japanese Covered Bridge', 'sightseeing', 'culture,photo,sightseeing,historic', 
    15.8772, 108.3262, 0, 45, '07:00:00', '21:00:00', 
    'high', 4.8, 'couple,family,solo,friends', 'morning,afternoon,evening',
    'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=500&q=80',
    'Biểu tượng văn hóa kiến trúc cổ kính hơn 400 năm tuổi giao thoa giữa Việt Nam và Nhật Bản.',
    'A 400-year-old architectural masterpiece symbolizing the historic cultural bridge between Vietnam and Japan.'
),

-- 2. Faifo Coffee (Cafe)
(
    'Faifo Coffee Đọc Sách', 'Faifo Coffee Rooftop', 'cafe', 'cafe,healing,chill,romantic,photo', 
    15.8778, 108.3282, 80000, 60, '08:00:00', '22:00:00', 
    'medium', 4.6, 'couple,solo', 'afternoon,evening',
    'https://images.unsplash.com/photo-1447078806655-409295609806?auto=format&fit=crop&w=500&q=80',
    'Quán cafe sân thượng nổi tiếng sở hữu góc nhìn toàn cảnh những mái ngói rêu phong của phố cổ Hội An.',
    'Famous rooftop coffee shop offering a stunning panoramic view over the golden tiled roofs of Hoi An ancient town.'
),

-- 3. Cơm Gà Bà Buội (Food)
(
    'Cơm Gà Bà Buội', 'Ba Buoi Chicken Rice', 'food', 'food,local,must-eat,traditional', 
    15.8776, 108.3312, 60000, 40, '10:00:00', '20:00:00', 
    'high', 4.7, 'couple,family,solo,friends', 'morning,afternoon',
    'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=500&q=80',
    'Thương hiệu cơm gà gia truyền nức tiếng từ những năm 1950 với hạt cơm dẻo thơm và thịt gà xé da giòn ngọt.',
    'Legendary family-owned chicken rice brand operating since the 1950s, famous for its savory rice and golden chicken.'
),

-- 4. Rừng Dừa Bảy Mẫu (Activity)
(
    'Rừng Dừa Bảy Mẫu Cẩm Thanh', 'Bay Mau Coconut Forest', 'activity', 'adventure,nature,boat,family', 
    15.8893, 108.3754, 150000, 120, '08:00:00', '17:00:00', 
    'medium', 4.5, 'family,friends', 'morning,afternoon',
    'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=500&q=80',
    'Trải nghiệm ngồi thuyền thúng bơi lội giữa rừng dừa nước bạt ngàn và xem múa thúng xoay vòng cảm giác mạnh.',
    'Thrilling basket boat ride through the lush nipa palm forest featuring amazing spinning basket performances.'
),

-- 5. Little Hoi An Boutique Hotel (Stay)
(
    'Khách sạn Little Hoi An Boutique', 'Little Hoi An Boutique Hotel', 'stay', 'stay,luxury,healing,comfort', 
    15.8765, 108.3235, 800000, 1440, '00:00:00', '23:59:59', 
    'low', 4.9, 'couple,family,solo,friends', 'all',
    'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=500&q=80',
    'Khách sạn boutique sang trọng nằm ven sông Thu Bồn sở hữu bể bơi vô cực và phong cách trang trí đậm chất di sản.',
    'A charming riverside boutique hotel boasting a stunning infinity pool and vintage heritage-inspired interiors.'
);
