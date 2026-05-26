package vn.histra.service;

import org.springframework.stereotype.Service;
import vn.histra.model.Spot;
import vn.histra.dto.TripRequest;
import java.util.Arrays;
import java.util.List;

@Service
public class ScoringService {

    public double calculateScore(Spot spot, TripRequest request) {
        double score = 0.0;

        // 1. Độ trùng khớp sở thích (Preference Match)
        if (spot.getTags() != null && !spot.getTags().isEmpty() && request.getInterests() != null) {
            List<String> spotTags = Arrays.asList(spot.getTags().toLowerCase().split(","));
            long matchCount = request.getInterests().stream()
                .filter(interest -> spotTags.stream().anyMatch(tag -> tag.trim().equalsIgnoreCase(interest.trim())))
                .count();
            score += matchCount * 25.0; // Mỗi sở thích trùng khớp cộng 25 điểm
        }

        // 2. Điểm đánh giá thực tế (Rating Score)
        if (spot.getRating() != null) {
            score += spot.getRating() * 8.0; // Tối đa 5.0 sao = cộng thêm 40 điểm
        }

        // 3. Khớp định mức ngân sách (Budget Fit Score)
        if (request.getBudget() != null && request.getPeople() != null && request.getDays() != null && spot.getAverageCost() != null) {
            int budgetPerPersonPerDay = request.getBudget() / request.getPeople() / request.getDays();
            
            if (spot.getAverageCost() <= budgetPerPersonPerDay * 0.3) {
                score += 20.0; // Điểm đến cực kỳ tiết kiệm, cộng 20 điểm
            } else if (spot.getAverageCost() > budgetPerPersonPerDay * 0.5) {
                score -= 15.0; // Điểm đến vượt quá khả năng tiêu dùng ngày, trừ 15 điểm
            }
        }

        // 4. Đối tượng nhóm phù hợp (Group Type Fit)
        if (spot.getSuitableFor() != null && request.getGroupType() != null) {
            if (spot.getSuitableFor().toLowerCase().contains(request.getGroupType().toLowerCase())) {
                score += 15.0; // Phù hợp nhóm đi cùng (ví dụ: 'family', 'couple'), cộng 15 điểm
            }
        }

        // 5. Tránh đông đúc khi chọn du lịch chữa lành (Healing Style Fit)
        if ("healing".equalsIgnoreCase(request.getStyle()) && "high".equalsIgnoreCase(spot.getCrowdLevel())) {
            score -= 20.0; // Muốn tĩnh dưỡng nhưng địa điểm quá ồn ào, trừ mạnh 20 điểm
        }

        return score;
    }
}
