package vn.histra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.histra.model.Expert;
import java.util.List;

@Repository
public interface ExpertRepository extends JpaRepository<Expert, Long> {
    
    // Lấy danh sách các chuyên gia đang online hỗ trợ
    List<Expert> findByIsOnlineTrue();
}
