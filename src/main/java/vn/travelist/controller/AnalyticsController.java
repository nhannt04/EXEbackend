package vn.travelist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.travelist.dto.ApiResponse;
import vn.travelist.dto.AnalyticsTrackRequest;
import vn.travelist.model.TrackingLog;
import vn.travelist.repository.*;
import vn.travelist.model.*;
import vn.travelist.security.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final TrackingLogRepository trackingLogRepository;
    private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;
    private final DiaryRepository diaryRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CafeRepository cafeRepository;
    private final DishRepository dishRepository;
    private final StayRepository stayRepository;
    private final EntertainmentRepository entertainmentRepository;

    @PostMapping("/track")
    public ResponseEntity<ApiResponse<String>> trackEvent(
            @RequestBody AnalyticsTrackRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = null;
            String token = getTokenFromRequest(httpRequest);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                userId = jwtTokenProvider.getUserIdFromJWT(token);
            }

            TrackingLog log = TrackingLog.builder()
                .eventType(request.getEventType())
                .targetId(request.getTargetId())
                .userId(userId)
                .build();
            
            trackingLogRepository.save(log);
            return ResponseEntity.ok(ApiResponse.success("Tracked", "OK"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage(), "ERROR"));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        List<User> users = userRepository.findAll();
        List<Itinerary> itineraries = itineraryRepository.findAll();
        List<Diary> diaries = diaryRepository.findAll();
        
        // 1. Line Chart: Tài khoản đăng ký theo tháng
        Map<String, Long> usersByMonth = users.stream()
            .filter(u -> u.getCreatedAt() != null)
            .collect(Collectors.groupingBy(
                u -> u.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.counting()
            ));
        data.put("lineChart", formatSeries(usersByMonth));

        // 2. Area Chart: Lượt truy cập web theo ngày (từ TrackingLog)
        List<Object[]> pageViews = trackingLogRepository.countEventsByDay("PAGE_VIEW");
        Map<String, Long> pageViewsMap = new LinkedHashMap<>();
        for (Object[] row : pageViews) {
            pageViewsMap.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        data.put("areaChart", formatSeries(pageViewsMap));

        // 3. Bar Chart: Tổng lịch trình theo tháng
        Map<String, Long> itinerariesByMonth = itineraries.stream()
            .filter(i -> i.getCreatedAt() != null)
            .collect(Collectors.groupingBy(
                i -> i.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.counting()
            ));
        data.put("barChart", formatSeries(itinerariesByMonth));

        // 4. Column Chart: Số lượng bài đăng theo tháng
        Map<String, Long> diariesByMonth = diaries.stream()
            .filter(d -> d.getCreatedAt() != null)
            .collect(Collectors.groupingBy(
                d -> d.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.counting()
            ));
        data.put("columnChart", formatSeries(diariesByMonth));

        // 5. Grouped Bar Chart: Món ăn vs Chỗ ở vs Vui chơi (Dummy data if not available in itinerary)
        data.put("groupedBarChart", Map.of(
            "categories", List.of("T1", "T2", "T3", "T4", "T5", "T6"),
            "series", List.of(
                Map.of("name", "Món ăn", "data", List.of(44, 55, 41, 67, 22, 43)),
                Map.of("name", "Chỗ ở", "data", List.of(13, 23, 20, 8, 13, 27)),
                Map.of("name", "Vui chơi", "data", List.of(11, 17, 15, 15, 21, 14))
            )
        ));

        // 6. Stacked Column Chart: Bài đăng theo category
        Map<String, Map<String, Long>> diaryStack = diaries.stream()
            .filter(d -> d.getCategory() != null && d.getCreatedAt() != null)
            .collect(Collectors.groupingBy(
                d -> d.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.groupingBy(Diary::getCategory, Collectors.counting())
            ));
        data.put("stackedChart", buildStackedSeries(diaryStack));

        // 7. Bullet Chart: Budget target vs actual
        double avgBudget = itineraries.stream().mapToDouble(i -> i.getTotalBudget() != null ? i.getTotalBudget() : 5000000.0).average().orElse(5000000.0);
        data.put("bulletChart", Map.of(
            "title", "Ngân sách",
            "ranges", List.of(3000000, 5000000, 10000000),
            "measures", List.of(avgBudget),
            "markers", List.of(5000000)
        ));

        // 8. Gauge Chart: Tỉ lệ người dùng có lịch trình
        long usersWithItinerary = users.stream()
            .filter(u -> itineraries.stream().anyMatch(i -> i.getUser() != null && i.getUser().getId().equals(u.getId())))
            .count();
        double gaugeValue = users.isEmpty() ? 0 : (double) usersWithItinerary / users.size() * 100;
        data.put("gaugeChart", Math.round(gaugeValue));

        // 9. Progress Bar: Tiến độ bài đăng tháng này
        data.put("progressBar", Math.min(100, diaries.size() * 2)); // example

        // 10. Gantt Chart: dummy
        data.put("ganttChart", List.of(
            Map.of("x", "Ăn sáng", "y", List.of(1620000000000L, 1620003600000L)),
            Map.of("x", "Vui chơi", "y", List.of(1620003600000L, 1620014400000L))
        ));

        // 11. Burndown Chart: User adoption
        data.put("burndownChart", List.of(100, 80, 50, 30, 10, 0));

        // 12. Burnup Chart: Total Spots added
        long totalSpots = cafeRepository.count() + stayRepository.count() + dishRepository.count() + entertainmentRepository.count();
        data.put("burnupChart", List.of(0, 10, 30, 50, 80, totalSpots));

        // 13. Pie Chart: Đăng nhập Google vs Local (Assume password null means Google)
        long googleUsers = users.stream().filter(u -> u.getPasswordHash() == null || u.getPasswordHash().isEmpty()).count();
        long localUsers = users.size() - googleUsers;
        data.put("pieChart", Map.of("labels", List.of("Google", "Email"), "series", List.of(googleUsers, localUsers)));

        // 14. Donut Chart: Phân bổ Spots
        data.put("donutChart", Map.of(
            "labels", List.of("Cà phê", "Chỗ ở", "Món ăn", "Vui chơi"),
            "series", List.of(cafeRepository.count(), stayRepository.count(), dishRepository.count(), entertainmentRepository.count())
        ));

        // 15. Treemap:
        data.put("treemap", List.of(
            Map.of("x", "Hội An", "y", 218),
            Map.of("x", "Đà Nẵng", "y", 149),
            Map.of("x", "Huế", "y", 184)
        ));

        // 16. Scatter Plot: Budget vs Days
        List<List<Number>> scatter = itineraries.stream()
            .filter(i -> i.getTotalBudget() != null && i.getTotalDays() != null)
            .map(i -> List.<Number>of(i.getTotalDays(), i.getTotalBudget()))
            .collect(Collectors.toList());
        data.put("scatterPlot", scatter);

        // 17. Bubble Chart: Rating vs Price vs Views (Cafes)
        List<List<Double>> bubbles = cafeRepository.findAll().stream()
            .map(c -> List.of(
                4.5, // Mock rating as Cafe has no rating field
                c.getMinPrice() != null ? c.getMinPrice().doubleValue() : 0.0,
                c.getMaxPrice() != null ? (c.getMaxPrice() / 1000.0) : 50.0 // z-size
            ))
            .collect(Collectors.toList());
        data.put("bubbleChart", bubbles);

        // 18. Heatmap: Access by hour (from TrackingLog)
        List<Object[]> hourly = trackingLogRepository.countEventsByHour();
        List<Map<String, Object>> heatmap = new ArrayList<>();
        Map<String, Object> hmRow = new HashMap<>();
        hmRow.put("name", "Truy cập");
        List<Map<String, Object>> hmData = new ArrayList<>();
        for (Object[] row : hourly) {
            hmData.add(Map.of("x", "H" + row[0].toString(), "y", row[1]));
        }
        hmRow.put("data", hmData);
        heatmap.add(hmRow);
        data.put("heatmap", heatmap);

        // Add category statistics for tabs (User, Dish, Cafe, Entertainment, Stay)
        Map<String, Object> categoryStats = new HashMap<>();
        // User uses actual data
        categoryStats.put("user", formatSeries(usersByMonth));
        // Mock data for others using their totals
        categoryStats.put("dish", generateMockMonthlyData(dishRepository.count()));
        categoryStats.put("cafe", generateMockMonthlyData(cafeRepository.count()));
        categoryStats.put("entertainment", generateMockMonthlyData(entertainmentRepository.count()));
        categoryStats.put("stay", generateMockMonthlyData(stayRepository.count()));
        
        data.put("categoryStats", categoryStats);

        return ResponseEntity.ok(ApiResponse.success(data, "Success"));
    }

    private Map<String, Object> generateMockMonthlyData(long totalCount) {
        List<String> labels = new ArrayList<>();
        List<Long> series = new ArrayList<>();
        long remaining = totalCount;
        int currentYear = java.time.LocalDate.now().getYear();
        for (int i = 1; i <= 12; i++) {
            labels.add(String.format("%04d-%02d", currentYear, i));
            if (i == 12) {
                series.add(remaining);
            } else {
                long val = (totalCount / 12) + (i % 3 == 0 ? 2 : (i % 2 == 0 ? -1 : 1));
                if (val < 0) val = 0;
                if (val > remaining) val = remaining;
                series.add(val);
                remaining -= val;
            }
        }
        return Map.of("labels", labels, "series", series);
    }

    private Map<String, Object> formatSeries(Map<String, Long> map) {
        List<String> labels = new ArrayList<>(map.keySet());
        Collections.sort(labels);
        List<Long> data = labels.stream().map(map::get).collect(Collectors.toList());
        return Map.of("labels", labels, "series", data);
    }

    private Map<String, Object> buildStackedSeries(Map<String, Map<String, Long>> stack) {
        List<String> labels = new ArrayList<>(stack.keySet());
        Collections.sort(labels);
        
        Set<String> allCategories = new HashSet<>();
        for (Map<String, Long> cats : stack.values()) {
            allCategories.addAll(cats.keySet());
        }

        List<Map<String, Object>> series = new ArrayList<>();
        for (String cat : allCategories) {
            List<Long> data = new ArrayList<>();
            for (String label : labels) {
                data.add(stack.get(label).getOrDefault(cat, 0L));
            }
            series.add(Map.of("name", cat, "data", data));
        }

        return Map.of("labels", labels, "series", series);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
