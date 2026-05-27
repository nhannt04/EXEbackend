package vn.histra.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.histra.model.Spot;
import java.util.List;

@Repository
public interface SpotRepository extends JpaRepository<Spot, Long> {
    
    // Tìm các địa điểm theo nhóm danh mục phù hợp với EntityGraph để tránh N+1 select
    @EntityGraph(attributePaths = {"images"})
    List<Spot> findByCategoryIn(List<String> categories);

    // Truy vấn SQL chuyên nghiệp kết hợp lọc theo danh mục và tìm kiếm theo tên/tags với EntityGraph
    @EntityGraph(attributePaths = {"images"})
    @Query("SELECT s FROM Spot s WHERE " +
           "(:category IS NULL OR :category = '' OR LOWER(s.category) = LOWER(:category)) AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(s.nameVi) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Spot> searchSpots(@Param("category") String category, @Param("keyword") String keyword);

    // Lấy các địa điểm nổi bật ngẫu nhiên (đã sắp xếp theo rating + random) cho trang chủ
    @EntityGraph(attributePaths = {"images"})
    @Query("SELECT s FROM Spot s ORDER BY s.rating DESC, FUNCTION('RANDOM')")
    List<Spot> findFeaturedSpots(Pageable pageable);

    // Lấy top spots theo từng danh mục cho section phân loại
    @EntityGraph(attributePaths = {"images"})
    @Query("SELECT s FROM Spot s WHERE LOWER(s.category) = LOWER(:category) ORDER BY s.rating DESC")
    List<Spot> findTopByCategory(@Param("category") String category, Pageable pageable);
}
