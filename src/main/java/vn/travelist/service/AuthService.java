package vn.travelist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.travelist.dto.AuthResponse;
import vn.travelist.dto.LoginRequest;
import vn.travelist.dto.RegisterRequest;
import vn.travelist.model.RefreshToken;
import vn.travelist.model.User;
import vn.travelist.repository.RefreshTokenRepository;
import vn.travelist.repository.UserRepository;
import vn.travelist.security.JwtTokenProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpService otpService;

    @Value("${GOOGLE_CLIENT_ID:}")
    private String googleClientId;

    /**
     * Đăng ký tài khoản người dùng mới
     */
    @Transactional
    public User register(RegisterRequest request) {
        if (request.getOtp() == null || !otpService.validateOtp(request.getEmail(), request.getOtp())) {
            throw new RuntimeException("Mã xác nhận (OTP) không chính xác hoặc đã hết hạn!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được đăng ký trên hệ thống!");
        }

        // Mã hóa mật khẩu an toàn bằng thuật toán SHA-256
        String hashedPassword = hashPassword(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .fullName(request.getFullName())
                .role("USER")
                .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&q=80") // Avatar mặc định
                .build();

        return userRepository.save(user);
    }

    /**
     * Kiểm tra xem email đã tồn tại hay chưa
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Đặt lại mật khẩu mới cho người dùng
     */
    @Transactional
    public void resetPassword(String email, String newPassword, String otp) {
        if (!otpService.validateOtp(email, otp)) {
            throw new RuntimeException("Mã xác nhận (OTP) không chính xác hoặc đã hết hạn!");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này!"));

        String hashedPassword = hashPassword(newPassword);
        user.setPasswordHash(hashedPassword);
        userRepository.save(user);
    }

    /**
     * Xác thực thông tin đăng nhập và cấp mã Token JWT + Refresh Token
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Tài khoản hoặc mật khẩu không chính xác!"));

        String hashedPassword = hashPassword(request.getPassword());
        if (!user.getPasswordHash().equals(hashedPassword)) {
            throw new RuntimeException("Tài khoản hoặc mật khẩu không chính xác!");
        }

        if (!user.getEnabled()) {
            throw new RuntimeException("Tài khoản của bạn hiện đang bị khóa!");
        }

        // Tạo JWT Access Token
        String accessToken = jwtTokenProvider.generateToken(user);

        // Tạo hoặc gia hạn Refresh Token
        RefreshToken refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();
    }

    /**
     * Xác thực với Google bằng ID Token
     */
    @Transactional
    public AuthResponse googleLogin(String idTokenString) {
        try {
            if (googleClientId == null || googleClientId.isEmpty()) {
                throw new RuntimeException("Chưa cấu hình Google Client ID trên Server");
            }
            
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");

                // Tìm user theo email hoặc tạo mới nếu chưa có
                User user = userRepository.findByEmail(email).orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .fullName(name != null ? name : "Người dùng Google")
                            .role("USER")
                            .avatarUrl(pictureUrl != null ? pictureUrl : "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&q=80")
                            .passwordHash(hashPassword(UUID.randomUUID().toString())) // Mật khẩu ngẫu nhiên cho user đăng nhập Google
                            .build();
                    return userRepository.save(newUser);
                });

                if (!user.getEnabled()) {
                    throw new RuntimeException("Tài khoản của bạn hiện đang bị khóa!");
                }

                // Cấp Token
                String accessToken = jwtTokenProvider.generateToken(user);
                RefreshToken refreshToken = createRefreshToken(user);

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken.getToken())
                        .user(AuthResponse.UserInfo.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .fullName(user.getFullName())
                                .role(user.getRole())
                                .avatarUrl(user.getAvatarUrl())
                                .build())
                        .build();

            } else {
                throw new RuntimeException("Mã Token Google không hợp lệ!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực Google: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo Refresh Token mới cho người dùng
     */
    public RefreshToken createRefreshToken(User user) {
        // Dọn dẹp các token cũ của người dùng này trước
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                // Hạn sử dụng của Refresh Token là 7 ngày
                .expiryDate(Instant.now().plusMillis(604800000))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Gia hạn Access Token từ Refresh Token còn hạn
     */
    @Transactional
    public String refreshAccessToken(String tokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Mã Refresh Token không tồn tại trên hệ thống!"));

        // Kiểm tra xem token đã hết hạn hay chưa
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại!");
        }

        // Cấp Access Token mới
        return jwtTokenProvider.generateToken(refreshToken.getUser());
    }

    /**
     * Hàm băm mật khẩu SHA-256
     */
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi hệ thống khi mã hóa mật khẩu", e);
        }
    }
}
