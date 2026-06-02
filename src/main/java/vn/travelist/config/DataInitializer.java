package vn.travelist.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.travelist.model.*;
import vn.travelist.repository.*;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ExpertRepository expertRepository;
    private final ExpertInquiryRepository expertInquiryRepository;
    private final DiaryRepository diaryRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        long userCount = userRepository.count();
        if (userCount == 0) {
            log.info("[DataInitializer] Cơ sở dữ liệu trống người dùng. Tiến hành nạp tài khoản mẫu hệ thống...");
            
            // Tài khoản du khách thường
            User traveler = User.builder()
                    .email("traveler@travelist.vn")
                    .passwordHash("73l8gRjwLftklgfdXT+MdiMEjJwGPVMsyVxe16iYpk8=")
                    .fullName("Nguyễn Du Khách")
                    .role("USER")
                    .enabled(true)
                    .build();
            
            // Tài khoản quản trị viên
            User admin = User.builder()
                    .email("admin@travelist.vn")
                    .passwordHash("73l8gRjwLftklgfdXT+MdiMEjJwGPVMsyVxe16iYpk8=")
                    .fullName("Admin Management")
                    .role("ADMIN")
                    .enabled(true)
                    .build();
            
            userRepository.save(traveler);
            userRepository.save(admin);
            log.info("[DataInitializer] Đã nạp thành công 2 tài khoản mẫu (traveler@travelist.vn, admin@travelist.vn).");

            // Đăng ký Admin Management làm Chuyên gia bản địa
            log.info("[DataInitializer] Đăng ký Admin Management làm Chuyên gia Bản Địa...");
            Expert expert = Expert.builder()
                    .user(admin)
                    .expertise("Ẩm thực & Di sản văn hóa cổ kính Hội An")
                    .descriptionVi("Chào bạn! Tôi sinh ra và lớn lên tại Hội An, sở hữu hơn 10 năm kinh nghiệm làm hướng dẫn viên chuyên nghiệp và am tường sâu sắc các ngõ ngách, giai thoại lịch sử cùng địa chỉ ẩm thực gia truyền.")
                    .descriptionEn("Hello! Born and raised in Hoi An with 10+ years of professional guiding experience, deep knowledge of hidden alleys, history, and secret authentic food addresses.")
                    .isOnline(true)
                    .rating(4.9)
                    .build();
            expertRepository.save(expert);

            // Thêm câu hỏi mẫu gửi tới Chuyên gia vừa đăng ký
            ExpertInquiry inquiry = ExpertInquiry.builder()
                    .expert(expert)
                    .user(traveler)
                    .question("Chào chuyên gia, mình định đi ăn cơm gà vào buổi tối ở Hội An thì tiệm Bà Buội còn mở cửa không ạ? Hay có địa chỉ nào khác ngon hơn không?")
                    .answer("Chào bạn! Tiệm Cơm gà Bà Buội thường hết hàng rất sớm (khoảng 19:30 - 20:00). Nếu bạn đi muộn hơn, mình gợi ý tiệm Cơm Gà Bà Nga ở số 8 Phan Châu Trinh, mở muộn và nước sốt đậm đà cực kỳ hợp ăn tối nhé!")
                    .build();
            expertInquiryRepository.save(inquiry);
            log.info("[DataInitializer] Đã nạp thành công dữ liệu mẫu Chuyên gia & Hỏi đáp trực tuyến.");

            // Tạo bài viết nhật ký mẫu của Nguyễn Du Khách
            log.info("[DataInitializer] Đang nạp bài viết nhật ký mẫu của du khách...");
            Diary diary1 = Diary.builder()
                    .user(traveler)
                    .category("sightseeing")
                    .contentVi("Một buổi sáng bình yên đi dạo quanh phố cổ Hội An, ngắm nhìn Chùa Cầu Nhật Bản cổ kính rêu phong dưới nắng mai rực rỡ. Mọi phiền muộn dường như tan biến!")
                    .contentEn("A peaceful morning walking around Hoi An ancient town, watching the historic Japanese Covered Bridge under the sparkling morning sunlight. All worries seem to fade away!")
                    .likesCount(42)
                    .build();
            
            DiaryImage img1 = DiaryImage.builder()
                    .diary(diary1)
                    .imageUrl("https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=600&q=80")
                    .build();
            diary1.setImages(new ArrayList<>());
            diary1.getImages().add(img1);
            diaryRepository.save(diary1);

            // Tạo bài viết nhật ký mẫu của Admin Management
            Diary diary2 = Diary.builder()
                    .user(admin)
                    .category("food")
                    .contentVi("Góc nhìn từ sân thượng Faifo Coffee chưa bao giờ làm mình thất vọng. Nhâm nhi tách cafe cốt dừa mát lạnh và ngắm những mái ngói rêu phong cổ kính thật tuyệt vời!")
                    .contentEn("The view from Faifo Coffee rooftop never disappoints. Sipping a cold coconut coffee and watching the mossy ancient tiled roofs is absolutely wonderful!")
                    .likesCount(28)
                    .build();
            
            DiaryImage img2 = DiaryImage.builder()
                    .diary(diary2)
                    .imageUrl("https://images.unsplash.com/photo-1447078806655-409295609806?auto=format&fit=crop&w=600&q=80")
                    .build();
            diary2.setImages(new ArrayList<>());
            diary2.getImages().add(img2);
            diaryRepository.save(diary2);

            // Thêm bình luận mẫu từ Admin Management vào bài viết Nguyễn Du Khách
            Comment comment = Comment.builder()
                    .diary(diary1)
                    .user(admin)
                    .content("Hình chụp góc này đẹp quá bạn ơi! Sáng sớm ở đây thực sự rất bình yên.")
                    .build();
            commentRepository.save(comment);
            log.info("[DataInitializer] Đã nạp thành công 2 bài đăng nhật ký và bình luận mẫu của cộng đồng.");
        } else {
            log.info("[DataInitializer] Cơ sở dữ liệu đã có sẵn {} tài khoản người dùng.", userCount);
        }
    }
}
