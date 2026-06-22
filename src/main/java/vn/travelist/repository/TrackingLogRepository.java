package vn.travelist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.travelist.model.TrackingLog;

import java.util.List;
import java.util.Map;

@Repository
public interface TrackingLogRepository extends JpaRepository<TrackingLog, Long> {
    
    // Đếm số lượng event theo từng ngày
    @Query("SELECT CAST(t.createdAt AS date) as date, COUNT(t) as count FROM TrackingLog t WHERE t.eventType = :eventType GROUP BY CAST(t.createdAt AS date) ORDER BY CAST(t.createdAt AS date)")
    List<Object[]> countEventsByDay(String eventType);

    // Đếm số lượng truy cập theo giờ trong ngày (Cho Heatmap)
    @Query("SELECT EXTRACT(HOUR FROM t.createdAt) as hour, COUNT(t) as count FROM TrackingLog t GROUP BY EXTRACT(HOUR FROM t.createdAt)")
    List<Object[]> countEventsByHour();
}
