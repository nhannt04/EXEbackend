package vn.travelist.dto;

import lombok.Data;

@Data
public class AnalyticsTrackRequest {
    private String eventType;
    private String targetId;
}
