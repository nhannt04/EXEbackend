package vn.travelist.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "itineraries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "destination")
    private String destination;

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    @Column(name = "total_budget", nullable = false)
    private Double totalBudget;

    @Column(name = "travel_style", nullable = false)
    private String travelStyle;

    @Column(name = "group_type", nullable = false)
    private String groupType;

    @Column(name = "title")
    private String title;

    @Column(name = "trip_data", columnDefinition = "TEXT")
    private String tripData;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.destination == null) {
            this.destination = "Hội An";
        }
        if (this.title == null) {
            this.title = "Lịch trình Hội An";
        }
        if (this.status == null) {
            this.status = "NOT_STARTED";
        }
    }
}
