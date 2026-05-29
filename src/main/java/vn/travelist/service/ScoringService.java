package vn.travelist.service;

import org.springframework.stereotype.Service;
import vn.travelist.model.Spot;
import vn.travelist.dto.TripRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ScoringService {

    // Ánh xạ từng phong cách du lịch → các tag/category ưu tiên
    private static final Map<String, List<String>> STYLE_PRIORITY_TAGS = Map.of(
        "healing",    List.of("healing", "retreat", "chill", "silence", "nature", "cozy", "garden"),
        "adventure",  List.of("adventure", "outdoor", "boat", "nature", "trekking", "beach", "snorkeling"),
        "culture",    List.of("culture", "heritage", "history", "traditional", "historic", "workshop"),
        "food",       List.of("food", "local taste", "specialty", "street food", "must-eat", "traditional"),
        "romantic",   List.of("romantic", "sunset", "views", "luxury", "chill", "couple"),
        "explorer",   List.of("adventure", "nature", "views", "check-in", "beach", "outdoor")
    );

    // Ánh xạ phong cách → category ưu tiên cao
    private static final Map<String, List<String>> STYLE_PRIORITY_CATEGORIES = Map.of(
        "healing",    List.of("stay", "cafe"),
        "adventure",  List.of("sightseeing", "activity"),
        "culture",    List.of("sightseeing"),
        "food",       List.of("food", "cafe"),
        "romantic",   List.of("cafe", "stay", "sightseeing"),
        "explorer",   List.of("sightseeing", "activity")
    );

    public double calculateScore(Spot spot, TripRequest request) {
        double score = 0.0;

        // ── 1. Tag Matching với interests của người dùng (25đ/tag trùng) ──────────
        if (spot.getTags() != null && !spot.getTags().isEmpty() && request.getInterests() != null) {
            List<String> spotTags = Arrays.asList(spot.getTags().toLowerCase().split("[,;]+"));
            long matchCount = request.getInterests().stream()
                .filter(interest -> spotTags.stream()
                    .anyMatch(tag -> tag.trim().toLowerCase().contains(interest.trim().toLowerCase())
                                 || interest.trim().toLowerCase().contains(tag.trim().toLowerCase())))
                .count();
            score += matchCount * 25.0;
        }

        // ── 2. Style-based scoring: ưu tiên tag/category theo phong cách ────────
        String style = request.getStyle() != null ? request.getStyle().toLowerCase() : "";
        if (!style.isEmpty()) {
            // 2a. Bonus nếu tag của spot trùng với tag ưu tiên của style
            List<String> priorityTags = STYLE_PRIORITY_TAGS.getOrDefault(style, List.of());
            if (spot.getTags() != null) {
                String tagsLower = spot.getTags().toLowerCase();
                long styleTagHits = priorityTags.stream()
                    .filter(t -> tagsLower.contains(t))
                    .count();
                score += styleTagHits * 18.0; // Mỗi tag style khớp = +18đ
            }

            // 2b. Bonus nếu category của spot là category ưu tiên của style
            List<String> priorityCats = STYLE_PRIORITY_CATEGORIES.getOrDefault(style, List.of());
            if (spot.getCategory() != null && priorityCats.contains(spot.getCategory().toLowerCase())) {
                score += 20.0; // Category đúng phong cách = +20đ
            }

            // 2c. Healing: ưu tiên chỗ ít đông + phạt nặng nơi đông đúc
            if ("healing".equals(style)) {
                if ("low".equalsIgnoreCase(spot.getCrowdLevel()))    score += 15.0;
                if ("medium".equalsIgnoreCase(spot.getCrowdLevel())) score += 5.0;
                if ("high".equalsIgnoreCase(spot.getCrowdLevel()))   score -= 25.0;
            }

            // 2d. Adventure/Explorer: ưu tiên trải nghiệm mới, bỏ qua chỗ nghỉ
            if ("adventure".equals(style) || "explorer".equals(style)) {
                if ("stay".equalsIgnoreCase(spot.getCategory())) score -= 20.0;
                if ("high".equalsIgnoreCase(spot.getCrowdLevel())) score += 8.0; // Điểm sôi động OK
            }

            // 2e. Food style: ưu tiên nhà hàng + cafe, phạt nặng chỗ nghỉ
            if ("food".equals(style)) {
                if ("food".equalsIgnoreCase(spot.getCategory()))  score += 30.0;
                if ("cafe".equalsIgnoreCase(spot.getCategory()))  score += 20.0;
                if ("stay".equalsIgnoreCase(spot.getCategory()))  score -= 30.0;
            }

            // 2f. Culture: ưu tiên điểm tham quan lịch sử, phạt food/cafe
            if ("culture".equals(style)) {
                if ("sightseeing".equalsIgnoreCase(spot.getCategory())) score += 25.0;
                if ("cafe".equalsIgnoreCase(spot.getCategory()))        score -= 10.0;
                if ("food".equalsIgnoreCase(spot.getCategory()))        score -= 5.0;
            }

            // 2g. Romantic: ưu tiên rating cao + view đẹp + cafe
            if ("romantic".equals(style)) {
                if (spot.getRating() != null && spot.getRating() >= 4.7) score += 15.0;
                if ("low".equalsIgnoreCase(spot.getCrowdLevel()))        score += 10.0;
            }
        }

        // ── 3. Rating Score (tối đa 5.0 sao = +40đ) ───────────────────────────
        if (spot.getRating() != null) {
            score += spot.getRating() * 8.0;
        }

        // ── 4. Budget Fit Score (gradient thay vì binary) ─────────────────────
        if (request.getBudget() != null && request.getPeople() != null
                && request.getDays() != null && spot.getAverageCost() != null) {
            int people = Math.max(1, request.getPeople());
            int days   = Math.max(1, request.getDays());
            int budgetPerPersonPerDay = request.getBudget() / people / days;
            int cost = spot.getAverageCost();

            if (cost == 0) {
                score += 10.0; // Miễn phí = nhẹ bonus
            } else if (cost <= budgetPerPersonPerDay * 0.20) {
                score += 22.0; // Rất rẻ
            } else if (cost <= budgetPerPersonPerDay * 0.40) {
                score += 15.0; // Vừa ngân sách
            } else if (cost <= budgetPerPersonPerDay * 0.60) {
                score += 5.0;  // Hơi cao nhưng chấp nhận được
            } else if (cost <= budgetPerPersonPerDay * 0.80) {
                score -= 10.0; // Khá đắt
            } else {
                score -= 25.0; // Vượt ngân sách → phạt nặng
            }
        }

        // ── 5. Group Type Fit (+15đ nếu suitable_for khớp) ───────────────────
        if (spot.getSuitableFor() != null && request.getGroupType() != null) {
            if (spot.getSuitableFor().toLowerCase().contains(request.getGroupType().toLowerCase())) {
                score += 15.0;
            }
        }

        // ── 6. Time-of-day Bonus: ưu tiên spot phù hợp thời điểm đặc trưng ──
        // (Tăng độ đa dạng lịch trình khi có morning/evening spots)
        if (spot.getTimeOfDay() != null) {
            String tod = spot.getTimeOfDay().toLowerCase();
            if (tod.contains("morning") || tod.contains("evening")) {
                score += 5.0; // Spot có giờ đặc thù → phân bổ slot dễ hơn
            }
        }

        return score;
    }
}
