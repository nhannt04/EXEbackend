package vn.travelist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertResponse {
    private Long id;
    private String expertise;
    private String descriptionVi;
    private String descriptionEn;
    private Boolean isOnline;
    private Double rating;
    private ProfileInfo user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileInfo {
        private Long id;
        private String fullName;
        private String email;
        private String avatarUrl;
    }
}
