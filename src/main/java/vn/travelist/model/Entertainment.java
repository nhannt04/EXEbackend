package vn.travelist.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "entertainments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entertainment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // Biển, Vui chơi, Workshop

    @Column(length = 500)
    private String interests; // Sở thích (Chill & Thư giãn, Sống ảo, Trải nghiệm,...)

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

    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
