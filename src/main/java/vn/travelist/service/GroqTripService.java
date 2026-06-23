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
import vn.travelist.repository.*;
import vn.travelist.model.*;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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

    private final TripService fallbackTripService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    private final DishRepository dishRepository;
    private final CafeRepository cafeRepository;
    private final StayRepository stayRepository;
    private final EntertainmentRepository entertainmentRepository;
    private final RentalRepository rentalRepository;

    @Value("${gemini.api-key:${GEMINI_API_KEY:}}")
    private String geminiApiKey;

    private final String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    @org.springframework.cache.annotation.Cacheable(value = "generatedItineraries", key = "#request.toString()")
    public TripResponse generateItinerary(TripRequest request) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.warn("[GroqTripService] GEMINI_API_KEY chưa được cấu hình. Chuyển sang rule-based fallback.");
            return fallbackTripService.generateItinerary(request);
        }

        try {
            List<Spot> allSpots = new ArrayList<>();

            // 1. Map Stays
            try {
                stayRepository.findAll().forEach(stay -> {
                    // Chỉ thêm nếu có hình ảnh
                    if (stay.getImageUrl() == null || stay.getImageUrl().isBlank()) {
                        log.debug("[GroqTripService] Bỏ qua Stay {} - không có hình ảnh", stay.getName());
                        return;
                    }

                    Spot spot = Spot.builder()
                        .id(100000L + stay.getId())
                        .nameVi(stay.getName())
                        .nameEn(stay.getName())
                        .category("stay")
                        .address(stay.getAddress())
                        .tags("khách sạn, homestay, lưu trú, nghỉ dưỡng, stay, " + stay.getType())
                        .latitude(stay.getLatitude() != null ? stay.getLatitude() : 15.8801)
                        .longitude(stay.getLongitude() != null ? stay.getLongitude() : 108.3380)
                        .minCost(stay.getMinPrice())
                        .maxCost(stay.getMaxPrice())
                        .averageCost(stay.getMinPrice() != null && stay.getMaxPrice() != null ? (stay.getMinPrice() + stay.getMaxPrice()) / 2 : 0)
                        .estimatedDurationMinutes(480)
                        .openingTime(LocalTime.of(0, 0))
                        .closingTime(LocalTime.of(23, 59))
                        .crowdLevel("low")
                        .rating(4.9)
                        .suitableFor("couple, family, group, solo")
                        .timeOfDay("evening")
                        .descriptionVi("Nơi lưu trú cao cấp: " + stay.getName() + " (" + stay.getType() + "). Sức chứa: " + stay.getCapacity() + ". Địa chỉ: " + stay.getAddress() + ". Ghi chú: " + stay.getNotes())
                        .descriptionEn("Premium accommodation: " + stay.getName() + " (" + stay.getType() + "). Capacity: " + stay.getCapacity() + ". Address: " + stay.getAddress() + ". Notes: " + stay.getNotes())
                        .build();

                    SpotImage img = SpotImage.builder().imageUrl(stay.getImageUrl()).build();
                    spot.setImages(new ArrayList<>(List.of(img)));
                    allSpots.add(spot);
                });
            } catch (Exception e) {
                log.error("[GroqTripService] Lỗi load stays: {}", e.getMessage());
            }

            // 2. Map Cafes
            try {
                cafeRepository.findAll().forEach(cafe -> {
                    // Chỉ thêm nếu có hình ảnh
                    if (cafe.getImageUrl() == null || cafe.getImageUrl().isBlank()) {
                        log.debug("[GroqTripService] Bỏ qua Cafe {} - không có hình ảnh", cafe.getName());
                        return;
                    }

                    Spot spot = Spot.builder()
                        .id(200000L + cafe.getId())
                        .nameVi(cafe.getName())
                        .nameEn(cafe.getName())
                        .category("cafe")
                        .address(cafe.getAddress())
                        .tags("cafe, cà phê, chill, nước uống" + (cafe.getStyle() != null ? ", " + cafe.getStyle() : ""))
                        .latitude(cafe.getLatitude() != null ? cafe.getLatitude() : 15.8801)
                        .longitude(cafe.getLongitude() != null ? cafe.getLongitude() : 108.3380)
                        .minCost(cafe.getMinPrice())
                        .maxCost(cafe.getMaxPrice())
                        .averageCost(cafe.getMinPrice() != null && cafe.getMaxPrice() != null ? (cafe.getMinPrice() + cafe.getMaxPrice()) / 2 : 0)
                        .estimatedDurationMinutes(60)
                        .openingTime(cafe.getOpeningTime() != null ? cafe.getOpeningTime() : LocalTime.of(7, 0))
                        .closingTime(cafe.getClosingTime() != null ? cafe.getClosingTime() : LocalTime.of(22, 0))
                        .crowdLevel("medium")
                        .rating(4.7)
                        .suitableFor("couple, family, group, solo")
                        .timeOfDay("morning, afternoon, evening")
                        .descriptionVi("Quán cà phê chill Hội An: " + cafe.getName() + " phong cách " + cafe.getStyle() + ". Địa chỉ: " + cafe.getAddress())
                        .descriptionEn("Chill Hoi An cafe: " + cafe.getName() + " styled as " + cafe.getStyle() + ". Address: " + cafe.getAddress())
                        .build();

                    SpotImage img = SpotImage.builder().imageUrl(cafe.getImageUrl()).build();
                    spot.setImages(new ArrayList<>(List.of(img)));
                    allSpots.add(spot);
                });
            } catch (Exception e) {
                log.error("[GroqTripService] Lỗi load cafes: {}", e.getMessage());
            }

            // 3. Map Dishes (food)
            try {
                dishRepository.findAll().forEach(dish -> {
                    // Chỉ thêm nếu có hình ảnh
                    if (dish.getImageUrl() == null || dish.getImageUrl().isBlank()) {
                        log.debug("[GroqTripService] Bỏ qua Dish {} - không có hình ảnh", dish.getDishName());
                        return;
                    }

                    Spot spot = Spot.builder()
                        .id(300000L + dish.getId())
                        .nameVi(dish.getDishName() + " (" + dish.getRestaurantName() + ")")
                        .nameEn(dish.getDishName() + " (" + dish.getRestaurantName() + ")")
                        .category("food")
                        .address(dish.getAddress())
                        .tags("món ăn, ẩm thực, đặc sản, nhà hàng")
                        .latitude(dish.getLatitude() != null ? dish.getLatitude() : 15.8801)
                        .longitude(dish.getLongitude() != null ? dish.getLongitude() : 108.3380)
                        .minCost(dish.getMinPrice())
                        .maxCost(dish.getMaxPrice())
                        .averageCost(dish.getMinPrice() != null && dish.getMaxPrice() != null ? (dish.getMinPrice() + dish.getMaxPrice()) / 2 : 0)
                        .estimatedDurationMinutes(45)
                        .openingTime(dish.getOpeningTime() != null ? dish.getOpeningTime() : LocalTime.of(10, 0))
                        .closingTime(dish.getClosingTime() != null ? dish.getClosingTime() : LocalTime.of(22, 0))
                        .crowdLevel("medium")
                        .rating(4.8)
                        .suitableFor("couple, family, group, solo")
                        .timeOfDay("morning, afternoon, evening")
                        .descriptionVi("Món ngon đặc sản Hội An: " + dish.getDishName() + " tại " + dish.getRestaurantName() + ". Địa chỉ: " + dish.getAddress())
                        .descriptionEn("Delicious Hoi An specialty: " + dish.getDishName() + " at " + dish.getRestaurantName() + ". Address: " + dish.getAddress())
                        .build();

                    SpotImage img = SpotImage.builder().imageUrl(dish.getImageUrl()).build();
                    spot.setImages(new ArrayList<>(List.of(img)));
                    allSpots.add(spot);
                });
            } catch (Exception e) {
                log.error("[GroqTripService] Lỗi load dishes: {}", e.getMessage());
            }

            // 4. Map Entertainments (sightseeing)
            try {
                entertainmentRepository.findAll().forEach(ent -> {
                    // Chỉ thêm nếu có hình ảnh
                    if (ent.getImageUrl() == null || ent.getImageUrl().isBlank()) {
                        log.debug("[GroqTripService] Bỏ qua Entertainment {} - không có hình ảnh", ent.getName());
                        return;
                    }

                    Spot spot = Spot.builder()
                        .id(400000L + ent.getId())
                        .nameVi(ent.getName())
                        .nameEn(ent.getName())
                        .category("sightseeing")
                        .address(ent.getAddress())
                        .tags("vui chơi, giải trí, tham quan, " + ent.getType() + ", " + (ent.getInterests() != null ? ent.getInterests() : ""))
                        .latitude(ent.getLatitude() != null ? ent.getLatitude() : 15.8801)
                        .longitude(ent.getLongitude() != null ? ent.getLongitude() : 108.3380)
                        .minCost(ent.getMinPrice())
                        .maxCost(ent.getMaxPrice())
                        .averageCost(ent.getMinPrice() != null && ent.getMaxPrice() != null ? (ent.getMinPrice() + ent.getMaxPrice()) / 2 : 0)
                        .estimatedDurationMinutes(120)
                        .openingTime(LocalTime.of(8, 0))
                        .closingTime(LocalTime.of(21, 0))
                        .crowdLevel("medium")
                        .rating(4.6)
                        .suitableFor("couple, family, group, solo")
                        .timeOfDay("morning, afternoon, evening")
                        .descriptionVi("Địa điểm giải trí thú vị: " + ent.getName() + " (" + ent.getType() + "). Phù hợp cho sở thích: " + ent.getInterests() + ". Địa chỉ: " + ent.getAddress())
                        .descriptionEn("Fun entertainment spot: " + ent.getName() + " (" + ent.getType() + "). Suitable for interests: " + ent.getInterests() + ". Address: " + ent.getAddress())
                        .build();

                    SpotImage img = SpotImage.builder().imageUrl(ent.getImageUrl()).build();
                    spot.setImages(new ArrayList<>(List.of(img)));
                    allSpots.add(spot);
                });
            } catch (Exception e) {
                log.error("[GroqTripService] Lỗi load entertainments: {}", e.getMessage());
            }

            // 5. Map Rentals (rental)
            try {
                rentalRepository.findAll().forEach(rental -> {
                    // Chỉ thêm nếu có hình ảnh
                    if (rental.getImageUrl() == null || rental.getImageUrl().isBlank()) {
                        log.debug("[GroqTripService] Bỏ qua Rental {} - không có hình ảnh", rental.getName());
                        return;
                    }

                    Spot spot = Spot.builder()
                        .id(500000L + rental.getId())
                        .nameVi(rental.getName() + " (" + rental.getType() + ")")
                        .nameEn(rental.getName() + " (" + rental.getType() + ")")
                        .category("rental")
                        .address(rental.getAddress())
                        .tags("dịch vụ, cho thuê, rental, " + rental.getType())
                        .latitude(rental.getLatitude() != null ? rental.getLatitude() : 15.8801)
                        .longitude(rental.getLongitude() != null ? rental.getLongitude() : 108.3380)
                        .minCost(rental.getMinPrice())
                        .maxCost(rental.getMaxPrice())
                        .averageCost(rental.getMinPrice() != null && rental.getMaxPrice() != null ? (rental.getMinPrice() + rental.getMaxPrice()) / 2 : 0)
                        .estimatedDurationMinutes(30)
                        .openingTime(rental.getOpeningTime() != null ? rental.getOpeningTime() : LocalTime.of(8, 0))
                        .closingTime(rental.getClosingTime() != null ? rental.getClosingTime() : LocalTime.of(21, 0))
                        .crowdLevel("low")
                        .rating(4.7)
                        .suitableFor("couple, family, group, solo")
                        .timeOfDay("morning, afternoon, evening")
                        .descriptionVi("Dịch vụ cho thuê: " + rental.getName() + " chuyên cung cấp " + rental.getType() + ". Địa chỉ: " + rental.getAddress())
                        .descriptionEn("Rental service: " + rental.getName() + " specializes in providing " + rental.getType() + ". Address: " + rental.getAddress())
                        .build();

                    SpotImage img = SpotImage.builder().imageUrl(rental.getImageUrl()).build();
                    spot.setImages(new ArrayList<>(List.of(img)));
                    allSpots.add(spot);
                });
            } catch (Exception e) {
                log.error("[GroqTripService] Lỗi load rentals: {}", e.getMessage());
            }

            if (allSpots.isEmpty()) {
                throw new RuntimeException("Database spots và các bảng liên quan trống. Vui lòng seed data trước!");
            }

            // Tối ưu hóa số lượng spots truyền vào Prompt theo độ khớp sở thích & phong cách của user
            List<Spot> optimizedSpots = new ArrayList<>();
            try {
                final String interestsStr = request.getInterests() != null ? String.join(" ", request.getInterests()).toLowerCase() : "";
                final String dishesStr = request.getSelectedDishes() != null ? String.join(" ", request.getSelectedDishes()).toLowerCase() : "";
                final String staysStr = request.getSelectedStayCategories() != null ? String.join(" ", request.getSelectedStayCategories()).toLowerCase() : "";
                final String entsStr = request.getSelectedEntCategories() != null ? String.join(" ", request.getSelectedEntCategories()).toLowerCase() : "";

                // Bộ tính điểm mức độ phù hợp sở thích & phong cách
                java.util.function.Function<Spot, Double> getScore = (s) -> {
                    double score = 0.0;
                    if (s == null) return score;

                    String name = (s.getNameVi() != null ? s.getNameVi() : "").toLowerCase();
                    String tags = (s.getTags() != null ? s.getTags() : "").toLowerCase();
                    String desc = (s.getDescriptionVi() != null ? s.getDescriptionVi() : "").toLowerCase();

                    // Khớp sở thích cá nhân
                    if (!interestsStr.isEmpty()) {
                        String[] keywords = interestsStr.split("[,\\s\\.\\-\\+]+");
                        for (String kw : keywords) {
                            if (kw.length() > 1) { // từ dài hơn 1 ký tự
                                if (name.contains(kw)) score += 20.0;
                                if (tags.contains(kw)) score += 10.0;
                                if (desc.contains(kw)) score += 5.0;
                            }
                        }
                    }

                    // Khớp lựa chọn từ tickboxes
                    String combinedSelections = dishesStr + " " + staysStr + " " + entsStr;
                    if (!combinedSelections.trim().isEmpty()) {
                        String[] selectionWords = combinedSelections.split("[,\\s]+");
                        for (String sw : selectionWords) {
                            if (sw.length() > 2) {
                                if (tags.contains(sw) || desc.contains(sw) || name.contains(sw)) {
                                    score += 8.0;
                                }
                            }
                        }
                    }

                    // Ưu tiên rating làm tie-breaker phụ
                    score += (s.getRating() != null ? s.getRating() : 0.0) * 0.1;
                    return score;
                };

                List<Spot> stays = allSpots.stream()
                    .filter(s -> "stay".equalsIgnoreCase(s.getCategory()))
                    .sorted((a, b) -> Double.compare(getScore.apply(b), getScore.apply(a)))
                    .limit(5)
                    .collect(Collectors.toList());

                List<Spot> cafes = allSpots.stream()
                    .filter(s -> "cafe".equalsIgnoreCase(s.getCategory()))
                    .sorted((a, b) -> Double.compare(getScore.apply(b), getScore.apply(a)))
                    .limit(8)
                    .collect(Collectors.toList());

                List<Spot> foods = allSpots.stream()
                    .filter(s -> "food".equalsIgnoreCase(s.getCategory()))
                    .sorted((a, b) -> Double.compare(getScore.apply(b), getScore.apply(a)))
                    .limit(10)
                    .collect(Collectors.toList());

                List<Spot> sightseeings = allSpots.stream()
                    .filter(s -> "sightseeing".equalsIgnoreCase(s.getCategory()))
                    .sorted((a, b) -> Double.compare(getScore.apply(b), getScore.apply(a)))
                    .limit(12)
                    .collect(Collectors.toList());

                List<Spot> rentals = allSpots.stream()
                    .filter(s -> "rental".equalsIgnoreCase(s.getCategory()))
                    .sorted((a, b) -> Double.compare(getScore.apply(b), getScore.apply(a)))
                    .limit(4)
                    .collect(Collectors.toList());

                optimizedSpots.addAll(stays);
                optimizedSpots.addAll(cafes);
                optimizedSpots.addAll(foods);
                optimizedSpots.addAll(sightseeings);
                // Tuyệt đối KHÔNG đưa dịch vụ cho thuê (rental) vào danh sách gửi cho AI lên lịch trình chính
                // optimizedSpots.addAll(rentals);
            } catch (Exception ex) {
                log.warn("[GroqTripService] Lỗi khi tối ưu hóa danh sách spots: {}", ex.getMessage());
            }

            if (optimizedSpots.isEmpty()) {
                optimizedSpots = allSpots;
            }

            // 1. Build prompt
            String prompt = buildPrompt(request, optimizedSpots);
            log.info("[GroqTripService] Gửi yêu cầu Gemini AI cho lịch trình {} ngày",
                request.getDays());

            // 2. Gọi Gemini API
            String jsonResponse = callGeminiApi(prompt);

            // 3. Parse JSON response → TripResponse
            TripResponse result = parseGroqResponse(jsonResponse, allSpots, request);
            result.setAiPowered(true);
            result.setAiEngine("Gemini 2.5 Flash");
            log.info("[GroqTripService] Sinh lịch trình AI thành công!");
            return result;

        } catch (Exception e) {
            log.error("[GroqTripService] Lỗi khi gọi Gemini API: {}. Chuyển sang rule-based fallback.", e.getMessage());
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
            m.put("category", s.getCategory()); // sightseeing, food, cafe, stay, rental
            m.put("tags", s.getTags());
            m.put("rating", s.getRating());
            m.put("cost", s.getMaxCost() != null ? s.getMaxCost() : (s.getAverageCost() != null ? s.getAverageCost() : 0));
            m.put("duration_min", s.getEstimatedDurationMinutes() != null ? s.getEstimatedDurationMinutes() : 60);
            m.put("suitable_for", s.getSuitableFor());
            m.put("time_of_day", s.getTimeOfDay()); // morning, afternoon, evening
            m.put("crowd", s.getCrowdLevel()); // low, medium, high
            m.put("opening_time", s.getOpeningTime() != null ? s.getOpeningTime().toString() : "00:00");
            m.put("closing_time", s.getClosingTime() != null ? s.getClosingTime().toString() : "23:59");
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
        String dishes = request.getSelectedDishes() != null && !request.getSelectedDishes().isEmpty()
            ? String.join(", ", request.getSelectedDishes()) : "Không yêu cầu cụ thể";
        String stays = request.getSelectedStayCategories() != null && !request.getSelectedStayCategories().isEmpty()
            ? String.join(", ", request.getSelectedStayCategories()) : "Không yêu cầu cụ thể";
        String ents = request.getSelectedEntCategories() != null && !request.getSelectedEntCategories().isEmpty()
            ? String.join(", ", request.getSelectedEntCategories()) : "Không yêu cầu cụ thể";
        String groupType = request.getGroupType() != null ? request.getGroupType() : "couple";
        String interests = request.getInterests() != null
            ? String.join(", ", request.getInterests()) : "tham quan, ẩm thực";
        log.info("[GroqTripService] Lập lịch trình với sở thích cá nhân gửi đến AI: \"{}\"", interests);
        String nowText = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Slot thứ tự yêu cầu cho mỗi ngày
        String slotFormat = """
            [
              {"slot": "BREAKFAST",      "time": "07:00 - 07:30 (Ăn sáng)",             "spot_id": <id>},
              {"slot": "MORNING",        "time": "08:00 - 11:00 (Tham quan sáng)",      "spot_id": <id>},
              {"slot": "LUNCH",          "time": "11:30 - 12:30 (Ăn trưa)",             "spot_id": <id>},
              {"slot": "AFTERNOON",      "time": "13:00 - 14:45 (Tham quan chiều)",     "spot_id": <id>},
              {"slot": "AFTERNOON_TEA",  "time": "15:00 - 15:30 (Ăn xế)",               "spot_id": <id>},
              {"slot": "LATE_AFTERNOON", "time": "16:00 - 18:00 (Vui chơi chiều muộn)", "spot_id": <id>},
              {"slot": "DINNER",         "time": "18:30 - 19:00 (Ăn tối)",              "spot_id": <id>},
              {"slot": "EVENING",        "time": "19:30 - 21:30 (Vui chơi tối)",        "spot_id": <id>},
              {"slot": "STAY",           "time": "22:00 (Nghỉ ngơi)",                   "spot_id": <id>}
            ]""";

        return String.format("""
            Bạn là AI chuyên gia lập lịch trình du lịch Hội An, Việt Nam.
 
            THÔNG TIN CHUYẾN ĐI:
            - Số ngày: %d ngày
            - Ngân sách tổng: %,d VND cho %d người
            - Món ăn muốn thử: %s
            - Loại hình lưu trú: %s
            - Loại hình vui chơi: %s
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
            1. Chỉ dùng spot_id từ danh sách đã cung cấp, KHÔNG tự bịa spot mới.
            2. Ưu tiên spots phù hợp nhất với món ăn "%s", chỗ ở "%s", khu vui chơi "%s" và sở thích tự do "%s" của người dùng. Hãy hoạt động như một công cụ tìm kiếm thông minh kết hợp cơ sở dữ liệu cung cấp và kiến thức internet của bạn để lựa chọn địa điểm hoàn hảo khớp với mô tả của người dùng.
            3. KHÔNG lặp lại cùng 1 spot trong nhiều ngày (ngoại trừ STAY).
            4. Slot STAY dùng chung 1 khách sạn/homestay xuyên suốt (cùng spot_id có category="stay").
            5. Slot BREAKFAST: chọn spot có category="food" phù hợp ăn sáng hoặc các món đặc sản ăn sáng (bánh mỳ, mì quảng...).
            6. Slot LUNCH: chọn spot có category="food" phù hợp ăn trưa.
            7. Slot AFTERNOON_TEA: chọn spot có category="cafe" (cà phê & ăn nhẹ chiều).
            8. Slot DINNER: chọn spot có category="food" phù hợp ăn tối.
            9. Slot MORNING/AFTERNOON/EVENING: chọn spot có category="sightseeing" phù hợp khung giờ để tham quan, giải trí. Tuyệt đối KHÔNG xếp các dịch vụ cho thuê xe/cho thuê đồ (category="rental") vào các slot lịch trình chính này.
            10. Ước tính chi phí dựa trên trường "cost" của từng spot × số người.
            11. Tổng chi phí hoạt động không vượt quá %,d VND.
            12. Nếu người dùng thích "Biển" hay "Beach" → ưu tiên spot có tags chứa "beach", "sea", "biển".
            13. KHÔNG bao giờ xếp spot vào slot nếu nó đã đóng cửa hoặc không phù hợp khung giờ thực tế.
            14. Mốc giờ hiện tại của hệ thống là %s (Asia/Ho_Chi_Minh). Hãy sinh lịch trình theo logic giờ mở cửa thực tế, không bịa giờ.
            """,
            days, budget, people, dishes, stays, ents, interests, groupType,
            spotsJson,
            days, slotFormat,
            dishes, stays, ents, interests,
            budget,
            nowText
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CALL GEMINI API
    // ─────────────────────────────────────────────────────────────────────────

    private String callGeminiApi(String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", geminiApiKey);

        Map<String, Object> part = Map.of("text", userPrompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        
        Map<String, Object> generationConfig = Map.of("responseMimeType", "application/json");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", List.of(content));
        body.put("generationConfig", generationConfig);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // Tạo RestTemplate cục bộ với timeout cao để chắc chắn không bị ảnh hưởng bởi context hot-swap hoặc bean config khác
        org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(20000); // 20s
        requestFactory.setReadTimeout(120000);  // 120s
        RestTemplate localRestTemplate = new RestTemplate(requestFactory);

        ResponseEntity<String> response = localRestTemplate.postForEntity(geminiApiUrl, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Gemini API trả về lỗi: " + response.getStatusCode());
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            throw new RuntimeException("Không thể parse Gemini API response: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PARSE GROQ RESPONSE → TripResponse
    // ─────────────────────────────────────────────────────────────────────────

    private TripResponse parseGroqResponse(String jsonStr, List<Spot> allSpots, TripRequest request) throws Exception {
        // Build lookup map: id → Spot
        Map<Long, Spot> spotMap = allSpots.stream()
            .collect(Collectors.toMap(Spot::getId, s -> s));
        Set<Long> usedSpotIds = new HashSet<>();

        JsonNode root = objectMapper.readTree(jsonStr);

        // Parse days
        List<TripResponse.DaySchedule> daySchedules = new ArrayList<>();
        JsonNode daysNode = root.path("days");

        for (JsonNode dayNode : daysNode) {
            int dayNum = dayNode.path("day").asInt(1);
            List<TripResponse.ScheduledSpot> scheduledSpots = new ArrayList<>();

            for (JsonNode slotNode : dayNode.path("spots")) {
                String slot   = slotNode.path("slot").asText("MORNING");
                String time   = normalizeSlotTime(slot);
                long spotId   = slotNode.path("spot_id").asLong(-1);

                Spot spot = spotMap.get(spotId);
                if (spot == null) {
                    log.warn("[GroqTripService] Groq trả về spot_id={} không tồn tại, bỏ qua.", spotId);
                    continue;
                }

                if (!isSpotCompatibleWithSlot(spot, slot)) {
                    Spot fallbackSpot = findFallbackSpot(allSpots, usedSpotIds, slot, spot.getCategory());
                    if (fallbackSpot == null) {
                        log.warn("[GroqTripService] Spot {} không hợp slot {} (giờ mở cửa không phù hợp), bỏ qua.", spot.getId(), slot);
                        continue;
                    }
                    log.warn("[GroqTripService] Spot {} không hợp slot {}, thay bằng spot {}.", spot.getId(), slot, fallbackSpot.getId());
                    spot = fallbackSpot;
                }

                if (!"STAY".equalsIgnoreCase(slot)) {
                    if (usedSpotIds.contains(spot.getId())) {
                        Spot fallbackSpot = findFallbackSpot(allSpots, usedSpotIds, slot, spot.getCategory());
                        if (fallbackSpot == null) {
                            log.warn("[GroqTripService] Spot {} đã dùng trước đó cho slot {}, bỏ qua.", spot.getId(), slot);
                            continue;
                        }
                        spot = fallbackSpot;
                    }
                    usedSpotIds.add(spot.getId());
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

    private String normalizeSlotTime(String slot) {
        if (slot == null) return "";
        return switch (slot.toUpperCase()) {
            case "BREAKFAST" -> "07:00 - 07:30 (Ăn sáng)";
            case "MORNING" -> "08:00 - 11:00 (Tham quan sáng)";
            case "LUNCH" -> "11:30 - 12:30 (Ăn trưa)";
            case "AFTERNOON" -> "13:00 - 14:45 (Tham quan chiều)";
            case "AFTERNOON_TEA", "CAFE" -> "15:00 - 15:30 (Ăn xế)";
            case "LATE_AFTERNOON" -> "16:00 - 18:00 (Vui chơi chiều muộn)";
            case "DINNER" -> "18:30 - 19:00 (Ăn tối)";
            case "EVENING" -> "19:30 - 21:30 (Vui chơi tối)";
            case "STAY" -> "22:00 (Nghỉ ngơi)";
            default -> "";
        };
    }

    private boolean isSpotCompatibleWithSlot(Spot spot, String slot) {
        if (spot == null || slot == null) return false;

        // Bỏ hoàn toàn các spot thuộc nhóm dịch vụ cho thuê (rental) khỏi các slot lịch trình chính
        if ("rental".equalsIgnoreCase(spot.getCategory())) {
            return false;
        }

        if ("STAY".equalsIgnoreCase(slot)) {
            return "stay".equalsIgnoreCase(spot.getCategory());
        }

        LocalTime slotStart = getSlotStartTime(slot);
        if (slotStart != null && !isWithinOperatingHours(spot, slotStart)) {
            return false;
        }

        String tod = spot.getTimeOfDay() != null ? spot.getTimeOfDay().toLowerCase() : "";
        return switch (slot.toUpperCase()) {
            case "BREAKFAST" -> "food".equalsIgnoreCase(spot.getCategory());
            case "MORNING" -> tod.isEmpty() || tod.contains("morning") || tod.contains("day") || tod.contains("all");
            case "LUNCH" -> "food".equalsIgnoreCase(spot.getCategory());
            case "AFTERNOON" -> tod.isEmpty() || tod.contains("afternoon") || tod.contains("day") || tod.contains("all");
            case "AFTERNOON_TEA", "CAFE" -> "cafe".equalsIgnoreCase(spot.getCategory());
            case "DINNER" -> "food".equalsIgnoreCase(spot.getCategory());
            case "EVENING" -> tod.isEmpty() || tod.contains("evening") || tod.contains("night") || tod.contains("all");
            default -> true;
        };
    }

    private boolean isWithinOperatingHours(Spot spot, LocalTime slotStart) {
        if (spot == null || slotStart == null) return true;
        LocalTime open = spot.getOpeningTime();
        LocalTime close = spot.getClosingTime();
        if (open == null || close == null) return true;
        if (open.equals(close)) return true;
        if (open.isBefore(close) || open.equals(close)) {
            return !slotStart.isBefore(open) && !slotStart.isAfter(close);
        }
        // Overnight business hours (rare) – treat as open across midnight
        return !slotStart.isBefore(open) || !slotStart.isAfter(close);
    }

    private LocalTime getSlotStartTime(String slot) {
        if (slot == null) return null;
        return switch (slot.toUpperCase()) {
            case "BREAKFAST" -> LocalTime.of(7, 0);
            case "MORNING" -> LocalTime.of(8, 0);
            case "LUNCH" -> LocalTime.of(11, 30);
            case "AFTERNOON" -> LocalTime.of(13, 0);
            case "AFTERNOON_TEA", "CAFE" -> LocalTime.of(15, 0);
            case "LATE_AFTERNOON" -> LocalTime.of(16, 0);
            case "DINNER" -> LocalTime.of(18, 30);
            case "EVENING" -> LocalTime.of(19, 30);
            case "STAY" -> LocalTime.of(22, 0);
            default -> null;
        };
    }

    private Spot findFallbackSpot(List<Spot> allSpots, Set<Long> usedSpotIds, String slot, String preferredCategory) {
        if (allSpots == null || allSpots.isEmpty()) return null;
        List<Spot> candidates = allSpots.stream()
            .filter(s -> s != null && s.getId() != null)
            .filter(s -> !usedSpotIds.contains(s.getId()))
            .filter(s -> isSpotCompatibleWithSlot(s, slot))
            .collect(Collectors.toList());

        if (preferredCategory != null && !preferredCategory.isBlank()) {
            for (Spot candidate : candidates) {
                if (preferredCategory.equalsIgnoreCase(candidate.getCategory())) {
                    return candidate;
                }
            }
        }

        if ("STAY".equalsIgnoreCase(slot)) {
            return candidates.stream()
                .filter(s -> "stay".equalsIgnoreCase(s.getCategory()))
                .findFirst()
                .orElse(null);
        }

        return candidates.stream()
            .filter(s -> !"stay".equalsIgnoreCase(s.getCategory()))
            .findFirst()
            .orElseGet(() -> candidates.stream().findFirst().orElse(null));
    }
}
