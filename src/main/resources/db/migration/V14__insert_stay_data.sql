-- V14__insert_stay_data.sql
-- Nạp dữ liệu từ file chổ_ở.md vào bảng stays

INSERT INTO stays (
    type, name, address, latitude, longitude,
    capacity, min_price, max_price, notes, image_url
) VALUES
('Hotel', 'Little Pie Hội An', 'Trà Quế, Hội An Tây', 15.9040260, 108.3358285, '2 lớn, 1 trẻ em', 1400000, 1400000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=500&q=80'),
('Hotel', 'Little Pie Hội An - Căn 1 PN', 'Trà Quế, Hội An Tây', 15.9040260, 108.3358285, '4 người (đôi), căn 1 phòng', 1400000, 1400000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=500&q=80'),
('Villa', 'An Tung Tăng', 'Cẩm Thanh, Hội An', 15.8758048, 108.3616358, '2 người lớn, 2 lớn + 1 trẻ em', 1600000, 1600000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://a0.muscache.com/im/pictures/hosting/Hosting-1395257414050017620/original/15d0fb15-fc1e-46a5-b605-ad8ad9f464ca.jpeg?im_w=720&width=720&quality=70&auto=webp'),
('Villa', 'Anoi Home - Căn 1 giường', 'ĐX18, Hội An, Đà Nẵng', 15.8742627, 108.3623680, '2 người lớn, căn 1 giường', 2000000, 2000000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=500&q=80'),
('Villa', 'Anoi Home - Căn 3 khách', 'ĐX18, Hội An, Đà Nẵng', 15.8742627, 108.3623680, '2 lớn, 1 trẻ em', 2600000, 2600000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1613490493576-7fde63acd811?auto=format&fit=crop&w=500&q=80'),
('Homestay', 'Pea Home - Nhà Đậu', 'Tống Văn Sương, Hội An Đông', 15.8826705, 108.3595155, '2 người lớn', 500000, 500000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=500&q=80'),
('Homestay', 'Hương Mùa Hè Homestay', '12 Trần Quốc Toản, Hội An', 15.8818119, 108.3476059, '2 lớn, 1 trẻ em', 500000, 500000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=500&q=80'),
('Homestay', 'Ri''s House Homestay', 'Huỳnh Thị Lựu, Hội An Đông', 15.8794972, 108.3623412, '2 người lớn, 2 lớn + 1 trẻ em', 280000, 280000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=500&q=80'),
('Villa', 'Villa Brother', '110 Đào Duy Từ, Hội An', 15.8771506, 108.3189502, '2 người lớn', 450000, 450000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?auto=format&fit=crop&w=500&q=80'),
('Villa', 'Le Petit Villa Hoi An', '01 Phan Đình Phùng, Hội An', 15.8896377, 108.3308523, '2 người lớn', 500000, 500000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1613977257592-4871e5f47d99?auto=format&fit=crop&w=500&q=80'),
('Villa', 'Di Home - Căn 2 khách', '15/3 Lê Quý Đôn, Hội An', 15.8821147, 108.3213987, '2 người lớn', 550000, 550000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=500&q=80'),
('Villa', 'Di Home - Căn 4 khách', '15/3 Lê Quý Đôn, Hội An', 15.8821147, 108.3213987, '4 người (đôi)', 900000, 900000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=500&q=80'),
('Villa', 'Sen Village', 'Cửa Đại, Cẩm Châu, Hội An', 15.8807023, 108.3408307, '2 lớn + 1 trẻ em / căn 2 phòng / 2 người lớn', 1100000, 3200000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=500&q=80'),
('Homestay', 'Green House Homestay', '393/43 Lý Thái Tổ, Hội An Đông', 15.8895454, 108.3314496, '2 người lớn', 350000, 500000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=500&q=80'),
('Homestay', 'Green Garden Homestay', '243 Cửa Đại, Hội An', 15.8850479, 108.3498533, '2 người lớn', 500000, 600000, 'Giá có thể thay đổi, đặt sớm giá rẻ hơn', 'https://images.unsplash.com/photo-1568605114967-8130f3a36994?auto=format&fit=crop&w=500&q=80');
