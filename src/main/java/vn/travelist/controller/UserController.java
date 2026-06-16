package vn.travelist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.travelist.dto.ApiResponse;
import vn.travelist.dto.AuthResponse;
import vn.travelist.model.User;
import vn.travelist.repository.UserRepository;
import vn.travelist.service.AuthService;
import vn.travelist.service.CloudflareImageService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final CloudflareImageService cloudflareImageService;
    private final AuthService authService;

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

    /**
     * Cập nhật ảnh đại diện cho người dùng bằng JSON (Presigned URL flow)
     */
    @PutMapping("/{id}/avatar")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> updateAvatar(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        String avatarCfId = payload.get("avatarCfId");
        String avatarUrl = payload.get("avatarUrl");

        if (avatarUrl == null || avatarUrl.isBlank()) {
            throw new RuntimeException("avatarUrl không được để trống!");
        }

        // Xóa ảnh cũ trên Cloudflare nếu tồn tại
        if (user.getAvatarCfId() != null) {
            try {
                cloudflareImageService.deleteImage(user.getAvatarCfId());
            } catch (Exception e) {
                // Bỏ qua lỗi xóa để tránh crash
            }
        }

        user.setAvatarCfId(avatarCfId);
        user.setAvatarUrl(avatarUrl);
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

    /**
     * Cập nhật thông tin hồ sơ người dùng
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> updateProfile(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        String fullName = payload.get("fullName");
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new RuntimeException("Họ và Tên không được để trống!");
        }

        user.setFullName(fullName.trim());
        userRepository.save(user);

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return ResponseEntity.ok(ApiResponse.success(userInfo, "Cập nhật thông tin thành công!"));
    }

    /**
     * Đổi mật khẩu
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            throw new RuntimeException("Vui lòng nhập đầy đủ mật khẩu cũ và mới!");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("Mật khẩu mới phải có ít nhất 6 ký tự!");
        }

        String hashedOldPassword = authService.hashPassword(oldPassword);
        if (!user.getPasswordHash().equals(hashedOldPassword)) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }

        user.setPasswordHash(authService.hashPassword(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(null, "Đổi mật khẩu thành công!"));
    }
}
