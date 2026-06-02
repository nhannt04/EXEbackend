package vn.travelist.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.travelist.model.Spot;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final VirtualSpotLoader virtualSpotLoader;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key:${GEMINI_API_KEY:}}")
    private String geminiApiKey;

    private final String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    public String getChatReply(String userMessage) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.warn("[ChatService] GEMINI_API_KEY chưa được cấu hình. Sử dụng offline reply.");
            return "Hệ thống AI Chat đang ngoại tuyến do thiếu API Key. Vui lòng liên hệ quản trị viên!";
        }

        try {
            List<Spot> spots = virtualSpotLoader.getAllVirtualSpots();
            String spotsContext = spots.stream()
                    .limit(30) // limit context size to prevent prompt bloating
                    .map(s -> String.format("- %s (%s): %s", s.getNameVi(), s.getCategory(), s.getAddress()))
                    .collect(Collectors.joining("\n"));

            String systemPrompt = "System instruction:\n" +
                    "Bạn là trợ lý du lịch ảo thông minh 'Travelist Guide' của ứng dụng Travelist tại Hội An, Quảng Nam, Việt Nam.\n" +
                    "Bạn chỉ được trả lời các câu hỏi về du lịch, ẩm thực, lịch sử, văn hóa, danh lam thắng cảnh ở Hội An.\n" +
                    "Nếu câu hỏi của người dùng nằm ngoài khu vực Hội An (ví dụ hỏi về Hà Nội, TP.HCM, Đà Nẵng, hoặc bất kỳ địa điểm du lịch quốc tế/trong nước nào ngoài Hội An), " +
                    "bạn bắt buộc phải trả lời đúng nội dung sau: 'Travelist hiện tại chưa hỗ trợ dữ liệu ngoài khu vực Hội An. Tôi chỉ có thể trả lời các địa điểm nằm trong Hội An dựa trên cơ sở dữ liệu của Travelist hoặc tra cứu thêm thông tin từ internet dựa trên dữ liệu địa điểm Hội An sẵn có.'\n" +
                    "Tuyệt đối không được bịa thông tin hoặc trả lời chi tiết về các địa điểm ngoài Hội An.\n\n" +
                    "Dưới đây là một số địa điểm từ cơ sở dữ liệu của chúng tôi để bạn tham khảo khi trả lời:\n" +
                    spotsContext + "\n\n" +
                    "Nếu người dùng hỏi về các địa điểm có trong danh sách trên hoặc liên quan đến Hội An, hãy dựa trên dữ liệu trên và tìm kiếm thêm thông tin hữu ích trên mạng để cung cấp câu trả lời phong phú, chính xác và thân thiện nhất.\n" +
                    "Câu hỏi của người dùng: " + userMessage;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-goog-api-key", geminiApiKey);

            Map<String, Object> part = Map.of("text", systemPrompt);
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> body = Map.of("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(10000);
            requestFactory.setReadTimeout(30000);
            RestTemplate restTemplate = new RestTemplate(requestFactory);

            ResponseEntity<String> response = restTemplate.postForEntity(geminiApiUrl, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            }
        } catch (Exception e) {
            log.error("[ChatService] Lỗi khi gọi Gemini API: {}", e.getMessage());
        }

        return "Hiện tại tôi đang gặp khó khăn khi kết nối. Hãy thử lại sau nhé!";
    }
}
