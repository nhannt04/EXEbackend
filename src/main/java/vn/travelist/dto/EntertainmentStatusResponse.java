package vn.travelist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntertainmentStatusResponse {
    private Long id;
    private String name;
    private boolean open;
    private boolean overnight;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private String message;
}

