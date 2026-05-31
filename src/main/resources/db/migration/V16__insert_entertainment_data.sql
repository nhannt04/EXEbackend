-- V16__insert_entertainment_data.sql
-- Nạp dữ liệu từ file Khu_vui_chơi.md vào bảng entertainments

INSERT INTO entertainments (
    type, interests, name, address, latitude, longitude, min_price, max_price, image_url
) VALUES
('Biển', 'Chill & Thư giãn, Sống ảo, Trải nghiệm', 'Biển An Bàng', 'Hai Bà Trưng, Hội An Tây', 15.9136072, 108.3406489, 0, 0, 'https://ik.imagekit.io/tvlk/blog/2023/03/bien-an-bang-1.jpg?tr=q-70,c-at_max,w-1000,h-600'),
('Biển', 'Chill & Thư giãn, Trải nghiệm, Sống ảo', 'Biển Cửa Đại', 'Cửa Đại, Hội An', 15.9018092, 108.3600129, 0, 0, 'https://static.vinwonders.com/production/bien-cua-dai-anh-thumb-1.jpg'),
('Vui chơi', 'Trải nghiệm', 'Cù Lao Chàm', 'Cù Lao Chàm, Hội An', 15.9130325, 108.4496258, 0, 0, 'https://media.baoquangninh.vn/upload/image/202307/medium/2102820_clc1_10313710.jpg'),
('Vui chơi', 'Trải nghiệm', 'Chợ đêm Nguyễn Hoàng', 'Nguyễn Hoàng, An Hội, Hội An', 15.8759693, 108.3260184, 0, 0, 'https://images.unsplash.com/photo-1571216390117-64df7f495537?auto=format&fit=crop&w=500&q=80'),
('Vui chơi', 'Trải nghiệm, Sống ảo, Chill & Thư giãn', 'Thả hoa đăng', 'Phố cổ Hội An', 15.8783812, 108.3324215, 135000, 140000, 'https://images.unsplash.com/photo-1508009603885-50cf7c579365?auto=format&fit=crop&w=500&q=80'),
('Vui chơi', 'Chill & Thư giãn, Trải nghiệm', 'VinWonders Nam Hội An', 'Võ Chí Công, Thăng An', 15.7868077, 108.4086898, 450000, 650000, 'https://reviewdanang.com.vn/wp-content/uploads/2025/11/bat-mi-bi-kip-choi-het-nat-tai-vinwonders-nam-hoi-an-to-hop-khu-vui-choi-giai-tri-so-1-hoi-an.webp'),
('Vui chơi', 'Chill & Thư giãn, Trải nghiệm, Sống ảo', 'Rừng dừa Bảy Mẫu', 'Thôn Thanh, Hội An Đông', 15.8782982, 108.3724553, 200000, 200000, 'https://images.unsplash.com/photo-1542856391-010fb87dcfed?auto=format&fit=crop&w=500&q=80'),
('Workshop', 'Chill & Thư giãn', 'Làng Gốm Thanh Hà', 'Phạm Phán, Hội An Tây', 15.8781302, 108.3005058, 40000, 150000, 'https://bizweb.dktcdn.net/thumb/grande/100/101/075/articles/th-5e952b24-bf06-4c0c-b1ef-ed6318ddd655.jpg?v=1560419808957'),
('Workshop', 'Trải nghiệm', 'Phoenicia Stained Glass Studio', '87 Nguyễn Duy Hiệu, Hội An', 15.8815889, 108.3493338, 900000, 1200000, 'https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=500&q=80'),
('Workshop', 'Trải nghiệm', 'Taboo Bamboo Workshop', 'Thanh Tam Dong Trần Nhân Tông, Hội An Đông', 15.8724077, 108.3740598, 700000, 700000, 'https://images.unsplash.com/photo-1549490349-8643362247b5?auto=format&fit=crop&w=500&q=80'),
('Workshop', 'Trải nghiệm', 'Workshop làm đèn lồng', '57 Đào Duy Từ, Hội An', 15.8769056, 108.3193095, 250000, 350000, 'https://images.unsplash.com/photo-1528605248644-14dd04022da1?auto=format&fit=crop&w=500&q=80');
