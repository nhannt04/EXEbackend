package vn.travelist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.travelist.dto.ApiResponse;
import vn.travelist.dto.CommentResponse;
import vn.travelist.dto.DiaryResponse;
import vn.travelist.service.DiaryService;
import vn.travelist.security.JwtTokenProvider;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DiaryResponse>>> getAllDiaries(
            @RequestParam(required = false) String category) {
        try {
            List<DiaryResponse> diaries = diaryService.getAllDiaries(category);
            return ResponseEntity.ok(ApiResponse.success(diaries, "Lấy danh sách nhật ký du ký thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Lấy danh sách nhật ký thất bại: " + e.getMessage(), "GET_DIARIES_FAILED")
            );
        }
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<DiaryResponse>> createDiary(
            @RequestParam Long userId,
            @RequestParam String category,
            @RequestParam String contentVi,
            @RequestParam String contentEn,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            DiaryResponse response = diaryService.createDiary(userId, category, contentVi, contentEn, null, image);
            return ResponseEntity.ok(ApiResponse.success(response, "Đăng tải bài viết nhật ký du ký thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Đăng tải bài viết thất bại: " + e.getMessage(), "CREATE_DIARY_FAILED")
            );
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DiaryResponse>> createDiaryJson(
            @RequestBody vn.travelist.dto.DiaryRequest request) {
        try {
            DiaryResponse response = diaryService.createDiaryJson(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Đăng tải bài viết nhật ký du ký thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Đăng tải bài viết thất bại: " + e.getMessage(), "CREATE_DIARY_FAILED")
            );
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> likeDiary(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    userId = jwtTokenProvider.getUserIdFromJWT(token);
                }
            }

            if (userId == null) {
                return ResponseEntity.status(401).body(
                    ApiResponse.error("Bạn cần đăng nhập để thực hiện hành động này!", "UNAUTHORIZED")
                );
            }

            diaryService.toggleLike(id, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "Thả tim bài viết thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Không thể thả tim bài viết: " + e.getMessage(), "LIKE_DIARY_FAILED")
            );
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        try {
            Number userIdNum = (Number) payload.get("userId");
            String content = (String) payload.get("content");
            Number parentIdNum = (Number) payload.get("parentCommentId");

            if (userIdNum == null || content == null || content.isEmpty()) {
                throw new RuntimeException("Thiếu tham số userId hoặc nội dung bình luận!");
            }

            Long userId = userIdNum.longValue();
            Long parentCommentId = parentIdNum != null ? parentIdNum.longValue() : null;

            CommentResponse response = diaryService.addComment(id, userId, content, parentCommentId);
            return ResponseEntity.ok(ApiResponse.success(response, "Đăng bình luận thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Đăng bình luận thất bại: " + e.getMessage(), "ADD_COMMENT_FAILED")
            );
        }
    }
}
