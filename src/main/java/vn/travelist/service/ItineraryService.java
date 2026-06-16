package vn.travelist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.travelist.dto.ItinerarySaveRequest;
import vn.travelist.dto.ItineraryResponse;
import vn.travelist.model.Itinerary;
import vn.travelist.model.User;
import vn.travelist.repository.ItineraryRepository;
import vn.travelist.repository.UserRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key:${GEMINI_API_KEY:}}")
    private String geminiApiKey;

    private final String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    public ItineraryResponse saveItinerary(ItinerarySaveRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        Itinerary itinerary = Itinerary.builder()
                .user(user)
                .title(request.getTitle())
                .totalDays(request.getTotalDays())
                .totalBudget(request.getTotalBudget())
                .travelStyle(request.getTravelStyle())
                .groupType(request.getGroupType())
                .tripData(request.getTripData())
                .build();

        Itinerary saved = itineraryRepository.save(itinerary);
        return mapToResponse(saved);
    }

    public List<ItineraryResponse> getMyItineraries(Long userId) {
        return itineraryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ItineraryResponse> getCompletedItineraries(Long userId) {
        return itineraryRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "COMPLETED").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteItinerary(Long id, Long userId) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch trình không tồn tại!"));
        
        if (!itinerary.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa lịch trình này!");
        }

        itineraryRepository.delete(itinerary);
    }

    public ItineraryResponse updateItineraryStatus(Long id, String status, Long userId) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch trình không tồn tại!"));

        if (!itinerary.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền cập nhật lịch trình này!");
        }

        itinerary.setStatus(status);
        Itinerary saved = itineraryRepository.save(itinerary);
        return mapToResponse(saved);
    }

    public String generateHandbookForItinerary(Long id, Long userId) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch trình không tồn tại!"));

        if (!itinerary.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền truy cập lịch trình này!");
        }

        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return "{\"error\": \"Gemini API key is not configured.\"}";
        }

        try {
            String prompt = "Bạn là chuyên gia du lịch Travelist.\n" +
                    "Hãy viết một cẩm nang bỏ túi (pocket handbook) bằng tiếng Việt cho lịch trình du lịch Hội An sau:\n" +
                    "- Tiêu đề: " + itinerary.getTitle() + "\n" +
                    "- Số ngày: " + itinerary.getTotalDays() + " ngày\n" +
                    "- Ngân sách: " + itinerary.getTotalBudget() + " VND\n" +
                    "- Phong cách: " + itinerary.getTravelStyle() + "\n" +
                    "- Chi tiết lịch trình: " + itinerary.getTripData() + "\n\n" +
                    "Yêu cầu:\n" +
                    "Trả về kết quả dưới dạng JSON thuần túy (không nằm trong dấu nháy ```json hoặc bất kỳ ký tự nào khác), có cấu trúc như sau:\n" +
                    "{\n" +
                    "  \"title\": \"Tiêu đề cẩm nang\",\n" +
                    "  \"subtitle\": \"Mô tả ngắn gọn\",\n" +
                    "  \"sections\": [\n" +
                    "    {\n" +
                    "      \"title\": \"Tiêu đề mục (Ví dụ: Chuẩn bị hành lý, Ẩm thực khuyên dùng, Quy tắc ứng xử...)\",\n" +
                    "      \"content\": \"Nội dung chi tiết mục đó, viết sinh động, thân thiện và chuyên nghiệp.\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n" +
                    "Tạo khoảng 4-5 mục thiết thực, phù hợp với phong cách chuyến đi và ngân sách của người dùng.";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-goog-api-key", geminiApiKey);

            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> body = Map.of("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(15000);
            requestFactory.setReadTimeout(45000);
            RestTemplate restTemplate = new RestTemplate(requestFactory);

            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(geminiApiUrl, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String aiText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
                // Clean markdown code blocks if any
                if (aiText.contains("```json")) {
                    aiText = aiText.substring(aiText.indexOf("```json") + 7);
                    if (aiText.contains("```")) {
                        aiText = aiText.substring(0, aiText.indexOf("```"));
                    }
                } else if (aiText.contains("```")) {
                    aiText = aiText.substring(aiText.indexOf("```") + 3);
                    if (aiText.contains("```")) {
                        aiText = aiText.substring(0, aiText.indexOf("```"));
                    }
                }
                return aiText.trim();
            }
        } catch (Exception e) {
            return "{\"error\": \"Lỗi khi tạo cẩm nang: " + e.getMessage() + "\"}";
        }
        return "{\"error\": \"Không thể tạo cẩm nang từ AI. Vui lòng thử lại sau!\"}";
    }

    private ItineraryResponse mapToResponse(Itinerary itinerary) {
        return ItineraryResponse.builder()
                .id(itinerary.getId())
                .title(itinerary.getTitle())
                .destination(itinerary.getDestination())
                .totalDays(itinerary.getTotalDays())
                .totalBudget(itinerary.getTotalBudget())
                .travelStyle(itinerary.getTravelStyle())
                .groupType(itinerary.getGroupType())
                .tripData(itinerary.getTripData())
                .status(itinerary.getStatus())
                .createdAt(itinerary.getCreatedAt())
                .build();
    }
}
