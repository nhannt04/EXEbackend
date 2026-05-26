package vn.histra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.histra.model.Spot;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpotWithDistance {
    private Spot spot;
    private Double distance; // km
}
