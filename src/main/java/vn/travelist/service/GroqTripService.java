package vn.travelist.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.travelist.dto.TripRequest;
import vn.travelist.dto.TripResponse;
import vn.travelist.model.Spot;
import vn.travelist.repository.SpotRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * GemAI Trip Planner sử dụng Groq API (LLaMA 3.3-70B) để sinh lịch trình du lịch
 * thông minh hơn rule-based scoring, hiểu ngữ cảnh tiếng Việt và sở thích phức hợp.
 *
 * Fallback tự động sang TripService (rule-based) nếu:
 *   - GROQ_API_KEY chưa được cấu hình
 *   - API Groq gặp lỗi/timeout
 *   - Parse JSON response thất bại
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroqTripService {

    private final SpotRepository spotRepository;
    private final TripService fallbackTripService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${groq.api-key:}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    @Value("${groq.api-url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    public TripResponse generateItinerary(TripRequest request) {
        // Nếu chưa có API key → dùng rule-based
        if (groqApiKey == null || groqApiKey.isBlank() || groqApiKey.equals("your_groq_api_key_here")) {
            log.warn("[GroqTripService] GROQ_API_KEY chưa được cấu hình. Chuyển sang rule-based fallback.");
            return fallbackTripService.generateItinerary(request);
        }

        try {
            List<Spot> allSpots = spotRepository.findAll();
            if (allSpots.isEmpty()) {
                throw new RuntimeException("Database spots trống. Vui lòng seed data trước!");
            }

            // 1. Build prompt
            String prompt = buildPrompt(request, allSpots);
            log.info("[GroqTripService] Gửi yêu cầu Groq AI cho lịch trình {} ngày, phong cách: {}",
                request.getDays(), request.getStyle());

            // 2. Gọi Groq API
            String jsonResponse = callGroqApi(prompt);

            // 3. Parse JSON response → TripResponse
            TripResponse result = parseGroqResponse(jsonResponse, allSpots, request);
            result.setAiPowered(true);
            result.setAiEngine("Groq " + groqModel);
            log.info("[GroqTripService] Sinh lịch trình AI thành công!");
            return result;

        } catch (Exception e) {
            log.error("[GroqTripService] Lỗi khi gọi Groq API: {}. Chuyển sang rule-based fallback.", e.getMessage());
            TripResponse fallback = fallbackTripService.generateItinerary(request);
            fallback.setAiPowered(false);
            fallback.setAiEngine("Rule-based Scoring");
            return fallback;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUILD PROMPT
    // ─────────────────────────────────────────────────────────────────────────

    private String buildPrompt(TripRequest request, List<Spot> spots) {
        // Tóm gọn spots thành JSON ngắn (chỉ giữ trường cần thiết để tối ưu token)
        List<Map<String, Object>> spotsData = spots.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("name_vi", s.getNameVi());
            m.put("name_en", s.getNameEn());
            m.put("category", s.getCategory()); // sightseeing, food, cafe, stay
            m.put("tags", s.getTags());
            m.put("rating", s.getRating());
            m.put("cost", s.getMaxCost() != null ? s.getMaxCost() : (s.getAverageCost() != null ? s.getAverageCost() : 0));
            m.put("duration_min", s.getEstimatedDurationMinutes() != null ? s.getEstimatedDurationMinutes() : 60);
            m.put("suitable_for", s.getSuitableFor());
            m.put("time_of_day", s.getTimeOfDay()); // morning, afternoon, evening
            m.put("crowd", s.getCrowdLevel()); // low, medium, high
            m.put("desc_vi", s.getDescriptionVi() != null
                ? s.getDescriptionVi().substring(0, Math.min(80, s.getDescriptionVi().length()))
                : "");
            return m;
        }).collect(Collectors.toList());

        String spotsJson;
        try {
            spotsJson = objectMapper.writeValueAsString(spotsData);
        } catch (Exception e) {
            spotsJson = "[]";
        }

        int days    = request.getDays()    != null ? request.getDays()    : 2;
        int people  = request.getPeople()  != null ? request.getPeople()  : 2;
        int budget  = request.getBudget()  != null ? request.getBudget()  : 5000000;
        String style     = request.getStyle()     != null ? request.getStyle()     : "Khám phá";
        String groupType = request.getGroupType() != null ? request.getGroupType() : "couple";
        String interests = request.getInterests() != null
            ? String.join(", ", request.getInterests()) : "tham quan, ẩm thực";

        // Slot thứ tự yêu cầu cho mỗi ngày
        String slotFormat = """
            [
              {"slot": "MORNING",   "time": "08:00 - 09:30 (Tham quan buổi sáng)", "spot_id": <id>},
              {"slot": "CAFE",      "time": "10:00 - 11:00 (Cà phê & Chill)",       "spot_id": <id>},
              {"slot": "LUNCH",     "time": "12:00 - 13:00 (Ăn trưa)",             "spot_id": <id>},
              {"slot": "AFTERNOON", "time": "14:00 - 16:00 (Buổi chiều)",           "spot_id": <id>},
              {"slot": "EVENING",   "time": "18:00 - 20:00 (Buổi tối)",            "spot_id": <id>},
              {"slot": "STAY",      "time": "21:00 (Nghỉ ngơi)",                   "spot_id": <id>}
            ]""";

        return String.format("""
            Bạn là AI chuyên gia lập lịch trình du lịch Hội An, Việt Nam.

            THÔNG TIN CHUYẾN ĐI:
            - Số ngày: %d ngày
            - Ngân sách tổng: %,d VND cho %d người
            - Phong cách: %s
            - Sở thích cá nhân: %s
            - Loại nhóm: %s

            DANH SÁCH ĐỊA ĐIỂM TẠI HỘI AN (JSON):
            %s

            YÊU CẦU:
            Tạo lịch trình %d ngày hoàn chỉnh. Trả về JSON THUẦN TÚY (không markdown, không giải thích thêm) theo format:
            {
              "days": [
                {
                  "day": 1,
                  "spots": %s
                }
              ],
              "total_cost": <tổng chi phí ước tính VND>,
              "activity_cost": <chi phí hoạt động>,
              "hotel_estimate": <ước tính lưu trú/đêm x số ngày>,
              "transport_estimate": <ước tính di chuyển>
            }

            NGUYÊN TẮC QUAN TRỌNG:
            1. Chỉ dùng spot_id từ danh sách đã cung cấp, KHÔNG tự bịa spot mới
            2. Ưu tiên spots phù hợp sở thích "%s" và phong cách "%s"
            3. KHÔNG lặp lại cùng 1 spot trong nhiều ngày (ngoại trừ STAY)
            4. Slot STAY dùng chung 1 khách sạn/homestay xuyên suốt (cùng spot_id)
            5. Slot CAFE: chọn spot có category="cafe"
            6. Slot LUNCH/MORNING/AFTERNOON: chọn spot phù hợp time_of_day
            7. Slot EVENING: chọn spot có time_of_day chứa "evening"
            8. Ước tính chi phí dựa trên trường "cost" của từng spot × số người
            9. Tổng chi phí hoạt động không vượt quá %,d VND
            10. Nếu người dùng thích "Biển" hay "Beach" → ưu tiên spot có tags chứa "beach", "sea", "biển"
            """,
            days, budget, people, style, interests, groupType,
            spotsJson,
            days, slotFormat,
            interests, style,
            budget
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CALL GROQ API
    // ─────────────────────────────────────────────────────────────────────────

    private String callGroqApi(String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", groqModel);
        body.put("temperature", 0.3); // Thấp → JSON ổn định hơn
        body.put("max_tokens", 4096);
        body.put("response_format", Map.of("type", "json_object")); // Force JSON output

        body.put("messages", List.of(
            Map.of("role", "system",
                   "content", "Bạn là AI chuyên gia du lịch Hội An. Luôn trả về JSON hợp lệ, không markdown."),
            Map.of("role", "user", "content", userPrompt)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(groqApiUrl, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Groq API trả về lỗi: " + response.getStatusCode());
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Không thể parse Groq API response: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PARSE GROQ RESPONSE → TripResponse
    // ─────────────────────────────────────────────────────────────────────────

    private TripResponse parseGroqResponse(String jsonStr, List<Spot> allSpots, TripRequest request) throws Exception {
        // Build lookup map: id → Spot
        Map<Long, Spot> spotMap = allSpots.stream()
            .collect(Collectors.toMap(Spot::getId, s -> s));

        JsonNode root = objectMapper.readTree(jsonStr);

        // Parse days
        List<TripResponse.DaySchedule> daySchedules = new ArrayList<>();
        JsonNode daysNode = root.path("days");

        for (JsonNode dayNode : daysNode) {
            int dayNum = dayNode.path("day").asInt(1);
            List<TripResponse.ScheduledSpot> scheduledSpots = new ArrayList<>();

            for (JsonNode slotNode : dayNode.path("spots")) {
                String slot   = slotNode.path("slot").asText("MORNING");
                String time   = slotNode.path("time").asText("");
                long spotId   = slotNode.path("spot_id").asLong(-1);

                Spot spot = spotMap.get(spotId);
                if (spot == null) {
                    log.warn("[GroqTripService] Groq trả về spot_id={} không tồn tại, bỏ qua.", spotId);
                    continue;
                }

                scheduledSpots.add(TripResponse.ScheduledSpot.builder()
                    .slot(slot)
                    .time(time)
                    .spot(spot)
                    .build());
            }

            daySchedules.add(TripResponse.DaySchedule.builder()
                .day(dayNum)
                .spots(scheduledSpots)
                .build());
        }

        // Parse costs (fallback nếu Groq không trả về)
        int totalCost     = root.path("total_cost").asInt(0);
        int activityCost  = root.path("activity_cost").asInt(0);
        int hotelEstimate = root.path("hotel_estimate").asInt(0);
        int transportEst  = root.path("transport_estimate").asInt(0);

        // Fallback cost nếu AI không ước tính
        if (totalCost == 0) {
            int people = request.getPeople() != null ? request.getPeople() : 1;
            int days   = request.getDays()   != null ? request.getDays()   : 1;
            transportEst  = 100000 * people * days;
            hotelEstimate = 300000 * people * days;
            activityCost = daySchedules.stream()
                .flatMap(d -> d.getSpots().stream())
                .filter(s -> !"STAY".equals(s.getSlot()))
                .mapToInt(s -> {
                    Integer max = s.getSpot().getMaxCost();
                    Integer avg = s.getSpot().getAverageCost();
                    return (max != null ? max : (avg != null ? avg : 0)) * people;
                })
                .sum();
            totalCost = activityCost + hotelEstimate + transportEst;
        }

        return TripResponse.builder()
            .totalCost(totalCost)
            .activityCost(activityCost)
            .hotelEstimate(hotelEstimate)
            .transportEstimate(transportEst)
            .days(daySchedules)
            .build();
    }
}
