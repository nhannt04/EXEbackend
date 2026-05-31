package vn.travelist.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "cafes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cafe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String style;

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
