package vn.travelist.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "rentals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // Thuê máy ảnh, Thuê đồ, Thuê xe, Photobooth

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String address;

    private Double latitude;

    private Double longitude;

    @Column(name = "min_price")
    @Min(value = 0)
    private Integer minPrice;

    @Column(name = "max_price")
    @Min(value = 0)
    private Integer maxPrice;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
