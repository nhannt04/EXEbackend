-- V5__seed_experts.sql
-- Seed dữ liệu mẫu Chuyên gia bản địa và lịch sử hỏi đáp trực tuyến tư vấn du lịch

-- 1. Đăng ký Trần Admin (user_id = 2) làm Chuyên gia Bản Địa
INSERT INTO experts (user_id, expertise, description_vi, description_en, is_online, rating) VALUES
(
    2,
    'Ẩm thực & Di sản văn hóa cổ kính Hội An',
    'Chào bạn! Tôi sinh ra và lớn lên tại Hội An, sở hữu hơn 10 năm kinh nghiệm làm hướng dẫn viên chuyên nghiệp và am tường sâu sắc các ngõ ngách, giai thoại lịch sử cùng địa chỉ ẩm thực gia truyền.',
    'Hello! Born and raised in Hoi An with 10+ years of professional guiding experience, deep knowledge of hidden alleys, history, and secret authentic food addresses.',
    TRUE,
    4.9
);

-- 2. Thêm 1 câu hỏi mẫu gửi tới Chuyên gia vừa đăng ký
INSERT INTO expert_inquiries (expert_id, user_id, question, answer) VALUES
(
    1,
    1,
    'Chào chuyên gia, mình định đi ăn cơm gà vào buổi tối ở Hội An thì tiệm Bà Buội còn mở cửa không ạ? Hay có địa chỉ nào khác ngon hơn không?',
    'Chào bạn! Tiệm Cơm gà Bà Buội thường hết hàng rất sớm (khoảng 19:30 - 20:00). Nếu bạn đi muộn hơn, mình gợi ý tiệm Cơm Gà Bà Nga ở số 8 Phan Châu Trinh, mở muộn và nước sốt đậm đà cực kỳ hợp ăn tối nhé!'
);
