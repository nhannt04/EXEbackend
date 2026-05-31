package vn.travelist.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

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

    @Column(name = "opening_time")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closingTime;

    @Column(name = "overnight", nullable = false)
    private Boolean overnight = false;
}
