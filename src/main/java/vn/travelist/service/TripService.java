package vn.travelist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.travelist.dto.TripRequest;
import vn.travelist.dto.TripResponse;
import vn.travelist.model.Spot;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final SpotService spotService;
    private final ScoringService scoringService;
    private final RouteOptimizationService routeOptimizationService;

    public TripResponse generateItinerary(TripRequest request) {
        // 1. Tải toàn bộ địa điểm từ Database
        List<Spot> allSpots = spotService.searchSpots(null, null);
        if (allSpots.isEmpty()) {
            throw new RuntimeException("Cơ sở dữ liệu địa điểm đang rỗng. Vui lòng seed data trước!");
        }

        int daysCount  = (request.getDays()   != null && request.getDays()   > 0) ? request.getDays()   : 1;
        int people     = (request.getPeople() != null && request.getPeople() > 0) ? request.getPeople() : 1;
        String style   = "";

        // ── Điểm khởi hành tối ưu lộ trình ──────────────────────────────────────
        // Trung tâm phố cổ Hội An (tọa độ chuẩn)
        final double HOI_AN_LAT = 15.8771;
        final double HOI_AN_LNG = 108.3267;

        // Chỉ dùng GPS thực của người dùng nếu họ đang ở GẦN Hội An (trong bán kính 30km).
        // Nếu đang lên kế hoạch trước từ xa (Hà Nội, TP.HCM...) → dùng trung tâm Hội An
        // để tối ưu lộ trình theo địa lý nội vùng Hội An, không phải từ vị trí hiện tại.
        double userLat  = request.getCurrentLat()  != null ? request.getCurrentLat()  : HOI_AN_LAT;
        double userLng  = request.getCurrentLng()  != null ? request.getCurrentLng()  : HOI_AN_LNG;
        boolean isNearHoiAn = haversineKm(userLat, userLng, HOI_AN_LAT, HOI_AN_LNG) <= 30.0;
        double curLat = isNearHoiAn ? userLat : HOI_AN_LAT;
        double curLng = isNearHoiAn ? userLng : HOI_AN_LNG;

        // 2. Tính điểm và sắp xếp toàn bộ spots
        Map<Long, Double> scoreMap = new HashMap<>();
        for (Spot s : allSpots) {
            scoreMap.put(s.getId(), scoringService.calculateScore(s, request));
        }
        List<Spot> ranked = allSpots.stream()
            .sorted((a, b) -> Double.compare(scoreMap.get(b.getId()), scoreMap.get(a.getId())))
            .collect(Collectors.toList());

        // 3. Phân nhóm theo category (sau khi đã sort theo score)
        List<Spot> sightseeingPool = filterBy(ranked, "sightseeing");
        List<Spot> foodPool        = filterBy(ranked, "food");
        List<Spot> cafePool        = filterBy(ranked, "cafe");
        List<Spot> stayPool        = filterBy(ranked, "stay");

        // 4. Chọn chỗ nghỉ phù hợp nhất (dùng xuyên suốt chuyến đi)
        Spot staySpot = pickBestStay(stayPool, style, request);

        // 5. Số spots tham quan mỗi ngày = linh động theo độ dài chuyến đi
        int sightseeingPerDay = daysCount <= 2 ? 3 : (daysCount <= 4 ? 4 : 3);

        Set<Long> visited = new HashSet<>();
        if (staySpot != null) visited.add(staySpot.getId());

        List<TripResponse.DaySchedule> daySchedules = new ArrayList<>();
        int totalActivityCost = 0;

        for (int day = 1; day <= daysCount; day++) {
            List<TripResponse.ScheduledSpot> dailySlots = new ArrayList<>();

            // 1. Chọn bữa ăn sáng (Breakfast)
            Spot breakfastSpot = pickUnvisited(foodPool, visited, 1).stream().findFirst().orElse(
                foodPool.isEmpty() ? null : foodPool.get(0)
            );
            if (breakfastSpot != null) visited.add(breakfastSpot.getId());

            // 2. Chọn pool spots tham quan chưa ghé (đủ sightseeingPerDay spots)
            List<Spot> daySightseeing = pickUnvisited(sightseeingPool, visited, sightseeingPerDay);
            daySightseeing.forEach(s -> visited.add(s.getId()));

            // 3. Chọn bữa trưa (Lunch)
            Spot lunchSpot = pickUnvisited(foodPool, visited, 1).stream().findFirst().orElse(
                foodPool.isEmpty() ? null : foodPool.get(0)
            );
            if (lunchSpot != null) visited.add(lunchSpot.getId());

            // 4. Chọn cafe xế chiều (Afternoon tea/Cafe)
            Spot cafeSpot = pickUnvisited(cafePool, visited, 1).stream().findFirst().orElse(
                cafePool.isEmpty() ? null : cafePool.get(0)
            );
            if (cafeSpot != null) visited.add(cafeSpot.getId());

            // 5. Chọn bữa tối (Dinner)
            Spot dinnerSpot = pickUnvisited(foodPool, visited, 1).stream().findFirst().orElse(
                foodPool.isEmpty() ? null : foodPool.get(0)
            );
            if (dinnerSpot != null) visited.add(dinnerSpot.getId());

            // Tối ưu lộ trình tham quan theo Haversine Greedy
            List<Spot> optimized = routeOptimizationService.optimizeRoute(daySightseeing, curLat, curLng);

            // Phân bổ spots tham quan theo slot thời gian
            Spot morningSpot   = pickSlotSpot(optimized, 8,  0, 11,  0, "morning",   new HashSet<>());
            Spot afternoonSpot = pickSlotSpot(optimized, 13, 0, 14, 45,  "afternoon", morningSpot != null ? Set.of(morningSpot.getId()) : Set.of());
            Spot eveningSpot   = pickSlotSpot(optimized, 19, 30, 21, 30,  "evening",
                morningSpot != null && afternoonSpot != null
                    ? Set.of(morningSpot.getId(), afternoonSpot.getId())
                    : morningSpot != null ? Set.of(morningSpot.getId())
                    : afternoonSpot != null ? Set.of(afternoonSpot.getId())
                    : Set.of());

            // Fallback: nếu slot nào còn trống thì lấy spot còn lại
            List<Spot> unassigned = new ArrayList<>(optimized);
            if (morningSpot   != null) unassigned.remove(morningSpot);
            if (afternoonSpot != null) unassigned.remove(afternoonSpot);
            if (eveningSpot   != null) unassigned.remove(eveningSpot);

            if (morningSpot == null   && !unassigned.isEmpty()) { morningSpot   = unassigned.remove(0); }
            if (afternoonSpot == null && !unassigned.isEmpty()) { afternoonSpot = unassigned.remove(0); }
            if (eveningSpot == null   && !unassigned.isEmpty()) { eveningSpot   = unassigned.remove(0); }

            // Xây dựng timeline ngày 8 slot đồng bộ hoàn toàn
            if (breakfastSpot != null) {
                dailySlots.add(buildSlot("BREAKFAST", "07:00 - 07:30 (Ăn sáng)", breakfastSpot));
                totalActivityCost += getSpotCost(breakfastSpot);
            }
            if (morningSpot != null) {
                dailySlots.add(buildSlot("MORNING", "08:00 - 11:00 (Tham quan sáng)", morningSpot));
                totalActivityCost += getSpotCost(morningSpot);
            }
            if (lunchSpot != null) {
                dailySlots.add(buildSlot("LUNCH", "11:30 - 12:30 (Ăn trưa)", lunchSpot));
                totalActivityCost += getSpotCost(lunchSpot);
            }
            if (afternoonSpot != null) {
                dailySlots.add(buildSlot("AFTERNOON", "13:00 - 14:45 (Tham quan chiều)", afternoonSpot));
                totalActivityCost += getSpotCost(afternoonSpot);
            }
            if (cafeSpot != null) {
                dailySlots.add(buildSlot("AFTERNOON_TEA", "15:00 - 15:30 (Ăn xế)", cafeSpot));
                totalActivityCost += getSpotCost(cafeSpot);
            }
            if (dinnerSpot != null) {
                dailySlots.add(buildSlot("DINNER", "18:30 - 19:00 (Ăn tối)", dinnerSpot));
                totalActivityCost += getSpotCost(dinnerSpot);
            }
            if (eveningSpot != null) {
                dailySlots.add(buildSlot("EVENING", "19:30 - 21:30 (Vui chơi tối)", eveningSpot));
                totalActivityCost += getSpotCost(eveningSpot);
            }
            if (staySpot != null) {
                dailySlots.add(buildSlot("STAY", "22:00 (Nghỉ ngơi)", staySpot));
            }

            daySchedules.add(TripResponse.DaySchedule.builder().day(day).spots(dailySlots).build());

            // Tọa độ cuối ngày = điểm khởi hành ngày tiếp theo
            if (!dailySlots.isEmpty()) {
                Spot last = dailySlots.get(dailySlots.size() - 1).getSpot();
                if (last != null) { curLat = last.getLatitude(); curLng = last.getLongitude(); }
            }
        }

        // 6. Tính chi phí tổng thực tế
        int totalActivityCostTotal = totalActivityCost * people;
        int stayCostVal = staySpot != null ? getSpotCost(staySpot) : 300000;
        if (stayCostVal <= 0) stayCostVal = 300000;
        int stayEstimate = stayCostVal * people * daysCount;
        int transportEstimate = 100000 * people * daysCount;
        int totalCost = totalActivityCostTotal + stayEstimate + transportEstimate;

        return TripResponse.builder()
            .totalCost(totalCost)
            .activityCost(totalActivityCostTotal)
            .hotelEstimate(stayEstimate)
            .transportEstimate(transportEstimate)
            .days(daySchedules)
            .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    /** Công thức Haversine tính khoảng cách thực tế giữa 2 tọa độ (km) */
    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** Lọc spots theo category */
    private List<Spot> filterBy(List<Spot> spots, String category) {
        return spots.stream()
            .filter(s -> category.equalsIgnoreCase(s.getCategory()))
            .collect(Collectors.toList());
    }

    /** Lấy N spots chưa được ghé thăm từ một pool */
    private List<Spot> pickUnvisited(List<Spot> pool, Set<Long> visited, int limit) {
        return pool.stream()
            .filter(s -> !visited.contains(s.getId()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private int getSpotCost(Spot s) {
        if (s == null) return 0;
        Integer max = s.getMaxCost();
        if (max != null) return max;
        Integer avg = s.getAverageCost();
        return avg != null ? avg : 0;
    }

    /** Chọn chỗ nghỉ phù hợp nhất theo style */
    private Spot pickBestStay(List<Spot> stayPool, String style, TripRequest request) {
        if (stayPool.isEmpty()) return null;
        // Healing/Romantic → chọn chỗ có crowd_level = low
        if ("healing".equals(style) || "romantic".equals(style)) {
            return stayPool.stream()
                .filter(s -> "low".equalsIgnoreCase(s.getCrowdLevel()))
                .findFirst()
                .orElse(stayPool.get(0));
        }
        // Adventure → không cần luxury, chọn giá hợp lý nhất
        if ("adventure".equals(style) || "explorer".equals(style)) {
            return stayPool.stream()
                .filter(s -> getSpotCost(s) < 1000000)
                .findFirst()
                .orElse(stayPool.get(0));
        }
        return stayPool.get(0);
    }

    /**
     * Chọn spot phù hợp nhất cho 1 slot giờ cụ thể từ danh sách candidates.
     * Ưu tiên: (1) mở cửa đúng giờ + preferred timeOfDay, (2) chỉ mở cửa, (3) bất kỳ
     */
    private Spot pickSlotSpot(List<Spot> candidates, int startH, int startM,
                               int endH, int endM, String preferredTod, Set<Long> usedInDay) {
        LocalTime slotStart = LocalTime.of(startH, startM);
        LocalTime slotEnd   = LocalTime.of(endH,   endM);

        // Pass 1: mở cửa đúng giờ + preferred timeOfDay
        for (Spot s : candidates) {
            if (usedInDay.contains(s.getId())) continue;
            if (isOpen(s, slotStart, slotEnd) && hasTod(s, preferredTod)) return s;
        }
        // Pass 2: chỉ cần mở cửa
        for (Spot s : candidates) {
            if (usedInDay.contains(s.getId())) continue;
            if (isOpen(s, slotStart, slotEnd)) return s;
        }
        // Pass 3: bất kỳ spot chưa dùng
        for (Spot s : candidates) {
            if (!usedInDay.contains(s.getId())) return s;
        }
        return null;
    }

    private boolean isOpen(Spot s, LocalTime start, LocalTime end) {
        boolean okOpen  = s.getOpeningTime()  == null || !s.getOpeningTime().isAfter(start);
        boolean okClose = s.getClosingTime() == null || !s.getClosingTime().isBefore(end);
        return okOpen && okClose;
    }

    private boolean hasTod(Spot s, String tod) {
        return s.getTimeOfDay() != null && s.getTimeOfDay().toLowerCase().contains(tod.toLowerCase());
    }

    private TripResponse.ScheduledSpot buildSlot(String slot, String time, Spot spot) {
        return TripResponse.ScheduledSpot.builder().slot(slot).time(time).spot(spot).build();
    }

    private String timeRange(int h, int m, int dur, String label) {
        LocalTime start = LocalTime.of(h, m);
        LocalTime end   = start.plusMinutes(dur);
        return String.format("%02d:%02d - %02d:%02d (%s, ~%d phút)",
            start.getHour(), start.getMinute(), end.getHour(), end.getMinute(), label, dur);
    }

    private int orDefault(Integer val, int def) {
        return val != null ? val : def;
    }
}
