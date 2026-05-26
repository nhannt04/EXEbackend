package vn.histra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertInquiryResponse {
    private Long id;
    private String question;
    private String answer;
    private LocalDateTime createdAt;
    private String expertName;
    private String userName;
    private String userAvatarUrl;
}
