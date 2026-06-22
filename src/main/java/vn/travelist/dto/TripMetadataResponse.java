package vn.travelist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripMetadataResponse {
    private List<String> dishes;
    private List<String> stayTypes;
    private List<String> entertainmentTypes;
}
