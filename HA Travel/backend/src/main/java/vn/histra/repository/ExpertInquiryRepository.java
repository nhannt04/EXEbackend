package vn.histra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.histra.model.ExpertInquiry;
import java.util.List;

@Repository
public interface ExpertInquiryRepository extends JpaRepository<ExpertInquiry, Long> {
    
    // Lấy danh sách hỏi đáp của 1 Chuyên gia cụ thể sắp xếp theo ngày gửi mới nhất
    List<ExpertInquiry> findByExpertIdOrderByCreatedAtDesc(Long expertId);

    // Lấy danh sách câu hỏi của 1 du khách cụ thể sắp xếp theo ngày gửi mới nhất
    List<ExpertInquiry> findByUserIdOrderByCreatedAtDesc(Long userId);
}
