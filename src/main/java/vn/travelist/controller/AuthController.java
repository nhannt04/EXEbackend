package vn.travelist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.travelist.dto.ApiResponse;
import vn.travelist.dto.AuthResponse;
import vn.travelist.dto.LoginRequest;
import vn.travelist.dto.RegisterRequest;
import vn.travelist.model.User;
import vn.travelist.service.AuthService;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final vn.travelist.service.EmailService emailService;
    private final vn.travelist.service.OtpService otpService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String type = payload.getOrDefault("type", "register"); // "register" or "forgot_password"
            
            if (email == null || email.isEmpty()) {
                throw new RuntimeException("Email không được để trống!");
            }
            
            boolean emailExists = authService.existsByEmail(email);
            
            if ("register".equals(type) && emailExists) {
                throw new RuntimeException("Email đã được đăng ký trên hệ thống!");
            }
            
            if ("forgot_password".equals(type) && !emailExists) {
                throw new RuntimeException("Tài khoản chưa được đăng ký!");
            }
            
            String otp = otpService.generateOtp(email);
            
            // Send email
            String subject = "register".equals(type) 
                ? "Mã xác nhận đăng ký tài khoản Travelist" 
                : "Mã khôi phục mật khẩu Travelist";
                
            String body = "Xin chào,\n\nMã xác nhận (OTP) của bạn là: " + otp + "\nMã này sẽ hết hạn trong 5 phút.\n\nTrân trọng,\nĐội ngũ Travelist";
            emailService.sendSimpleEmail(email, subject, body);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Đã gửi mã OTP đến email của bạn!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Gửi mã OTP thất bại: " + e.getMessage(), "SEND_OTP_FAILED")
            );
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String otp = payload.get("otp");
            String newPassword = payload.get("newPassword");
            
            if (email == null || otp == null || newPassword == null) {
                throw new RuntimeException("Vui lòng cung cấp đầy đủ thông tin!");
            }
            if (newPassword.length() < 6) {
                throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự!");
            }
            
            authService.resetPassword(email, newPassword, otp);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Đổi mật khẩu thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Đổi mật khẩu thất bại: " + e.getMessage(), "RESET_PASSWORD_FAILED")
            );
        }
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@RequestBody Map<String, String> payload) {
        try {
            String idToken = payload.get("idToken");
            if (idToken == null || idToken.isEmpty()) {
                throw new RuntimeException("Mã Token Google không hợp lệ!");
            }

            AuthResponse response = authService.googleLogin(idToken);
            return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập bằng Google thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Đăng nhập bằng Google thất bại: " + e.getMessage(), "GOOGLE_AUTH_FAILED")
            );
        }
    }

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
