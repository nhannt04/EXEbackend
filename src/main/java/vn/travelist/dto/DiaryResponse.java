package vn.travelist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryResponse {
    private Long id;
    private String category;
    private String contentVi;
    private String contentEn;
    private String imageUrl;
    private String imageCfId;
    private java.util.List<vn.travelist.model.DiaryImage> images;
    private Integer likesCount;
    private LocalDateTime createdAt;
    private AuthorInfo user;
    private List<CommentResponse> comments;
    private Long spotId;
    private vn.travelist.model.Spot spot;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String fullName;
        private String email;
        private String role;
        private String avatarUrl;
    }
}
