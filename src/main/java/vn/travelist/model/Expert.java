package vn.travelist.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "experts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Expert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String expertise; // Lĩnh vực chuyên sâu (ví dụ: 'Ẩm thực phố cổ', 'Lịch sử di sản')

    @Column(name = "description_vi", columnDefinition = "TEXT")
    private String descriptionVi;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "is_online")
    @Builder.Default
    private Boolean isOnline = false;

    @Builder.Default
    private Double rating = 5.0;
}
