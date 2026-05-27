package vn.histra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.histra.model.UploadType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresignedUploadRequest {
    private String fileName;
    private String contentType;
    private UploadType uploadType;
}
