package vn.histra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.histra.model.Diary;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    
    // Lấy danh sách nhật ký sắp xếp theo ngày đăng mới nhất
    List<Diary> findAllByOrderByCreatedAtDesc();

    // Lọc nhật ký theo danh mục sắp xếp theo ngày đăng mới nhất
    List<Diary> findByCategoryOrderByCreatedAtDesc(String category);
}
