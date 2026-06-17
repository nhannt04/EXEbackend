package vn.travelist.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class EmailService {

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${SPRING_MAIL_USERNAME:nhannguyen.070704@gmail.com}")
    private String senderEmail;

    public void sendSimpleEmail(String toEmail, String subject, String body) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // Xử lý các ký tự xuống dòng để đưa vào chuỗi JSON hợp lệ
            String escapedBody = body.replace("\n", "\\n").replace("\"", "\\\"");
            
            // Format JSON cho Brevo API
            String jsonPayload = String.format(
                    "{\"sender\": {\"name\": \"Travelist OTP\", \"email\": \"%s\"}, \"to\": [{\"email\": \"%s\"}], \"subject\": \"%s\", \"textContent\": \"%s\"}",
                    senderEmail, toEmail, subject, escapedBody
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(10))
                    .header("api-key", brevoApiKey)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Lỗi từ máy chủ Brevo: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Gửi email thất bại qua Brevo: " + e.getMessage(), e);
        }
    }
}
