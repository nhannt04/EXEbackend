package vn.travelist.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    // Cache with 5-minute expiration
    private final Cache<String, String> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public String generateOtp(String email) {
        // Generate 6-digit random OTP
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);
        
        otpCache.put(email, otp);
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        String storedOtp = otpCache.getIfPresent(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpCache.invalidate(email); // Invalidate after successful use
            return true;
        }
        return false;
    }
}
