package vn.histra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRequest {
    private Integer days;
    private Integer budget;
    private String style;            // 'cultural', 'food', 'healing', 'adventure'
    private Integer people;          // Số lượng người đi cùng
    private String groupType;        // 'couple', 'family', 'solo', 'friends'
    private List<String> interests;  // Danh sách tags sở thích
    private Double currentLat;       // Vĩ độ điểm khởi hành
    private Double currentLng;       // Kinh độ điểm khởi hành
}
