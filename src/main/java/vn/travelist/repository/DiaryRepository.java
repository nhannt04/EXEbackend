package vn.travelist.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.travelist.model.Diary;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    
    // Lấy danh sách nhật ký sắp xếp theo ngày đăng mới nhất với EntityGraph để tránh N+1 select
    @EntityGraph(attributePaths = {"images", "user"})
    List<Diary> findAllByOrderByCreatedAtDesc();

    // Lọc nhật ký theo danh mục sắp xếp theo ngày đăng mới nhất với EntityGraph để tránh N+1 select
    @EntityGraph(attributePaths = {"images", "user"})
    List<Diary> findByCategoryOrderByCreatedAtDesc(String category);
}
