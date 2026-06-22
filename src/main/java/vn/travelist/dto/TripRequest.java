package vn.travelist.dto;

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
    private Integer people;          // Số lượng người đi cùng
    private String groupType;        // 'couple', 'family', 'solo', 'friends'
    private List<String> interests;  // Danh sách tags sở thích
    
    // New fields replacing style
    private List<String> selectedDishes;
    private List<String> selectedStayCategories;
    private List<String> selectedEntCategories;

    private Double currentLat;       // Vĩ độ điểm khởi hành
    private Double currentLng;       // Kinh độ điểm khởi hành
}
