package vn.travelist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryResponse {
    private Long id;
    private String title;
    private String destination;
    private Integer totalDays;
    private Double totalBudget;
    private String travelStyle;
    private String groupType;
    private String tripData; // JSON serialized string
    private LocalDateTime createdAt;
}
