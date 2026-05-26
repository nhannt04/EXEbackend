package vn.histra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.histra.dto.SpotWithDistance;
import vn.histra.model.Spot;
import vn.histra.repository.SpotRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotService {

    private final SpotRepository spotRepository;

    /**
     * Tìm kiếm và lọc địa điểm theo danh mục và từ khóa (tên/tags)
     */
    public List<Spot> searchSpots(String category, String keyword) {
        return spotRepository.searchSpots(category, keyword);
    }

    /**
     * Xem thông tin chi tiết của 1 địa điểm
     */
    public Spot getSpotById(Long id) {
        return spotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa điểm du lịch với ID: " + id));
    }

    /**
     * Lọc và sắp xếp các địa điểm gần tọa độ người dùng nhất trong bán kính cho trước
     */
    public List<SpotWithDistance> getNearbySpots(double userLat, double userLng, double radiusInKm) {
        List<Spot> allSpots = spotRepository.findAll();
        List<SpotWithDistance> nearbyList = new ArrayList<>();

        for (Spot spot : allSpots) {
            double distance = haversine(userLat, userLng, spot.getLatitude(), spot.getLongitude());
            
            // Nếu khoảng cách nằm trong bán kính cho phép
            if (distance <= radiusInKm) {
                // Làm tròn khoảng cách 2 chữ số thập phân
                double roundedDistance = Math.round(distance * 100.0) / 100.0;
                
                nearbyList.add(SpotWithDistance.builder()
                        .spot(spot)
                        .distance(roundedDistance)
                        .build());
            }
        }

        // Sắp xếp tăng dần theo khoảng cách gần nhất
        return nearbyList.stream()
                .sorted(Comparator.comparingDouble(SpotWithDistance::getDistance))
                .collect(Collectors.toList());
    }

    /**
     * Công thức Haversine tính khoảng cách thực tế giữa 2 tọa độ (km)
     */
    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Bán kính trái đất (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
                 
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Tạo địa điểm du lịch mới
     */
    public Spot createSpot(Spot spot) {
        if (spot.getTags() == null) {
            spot.setTags("Hội An, Điểm đến, Du lịch");
        }
        if (spot.getSuitableFor() == null) {
            spot.setSuitableFor("couple, family, friends");
        }
        if (spot.getTimeOfDay() == null) {
            spot.setTimeOfDay("morning, afternoon, evening");
        }
        if (spot.getEstimatedDurationMinutes() == null) {
            spot.setEstimatedDurationMinutes(60);
        }
        return spotRepository.save(spot);
    }

    /**
     * Xoá địa điểm du lịch theo ID
     */
    public void deleteSpot(Long id) {
        if (!spotRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy địa điểm du lịch với ID: " + id);
        }
        spotRepository.deleteById(id);
    }
}
