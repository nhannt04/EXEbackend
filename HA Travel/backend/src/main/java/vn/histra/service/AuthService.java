package vn.histra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.histra.dto.AuthResponse;
import vn.histra.dto.LoginRequest;
import vn.histra.dto.RegisterRequest;
import vn.histra.model.RefreshToken;
import vn.histra.model.User;
import vn.histra.repository.RefreshTokenRepository;
import vn.histra.repository.UserRepository;
import vn.histra.security.JwtTokenProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Đăng ký tài khoản người dùng mới
     */
    @Transactional
    public User register(RegisterRequest request) {
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
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi hệ thống khi mã hóa mật khẩu", e);
        }
    }
}
