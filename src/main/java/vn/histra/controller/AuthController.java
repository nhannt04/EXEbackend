package vn.histra.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.histra.dto.ApiResponse;
import vn.histra.dto.AuthResponse;
import vn.histra.dto.LoginRequest;
import vn.histra.dto.RegisterRequest;
import vn.histra.model.User;
import vn.histra.service.AuthService;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success(user, "Đăng ký tài khoản thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Đăng ký không thành công: " + e.getMessage(), "REGISTRATION_FAILED")
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Đăng nhập thất bại: " + e.getMessage(), "AUTHENTICATION_FAILED")
            );
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@RequestBody Map<String, String> payload) {
        try {
            String refreshToken = payload.get("refreshToken");
            if (refreshToken == null || refreshToken.isEmpty()) {
                throw new RuntimeException("Mã Refresh Token bắt buộc phải có!");
            }
            
            String newAccessToken = authService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.success(
                Map.of("accessToken", newAccessToken), 
                "Làm mới Token JWT thành công!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Làm mới Token thất bại: " + e.getMessage(), "REFRESH_TOKEN_EXPIRED_OR_INVALID")
            );
        }
    }
}
