package vn.travelist.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType; // PAGE_VIEW, SPOT_VIEW, ITINERARY_GENERATE

    @Column(name = "target_id")
    private String targetId; // Có thể là spot_id, page_url, v.v.

    @Column(name = "user_id")
    private Long userId; // Nullable cho khách vãng lai

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
