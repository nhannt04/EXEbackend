package vn.travelist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.travelist.dto.ApiResponse;
import vn.travelist.dto.TripRequest;
import vn.travelist.dto.TripResponse;
import vn.travelist.service.GroqTripService;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final GroqTripService groqTripService;

    @Value("${groq.api-key:}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    /**
     * Kiểm tra trạng thái AI: GET /api/v1/trips/status
     * Trả về thông tin Groq có đang hoạt động hay đang dùng rule-based fallback
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAiStatus() {
        boolean isGroqActive = groqApiKey != null
            && !groqApiKey.isBlank()
            && !groqApiKey.equals("your_groq_api_key_here");

        Map<String, Object> status = Map.of(
            "ai_powered",   isGroqActive,
            "engine",       isGroqActive ? "Groq " + groqModel : "Rule-based Scoring",
            "model",        isGroqActive ? groqModel : "N/A",
            "status",       isGroqActive ? "ACTIVE" : "FALLBACK",
            "message",      isGroqActive
                ? "Hệ thống đang dùng Groq AI (" + groqModel + ") để sinh lịch trình thông minh!"
                : "Groq API key chưa được cấu hình. Đang dùng rule-based scoring."
        );
        return ResponseEntity.ok(ApiResponse.success(status, "Kiểm tra trạng thái AI thành công!"));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<TripResponse>> generateTrip(@RequestBody TripRequest request) {
        try {
            TripResponse response = groqTripService.generateItinerary(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Sinh lịch trình du lịch tối ưu thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Không thể tự động sinh lịch trình: " + e.getMessage(), "ITINERARY_GENERATION_FAILED")
            );
        }
    }
}
