package vn.histra.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.histra.dto.ApiResponse;
import vn.histra.dto.AuthResponse;
import vn.histra.model.User;
import vn.histra.repository.UserRepository;
import vn.histra.service.CloudflareImageService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final CloudflareImageService cloudflareImageService;

    /**
     * Upload / cập nhật ảnh đại diện cho người dùng
     */
    @PostMapping(value = "/{id}/avatar", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // Xóa ảnh cũ trên Cloudflare nếu tồn tại
        if (user.getAvatarCfId() != null) {
            try {
                cloudflareImageService.deleteImage(user.getAvatarCfId());
            } catch (Exception e) {
                // Bỏ qua lỗi xóa để tránh crash
            }
        }

        // Upload ảnh mới
        Map<String, String> uploadResult = cloudflareImageService.uploadImage(image);
        user.setAvatarCfId(uploadResult.get("cfId"));
        user.setAvatarUrl(uploadResult.get("url"));
        userRepository.save(user);

        // Tạo User Info cập nhật mới
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return ResponseEntity.ok(ApiResponse.success(userInfo, "Cập nhật ảnh đại diện thành công!"));
    }
}
