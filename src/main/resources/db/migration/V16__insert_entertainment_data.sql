-- V16__insert_entertainment_data.sql
-- Nạp dữ liệu từ file Khu_vui_chơi (1).md vào bảng entertainments

INSERT INTO entertainments (
    type, interests, name, address, latitude, longitude, opening_time, closing_time, min_price, max_price, image_url
) VALUES
('Biển', 'Chill & Thư giãn, Sống ảo, Trải nghiệm', 'Biển An Bàng', 'Hai Bà Trưng, Hội An Tây', 15.9136072, 108.3406489, '00:00', '23:59', 0, 0, 'https://ik.imagekit.io/tvlk/blog/2023/03/bien-an-bang-1.jpg?tr=q-70,c-at_max,w-1000,h-600'),
('Biển', 'Chill & Thư giãn, Trải nghiệm, Sống ảo', 'Biển Cửa Đại', 'Cửa Đại, Hội An', 15.9018092, 108.3600129, '00:00', '23:59', 0, 0, 'https://static.vinwonders.com/production/bien-cua-dai-anh-thumb-1.jpg'),
('Vui chơi', 'Trải nghiệm', 'Cù Lao Chàm', 'Cù Lao Chàm, Hội An', 15.9130325, 108.4496258, '08:30', '16:00', NULL, NULL, 'https://media.baoquangninh.vn/upload/image/202307/medium/2102820_clc1_10313710.jpg'),
('Vui chơi', 'Trải nghiệm', 'Chợ đêm Nguyễn Hoàng', 'Nguyễn Hoàng, An Hội, Hội An', 15.8759693, 108.3260184, '17:00', '23:00', 0, 0, 'https://maps.app.goo.gl/j8X3FUy8fLCQdDVd6'),
('Vui chơi', 'Trải nghiệm, Sống ảo, Chill & Thư giãn', 'Thả hoa đăng', 'Phố cổ Hội An', 15.8783812, 108.3324215, '17:00', '22:00', 135000, 140000, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTPioeM0wOkptUt1QPgF5OT2eACaW5aem7Bsg&s'),
('Vui chơi', 'Chill & Thư giãn, Trải nghiệm', 'VinWonders Nam Hội An', 'Võ Chí Công, Thăng An', 15.7868077, 108.4086898, '09:00', '20:00', 450000, 650000, 'https://reviewdanang.com.vn/wp-content/uploads/2025/11/bat-mi-bi-kip-choi-het-nat-tai-vinwonders-nam-hoi-an-to-hop-khu-vui-choi-giai-tri-so-1-hoi-an.webp'),
('Vui chơi', 'Chill & Thư giãn, Trải nghiệm, Sống ảo', 'Rừng dừa Bảy Mẫu', 'Thôn Thanh, Hội An Đông', 15.8782982, 108.3724553, '07:00', '17:00', 200000, 200000, 'https://lh4.googleusercontent.com/proxy/dyjBUd7l35Id64qeNMAjJdmXaYZvljbJcC09w5c_68t3fatEBK6jzTq9fW20xzjsmV4pc06Bu52Slk3mrGxjc8Kkg3Gb-tL-nb7od8nDFbcUR2KjVtY9dLkywo99cR56Zb3a0g'),
('Workshop', 'Chill & Thư giãn', 'Làng Gốm Thanh Hà', 'Phạm Phán, Hội An Tây', 15.8781302, 108.3005058, '08:00', '17:30', 40000, 150000, 'https://bizweb.dktcdn.net/thumb/grande/100/101/075/articles/th-5e952b24-bf06-4c0c-b1ef-ed6318ddd655.jpg?v=1560419808957'),
('Workshop', 'Trải nghiệm', 'Phoenicia Stained Glass Studio', '87 Nguyễn Duy Hiệu, Hội An', 15.8815889, 108.3493338, '09:00', '17:00', 900000, 1200000, 'https://maps.app.goo.gl/dZswHJsjhYgVsByB7'),
('Workshop', 'Trải nghiệm', 'Taboo Bamboo Workshop', 'Thanh Tam Dong Trần Nhân Tông, Hội An Đông', 15.8724077, 108.3740598, '08:00', '17:00', 700000, 700000, 'https://maps.app.goo.gl/rwiNuyt452JZYVny7'),
('Workshop', 'Trải nghiệm', 'Workshop làm đèn lồng', '57 Đào Duy Từ, Hội An', 15.8769056, 108.3193095, '09:00', '21:00', 250000, 350000, 'https://maps.app.goo.gl/DaJnVKH2VPj8uFjB8');
