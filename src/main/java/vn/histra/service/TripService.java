package vn.histra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.histra.dto.TripRequest;
import vn.histra.dto.TripResponse;
import vn.histra.model.Spot;
import vn.histra.repository.SpotRepository;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final SpotRepository spotRepository;
    private final ScoringService scoringService;
    private final RouteOptimizationService routeOptimizationService;

    public TripResponse generateItinerary(TripRequest request) {
        // 1. Tải toàn bộ danh sách điểm đến từ Database Neon
        List<Spot> allSpots = spotRepository.findAll();

        if (allSpots.isEmpty()) {
            throw new RuntimeException("Cơ sở dữ liệu địa điểm (spots) hiện đang rỗng. Vui lòng seed data trước!");
        }

        // 2. Tính điểm độ phù hợp cho từng Spot
        Map<Spot, Double> scoredSpotsMap = new HashMap<>();
        for (Spot spot : allSpots) {
            double score = scoringService.calculateScore(spot, request);
            scoredSpotsMap.put(spot, score);
        }

        // 3. Sắp xếp danh sách giảm dần theo điểm số
        List<Spot> sortedSpots = allSpots.stream()
                .sorted((s1, s2) -> Double.compare(scoredSpotsMap.get(s2), scoredSpotsMap.get(s1)))
                .collect(Collectors.toList());

        // Phân nhóm địa điểm ăn uống (food) để làm điểm ăn trưa bắt buộc
        List<Spot> foodSpots = sortedSpots.stream()
                .filter(s -> "food".equalsIgnoreCase(s.getCategory()))
                .collect(Collectors.toList());

        // Các địa điểm tham quan/cafe còn lại
        List<Spot> nonFoodSpots = sortedSpots.stream()
                .filter(s -> !"food".equalsIgnoreCase(s.getCategory()) && !"stay".equalsIgnoreCase(s.getCategory()))
                .collect(Collectors.toList());

        // Địa điểm lưu trú
        Spot staySpot = sortedSpots.stream()
                .filter(s -> "stay".equalsIgnoreCase(s.getCategory()))
                .findFirst()
                .orElse(null);

        List<TripResponse.DaySchedule> daySchedules = new ArrayList<>();
        Set<Long> visitedSpotIds = new HashSet<>();
        
        int daysCount = (request.getDays() != null && request.getDays() > 0) ? request.getDays() : 1;
        double currentLat = request.getCurrentLat() != null ? request.getCurrentLat() : 15.8821;
        double currentLng = request.getCurrentLng() != null ? request.getCurrentLng() : 108.3371;

        int totalActivityCostPerPerson = 0;

        for (int d = 1; d <= daysCount; d++) {
            List<TripResponse.ScheduledSpot> dailySpots = new ArrayList<>();
            List<Spot> candidatesForDay = new ArrayList<>();

            // A. Điểm ăn trưa 12h00 bắt buộc (không lặp lại điểm cũ nếu có thể)
            Spot lunchSpot = foodSpots.stream()
                    .filter(s -> !visitedSpotIds.contains(s.getId()))
                    .findFirst()
                    .orElse(!foodSpots.isEmpty() ? foodSpots.get(0) : null);

            if (lunchSpot != null) {
                visitedSpotIds.add(lunchSpot.getId());
            }

            // B. Chọn 3 điểm tham quan có điểm số cao nhất chưa ghé qua
            List<Spot> availableSightseeing = nonFoodSpots.stream()
                    .filter(s -> !visitedSpotIds.contains(s.getId()))
                    .limit(3)
                    .collect(Collectors.toList());

            candidatesForDay.addAll(availableSightseeing);
            
            // Đánh dấu đã tham quan các điểm này
            for (Spot s : availableSightseeing) {
                visitedSpotIds.add(s.getId());
            }

            // C. Tối ưu thứ tự đi của 3 điểm tham quan trong ngày hôm đó bằng thuật toán Haversine Greedy
            List<Spot> optimizedSightseeing = routeOptimizationService.optimizeRoute(candidatesForDay, currentLat, currentLng);

            int sightseeingIndex = 0;

            // 1. Phân bổ Buổi Sáng (08:00)
            if (sightseeingIndex < optimizedSightseeing.size()) {
                Spot morningSpot = optimizedSightseeing.get(sightseeingIndex++);
                dailySpots.add(TripResponse.ScheduledSpot.builder()
                        .slot("MORNING")
                        .time("08:00")
                        .spot(morningSpot)
                        .build());
                totalActivityCostPerPerson += morningSpot.getAverageCost() != null ? morningSpot.getAverageCost() : 0;
            }

            // 2. Phân bổ Buổi Trưa (12:00) - Ăn đặc sản
            if (lunchSpot != null) {
                dailySpots.add(TripResponse.ScheduledSpot.builder()
                        .slot("LUNCH")
                        .time("12:00")
                        .spot(lunchSpot)
                        .build());
                totalActivityCostPerPerson += lunchSpot.getAverageCost() != null ? lunchSpot.getAverageCost() : 0;
            }

            // 3. Phân bổ Buổi Chiều (15:00)
            if (sightseeingIndex < optimizedSightseeing.size()) {
                Spot afternoonSpot = optimizedSightseeing.get(sightseeingIndex++);
                dailySpots.add(TripResponse.ScheduledSpot.builder()
                        .slot("AFTERNOON")
                        .time("15:00")
                        .spot(afternoonSpot)
                        .build());
                totalActivityCostPerPerson += afternoonSpot.getAverageCost() != null ? afternoonSpot.getAverageCost() : 0;
            }

            // 4. Phân bổ Buổi Tối (19:00)
            if (sightseeingIndex < optimizedSightseeing.size()) {
                Spot eveningSpot = optimizedSightseeing.get(sightseeingIndex++);
                dailySpots.add(TripResponse.ScheduledSpot.builder()
                        .slot("EVENING")
                        .time("19:00")
                        .spot(eveningSpot)
                        .build());
                totalActivityCostPerPerson += eveningSpot.getAverageCost() != null ? eveningSpot.getAverageCost() : 0;
            }

            // 5. Khách sạn nghỉ ngơi buổi tối (21:00)
            if (staySpot != null) {
                dailySpots.add(TripResponse.ScheduledSpot.builder()
                        .slot("STAY")
                        .time("21:00")
                        .spot(staySpot)
                        .build());
            }

            daySchedules.add(TripResponse.DaySchedule.builder()
                    .day(d)
                    .spots(dailySpots)
                    .build());

            // Tọa độ của điểm đến cuối cùng trong ngày sẽ trở thành điểm khởi hành cho ngày hôm sau
            if (!dailySpots.isEmpty()) {
                Spot lastSpot = dailySpots.get(dailySpots.size() - 1).getSpot();
                if (lastSpot != null) {
                    currentLat = lastSpot.getLatitude();
                    currentLng = lastSpot.getLongitude();
                }
            }
        }

        // 4. TÍNH TOÁN VÀ VALIDATE CHI PHÍ TỔNG ĐỒNG BỘ
        int people = (request.getPeople() != null && request.getPeople() > 0) ? request.getPeople() : 1;
        int totalActivityCost = totalActivityCostPerPerson * people;
        
        // Định mức lưu trú homestay: 300.000 VND / người / ngày
        int hotelEstimate = 300000 * people * daysCount;
        
        // Định mức chi phí xăng xe di chuyển: 100.000 VND / người / ngày
        int transportEstimate = 100000 * people * daysCount;
        
        int totalCost = totalActivityCost + hotelEstimate + transportEstimate;

        return TripResponse.builder()
                .totalCost(totalCost)
                .activityCost(totalActivityCost)
                .hotelEstimate(hotelEstimate)
                .transportEstimate(transportEstimate)
                .days(daySchedules)
                .build();
    }
}
