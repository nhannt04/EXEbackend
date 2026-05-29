package vn.travelist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresignedUploadResponse {
    private String uploadUrl;
    private String fileUrl;
    private String fileKey;
    private long expiresInSeconds;
}
