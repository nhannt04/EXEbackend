package vn.travelist.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "dishes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dish_name", nullable = false)
    private String dishName;

    @Column(name = "restaurant_name", nullable = false)
    private String restaurantName;

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
