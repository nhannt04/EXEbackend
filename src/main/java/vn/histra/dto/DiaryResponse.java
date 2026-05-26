package vn.histra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.histra.model.Spot;
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
    private Integer likesCount;
    private LocalDateTime createdAt;
    private Spot spot;
    private AuthorInfo user;
    private List<CommentResponse> comments;

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
