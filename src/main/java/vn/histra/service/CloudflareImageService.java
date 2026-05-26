package vn.histra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudflareImageService {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.account-id}")
    private String accountId;

    @Value("${cloudflare.r2.custom-domain:}")
    private String customDomain;

    /**
     * Upload ảnh lên Cloudflare R2
     */
    public Map<String, String> uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File rỗng, không thể upload!");
        }

        try {
            String objectKey = generateObjectKey(file.getOriginalFilename());
            byte[] fileContent = file.getBytes();

            // Upload lên R2
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .contentLength((long) fileContent.length)
                            .contentType(file.getContentType())
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(fileContent));

            // Tạo public delivery URL từ R2 (hoặc custom domain nếu có)
            String publicUrl;
            if (customDomain != null && !customDomain.isBlank()) {
                // Dùng custom domain nếu có
                publicUrl = String.format("%s/%s", customDomain.replaceFirst("/$", ""), objectKey);
            } else {
                // Dùng R2 direct URL
                publicUrl = String.format(
                        "https://%s.r2.cloudflarestorage.com/%s/%s",
                        accountId, bucketName, objectKey);
            }

            return Map.of("cfId", objectKey, "url", publicUrl);

        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload ảnh lên Cloudflare R2: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa ảnh khỏi Cloudflare R2
     */
    public void deleteImage(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }

        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build());
        } catch (Exception e) {
            // Silent catch to prevent deletion crashes
            System.err.println("Warning: Không thể xóa ảnh từ R2: " + e.getMessage());
        }
    }

    /**
     * Generate unique object key
     */
    private String generateObjectKey(String originalFilename) {
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "images/" + UUID.randomUUID() + fileExtension;
    }
}
