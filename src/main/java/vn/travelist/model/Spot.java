package vn.travelist.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Spot {

    private Long id;
    private String nameVi;
    private String nameEn;
    private String category;
    private String tags;
    private Double latitude;
    private Double longitude;
    private Integer minCost;
    private Integer maxCost;
    private Integer averageCost;
    private Integer estimatedDurationMinutes;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private String crowdLevel;
    private Double rating;
    private String suitableFor;
    private String timeOfDay;

    @Builder.Default
    private java.util.List<SpotImage> images = new java.util.ArrayList<>();

    private String descriptionVi;
    private String descriptionEn;
}
