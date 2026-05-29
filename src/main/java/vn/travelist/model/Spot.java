package vn.travelist.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "spots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Spot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_vi", nullable = false)
    private String nameVi;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String tags;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "min_cost")
    @Min(value = 0, message = "Giá vé tối thiểu không được nhỏ hơn 0")
    private Integer minCost;

    @Column(name = "max_cost")
    @Min(value = 0, message = "Giá vé tối đa không được nhỏ hơn 0")
    private Integer maxCost;

    @Column(name = "average_cost")
    @Min(value = 0, message = "Giá vé trung bình không được nhỏ hơn 0")
    private Integer averageCost;

    @PrePersist
    @PreUpdate
    public void calculateAverageCost() {
        int min = minCost != null ? minCost : 0;
        int max = maxCost != null ? maxCost : 0;
        this.averageCost = (min + max) / 2;
    }

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "crowd_level")
    private String crowdLevel;

    private Double rating;

    @Column(name = "suitable_for", nullable = false)
    private String suitableFor;

    @Column(name = "time_of_day", nullable = false)
    private String timeOfDay;

    @OneToMany(mappedBy = "spot", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<SpotImage> images = new java.util.ArrayList<>();

    @Column(name = "description_vi", columnDefinition = "TEXT")
    private String descriptionVi;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;
}
