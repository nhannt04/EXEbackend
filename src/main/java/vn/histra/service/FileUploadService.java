package vn.histra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import vn.histra.dto.PresignedUploadRequest;
import vn.histra.dto.PresignedUploadResponse;
import vn.histra.model.UploadType;

import java.net.URL;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.account-id}")
    private String accountId;

    @Value("${cloudflare.r2.custom-domain:}")
    private String customDomain;

    private static final long EXPIRES_IN_SECONDS = 300L;

    /**
     * Tạo đường dẫn Presigned Upload URL cho Client tự tải ảnh lên Cloudflare R2
     */
    public PresignedUploadResponse generatePresignedUploadUrl(PresignedUploadRequest request) {
        validateRequest(request);

        String sanitizedFileName = sanitizeFileName(request.getFileName());
        String folder = resolveFolder(request.getUploadType());
        String fileKey = folder + "/" + UUID.randomUUID() + "-" + sanitizedFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(request.getContentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(EXPIRES_IN_SECONDS))
                .putObjectRequest(putObjectRequest)
                .build();

        URL uploadUrl = s3Presigner.presignPutObject(presignRequest).url();
        String fileUrl = buildPublicUrl(fileKey);

        return PresignedUploadResponse.builder()
                .uploadUrl(uploadUrl.toString())
                .fileUrl(fileUrl)
                .fileKey(fileKey)
                .expiresInSeconds(EXPIRES_IN_SECONDS)
                .build();
    }

    /**
     * Xóa ảnh khỏi Cloudflare R2 bằng fileKey
     */
    public void deleteFileByKey(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) return;

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build());
        } catch (Exception e) {
            System.err.println("Warning: Không thể xóa file từ R2: " + e.getMessage());
        }
    }

    private void validateRequest(PresignedUploadRequest request) {
        if (request == null) throw new RuntimeException("Request không được null");
        if (request.getFileName() == null || request.getFileName().isBlank())
            throw new RuntimeException("fileName không được để trống");
        if (request.getContentType() == null || request.getContentType().isBlank())
            throw new RuntimeException("contentType không được để trống");
        if (request.getUploadType() == null)
            throw new RuntimeException("uploadType không được để trống");

        validateContentType(request.getUploadType(), request.getContentType());
    }

    private void validateContentType(UploadType uploadType, String contentType) {
        Set<String> imageTypes = Set.of("image/jpeg", "image/png", "image/webp");

        switch (uploadType) {
            case AVATAR -> {
                if (!imageTypes.contains(contentType))
                    throw new RuntimeException("Avatar chỉ hỗ trợ JPG, PNG, WEBP");
            }
            case SPOT_IMAGE -> {
                if (!imageTypes.contains(contentType))
                    throw new RuntimeException("Ảnh địa điểm chỉ hỗ trợ JPG, PNG, WEBP");
            }
            case DIARY_IMAGE -> {
                if (!imageTypes.contains(contentType))
                    throw new RuntimeException("Ảnh bài viết chỉ hỗ trợ JPG, PNG, WEBP");
            }
        }
    }

    private String resolveFolder(UploadType uploadType) {
        return switch (uploadType) {
            case AVATAR -> "avatars";
            case SPOT_IMAGE -> "spots";
            case DIARY_IMAGE -> "diaries";
        };
    }

    private String sanitizeFileName(String fileName) {
        return fileName.trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-zA-Z0-9._-]", "");
    }

    public String buildPublicUrl(String fileKey) {
        if (customDomain != null && !customDomain.isBlank()) {
            return customDomain.endsWith("/") ? customDomain + fileKey : customDomain + "/" + fileKey;
        } else {
            return String.format("https://%s.r2.cloudflarestorage.com/%s/%s", accountId, bucketName, fileKey);
        }
    }
}
