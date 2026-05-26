package vn.histra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.histra.model.Spot;
import java.util.List;


@Repository
public interface SpotRepository extends JpaRepository<Spot, Long> {
    
    // Tìm các địa điểm theo nhóm danh mục phù hợp
    List<Spot> findByCategoryIn(List<String> categories);

    // Truy vấn SQL chuyên nghiệp kết hợp lọc theo danh mục và tìm kiếm theo tên/tags
    @Query("SELECT s FROM Spot s WHERE " +
           "(:category IS NULL OR :category = '' OR LOWER(s.category) = LOWER(:category)) AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(s.nameVi) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Spot> searchSpots(@Param("category") String category, @Param("keyword") String keyword);
}

