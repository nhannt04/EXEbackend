package vn.travelist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.travelist.model.Spot;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpotWithDistance {
    private Spot spot;
    private Double distance; // km
}
