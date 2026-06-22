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

        // ── 2. Style-based scoring: deleted ────────

        // ── 3. Rating Score (tối đa 5.0 sao = +40đ) ───────────────────────────
        if (spot.getRating() != null) {
            score += spot.getRating() * 8.0;
        }

        // ── 4. Budget Fit Score (gradient thay vì binary) ─────────────────────
        Integer maxOrAvgCost = spot.getMaxCost() != null ? spot.getMaxCost() : spot.getAverageCost();
        if (request.getBudget() != null && request.getPeople() != null
                && request.getDays() != null && maxOrAvgCost != null) {
            int people = Math.max(1, request.getPeople());
            int days   = Math.max(1, request.getDays());
            int budgetPerPersonPerDay = request.getBudget() / people / days;
            int cost = maxOrAvgCost;

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
