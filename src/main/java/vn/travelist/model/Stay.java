package vn.travelist.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "stays")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // Hotel, Villa, Homestay

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String address;

    private Double latitude;

    private Double longitude;

    private String capacity;

    @Column(name = "min_price")
    @Min(value = 0)
    private Integer minPrice;

    @Column(name = "max_price")
    @Min(value = 0)
    private Integer maxPrice;

    @Column(length = 1000)
    private String notes;

    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
