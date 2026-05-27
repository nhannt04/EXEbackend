package vn.histra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItinerarySaveRequest {
    private String title;
    private Integer totalDays;
    private Double totalBudget;
    private String travelStyle;
    private String groupType;
    private String tripData; // JSON serialized string
}
