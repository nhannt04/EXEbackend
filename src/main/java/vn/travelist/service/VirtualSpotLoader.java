package vn.travelist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.travelist.model.Spot;
import vn.travelist.model.SpotImage;
import vn.travelist.repository.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VirtualSpotLoader {

    private final DishRepository dishRepository;
    private final CafeRepository cafeRepository;
    private final StayRepository stayRepository;
    private final EntertainmentRepository entertainmentRepository;
    private final RentalRepository rentalRepository;

    @Cacheable(value = "allVirtualSpots", key = "'all'")
    public List<Spot> getAllVirtualSpots() {
        List<Spot> allSpots = new ArrayList<>();
        // 1. Map Stays
        try {
            stayRepository.findAll().forEach(stay -> {
                // Chỉ thêm nếu có hình ảnh
                if (stay.getImageUrl() == null || stay.getImageUrl().isBlank()) {
                    return;
                }

                Spot spot = Spot.builder()
                    .id(100000L + stay.getId())
                    .nameVi(stay.getName())
                    .nameEn(stay.getName())
                    .category("stay")
                    .tags("khách sạn, homestay, lưu trú, nghỉ dưỡng, stay, " + stay.getType())
                    .address(stay.getAddress())
                    .latitude(stay.getLatitude() != null ? stay.getLatitude() : 15.8801)
                    .longitude(stay.getLongitude() != null ? stay.getLongitude() : 108.3380)
                    .minCost(stay.getMinPrice())
                    .maxCost(stay.getMaxPrice())
                    .averageCost(stay.getMinPrice() != null && stay.getMaxPrice() != null ? (stay.getMinPrice() + stay.getMaxPrice()) / 2 : 0)
                    .estimatedDurationMinutes(480)
                    .openingTime(LocalTime.of(0, 0))
                    .closingTime(LocalTime.of(23, 59))
                    .crowdLevel("low")
                    .rating(4.9)
                    .suitableFor("couple, family, group, solo")
                    .timeOfDay("evening")
                    .descriptionVi("Nơi lưu trú cao cấp: " + stay.getName() + " (" + stay.getType() + "). Sức chứa: " + stay.getCapacity() + ". Địa chỉ: " + stay.getAddress() + ". Ghi chú: " + stay.getNotes())
                    .descriptionEn("Premium accommodation: " + stay.getName() + " (" + stay.getType() + "). Capacity: " + stay.getCapacity() + ". Address: " + stay.getAddress() + ". Notes: " + stay.getNotes())
                    .build();

                SpotImage img = SpotImage.builder().id(stay.getId()).imageUrl(stay.getImageUrl()).build();
                spot.setImages(new ArrayList<>(List.of(img)));
                allSpots.add(spot);
            });
        } catch (Exception e) {}

        // 2. Map Cafes
        try {
            cafeRepository.findAll().forEach(cafe -> {
                // Chỉ thêm nếu có hình ảnh
                if (cafe.getImageUrl() == null || cafe.getImageUrl().isBlank()) {
                    return;
                }

                Spot spot = Spot.builder()
                    .id(200000L + cafe.getId())
                    .nameVi(cafe.getName())
                    .nameEn(cafe.getName())
                    .category("cafe")
                    .tags("cafe, cà phê, chill, nước uống, " + (cafe.getStyle() != null ? cafe.getStyle() : ""))
                    .address(cafe.getAddress())
                    .latitude(cafe.getLatitude() != null ? cafe.getLatitude() : 15.8801)
                    .longitude(cafe.getLongitude() != null ? cafe.getLongitude() : 108.3380)
                    .minCost(cafe.getMinPrice())
                    .maxCost(cafe.getMaxPrice())
                    .averageCost(cafe.getMinPrice() != null && cafe.getMaxPrice() != null ? (cafe.getMinPrice() + cafe.getMaxPrice()) / 2 : 0)
                    .estimatedDurationMinutes(60)
                    .openingTime(cafe.getOpeningTime() != null ? cafe.getOpeningTime() : LocalTime.of(7, 0))
                    .closingTime(cafe.getClosingTime() != null ? cafe.getClosingTime() : LocalTime.of(22, 0))
                    .crowdLevel("medium")
                    .rating(4.7)
                    .suitableFor("couple, family, group, solo")
                    .timeOfDay("morning, afternoon, evening")
                    .descriptionVi("Quán cà phê chill Hội An: " + cafe.getName() + " phong cách " + cafe.getStyle() + ". Địa chỉ: " + cafe.getAddress())
                    .descriptionEn("Chill Hoi An cafe: " + cafe.getName() + " styled as " + cafe.getStyle() + ". Address: " + cafe.getAddress())
                    .build();

                SpotImage img = SpotImage.builder().id(cafe.getId()).imageUrl(cafe.getImageUrl()).build();
                spot.setImages(new ArrayList<>(List.of(img)));
                allSpots.add(spot);
            });
        } catch (Exception e) {}

        // 3. Map Dishes
        try {
            dishRepository.findAll().forEach(dish -> {
                // Chỉ thêm nếu có hình ảnh
                if (dish.getImageUrl() == null || dish.getImageUrl().isBlank()) {
                    return;
                }

                Spot spot = Spot.builder()
                    .id(300000L + dish.getId())
                    .nameVi(dish.getDishName() + " (" + dish.getRestaurantName() + ")")
                    .nameEn(dish.getDishName() + " (" + dish.getRestaurantName() + ")")
                    .category("food")
                    .tags("món ăn, ẩm thực, đặc sản, nhà hàng")
                    .address(dish.getAddress())
                    .latitude(dish.getLatitude() != null ? dish.getLatitude() : 15.8801)
                    .longitude(dish.getLongitude() != null ? dish.getLongitude() : 108.3380)
                    .minCost(dish.getMinPrice())
                    .maxCost(dish.getMaxPrice())
                    .averageCost(dish.getMinPrice() != null && dish.getMaxPrice() != null ? (dish.getMinPrice() + dish.getMaxPrice()) / 2 : 0)
                    .estimatedDurationMinutes(45)
                    .openingTime(dish.getOpeningTime() != null ? dish.getOpeningTime() : LocalTime.of(10, 0))
                    .closingTime(dish.getClosingTime() != null ? dish.getClosingTime() : LocalTime.of(22, 0))
                    .crowdLevel("medium")
                    .rating(4.8)
                    .suitableFor("couple, family, group, solo")
                    .timeOfDay("morning, afternoon, evening")
                    .descriptionVi("Món ngon đặc sản Hội An: " + dish.getDishName() + " tại " + dish.getRestaurantName() + ". Địa chỉ: " + dish.getAddress())
                    .descriptionEn("Delicious Hoi An specialty: " + dish.getDishName() + " at " + dish.getRestaurantName() + ". Address: " + dish.getAddress())
                    .build();

                SpotImage img = SpotImage.builder().id(dish.getId()).imageUrl(dish.getImageUrl()).build();
                spot.setImages(new ArrayList<>(List.of(img)));
                allSpots.add(spot);
            });
        } catch (Exception e) {}

        // 4. Map Entertainments
        try {
            entertainmentRepository.findAll().forEach(ent -> {
                // Chỉ thêm nếu có hình ảnh
                if (ent.getImageUrl() == null || ent.getImageUrl().isBlank()) {
                    return;
                }

                Spot spot = Spot.builder()
                    .id(400000L + ent.getId())
                    .nameVi(ent.getName())
                    .nameEn(ent.getName())
                    .category("sightseeing")
                    .tags("vui chơi, giải trí, tham quan, " + ent.getType() + ", " + (ent.getInterests() != null ? ent.getInterests() : ""))
                    .address(ent.getAddress())
                    .latitude(ent.getLatitude() != null ? ent.getLatitude() : 15.8801)
                    .longitude(ent.getLongitude() != null ? ent.getLongitude() : 108.3380)
                    .minCost(ent.getMinPrice())
                    .maxCost(ent.getMaxPrice())
                    .averageCost(ent.getMinPrice() != null && ent.getMaxPrice() != null ? (ent.getMinPrice() + ent.getMaxPrice()) / 2 : 0)
                    .estimatedDurationMinutes(120)
                    .openingTime(ent.getOpeningTime() != null ? ent.getOpeningTime() : LocalTime.of(8, 0))
                    .closingTime(ent.getClosingTime() != null ? ent.getClosingTime() : LocalTime.of(21, 0))
                    .crowdLevel("medium")
                    .rating(4.6)
                    .suitableFor("couple, family, group, solo")
                    .timeOfDay("morning, afternoon, evening")
                    .descriptionVi("Địa điểm giải trí thú vị: " + ent.getName() + " (" + ent.getType() + "). Phù hợp cho sở thích: " + ent.getInterests() + ". Địa chỉ: " + ent.getAddress())
                    .descriptionEn("Fun entertainment spot: " + ent.getName() + " (" + ent.getType() + "). Suitable for interests: " + ent.getInterests() + ". Address: " + ent.getAddress())
                    .build();

                SpotImage img = SpotImage.builder().id(ent.getId()).imageUrl(ent.getImageUrl()).build();
                spot.setImages(new ArrayList<>(List.of(img)));
                allSpots.add(spot);
            });
        } catch (Exception e) {}

        // 5. Map Rentals
        try {
            rentalRepository.findAll().forEach(rental -> {
                // Chỉ thêm nếu có hình ảnh
                if (rental.getImageUrl() == null || rental.getImageUrl().isBlank()) {
                    return;
                }

                Spot spot = Spot.builder()
                    .id(500000L + rental.getId())
                    .nameVi(rental.getName() + " (" + rental.getType() + ")")
                    .nameEn(rental.getName() + " (" + rental.getType() + ")")
                    .category("rental")
                    .tags("dịch vụ, cho thuê, rental, " + rental.getType())
                    .address(rental.getAddress())
                    .latitude(rental.getLatitude() != null ? rental.getLatitude() : 15.8801)
                    .longitude(rental.getLongitude() != null ? rental.getLongitude() : 108.3380)
                    .minCost(rental.getMinPrice())
                    .maxCost(rental.getMaxPrice())
                    .averageCost(rental.getMinPrice() != null && rental.getMaxPrice() != null ? (rental.getMinPrice() + rental.getMaxPrice()) / 2 : 0)
                    .estimatedDurationMinutes(30)
                    .openingTime(rental.getOpeningTime() != null ? rental.getOpeningTime() : LocalTime.of(8, 0))
                    .closingTime(rental.getClosingTime() != null ? rental.getClosingTime() : LocalTime.of(21, 0))
                    .crowdLevel("low")
                    .rating(4.7)
                    .suitableFor("couple, family, group, solo")
                    .timeOfDay("morning, afternoon, evening")
                    .descriptionVi("Dịch vụ cho thuê: " + rental.getName() + " chuyên cung cấp " + rental.getType() + ". Địa chỉ: " + rental.getAddress())
                    .descriptionEn("Rental service: " + rental.getName() + " specializes in providing " + rental.getType() + ". Address: " + rental.getAddress())
                    .build();

                SpotImage img = SpotImage.builder().id(rental.getId()).imageUrl(rental.getImageUrl()).build();
                spot.setImages(new ArrayList<>(List.of(img)));
                allSpots.add(spot);
            });
        } catch (Exception e) {}

        return allSpots;
    }
}
