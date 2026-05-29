package vn.travelist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryRequest {
    private Long userId;
    private String category;
    private String contentVi;
    private String contentEn;
    private Long spotId;
    private List<ImageDto> images;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageDto {
        private String imageCfId;
        private String imageUrl;
    }
}
