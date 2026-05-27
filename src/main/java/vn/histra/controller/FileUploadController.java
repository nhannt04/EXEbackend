package vn.histra.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.histra.dto.ApiResponse;
import vn.histra.dto.PresignedUploadRequest;
import vn.histra.dto.PresignedUploadResponse;
import vn.histra.service.FileUploadService;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * POST /api/v1/uploads/presigned
     * Sinh Presigned URL để upload file trực tiếp từ client lên Cloudflare R2
     */
    @PostMapping("/presigned")
    public ResponseEntity<ApiResponse<PresignedUploadResponse>> getPresignedUrl(
            @RequestBody PresignedUploadRequest request) {
        try {
            PresignedUploadResponse response = fileUploadService.generatePresignedUploadUrl(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Sinh Presigned Upload URL thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Sinh Presigned Upload URL thất bại: " + e.getMessage(), "PRESIGNED_GENERATE_FAILED")
            );
        }
    }
}
