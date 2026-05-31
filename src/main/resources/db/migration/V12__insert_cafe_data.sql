-- V12__insert_cafe_data.sql
-- Nạp dữ liệu từ file cafe.md vào bảng cafes

INSERT INTO cafes (
    name, style, address, latitude, longitude,
    min_price, max_price, opening_time, closing_time, image_url
) VALUES
('FeFe Coffee', 'Chill & Thư giãn', 'Đường Bầu Ốc Thượng, thôn Bầu Ốc, Hội An Tây', 15.9003701, 108.3070379, 45000, 45000, '08:00:00', '17:00:00', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=500&q=80'),
('Cam Coffee n More', 'Chill & Thư giãn', '45 Đào Duy Từ, Hội An', 15.8768922, 108.3195189, 67000, 67000, '07:00:00', '19:30:00', 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=500&q=80'),
('Trốn Hội An Coffee', 'Chill & Thư giãn', '151 Trần Hưng Đạo, Hội An', 15.8794551, 108.3242773, 47000, 47000, '07:00:00', '22:00:00', 'https://images.unsplash.com/photo-1498804103079-a6351b050096?auto=format&fit=crop&w=500&q=80'),
('Little Pie Garden', 'Chill & Thư giãn', 'Làng rau Trà Quế, Hội An', 15.9043488, 108.3361582, 35000, 50000, '09:00:00', '16:20:00', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=500&q=80'),
('92 Station Restaurant & Cafe', 'Chill & Thư giãn, Sống ảo', '92 Trần Phú, Old Town, Hội An', 15.8769853, 108.3293063, 80000, 80000, '07:00:00', '22:00:00', 'https://images.unsplash.com/photo-1541167760496-1628856ab772?auto=format&fit=crop&w=500&q=80'),
('Moments Hoi An', 'Chill & Thư giãn, Sống ảo', '47 Lê Lợi, Old Town, Hội An', 15.877027, 108.3286748, 80000, 80000, '08:00:00', '22:00:00', 'https://images.unsplash.com/photo-1554118811-1e0d58224f24?auto=format&fit=crop&w=500&q=80'),
('Mót Hội An', 'Trải nghiệm, Sống ảo', '150 Trần Phú, Old Town, Hội An', 15.8771714, 108.3273342, 15000, 25000, '08:00:00', '22:00:00', 'https://images.unsplash.com/photo-1507133750040-4a8f57021571?auto=format&fit=crop&w=500&q=80'),
('Café Slow - Trà Quế', 'Chill & Thư giãn', 'Vườn rau Trà Quế, Hội An', 15.9022847, 108.3396308, 55000, 55000, '07:30:00', '18:30:00', 'https://imgcdn.bokun.tools/72aaec77-c839-44d6-98db-5693eea5d045.jpeg?w=1500&h=1500&mode=crop'),
('HanaPan', 'Trải nghiệm', '303 Cửa Đại, Cẩm Châu, Hội An', 15.8829492, 108.3466381, 30000, 60000, '08:00:00', '17:00:00', 'https://images.unsplash.com/photo-1497935586351-b67a49e012bf?auto=format&fit=crop&w=500&q=80'),
('The Deckhouse', 'Chill & Thư giãn, Sát biển', 'Biển An Bàng, Hội An Tây', 15.9142883, 108.3395524, 80000, 150000, '07:00:00', '22:00:00', 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=500&q=80'),
('Sound of Silence', 'Yên bình & Sát biển', '40 Nguyễn Phan Vinh, Hội An Tây', 15.9087375, 108.3473865, 40000, 90000, '07:00:00', '19:00:00', 'https://images.unsplash.com/photo-1442512595331-e89e73853f31?auto=format&fit=crop&w=500&q=80'),
('Thung Chai Sea Bar & Kitchen', 'Chill & Thư giãn, Sea bar', '20 Nguyễn Phan Vinh, Hội An Tây', 15.9080578, 108.3486685, 50000, 120000, '08:30:00', '21:30:00', 'https://images.unsplash.com/photo-1559925393-8be0ec4767c8?auto=format&fit=crop&w=500&q=80'),
('The Taste of Sea', 'Chill & Thư giãn, Sea bar', '58 Nguyễn Phan Vinh, Hội An Tây', 15.9093937, 108.3462163, 40000, 100000, '07:30:00', '22:00:00', 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=500&q=80');
