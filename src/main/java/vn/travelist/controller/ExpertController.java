package vn.travelist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.travelist.dto.ApiResponse;
import vn.travelist.dto.ExpertInquiryRequest;
import vn.travelist.dto.ExpertInquiryResponse;
import vn.travelist.dto.ExpertResponse;
import vn.travelist.service.ExpertService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/experts")
@RequiredArgsConstructor
public class ExpertController {

    private final ExpertService expertService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpertResponse>>> getExperts(
            @RequestParam(required = false) Boolean online) {
        try {
            List<ExpertResponse> experts;
            if (online != null && online) {
                experts = expertService.getOnlineExperts();
            } else {
                experts = expertService.getAllExperts();
            }
            return ResponseEntity.ok(ApiResponse.success(experts, "Lấy danh sách chuyên gia bản địa thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Lấy danh sách chuyên gia thất bại: " + e.getMessage(), "GET_EXPERTS_FAILED")
            );
        }
    }

    @PostMapping("/inquiries")
    public ResponseEntity<ApiResponse<ExpertInquiryResponse>> askQuestion(
            @RequestBody ExpertInquiryRequest request) {
        try {
            ExpertInquiryResponse response = expertService.askQuestion(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Gửi câu hỏi tới chuyên gia thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Gửi câu hỏi thất bại: " + e.getMessage(), "ASK_QUESTION_FAILED")
            );
        }
    }

    @PostMapping("/inquiries/{id}/answer")
    public ResponseEntity<ApiResponse<ExpertInquiryResponse>> answerQuestion(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String answer = payload.get("answer");
            if (answer == null || answer.isEmpty()) {
                throw new RuntimeException("Nội dung câu trả lời của chuyên gia bắt buộc phải có!");
            }
            
            ExpertInquiryResponse response = expertService.answerQuestion(id, answer);
            return ResponseEntity.ok(ApiResponse.success(response, "Gửi câu trả lời thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Gửi câu trả lời thất bại: " + e.getMessage(), "ANSWER_QUESTION_FAILED")
            );
        }
    }

    @GetMapping("/{id}/inquiries")
    public ResponseEntity<ApiResponse<List<ExpertInquiryResponse>>> getExpertInquiries(
            @PathVariable Long id) {
        try {
            List<ExpertInquiryResponse> inquiries = expertService.getExpertInquiries(id);
            return ResponseEntity.ok(ApiResponse.success(inquiries, "Lấy lịch sử hỏi đáp của chuyên gia thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Lấy lịch sử hỏi đáp thất bại: " + e.getMessage(), "GET_INQUIRIES_FAILED")
            );
        }
    }

    @GetMapping("/users/{userId}/inquiries")
    public ResponseEntity<ApiResponse<List<ExpertInquiryResponse>>> getUserInquiries(
            @PathVariable Long userId) {
        try {
            List<ExpertInquiryResponse> inquiries = expertService.getUserInquiries(userId);
            return ResponseEntity.ok(ApiResponse.success(inquiries, "Lấy lịch sử hỏi đáp của du khách thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Lấy lịch sử hỏi đáp của du khách thất bại: " + e.getMessage(), "GET_USER_INQUIRIES_FAILED")
            );
        }
    }
}
