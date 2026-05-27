package vn.histra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.histra.model.Spot;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripResponse {
    private Integer totalCost;
    private Integer activityCost;
    private Integer hotelEstimate;
    private Integer transportEstimate;
    @Builder.Default
    private Boolean aiPowered = false;
    private String  aiEngine;
    private List<DaySchedule> days;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DaySchedule {
        private Integer day;
        private List<ScheduledSpot> spots;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduledSpot {
        private String slot; // 'MORNING', 'LUNCH', 'AFTERNOON', 'EVENING'
        private String time; // '08:00', '12:00', '15:00', '19:00'
        private Spot spot;
    }
}
