-- V18__insert_rental_data.sql
-- Nạp dữ liệu từ file dịch_vụ_cho_thuê.md vào bảng rentals

INSERT INTO rentals (
    type, name, address, latitude, longitude, min_price, max_price, opening_time, closing_time, image_url
) VALUES
('Thuê máy ảnh', 'Mindigicamehoian', '32 Phan Tình, Hội An', 15.8977602, 108.3629803, 79000, 289000, '08:00:00', '21:00:00', 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=500&q=80'),
('Thuê máy ảnh', 'Hội An Camera Shop', '346 Nguyễn Duy Hiệu, Hội An', 15.8781198, 108.3348032, 120000, 300000, '07:30:00', '22:00:00', 'https://images.unsplash.com/photo-1502920917128-1aa500764cbd?auto=format&fit=crop&w=500&q=80'),
('Thuê đồ', 'Thần Sách Clothing', '21 Thái Phiên, Hội An', 15.8816475, 108.3271746, 50000, 200000, '08:00:00', '20:30:00', 'https://images.unsplash.com/photo-1590075865003-e48277faa558?auto=format&fit=crop&w=500&q=80'),
('Thuê đồ', 'Tiệm cô Thắm', '115 Lý Thái Tổ, Hội An', 15.8868935, 108.3330823, 80000, 200000, '07:00:00', '21:00:00', 'https://images.unsplash.com/photo-1566174053879-31528523f8ae?auto=format&fit=crop&w=500&q=80'),
('Thuê đồ', 'Julyn', '52 Phan Đình Phùng, Hội An Tây', 15.8864450, 108.3289128, 69000, 109000, '09:00:00', '19:00:00', 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=500&q=80'),
('Thuê xe', 'Duong Motorbike For Rent Hoi An', '38 Lê Văn Hưu, Hội An', 15.8887118, 108.3249717, 100000, 100000, '00:00:00', '23:59:59', 'https://images.unsplash.com/photo-1558981806-ec527fa84c39?auto=format&fit=crop&w=500&q=80'),
('Thuê xe', 'SIN E-scooter Rental Hoi An', '57 Thích Quảng Đức, Hội An', 15.8890390, 108.3280429, 150000, 150000, '07:00:00', '23:00:00', 'https://images.unsplash.com/photo-1595590424283-b8f17842773f?auto=format&fit=crop&w=500&q=80'),
('Photobooth', 'Life4cuts Photobooth', '9 Công Nữ Ngọc Hoa, Hội An', 15.8769011, 108.3255974, 80000, 100000, '09:00:00', '22:00:00', 'https://images.unsplash.com/photo-1527529482837-4698179dc6ce?auto=format&fit=crop&w=500&q=80'),
('Photobooth', 'Cây kim sợi chỉ Photobooth', '679/8 Hai Bà Trưng, Old Town, Hội An', 15.8779255, 108.3271480, 80000, 80000, '09:00:00', '22:00:00', 'https://images.unsplash.com/photo-1542038784456-1ea8e935640e?auto=format&fit=crop&w=500&q=80'),
('Photobooth', 'Tiệm ảnh Hội An', '24 Phan Chu Trinh, Hội An', 15.8784950, 108.3303504, 110000, 110000, '10:00:00', '21:00:00', 'https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?auto=format&fit=crop&w=500&q=80'),
('Photobooth', 'Calm Frame Photobooth', 'Biển An Bàng, Hội An Tây', 15.9140672, 108.3396240, 90000, 120000, '10:00:00', '21:00:00', 'https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&w=500&q=80');
