package vn.travelist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.travelist.dto.ApiResponse;
import vn.travelist.dto.ChatRequest;
import vn.travelist.dto.ChatResponse;
import vn.travelist.service.ChatService;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> getChatReply(@RequestBody ChatRequest request) {
        try {
            String reply = chatService.getChatReply(request.getMessage());
            ChatResponse response = new ChatResponse(reply);
            return ResponseEntity.ok(ApiResponse.success(response, "Lấy phản hồi từ trợ lý ảo thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Gặp lỗi khi xử lý chat: " + e.getMessage(), "CHAT_PROCESSING_FAILED")
            );
        }
    }
}
