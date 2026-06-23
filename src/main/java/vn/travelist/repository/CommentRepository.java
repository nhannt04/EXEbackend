package vn.travelist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.travelist.model.Comment;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Lấy danh sách bình luận của 1 bài nhật ký du ký sắp xếp từ cũ đến mới
    List<Comment> findByDiaryIdOrderByCreatedAtAsc(Long diaryId);
    void deleteByDiaryId(Long diaryId);
}
